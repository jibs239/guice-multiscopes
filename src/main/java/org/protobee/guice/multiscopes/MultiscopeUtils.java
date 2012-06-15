package org.protobee.guice.multiscopes;

import java.util.Map;

import com.google.common.collect.MapMaker;
import com.google.inject.Key;

public final class MultiscopeUtils {

  private MultiscopeUtils() {}

  /**
   * Creates a default scope map with concurrency level of 8 and initial capacity of 100.
   */
  public static Map<Key<?>, Object> createDefaultScopeMap() {
    return new MapMaker().concurrencyLevel(8).initialCapacity(100).makeMap();
  }
}
