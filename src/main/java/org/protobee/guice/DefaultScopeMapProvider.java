package org.protobee.guice;

import java.util.Map;

import com.google.common.collect.MapMaker;
import com.google.inject.Key;
import com.google.inject.Provider;

/**
 * The default scope map provider, creates a concurrent map with concurrency of 8 and initial
 * capacity of 100
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public class DefaultScopeMapProvider implements Provider<Map<Key<?>, Object>> {
  @Override
  public Map<Key<?>, Object> get() {
    return new MapMaker().concurrencyLevel(8).initialCapacity(100).makeMap();
  }
}
