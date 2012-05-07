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
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * A scope that supports multiple instances of the scope itself. After binding your multiscope with
 * {@link Multiscopes#bindMultiscope(com.google.inject.Binder, Multiscope, Class, Class)} or
 * {@link Multiscopes#bindMultiscope(com.google.inject.Binder, Multiscope, Class, Class, Class)},
 * you can create new instances of your scope by injecting a {@link ScopeInstance} annotated with
 * the annotation 'newInstanceAnnotation' given when you bind this scope. This instance is then used
 * to enter and exit the scope.<br/>
 * <br/>
 * So there are two annotations at work here, the 'scope instance' binding annotation and the 'new
 * scope instance' binding annotation. The 'new scope instance' annotation is to specify the
 * creation of a new {@link ScopeInstance} for the corresponding scope. When the new
 * {@link ScopeInstance} is created, it is is also bound in that scope with the 'scope instance'
 * annotation. This allows objects to inject the scope instance of the current scope. This is used
 * when making {@link ScopeInstance} holder classes, which is outlined in the wiki <a
 * href="http://code.google.com/p/guice-multiscopes/wiki/BestPractices">Best Practices</a> page, and
 * shown in the examples.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public class Multiscope implements Scope {

  /** A sentinel attribute value representing null. */
  private static enum NullObject {
    INSTANCE
  }

  private final ThreadLocal<Map<Key<?>, Object>> scopeContext =
      new ThreadLocal<Map<Key<?>, Object>>();

  private final Object scopeLock = new Object();
  private final AtomicInteger scopeCounter = new AtomicInteger(0);

  private final String uniqueName;
  private final Class<? extends Annotation> instanceAnnotation;

  /**
   * Constructs a multiscope with the given name and instance binding annotation, which is used to
   * specify the {@link ScopeInstance} instance in it's scope.
   * 
   * @param uniqueName
   * @param instanceAnnotation
   */
  public Multiscope(String uniqueName, Class<? extends Annotation> instanceAnnotation) {
    Preconditions.checkNotNull(uniqueName);
    Preconditions.checkNotNull(instanceAnnotation);

    this.uniqueName = uniqueName;
    this.instanceAnnotation = instanceAnnotation;
  }

  /**
   * Gets the annotation used to specify the scope instance
   */
  public Class<? extends Annotation> getInstanceAnnotation() {
    return instanceAnnotation;
  }

  @Override
  public <T> Provider<T> scope(final Key<T> key, final Provider<T> creator) {
    final Object putLock = new Object();
    final Multiscope scope = this;
    return new Provider<T>() {
      @SuppressWarnings("unchecked")
      public T get() {

        Map<Key<?>, Object> scopeMap = scopeContext.get();
        if (scopeMap != null) {
          T t = (T) scopeMap.get(key);

          if (t == null) {
            synchronized (putLock) {
              t = (T) scopeMap.get(key);
              if (t == null) {
                t = creator.get();
                // TODO: for next guice release, add this check:
                // if (!Scopes.isCircularProxy(t)) {
                // Store a sentinel for provider-given null values.
                scopeMap.put(key, t != null ? t : NullObject.INSTANCE);
                // }
              }
            }
          }

          // Accounts for @Nullable providers.
          if (NullObject.INSTANCE == t) {
            return null;
          }

          return t;
        }
        throw new OutOfScopeException("Cannot access session scoped object '" + key
            + "'. This means we are not inside of a " + uniqueName + " scoped call.");
      }

      @Override
      public String toString() {
        return "{ key: " + key + " unscopedProvider: " + creator + " scope: " + scope + "}";
      }
    };
  }

  @Override
  public String toString() {
    return "Scope." + uniqueName;
  }

  /**
   * @return If we're in this scope on this thread.
   */
  public boolean isInScope() {
    return scopeContext.get() != null;
  }

  /**
   * Makes sure this scope is not entered on the current thread.
   */
  public void exitScope() {
    synchronized (scopeLock) {
      scopeContext.set(null);
    }
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
  protected ScopeInstance createScopeInstance(final Map<Key<?>, Object> scopeMap) {
    final int instanceId = scopeCounter.getAndIncrement();
    ScopeInstance instance = new ScopeInstance() {

      @Override
      public boolean isInScope() {
        return scopeContext.get() == scopeMap;
      }

      @Override
      public void exitScope() {
        synchronized (scopeLock) {
          scopeContext.set(null);
        }
      }

      @Override
      public void enterScope() throws IllegalStateException {
        synchronized (scopeLock) {
          Preconditions.checkState(scopeContext.get() == null, "Already in " + uniqueName
              + " scope");
          scopeContext.set(scopeMap);
        }
      }

      @Override
      public void putInScope(Key<?> key, Object object) {
        putObjectInScope(key, object, scopeMap);
      }

      @Override
      public int getInstanceId() {
        return instanceId;
      }

      @Override
      public String toString() {
        return "{ instanceId: " + instanceId + ", scope: " + uniqueName + "}";
      }
    };

    instance.putInScope(Key.get(ScopeInstance.class, instanceAnnotation), instance);
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
