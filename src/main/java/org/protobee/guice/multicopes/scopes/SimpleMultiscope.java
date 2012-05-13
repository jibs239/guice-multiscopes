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
package org.protobee.guice.multicopes.scopes;

import java.lang.annotation.Annotation;
import java.util.Map;

import org.protobee.guice.multicopes.Multiscope;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;

public class SimpleMultiscope extends AbstractMultiscope {

  public SimpleMultiscope(Class<? extends Annotation> instanceAnnotation) {
    super(instanceAnnotation);
  }

  @Override
  public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
    final Object putLock = new Object();
    final Multiscope scope = this;
    return new Provider<T>() {
      @SuppressWarnings("unchecked")
      public T get() {

        Map<Key<?>, Object> scopeMap = scopeContext.get();

        if (scopeMap == null) {
          throw new OutOfScopeException("Cannot access session scoped object '" + key
              + "'. This means we are not inside of a " + getName() + " scoped call.");
        }
        Object preT = scopeMap.get(key);

        if (preT == null) {
          synchronized (putLock) {
            preT = scopeMap.get(key);
            if (preT == null) {
              preT = creator.get();
              // TODO: for next guice release, add this check:
              // if (!Scopes.isCircularProxy(t)) {
              // Store a sentinel for provider-given null values.
              scopeMap.put(key, preT != null ? preT : NullObject.INSTANCE);
              // }
            }
          }
        }

        // Accounts for @Nullable providers.
        if (NullObject.INSTANCE == preT) {
          return null;
        }

        return (T) preT;
      }

      @Override
      public String toString() {
        return "{ key: " + key + ", unscopedProvider: " + creator + ", scope: " + scope + "}";
      }
    };
  }
}
