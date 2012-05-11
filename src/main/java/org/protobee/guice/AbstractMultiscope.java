package org.protobee.guice;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
