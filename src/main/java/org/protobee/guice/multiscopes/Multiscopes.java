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
import com.google.inject.BindingAnnotation;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.ScopeAnnotation;
import com.google.inject.TypeLiteral;
import com.google.inject.binder.LinkedBindingBuilder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.Toolable;

/**
 * Class for binding multiscopes.<br/>
 * An unbounded (normal) multiscope binder from {@link #newBinder(Binder, Class, Class, Class)}
 * needs
 * <ul>
 * <li>A scope annotation, like all scopes
 * <li>A scope binding annotation, to specify this scope to inject the current {@link ScopeInstance}
 * <li>A 'new scope instance' binding annotation, used to inject a new {@link ScopeInstance} for the
 * {@link Multiscope} (also to specify a new scope storage map internally)
 * <li>Optionally, after creation you can specify a provider for the scope storage map. This is
 * specified from the {@link MultiscopeBinder} after you create it. This defaults to
 * {@link MultiscopeUtils#createDefaultScopeMap()}.
 * </ul>
 * <br/>
 * A bounded multiscope binder from {@link #newBoundedBinder(Binder, Class, Class)} needs
 * <ul>
 * <li>A scope annotation, like all scopes
 * <li>A scope binding annotation, to specify this scope to inject the current {@link ScopeInstance}
 * <li>At least one scope instance, specified from the {@link BoundedMultiscopeBinder} (scopes can
 * be added on various modules, this is similar to the multiset).
 * <li>Optionally, you can specify a provider for the scope storage map. This is performed on the
 * {@link MultiscopeBinder} after you create it. This defaults to
 * {@link MultiscopeUtils#createDefaultScopeMap()}.
 * <li>Optionally, you can prescope keys in bounded scope instances using
 * {@link BoundedMultiscopeBinder#prescopeInstance(Class)}. This makes the prescoped objects show up
 * when using the scope binding annotation with the key from the prescoped object. This is used in
 * conjunction with {@link #bindAsPrescoped(Binder, Class, Class, Class)} or
 * {@link #bindAsPrescoped(Binder, Class, Class, TypeLiteral)}.
 * </ul>
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public final class Multiscopes {

  private Multiscopes() {}

  /**
   * Creates a new {@link MultiscopeBinder}. The scope annotation has be be a
   * {@link ScopeAnnotation}, and the binding annotations have to be {@link BindingAnnotation}s.
   */
  public static MultiscopeBinder newBinder(final Binder binder,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> scopeBindingAnnotation,
      final Class<? extends Annotation> newScopeBindingAnnotation) {
    Preconditions.checkNotNull(binder, "binder");
    Preconditions.checkNotNull(scopeAnnotation, "scopeAnnotation");
    Preconditions.checkNotNull(scopeBindingAnnotation, "scopeBindingAnnotation");
    Preconditions.checkNotNull(newScopeBindingAnnotation, "newScopeBindingAnnotation");
    RealMultiscopeModule real =
        new RealMultiscopeModule(binder, scopeAnnotation, scopeBindingAnnotation,
            newScopeBindingAnnotation);
    binder.install(real);
    return real;
  }

  /**
   * Creates a new {@link BoundedMultiscopeBinder}. The scope annotation has be be a
   * {@link ScopeAnnotation}, and the binding annotation has to be a {@link BindingAnnotation}.
   */
  public static BoundedMultiscopeBinder newBoundedBinder(final Binder binder,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> scopeBindingAnnotation) {
    Preconditions.checkNotNull(binder, "binder");
    Preconditions.checkNotNull(scopeAnnotation, "scopeAnnotation");
    Preconditions.checkNotNull(scopeBindingAnnotation, "scopeBindingAnnotation");
    RealBoundedMultiscopeModule mbinder =
        new RealBoundedMultiscopeModule(binder, scopeAnnotation, scopeBindingAnnotation);
    binder.install(mbinder);
    return mbinder;
  }

  /**
   * Binds the given class as a prescoped type in the given scope. This tells Guice that we can find
   * the given type annotated with the scope binding annotation in the given multiscope. This is
   * used with {@link BoundedMultiscopeBinder#prescopeInstance(Class)}, which is used to specify
   * where to get the given class for each specific scope instance.
   */
  public static <T> void bindAsPrescoped(final Binder binder,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> scopeBindingAnnotation, final Class<T> prescopedClass) {
    bindAsPrescoped(binder, scopeAnnotation, scopeBindingAnnotation,
        TypeLiteral.get(prescopedClass));
  }

  /**
   * Binds the given class as a prescoped type in the given scope. This tells Guice that we can find
   * the given type annotated with the scope binding annotation in the given multiscope. This is
   * used with {@link BoundedMultiscopeBinder#prescopeInstance(Class)}, which is used to specify
   * where to get the given class for each specific scope instance.
   */
  public static <T> void bindAsPrescoped(final Binder binder,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> scopeBindingAnnotation, final TypeLiteral<T> prescopedType) {
    binder.bind(prescopedType).annotatedWith(scopeBindingAnnotation)
        .toProvider(new PrescopedProvider<T>()).in(scopeAnnotation);
  }

  static class RealMultiscopeModule implements MultiscopeBinder, Module {

    protected final Class<? extends Annotation> scopeAnnotation;
    protected final Class<? extends Annotation> scopeBindingAnnotation;
    protected final Class<? extends Annotation> newScopeBindingAnnotation;
    protected Multiscope multiscope;
    protected Binder binder;

    RealMultiscopeModule(Binder binder, Class<? extends Annotation> scopeAnnotation,
        Class<? extends Annotation> scopeBindingAnnotation,
        @Nullable Class<? extends Annotation> newScopeBindingAnnotation) {
      this.binder = binder;
      this.scopeAnnotation = scopeAnnotation;
      this.scopeBindingAnnotation = scopeBindingAnnotation;
      this.newScopeBindingAnnotation = newScopeBindingAnnotation;
    }

    protected Multiscope createMultiscope() {
      return new SimpleMultiscope(scopeBindingAnnotation);
    }

    @Override
    public void configure(Binder binder) {
      this.multiscope = createMultiscope();
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

    @Override
    public boolean equals(Object o) {
      return o instanceof RealMultiscopeModule
          && ((RealMultiscopeModule) o).scopeAnnotation.equals(scopeAnnotation);
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

  static class RealBoundedMultiscopeModule extends RealMultiscopeModule
      implements
        Module,
        BoundedMultiscopeBinder {

    RealBoundedMultiscopeModule(Binder binder, Class<? extends Annotation> scopeAnnotation,
        Class<? extends Annotation> scopeBindingAnnotation) {
      super(binder, scopeAnnotation, scopeBindingAnnotation, null);
    }

    @Override
    protected Multiscope createMultiscope() {
      return new AssistedMultiscope(scopeBindingAnnotation);
    }

    @Override
    public BoundedMultiscopeBinder addInstance(final Class<? extends Annotation> instanceAnnotation) {
      binder
          .bind(ScopeInstance.class)
          .annotatedWith(instanceAnnotation)
          .toProvider(
              new PrescopingSingletonInstanceProvider(scopeBindingAnnotation, instanceAnnotation));

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
          Preconditions.checkNotNull(key, "key");
          Preconditions.checkNotNull(type, "type");
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
      return o instanceof RealBoundedMultiscopeModule
          && ((RealBoundedMultiscopeModule) o).scopeAnnotation.equals(scopeAnnotation);
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

      volatile ScopeInstance instance = null;
      final Object instanceLock = new Object();

      Multiscope multiscope;
      Binding<Map<Key<?>, Object>> scopeMapBuilder = null;
      ImmutableSet<BindingAndType> bindings;
      ImmutableSet<Dependency<?>> dependencies;
      boolean initialized = false;

      PrescopingSingletonInstanceProvider(Class<? extends Annotation> scopeBindingAnnotation,
          Class<? extends Annotation> scopeInstanceAnnotation) {
        this.scopeBindingAnnotation = scopeBindingAnnotation;
        this.scopeInstanceAnnotation = scopeInstanceAnnotation;
      }

      @Inject
      @Toolable
      void initialize(Injector injector) {
        initialized = true;

        multiscope = injector.getInstance(Key.get(Multiscope.class, scopeBindingAnnotation));
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
              Dependency.get(Key.get(keyType, scopeInstanceAnnotation)),
              Dependency.get(Key.get(Multiscope.class, scopeBindingAnnotation)));
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
