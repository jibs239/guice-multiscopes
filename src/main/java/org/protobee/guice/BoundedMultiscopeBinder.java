package org.protobee.guice;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.Toolable;

public abstract class BoundedMultiscopeBinder extends MultiscopeBinder {

  public static BoundedMultiscopeBinder newBoundedBinder(final Binder binder,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> scopeBindingAnnotation) {
    RealBoundedMultiscopeBinder mbinder =
        new RealBoundedMultiscopeBinder(binder, scopeAnnotation, scopeBindingAnnotation);
    binder.install(mbinder);
    return mbinder;
  }

  public abstract BoundedMultiscopeBinder addInstance(Class<? extends Annotation> instanceAnnotation);

  public abstract InstancePrescoper prescopeInstance(Class<? extends Annotation> instanceAnnotation);

  public static interface InstancePrescoper {
    <T> InstancePrescoper addInstanceObject(Key<T> key);
  }

  static class RealBoundedMultiscopeBinder extends BoundedMultiscopeBinder implements Module {
    private final Class<? extends Annotation> scopeAnnotation;
    private final Class<? extends Annotation> scopeBindingAnnotation;
    private final AbstractMultiscope multiscope;
    private Binder binder;

    RealBoundedMultiscopeBinder(Binder binder, Class<? extends Annotation> scopeAnnotation,
        Class<? extends Annotation> scopeBindingAnnotation) {
      this.binder = binder;
      this.scopeAnnotation = scopeAnnotation;
      this.scopeBindingAnnotation = scopeBindingAnnotation;
      this.multiscope = new AssistedMultithreadedMultiscope(scopeBindingAnnotation);
    }

    @Override
    public void configure(Binder binder) {
      binder.bindScope(scopeAnnotation, multiscope);
      binder
          .bind(ScopeInstance.class)
          .annotatedWith(multiscope.getBindingAnnotation())
          .toProvider(
              new PrescopedProvider<ScopeInstance>(
                  "ScopeInstance should have been bound internally.", scopeAnnotation
                      .getSimpleName() + "-ScopeInstanceFakeProvider")).in(scopeAnnotation);
      binder.bind(Multiscope.class).annotatedWith(scopeBindingAnnotation).toInstance(multiscope);

      Multibinder<Multiscope> scopes = Multibinder.newSetBinder(binder, Multiscope.class);
      scopes.addBinding().toInstance(multiscope);
    }

    @Override
    public BoundedMultiscopeBinder addInstance(final Class<? extends Annotation> instanceAnnotation) {
      binder
          .bind(ScopeInstance.class)
          .annotatedWith(instanceAnnotation)
          .toProvider(
              new PrescopingSingletonInstanceProvider(scopeBindingAnnotation, multiscope,
                  instanceAnnotation));

      Multibinder<ScopeInstance> instances =
          Multibinder.newSetBinder(binder, ScopeInstance.class, scopeBindingAnnotation);
      instances.addBinding().to(Key.get(ScopeInstance.class, instanceAnnotation));

      // set up for prescoped keys
      TypeLiteral<Key<?>> keyType = new TypeLiteral<Key<?>>() {};
      Multibinder.newSetBinder(binder, keyType, instanceAnnotation);
      return this;
    }

    @Override
    public InstancePrescoper prescopeInstance(Class<? extends Annotation> instanceAnnotation) {
      TypeLiteral<Key<?>> keyType = new TypeLiteral<Key<?>>() {};
      final Multibinder<Key<?>> prescopeedKeys =
          Multibinder.newSetBinder(binder, keyType, instanceAnnotation);

      InstancePrescoper instance = new InstancePrescoper() {
        @Override
        public <T> InstancePrescoper addInstanceObject(Key<T> key) {
          prescopeedKeys.addBinding().toInstance(key);
          return this;
        }
      };
      return instance;
    }

    @Override
    public boolean equals(Object o) {
      return o instanceof RealBoundedMultiscopeBinder
          && ((RealBoundedMultiscopeBinder) o).scopeAnnotation.equals(scopeAnnotation);
    }

    @Override
    public int hashCode() {
      return scopeAnnotation.hashCode();
    }

    @Override
    public LinkedBindingBuilder<Map<Key<?>, Object>> bindScopeStorageMap() {
      return binder
          .bind(Key.get(new TypeLiteral<Map<Key<?>, Object>>() {}, scopeBindingAnnotation));
    }

    static class PrescopingSingletonInstanceProvider
        implements
          Provider<ScopeInstance>,
          HasDependencies {

      final Class<? extends Annotation> scopeInstanceAnnotation;
      final Class<? extends Annotation> scopeBindingAnnotation;
      final AbstractMultiscope multiscope;

      ImmutableSet<Binding<?>> instanceToKeysBindings;

      volatile ScopeInstance instance = null;
      final Object instanceLock = new Object();

      Binding<Map<Key<?>, Object>> scopeMapBinding = null;
      Set<Dependency<?>> dependencies = Sets.newHashSet();
      boolean initialized = false;

      PrescopingSingletonInstanceProvider(Class<? extends Annotation> scopeBindingAnnotation,
          AbstractMultiscope multiscope, Class<? extends Annotation> scopeInstanceAnnotation) {
        this.scopeBindingAnnotation = scopeBindingAnnotation;
        this.multiscope = multiscope;
        this.scopeInstanceAnnotation = scopeInstanceAnnotation;
      }

      @Inject
      @Toolable
      void initialize(Injector injector) {
        initialized = true;

        TypeLiteral<Set<Key<?>>> keyType = new TypeLiteral<Set<Key<?>>>() {};
        Set<Binding<?>> bindings = Sets.newHashSet();

        Set<Key<?>> prescopedKeys = injector.getInstance(Key.get(keyType, scopeInstanceAnnotation));
        for (Key<?> key : prescopedKeys) {
          bindings.add(injector.getBinding(key));
          dependencies.add(Dependency.get(key));
        }
        instanceToKeysBindings = ImmutableSet.copyOf(bindings);

        scopeMapBinding =
            injector.getExistingBinding(Key.get(new TypeLiteral<Map<Key<?>, Object>>() {}));
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
        if (!initialized) {
          TypeLiteral<Set<Key<?>>> keyType = new TypeLiteral<Set<Key<?>>>() {};
          return ImmutableSet.<Dependency<?>>of(Dependency.get(Key.get(Injector.class)),
              Dependency.get(Key.get(keyType, scopeInstanceAnnotation)));
        }
        ImmutableSet.Builder<Dependency<?>> deps = ImmutableSet.builder();
        deps.addAll(dependencies);
        if (scopeMapBinding != null) {
          deps.add(Dependency.get(scopeMapBinding.getKey()));
        }
        return deps.build();
      }

      @Override
      public ScopeInstance get() {
        if (instance != null) {
          return instance;
        }
        synchronized (instanceLock) {
          if (instance != null) {
            return instance;
          }

          Map<Key<?>, Object> scopeMap;
          if (scopeMapBinding != null) {
            scopeMap = scopeMapBinding.getProvider().get();
          } else {
            scopeMap = MultiscopeUtils.createDefaultScopeMap();
          }
          for (Binding<?> prescoped : instanceToKeysBindings) {
            scopeMap.put(prescoped.getKey(), prescoped.getProvider().get());
          }
          instance = multiscope.createScopeInstance(scopeMap);
          return instance;
        }
      }

      @Override
      public String toString() {
        return multiscope.toString() + "-NewInstanceProvider";
      }
    }
  }

}
