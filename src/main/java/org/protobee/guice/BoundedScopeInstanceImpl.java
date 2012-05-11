package org.protobee.guice;

import java.lang.annotation.Annotation;

class BoundedScopeInstanceImpl implements BoundedScopeInstance {

  private final String scopeKey;
  private final String instanceKey;

  public BoundedScopeInstanceImpl(String scopeKey, String instanceKey) {
    this.scopeKey = scopeKey;
    this.instanceKey = instanceKey;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return BoundedScopeInstance.class;
  }

  @Override
  public String instanceKey() {
    return instanceKey;
  }

  @Override
  public String scopeKey() {
    return scopeKey;
  }

  @Override
  public String toString() {
    return "@" + BoundedScopeInstance.class.getName() + "(scopeKey=" + scopeKey + ",instanceKey="
        + instanceKey + ")";
  }

  @Override
  public int hashCode() {
    return 127 * ("scopeKey".hashCode() ^ scopeKey.hashCode()) + 127
        * ("instanceKey".hashCode() ^ instanceKey.hashCode());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    BoundedScopeInstanceImpl other = (BoundedScopeInstanceImpl) obj;
    if (instanceKey == null) {
      if (other.instanceKey != null) return false;
    } else if (!instanceKey.equals(other.instanceKey)) return false;
    if (scopeKey == null) {
      if (other.scopeKey != null) return false;
    } else if (!scopeKey.equals(other.scopeKey)) return false;
    return true;
  }
}
