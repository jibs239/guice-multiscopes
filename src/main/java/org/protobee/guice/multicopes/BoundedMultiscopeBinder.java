package org.protobee.guice.multicopes;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import org.protobee.guice.multicopes.scopes.AssistedMultiscope;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
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

  public static enum PrescopeType {
    EAGER, LAZY
  }

  public static interface InstancePrescoper {
    <T> InstancePrescoper addInstanceObject(Key<T> key);

    <T> InstancePrescoper addInstanceObject(Key<T> key, PrescopeType type);
  }

  static class RealBoundedMultiscopeBinder extends BoundedMultiscopeBinder implements Module {
    private final Class<? extends Annotation> scopeAnnotation;
    private final Class<? extends Annotation> scopeBindingAnnotation;
    private final Multiscope multiscope;
    private Binder binder;

    RealBoundedMultiscopeBinder(Binder binder, Class<? extends Annotation> scopeAnnotation,
        Class<? extends Annotation> scopeBindingAnnotation) {
      this.binder = binder;
      this.scopeAnnotation = scopeAnnotation;
      this.scopeBindingAnnotation = scopeBindingAnnotation;
      this.multiscope = new AssistedMultiscope(scopeBindingAnnotation);
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
      binder.bind(Descoper.class).annotatedWith(scopeBindingAnnotation)
          .toProvider(new DescoperProvider(multiscope));

      Multibinder<Descoper> descopers = Multibinder.newSetBinder(binder, Descoper.class);
      descopers.addBinding().to(Key.get(Descoper.class, scopeBindingAnnotation));

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
      Multibinder.newSetBinder(binder, KeyWrapper.class, instanceAnnotation);
      return this;
    }

    @Override
    public InstancePrescoper prescopeInstance(Class<? extends Annotation> instanceAnnotation) {
      final Multibinder<KeyWrapper> prescopeedKeys =
          Multibinder.newSetBinder(binder, KeyWrapper.class, instanceAnnotation);

      InstancePrescoper instance = new InstancePrescoper() {

        @Override
        public <T> InstancePrescoper addInstanceObject(Key<T> key, PrescopeType type) {
          Preconditions.checkNotNull(type, "type");
          prescopeedKeys.addBinding().toInstance(new KeyWrapper(key, type));
          return this;
        }

        @Override
        public <T> InstancePrescoper addInstanceObject(Key<T> key) {
          addInstanceObject(key, PrescopeType.EAGER);
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


    static class BindingAndType {
      final Binding<?> binding;
      final PrescopeType type;

      public BindingAndType(Binding<?> binding, PrescopeType type) {
        this.binding = binding;
        this.type = type;
      }
    }

    static class PrescopingSingletonInstanceProvider
        implements
          Provider<ScopeInstance>,
          HasDependencies {

      final Class<? extends Annotation> scopeInstanceAnnotation;
      final Class<? extends Annotation> scopeBindingAnnotation;
      final Multiscope multiscope;


      volatile ScopeInstance instance = null;
      final Object instanceLock = new Object();

      Binding<Map<Key<?>, Object>> scopeMapBuilder = null;
      ImmutableSet<BindingAndType> bindings;
      ImmutableSet<Dependency<?>> dependencies;
      boolean initialized = false;

      PrescopingSingletonInstanceProvider(Class<? extends Annotation> scopeBindingAnnotation,
          Multiscope multiscope, Class<? extends Annotation> scopeInstanceAnnotation) {
        this.scopeBindingAnnotation = scopeBindingAnnotation;
        this.multiscope = multiscope;
        this.scopeInstanceAnnotation = scopeInstanceAnnotation;
      }

      @Inject
      @Toolable
      void initialize(Injector injector) {
        initialized = true;

        TypeLiteral<Set<KeyWrapper>> keyType = new TypeLiteral<Set<KeyWrapper>>() {};
        Set<BindingAndType> bindings = Sets.newHashSet();

        Set<Dependency<?>> dependencies = Sets.newHashSet();
        Set<KeyWrapper> prescopedKeys =
            injector.getInstance(Key.get(keyType, scopeInstanceAnnotation));
        for (KeyWrapper key : prescopedKeys) {
          bindings.add(new BindingAndType(injector.getBinding(key.getKey()), key.getType()));
          dependencies.add(Dependency.get(key.getKey()));
        }
        this.dependencies = ImmutableSet.copyOf(dependencies);
        this.bindings = ImmutableSet.copyOf(bindings);

        scopeMapBuilder =
            injector.getExistingBinding(Key.get(new TypeLiteral<Map<Key<?>, Object>>() {}));
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
        if (!initialized) {
          TypeLiteral<Set<KeyWrapper>> keyType = new TypeLiteral<Set<KeyWrapper>>() {};
          return ImmutableSet.<Dependency<?>>of(Dependency.get(Key.get(Injector.class)),
              Dependency.get(Key.get(keyType, scopeInstanceAnnotation)));
        }
        ImmutableSet.Builder<Dependency<?>> deps = ImmutableSet.builder();
        deps.addAll(dependencies);
        if (scopeMapBuilder != null) {
          deps.add(Dependency.get(scopeMapBuilder.getKey()));
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
          if (scopeMapBuilder != null) {
            scopeMap = scopeMapBuilder.getProvider().get();
          } else {
            scopeMap = MultiscopeUtils.createDefaultScopeMap();
          }
          for (BindingAndType prescoped : bindings) {
            Binding<?> binding = prescoped.binding;
            Key<?> key = Key.get(binding.getKey().getTypeLiteral(), scopeBindingAnnotation);
            switch (prescoped.type) {
              case EAGER:
                scopeMap.put(key, binding.getProvider().get());
                break;
              case LAZY:
                scopeMap.put(key, new AssistedMultiscope.LazyScopedObject(binding.getProvider()));
                break;
              default:
                throw new ProvisionException("Prescope type cannot be null");
            }
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
