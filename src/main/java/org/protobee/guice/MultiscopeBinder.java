package org.protobee.guice;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
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

public abstract class MultiscopeBinder {

  static void bindScopeMap(Binder binder, Class<? extends Annotation> scopeBindingAnnotation,
      Provider<Map<Key<?>, Object>> mapProvider) {
    binder.bind(
        Key.get(new TypeLiteral<Map<Key<?>, Object>>() {},
            Preconditions.checkNotNull(scopeBindingAnnotation, "scopeBindingAnnotation")))
        .toProvider(Preconditions.checkNotNull(mapProvider, "mapProvider"));
  }

  public static MultiscopeBinder createMultiscopeModule(Binder binder,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> scopeBindingAnnotation,
      final Class<? extends Annotation> newScopeBindingAnnotation) {
    RealMultiscopeModule real =
        new RealMultiscopeModule(binder, scopeAnnotation, scopeBindingAnnotation,
            newScopeBindingAnnotation);
    binder.install(real);
    return real;
  }

  public abstract LinkedBindingBuilder<Map<Key<?>, Object>> bindScopeStorageMap();

  static class RealMultiscopeModule extends MultiscopeBinder implements Module {

    private final Class<? extends Annotation> scopeAnnotation;
    private final Class<? extends Annotation> scopeBindingAnnotation;
    private final Class<? extends Annotation> newScopeBindingAnnotation;
    private final AbstractMultiscope multiscope;
    private Binder binder;

    RealMultiscopeModule(Binder binder, Class<? extends Annotation> scopeAnnotation,
        Class<? extends Annotation> scopeBindingAnnotation,
        Class<? extends Annotation> newScopeBindingAnnotation) {
      this.binder = binder;
      this.scopeAnnotation = scopeAnnotation;
      this.scopeBindingAnnotation = scopeBindingAnnotation;
      this.newScopeBindingAnnotation = newScopeBindingAnnotation;
      this.multiscope = new DynamicMultiscope(scopeBindingAnnotation);
    }

    @Override
    public void configure(Binder binder) {
      binder.bindScope(scopeAnnotation, multiscope);
      binder.bind(ScopeInstance.class).annotatedWith(newScopeBindingAnnotation)
          .toProvider(new NewInstanceProvider(scopeBindingAnnotation, multiscope));
      binder
          .bind(ScopeInstance.class)
          .annotatedWith(multiscope.getBindingAnnotation())
          .toProvider(
              new PrescopedProvider<ScopeInstance>(
                  "ScopeInstance should have been bound internally.", scopeAnnotation
                      .getSimpleName() + "-ScopeInstanceFakeProvider")).in(scopeAnnotation);
      binder.bind(Multiscope.class).annotatedWith(scopeBindingAnnotation).toInstance(multiscope);

      Multibinder<Multiscope> exiters = Multibinder.newSetBinder(binder, Multiscope.class);
      exiters.addBinding().toInstance(multiscope);
    }

    @Override
    public LinkedBindingBuilder<Map<Key<?>, Object>> bindScopeStorageMap() {
      return binder
          .bind(Key.get(new TypeLiteral<Map<Key<?>, Object>>() {}, scopeBindingAnnotation));
    }

    static class ScopeExiterProvider implements Provider<MultiscopeInspector> {

      final Class<? extends Annotation> scopeBindingAnnotation;
      final AbstractMultiscope multiscope;

      ScopeExiterProvider(Class<? extends Annotation> scopeBindingAnnotation,
          AbstractMultiscope multiscope) {
        this.scopeBindingAnnotation = scopeBindingAnnotation;
        this.multiscope = multiscope;
      }

      @Override
      public MultiscopeInspector get() {
        return new MultiscopeInspector() {
          @Override
          public Class<? extends Annotation> getScopeBindingAnnotation() {
            return scopeBindingAnnotation;
          }

          @Override
          public Multiscope getScope() {
            return multiscope;
          }

          @Override
          public void ensureExit() {
            multiscope.exitScope();
          }
        };
      }

      @Override
      public String toString() {
        return multiscope.toString() + "-NewInstanceProvider";
      }
    }

    static class NewInstanceProvider implements Provider<ScopeInstance>, HasDependencies {

      final Class<? extends Annotation> scopeBindingAnnotation;
      final AbstractMultiscope multiscope;
      Binding<Map<Key<?>, Object>> scopeMapBinding = null;
      boolean initialized = false;

      NewInstanceProvider(Class<? extends Annotation> scopeBindingAnnotation,
          AbstractMultiscope multiscope) {
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
}