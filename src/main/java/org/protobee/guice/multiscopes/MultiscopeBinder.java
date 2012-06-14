package org.protobee.guice.multiscopes;

import java.util.Map;

import com.google.inject.Key;
import com.google.inject.binder.LinkedBindingBuilder;

public interface MultiscopeBinder {
  LinkedBindingBuilder<Map<Key<?>, Object>> bindScopeStorageMap();
}
