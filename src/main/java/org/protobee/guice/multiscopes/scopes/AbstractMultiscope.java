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
package org.protobee.guice.multiscopes.scopes;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.protobee.guice.multiscopes.Multiscope;
import org.protobee.guice.multiscopes.ScopeInstance;

import com.google.common.base.Preconditions;
import com.google.inject.Key;

abstract class AbstractMultiscope extends Multiscope {

  /** A sentinel attribute value representing null. */
  static enum NullObject {
    INSTANCE
  }

  protected final ThreadLocal<Map<Key<?>, Object>> scopeContext =
      new ThreadLocal<Map<Key<?>, Object>>();

  protected final AtomicInteger scopeCounter = new AtomicInteger(0);

  private final String name;

  public AbstractMultiscope(Class<? extends Annotation> bindingAnnotation) {
    super(bindingAnnotation);
    this.name = bindingAnnotation.getSimpleName();
  }

  /**
   * @return If we're in this scope on this thread.
   */
  @Override
  public boolean isInScope() {
    return scopeContext.get() != null;
  }

  /**
   * Makes sure this scope is not entered on the current thread.
   */
  @Override
  public void exitScope() {
    scopeContext.set(null);
  }

  protected String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "SCOPE." + name;
  }

  /**
   * Creates a new scope instance for this scope and adds the instance to the scope (annotated by
   * the instance annotation for this scope). Uses the given scope map.
   * 
   * @param scopeMap the map to use for storing scoped objects. This map should not be persisted and
   *        used otherwise, the only reason it's an argument is because of the injection limitations
   *        for scopes.
   * @return the scope instance
   */
  @Override
  protected ScopeInstance createScopeInstance(final Map<Key<?>, Object> scopeMap) {
    final int instanceId = scopeCounter.getAndIncrement();
    ScopeInstance instance = new ScopeInstance() {

      @Override
      public boolean isInScope() {
        return scopeContext.get() == scopeMap;
      }

      @Override
      public void exitScope() {
        scopeContext.set(null);
      }

      @Override
      public void enterScope() throws IllegalStateException {
        Preconditions.checkState(scopeContext.get() == null, "Already in " + getName() + " scope.");
        scopeContext.set(scopeMap);
      }

      @Override
      public void putInScope(Key<?> key, Object object) {
        Preconditions.checkNotNull(key, "key");
        putObjectInScope(key, object, scopeMap);
      }

      @Override
      public int getInstanceId() {
        return instanceId;
      }

      @Override
      public String toString() {
        return "{ instanceId: " + instanceId + ", scope: " + AbstractMultiscope.this.toString()
            + "}";
      }
    };

    instance.putInScope(Key.get(ScopeInstance.class, getBindingAnnotation()), instance);
    return instance;
  }


  static void putObjectInScope(Key<?> key, Object object, Map<Key<?>, Object> map) {
    map.put(key, validateAndCanonicalizeValue(key, object));
  }

  /**
   * Validates the key and object, ensuring the value matches the key type, and canonicalizing null
   * objects to the null sentinel.
   */
  static Object validateAndCanonicalizeValue(Key<?> key, Object object) {
    if (object == null || object == NullObject.INSTANCE) {
      return NullObject.INSTANCE;
    }

    Preconditions.checkArgument(key.getTypeLiteral().getRawType().isInstance(object), "Value '"
        + object + "' of type '" + object.getClass().getName() + "' is not compatible with key '"
        + key + "'");

    return object;
  }
}
