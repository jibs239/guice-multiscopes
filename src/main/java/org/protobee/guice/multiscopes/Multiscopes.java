/*******************************************************************************
 * Copyright (c) 2012, Daniel Murphy and Deanna Surma
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.protobee.guice.multiscopes;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.protobee.guice.multiscopes.scopes.AssistedMultiscope;
import org.protobee.guice.multiscopes.scopes.SimpleMultiscope;
import org.protobee.guice.multiscopes.util.Descoper;

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

/**
 * Class for binding multiscopes. Each multiscope binding needs
 * <ul>
 * <li>A {@link Multiscope} instance
 * <li>A scope annotation, like all scopes
 * <li>A 'new scope instance' binding annotation, used to inject a new {@link ScopeInstance} for the
 * {@link Multiscope} (also to specify a new scope storage map internally)
 * <li>Optionally, a provider for the scope storage map. This defaults to
 * {@link DefaultScopeMapProvider}
 * </ul>
 * 
 * You should be storing your multiscope instances as a <code>public static final</code> variable in
 * one of your classes. This way, you can access {@link Multiscope#isInScope()} and
 * {@link Multiscope#exitScope()} for testing and other cases where you need to check or exit the
 * scope when you don't have access to the scope instance itself.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public final class Multiscopes {

  private Multiscopes() {}

  public static MultiscopeBinder newBinder(Binder binder,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> scopeBindingAnnotation,
      final Class<? extends Annotation> newScopeBindingAnnotation) {
    RealMultiscopeModule real =
        new RealMultiscopeModule(binder, scopeAnnotation, scopeBindingAnnotation,
            newScopeBindingAnnotation);
    binder.install(real);
    return real;
  }

  public static BoundedMultiscopeBinder newBoundedBinder(final Binder binder,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> scopeBindingAnnotation) {
    RealBoundedMultiscopeBinder mbinder =
        new RealBoundedMultiscopeBinder(binder, scopeAnnotation, scopeBindingAnnotation);
    binder.install(mbinder);
    return mbinder;
  }

  static class RealMultiscopeModule implements MultiscopeBinder, Module {

    protected final Class<? extends Annotation> scopeAnnotation;
    protected final Class<? extends Annotation> scopeBindingAnnotation;
    protected final Class<? extends Annotation> newScopeBindingAnnotation;
    protected final Multiscope multiscope;
    protected Binder binder;

    RealMultiscopeModule(Binder binder, Class<? extends Annotation> scopeAnnotation,
        Class<? extends Annotation> scopeBindingAnnotation,
        Class<? extends Annotation> newScopeBindingAnnotation) {
      this(binder, scopeAnnotation, scopeBindingAnnotation, newScopeBindingAnnotation,
          new SimpleMultiscope(scopeBindingAnnotation));
    }

    RealMultiscopeModule(Binder binder, Class<? extends Annotation> scopeAnnotation,
        Class<? extends Annotation> scopeBindingAnnotation,
        @Nullable Class<? extends Annotation> newScopeBindingAnnotation, Multiscope multiscope) {
      this.binder = binder;
      this.scopeAnnotation = scopeAnnotation;
      this.scopeBindingAnnotation = scopeBindingAnnotation;
      this.newScopeBindingAnnotation = newScopeBindingAnnotation;
      this.multiscope = multiscope;
    }

    @Override
    public void configure(Binder binder) {
      binder.bindScope(scopeAnnotation, multiscope);
      if (newScopeBindingAnnotation != null) {
        binder.bind(ScopeInstance.class).annotatedWith(newScopeBindingAnnotation)
            .toProvider(new NewInstanceProvider(scopeBindingAnnotation, multiscope));
      }
      binder
          .bind(ScopeInstance.class)
          .annotatedWith(multiscope.getBindingAnnotation())
          .toProvider(
              new PrescopedProvider<ScopeInstance>(
                  "ScopeInstance should have been bound internally.", scopeAnnotation
                      .getSimpleName() + "-ScopeInstanceProvider")).in(scopeAnnotation);
      binder.bind(Multiscope.class).annotatedWith(scopeBindingAnnotation).toInstance(multiscope);

      binder.bind(Descoper.class).annotatedWith(scopeBindingAnnotation)
          .toProvider(new DescoperProvider(multiscope));

      Multibinder<Descoper> descopers = Multibinder.newSetBinder(binder, Descoper.class);
      descopers.addBinding().to(Key.get(Descoper.class, scopeBindingAnnotation));

      Multibinder<Multiscope> scopes = Multibinder.newSetBinder(binder, Multiscope.class);
      scopes.addBinding().to(Key.get(Multiscope.class, scopeBindingAnnotation));
    }

    @Override
    public LinkedBindingBuilder<Map<Key<?>, Object>> bindScopeStorageMap() {
      return binder
          .bind(Key.get(new TypeLiteral<Map<Key<?>, Object>>() {}, scopeBindingAnnotation));
    }

    static class NewInstanceProvider implements Provider<ScopeInstance>, HasDependencies {

      final Class<? extends Annotation> scopeBindingAnnotation;
      final Multiscope multiscope;
      Binding<Map<Key<?>, Object>> scopeMapBinding = null;
      boolean initialized = false;

      NewInstanceProvider(Class<? extends Annotation> scopeBindingAnnotation, Multiscope multiscope) {
        this.scopeBindingAnnotation = scopeBindingAnnotation;
        this.multiscope = multiscope;
      }

      @Inject
      @Toolable
      void initialize(Injector injector) {
        initialized = true;
        scopeMapBinding =
            injector.getExistingBinding(Key.get(new TypeLiteral<Map<Key<?>, Object>>() {}));
      }

      @Override
      public Set<Dependency<?>> getDependencies() {
        if (!initialized) {
          return ImmutableSet.<Dependency<?>>of(Dependency.get(Key.get(Injector.class)));
        } else if (scopeMapBinding != null) {
          return ImmutableSet.<Dependency<?>>of(Dependency.get(Key.get(
              new TypeLiteral<Map<Key<?>, Object>>() {}, scopeBindingAnnotation)));
        }
        return ImmutableSet.of();
      }

      @Override
      public ScopeInstance get() {
        Map<Key<?>, Object> scopeMap;
        if (scopeMapBinding != null) {
          scopeMap = scopeMapBinding.getProvider().get();
        } else {
          scopeMap = MultiscopeUtils.createDefaultScopeMap();
        }
        return multiscope.createScopeInstance(scopeMap);
      }

      @Override
      public String toString() {
        return multiscope.toString() + "-NewInstanceProvider";
      }
    }
  }

  static class RealBoundedMultiscopeBinder extends RealMultiscopeModule
      implements
        Module,
        BoundedMultiscopeBinder {

    RealBoundedMultiscopeBinder(Binder binder, Class<? extends Annotation> scopeAnnotation,
        Class<? extends Annotation> scopeBindingAnnotation) {
      super(binder, scopeAnnotation, scopeBindingAnnotation, null, new AssistedMultiscope(
          scopeBindingAnnotation));
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
          Preconditions.checkNotNull(key, "key");
          prescopeedKeys.addBinding().toInstance(new KeyWrapper(key, type));
          return this;
        }

        @Override
        public <T> InstancePrescoper addInstanceObject(Key<T> key) {
          addInstanceObject(key, PrescopeType.LAZY);
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


  /**
   * Binds the multiscope to the scope annotation, and binds {@link ScopeInstance} as prescoped in
   * the scope as well. Creating new scope instances for this multiscope is bound to the
   * newInstanceAnnotation, as well as the scope map provider. This call uses the default
   * {@link DefaultScopeMapProvider}.
   * 
   * @param binder the binder to use
   * @param multiscope the multiscope instance
   * @param scopeAnnotation the scope annotation itself
   * @param newScopeInstanceAnnotation the annotation for creating a new scope instance (and a new
   *        scope map)
   * 
   */
  @Deprecated
  public static void bindMultiscope(final Binder binder, final Multiscope multiscope,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> newScopeInstanceAnnotation) {
    bindMultiscope(binder, multiscope, scopeAnnotation, newScopeInstanceAnnotation,
        DefaultScopeMapProvider.class);
  }

  /**
   * Binds the multiscope to the scope annotation, and binds {@link ScopeInstance} as prescoped in
   * the scope as well. Creating new scope instances for this multiscope is bound to the
   * newInstanceAnnotation, as well as the scopeMapProvider.
   * 
   * @param binder the binder to use
   * @param multiscope the multiscope instance
   * @param scopeAnnotation the scope annotation itself
   * @param newInstanceAnnotation the annotation for creating a new scope instance (and a new scope
   *        map)
   * @param scopeMapProvider the provider for the scope map. If you're accessing your scoped
   *        concurrently, this map should be threadsafe.
   */
  @Deprecated
  public static void bindMultiscope(final Binder binder, final Multiscope multiscope,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> newInstanceAnnotation,
      final Class<? extends Provider<Map<Key<?>, Object>>> scopeMapProvider) {
    Preconditions.checkNotNull(multiscope);
    Preconditions.checkNotNull(scopeAnnotation);
    Preconditions.checkNotNull(binder);
    Preconditions.checkNotNull(scopeMapProvider);

    binder.bindScope(scopeAnnotation, multiscope);
    final TypeLiteral<Map<Key<?>, Object>> scopeMap = new TypeLiteral<Map<Key<?>, Object>>() {};
    binder.bind(scopeMap).annotatedWith(newInstanceAnnotation).toProvider(scopeMapProvider);

    final Provider<Map<Key<?>, Object>> mapProvider =
        binder.<Map<Key<?>, Object>>getProvider(Key.get(scopeMap, newInstanceAnnotation));

    binder
        .bind(ScopeInstance.class)
        .annotatedWith(multiscope.getBindingAnnotation())
        .toProvider(
            new PrescopedProvider<ScopeInstance>(
                "ScopeInstance should have been bound internally.", scopeAnnotation.getSimpleName()
                    + "-ScopeInstanceFakeProvider")).in(scopeAnnotation);

    binder.bind(ScopeInstance.class).annotatedWith(newInstanceAnnotation)
        .toProvider(new Provider<ScopeInstance>() {

          @Override
          public ScopeInstance get() {
            return multiscope.createScopeInstance(mapProvider.get());
          }

          @Override
          public String toString() {
            return scopeAnnotation.getSimpleName() + "-NewScopeInstanceProvider";
          }
        });
  }
}
