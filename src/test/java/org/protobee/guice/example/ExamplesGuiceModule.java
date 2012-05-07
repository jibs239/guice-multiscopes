package org.protobee.guice.example;

import static org.protobee.guice.Multiscopes.bindMultiscope;

import org.protobee.guice.example.scopedClasses.BattlestarFighterRoster;
import org.protobee.guice.example.scopedClasses.CommandDeck;
import org.protobee.guice.example.scopedClasses.FighterWeapons;
import org.protobee.guice.example.scopedClasses.Pilot;
import org.protobee.guice.example.scopes.BattlestarScope;
import org.protobee.guice.example.scopes.ExampleScopes;
import org.protobee.guice.example.scopes.FighterScope;
import org.protobee.guice.example.scopes.NewBattlestarScopeHolder;
import org.protobee.guice.example.scopes.NewFighterScopeHolder;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class ExamplesGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    bindMultiscope(binder(), ExampleScopes.BATTLESTAR, BattlestarScope.class, NewBattlestarScopeHolder.class);
    bindMultiscope(binder(), ExampleScopes.FIGHTER, FighterScope.class, NewFighterScopeHolder.class);
    
    bind(BattlestarFighterRoster.class).in(BattlestarScope.class);
    bind(CommandDeck.class).in(BattlestarScope.class);
    bind(FighterWeapons.class).in(FighterScope.class);
    bind(Pilot.class).in(FighterScope.class);
    
    bind(Battlestar.class).in(BattlestarScope.class);
    bind(BattlestarFactory.class).in(Singleton.class);
    bind(Fighter.class).in(FighterScope.class);
    bind(FighterFactory.class).in(Singleton.class);
  }
}
