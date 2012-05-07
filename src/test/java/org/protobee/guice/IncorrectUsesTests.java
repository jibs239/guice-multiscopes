package org.protobee.guice;

import org.junit.Test;
import org.protobee.guice.example.scopes.ExampleScopesGuiceModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class IncorrectUsesTests {

  @Test
  public void testExceptionOnUnscoped() {
    Injector inj = Guice.createInjector(new ExampleScopesGuiceModule());
    
    
  }
}
