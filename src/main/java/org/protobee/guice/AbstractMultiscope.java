package org.protobee.guice;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.google.inject.Key;

/**
 * A scope that supports multiple instances of the scope itself. After binding your multiscope with
 * {@link Multiscopes#bindMultiscope(com.google.inject.Binder, SimpleMultiscope, Class, Class)} or
 * {@link Multiscopes#bindMultiscope(com.google.inject.Binder, SimpleMultiscope, Class, Class, Class)}
 * , you can create new instances of your scope by injecting a {@link ScopeInstance} annotated with
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
public abstract class AbstractMultiscope implements Multiscope {

  /** A sentinel attribute value representing null. */
  static enum NullObject {
    INSTANCE
  }

  protected final ThreadLocal<Map<Key<?>, Object>> scopeContext =
      new ThreadLocal<Map<Key<?>, Object>>();

  protected final AtomicInteger scopeCounter = new AtomicInteger(0);

  private final Class<? extends Annotation> instanceAnnotation;
  private final String name;

  public AbstractMultiscope(Class<? extends Annotation> instanceAnnotation) {
    this.instanceAnnotation = Preconditions.checkNotNull(instanceAnnotation, "instanceAnnotation");
    this.name = instanceAnnotation.getSimpleName();
  }

  /**
   * Gets the annotation used to specify the scope instance
   */
  @Override
  public Class<? extends Annotation> getBindingAnnotation() {
    return instanceAnnotation;
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
