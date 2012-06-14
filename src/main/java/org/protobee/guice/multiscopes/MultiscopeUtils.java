package org.protobee.guice.multiscopes;

import java.util.Map;

import com.google.common.collect.MapMaker;
import com.google.inject.Key;

public class MultiscopeUtils {

  public static  Map<Key<?>, Object> createDefaultScopeMap() {
    return new MapMaker().concurrencyLevel(8).initialCapacity(100).makeMap();
  }
}
