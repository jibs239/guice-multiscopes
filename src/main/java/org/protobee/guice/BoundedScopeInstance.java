package org.protobee.guice;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import com.google.inject.BindingAnnotation;

/**
 * An internal binding annotation applied to each specifically per-scope assigned type
 */
@Retention(RUNTIME)
@BindingAnnotation
@interface BoundedScopeInstance {
  String instanceKey();
  String scopeKey();
}
