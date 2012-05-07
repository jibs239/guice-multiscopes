package org.protobee.guice;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.google.common.collect.MapMaker;
import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;
import com.google.inject.Scope;

/**
 * A scope that supports multiple instances of the scope itself. Scope instances are created by
 * calling {@link #createScopeHolder()} or {@link #createScopeHolder(Map)}. This holder is then used
 * to enter and exit the scope.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public class Multiscope implements Scope {

  /** A sentinel attribute value representing null. */
  public static enum NullObject {
    INSTANCE
  }

  private final ThreadLocal<Map<Key<?>, Object>> scopeContext =
      new ThreadLocal<Map<Key<?>, Object>>();

  private final Object scopeLock = new Object();
  private final AtomicInteger scopeCounter = new AtomicInteger(0);

  private final String uniqueName;
  private final Class<? extends Annotation> holderAnnotation;

  public Multiscope(String uniqueName, Class<? extends Annotation> holderAnnotation) {
    Preconditions.checkNotNull(uniqueName);
    Preconditions.checkNotNull(holderAnnotation);

    this.uniqueName = uniqueName;
    this.holderAnnotation = holderAnnotation;
  }

  public Class<? extends Annotation> getHolderAnnotation() {
    return holderAnnotation;
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
   * Creates a new scope holder for this scope and adds the holder to the scope (annotated by the
   * holder annotation for this scope). Calls {@link #createScopeHolder(Map)} with a default scope
   * map with a concurrency level of 8
   * 
   * @return
   */
  protected ScopeHolder createScopeHolder() {
    return this.createScopeHolder(new MapMaker().concurrencyLevel(8).<Key<?>, Object>makeMap());
  }

  /**
   * Creates a new scope holder for this scope and adds the holder to the scope (annotated by the
   * holder annotation for this scope). Uses the given scope map.
   * 
   * @param scopeMap the map to use for storing scoped objects. This map should not be persisted and
   *        used otherwise, the only reason it's an argument is because of the injection limitations
   *        for scopes.
   * @return the scope holder
   */
  protected ScopeHolder createScopeHolder(final Map<Key<?>, Object> scopeMap) {
    final int holderId = scopeCounter.getAndIncrement();
    ScopeHolder holder = new ScopeHolder() {

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
      public int getHolderId() {
        return holderId;
      }

      @Override
      public String toString() {
        return "{ holderId: " + holderId + ", scope: " + uniqueName + "}";
      }
    };

    holder.putInScope(Key.get(ScopeHolder.class, holderAnnotation), holder);
    return holder;
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
