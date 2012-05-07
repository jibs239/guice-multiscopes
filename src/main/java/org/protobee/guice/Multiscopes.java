package org.protobee.guice;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

/**
 * Class for binding multiscopes. Each multiscope needs
 * <ul>
 * <li>A scope annotation, like all scopes
 * <li>A 'new holder' annotation, used to inject a new instance of a {@link ScopeHolder} for this
 * scope (also to specify the new scope holder internally)
 * <li>Optionally, a provider for the scope map. This defaults to {@link DefaultScopeMapProvider}
 * </ul>
 * 
 * You should be storing your multiscope instances as a <code>public static final</code> variable in
 * one of your classes. This way, you can access {@link Multiscope#isInScope()} and
 * {@link Multiscope#exitScope()} for testing and other cases where you need to check or exit the
 * scope when you don't have access to the scope holder itself.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public final class Multiscopes {

  private Multiscopes() {}

  /**
   * Binds the multiscope to the scope annotation, and binds {@link ScopeHolder} as prescoped in the
   * scope as well. Creating new scope holders for this multiscope is bound to the
   * newHolderAnnotation, as well as the scope map provider. This call uses the default
   * {@link DefaultScopeMapProvider}.
   * 
   * @param binder the binder to use
   * @param multiscope the multiscope instance
   * @param scopeAnnotation the scope annotation itself
   * @param newHolderAnnotation the annotation for creating a new scope holder (and a new scope map)
   */
  public static void bindMultiscope(final Binder binder, final Multiscope multiscope,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> newHolderAnnotation) {
    bindMultiscope(binder, multiscope, scopeAnnotation, newHolderAnnotation,
        DefaultScopeMapProvider.class);
  }

  /**
   * Binds the multiscope to the scope annotation, and binds {@link ScopeHolder} as prescoped in the
   * scope as well. Creating new scope holders for this multiscope is bound to the
   * newHolderAnnotation, as well as the scopeMapProvider.
   * 
   * @param binder the binder to use
   * @param multiscope the multiscope instance
   * @param scopeAnnotation the scope annotation itself
   * @param newHolderAnnotation the annotation for creating a new scope holder (and a new scope map)
   * @param scopeMapProvider the provider for the scope map. If you're accessing your scoped
   *        concurrently, this map should be threadsafe.
   */
  public static void bindMultiscope(final Binder binder, final Multiscope multiscope,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> newHolderAnnotation,
      final Class<? extends Provider<Map<Key<?>, Object>>> scopeMapProvider) {
    Preconditions.checkNotNull(multiscope);
    Preconditions.checkNotNull(scopeAnnotation);
    Preconditions.checkNotNull(binder);
    Preconditions.checkNotNull(scopeMapProvider);

    binder.bindScope(scopeAnnotation, multiscope);
    final TypeLiteral<Map<Key<?>, Object>> scopeMap = new TypeLiteral<Map<Key<?>, Object>>() {};
    binder.bind(scopeMap).annotatedWith(newHolderAnnotation)
        .toProvider(DefaultScopeMapProvider.class);

    binder
        .bind(ScopeHolder.class)
        .annotatedWith(multiscope.getHolderAnnotation())
        .toProvider(
            new PrescopedProvider<ScopeHolder>("ScopeHolder should have been bound internally.",
                "PrescopedProvider-ScopeHolder-" + scopeAnnotation.toString())).in(scopeAnnotation);

    binder.bind(ScopeHolder.class).annotatedWith(newHolderAnnotation)
        .toProvider(new Provider<ScopeHolder>() {
          @Override
          public ScopeHolder get() {
            return multiscope.createScopeHolder(binder.<Map<Key<?>, Object>>getProvider(
                Key.get(scopeMap, newHolderAnnotation)).get());
          }

          @Override
          public String toString() {
            return scopeAnnotation + "-New-ScopeHolder-Provider";
          }
        });
  }
}
