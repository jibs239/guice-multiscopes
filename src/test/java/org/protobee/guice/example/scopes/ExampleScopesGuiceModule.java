package org.protobee.guice.example.scopes;

import static org.protobee.guice.Multiscopes.bindMultiscope;

import com.google.inject.AbstractModule;

public class ExampleScopesGuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    bindMultiscope(binder(), ExampleScopes.BATTLESTAR, BattlestarScope.class, NewBattlestarScopeHolder.class);
    bindMultiscope(binder(), ExampleScopes.FIGHTER, FighterScope.class, NewFighterScopeHolder.class);
  }
}
