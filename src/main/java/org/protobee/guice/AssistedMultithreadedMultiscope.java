package org.protobee.guice;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.google.inject.Key;
import com.google.inject.OutOfScopeException;
import com.google.inject.Provider;

public class AssistedMultithreadedMultiscope extends AbstractMultiscope {

  static interface AssistedScopedObject<T> {
    Provider<T> getProvider();
  }
  
  public AssistedMultithreadedMultiscope(Class<? extends Annotation> instanceAnnotation) {
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

        if (preT == null || preT instanceof AssistedScopedObject) {
          synchronized (putLock) {
            preT = scopeMap.get(key);
            if (preT == null || preT instanceof AssistedScopedObject) {
              if (preT instanceof AssistedScopedObject) {
                preT = ((AssistedScopedObject<T>) preT).getProvider().get();
              } else {
                preT = creator.get();
              }
              // TODO: for next guice release, add this check:
              // if (!Scopes.isCircularProxy(t)) {
              // Store a sentinel for provider-given null values.
              scopeMap.put(key, preT != null ? preT : NullObject.INSTANCE);
              // }
            }
          }
        }
        T t = (T) preT;

        // Accounts for @Nullable providers.
        if (NullObject.INSTANCE == t) {
          return null;
        }

        return t;
      }

      @Override
      public String toString() {
        return "{ key: " + key + ", unscopedProvider: " + creator + ", scope: " + scope + "}";
      }
    };
  }
}
