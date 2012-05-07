package org.protobee.guice.testscope;

import static org.protobee.guice.Multiscopes.bindMultiscope;

import com.google.inject.AbstractModule;

public class TestScopeGuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    bindMultiscope(binder(), TestScopes.DEPARTMENT, TestScope.class, NewTestScopeHolder.class);
  }
}
