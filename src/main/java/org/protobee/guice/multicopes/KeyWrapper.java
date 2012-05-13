package org.protobee.guice.multicopes;

import org.protobee.guice.multicopes.BoundedMultiscopeBinder.PrescopeType;

import com.google.inject.Key;

class KeyWrapper {

  private final Key<?> key;
  private final PrescopeType type;

  public KeyWrapper(Key<?> key, PrescopeType type) {
    this.key = key;
    this.type = type;
  }

  public Key<?> getKey() {
    return key;
  }
  
  public PrescopeType getType() {
    return type;
  }
}
