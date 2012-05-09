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
package org.protobee.guice;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;

/**
 * Class for binding multiscopes. Each multiscope binding needs
 * <ul>
 * <li>A {@link Multiscope} instance
 * <li>A scope annotation, like all scopes
 * <li>A 'new scope instance' binding annotation, used to inject a new {@link ScopeInstance} for
 * the {@link Multiscope} (also to specify a new scope storage map internally)
 * <li>Optionally, a provider for the scope storage map. This defaults to {@link DefaultScopeMapProvider}
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

  /**
   * Binds the multiscope to the scope annotation, and binds {@link ScopeInstance} as prescoped in the
   * scope as well. Creating new scope instances for this multiscope is bound to the
   * newInstanceAnnotation, as well as the scope map provider. This call uses the default
   * {@link DefaultScopeMapProvider}.
   * 
   * @param binder the binder to use
   * @param multiscope the multiscope instance
   * @param scopeAnnotation the scope annotation itself
   * @param newScopeInstanceAnnotation the annotation for creating a new scope instance (and a new scope map)
   */
  public static void bindMultiscope(final Binder binder, final Multiscope multiscope,
      final Class<? extends Annotation> scopeAnnotation,
      final Class<? extends Annotation> newScopeInstanceAnnotation) {
    bindMultiscope(binder, multiscope, scopeAnnotation, newScopeInstanceAnnotation,
        DefaultScopeMapProvider.class);
  }

  /**
   * Binds the multiscope to the scope annotation, and binds {@link ScopeInstance} as prescoped in the
   * scope as well. Creating new scope instances for this multiscope is bound to the
   * newInstanceAnnotation, as well as the scopeMapProvider.
   * 
   * @param binder the binder to use
   * @param multiscope the multiscope instance
   * @param scopeAnnotation the scope annotation itself
   * @param newInstanceAnnotation the annotation for creating a new scope instance (and a new scope map)
   * @param scopeMapProvider the provider for the scope map. If you're accessing your scoped
   *        concurrently, this map should be threadsafe.
   */
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
    binder.bind(scopeMap).annotatedWith(newInstanceAnnotation)
        .toProvider(scopeMapProvider);

    final Provider<Map<Key<?>, Object>> mapProvider =
        binder.<Map<Key<?>, Object>>getProvider(Key.get(scopeMap, newInstanceAnnotation));
    
    binder
        .bind(ScopeInstance.class)
        .annotatedWith(multiscope.getInstanceAnnotation())
        .toProvider(
            new PrescopedProvider<ScopeInstance>("ScopeInstance should have been bound internally.",
                scopeAnnotation.getSimpleName()+"-ScopeInstanceFakeProvider")).in(scopeAnnotation);

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
