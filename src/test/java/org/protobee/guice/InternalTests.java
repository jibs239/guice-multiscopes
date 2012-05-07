package org.protobee.guice;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Test;
import org.protobee.guice.example.ExamplesGuiceModule;
import org.protobee.guice.example.scopedClasses.CommandDeck;
import org.protobee.guice.example.scopes.BattlestarScope;
import org.protobee.guice.example.scopes.BattlestarScopeHolder;
import org.protobee.guice.example.scopes.ExampleScopes;
import org.protobee.guice.example.scopes.NewBattlestarScopeHolder;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ProvisionException;

public class InternalTests {

  @After
  public void clearScopes() {
    ExampleScopes.BATTLESTAR.exitScope();
    ExampleScopes.FIGHTER.exitScope();
  }

  @Test
  public void testNewHolder() {
    Injector inj = Guice.createInjector(new ExamplesGuiceModule());

    ScopeHolder holder =
        inj.getInstance(Key.get(ScopeHolder.class, NewBattlestarScopeHolder.class));
    assertFalse(holder.isInScope());

    CommandDeck deck;
    try {
      holder.enterScope();
      assertTrue(holder.isInScope());
      assertEquals(holder, inj.getInstance(Key.get(ScopeHolder.class, BattlestarScopeHolder.class)));

      deck = inj.getInstance(CommandDeck.class);
      assertTrue(deck == inj.getInstance(CommandDeck.class));
      holder.exitScope();
      holder.enterScope();
      assertTrue(deck == inj.getInstance(CommandDeck.class));

    } finally {
      holder.exitScope();
    }

    assertFalse(holder.isInScope());
  }

  @BattlestarScope
  public static class Captain {}

  @Test
  public void testPrescope() {
    Injector inj = Guice.createInjector(new ExamplesGuiceModule(), new AbstractModule() {

      @Override
      protected void configure() {
        bind(Captain.class).toProvider(
            new PrescopedProvider<Captain>("Captain should have been prescoped")).in(
            BattlestarScope.class);
      }
    });

    ScopeHolder holder =
        inj.getInstance(Key.get(ScopeHolder.class, NewBattlestarScopeHolder.class));

    Captain captain = new Captain();
    holder.putInScope(Key.get(Captain.class), captain);

    try {
      holder.enterScope();
      assertEquals(captain, inj.getInstance(Captain.class));
    } finally {
      holder.exitScope();
    }

    boolean caught = false;
    try {
      inj.getInstance(Captain.class);
    } catch (ProvisionException e) {
      caught = true;
    }
    assertTrue(caught);
  }
}
