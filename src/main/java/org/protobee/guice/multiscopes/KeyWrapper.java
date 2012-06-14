package org.protobee.guice.multiscopes;

import org.protobee.guice.multiscopes.BoundedMultiscopeBinder.PrescopeType;

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
