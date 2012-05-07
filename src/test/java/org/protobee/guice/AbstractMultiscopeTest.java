package org.protobee.guice;

import com.google.inject.Guice;

import org.junit.After;
import org.junit.Before;
import org.protobee.guice.example.ExamplesGuiceModule;
import org.protobee.guice.example.scopes.ExampleScopes;

import com.google.inject.Injector;

public class AbstractMultiscopeTest {

  protected Injector injector;
  
  @Before
  public void init() {
    injector = Guice.createInjector(new ExamplesGuiceModule());
  }
  
  @After
  public void clearScopes() {
    ExampleScopes.BATTLESTAR.exitScope();
    ExampleScopes.FIGHTER.exitScope();
  }
}
