/*******************************************************************************
 * Copyright (c) 2012, Daniel Murphy and Deanna Surma
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *   * Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.protobee.guice;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Test;
import org.protobee.guice.example.ExamplesGuiceModule;
import org.protobee.guice.example.scoped.CommandDeck;
import org.protobee.guice.example.scopes.BattlestarScope;
import org.protobee.guice.example.scopes.BattlestarScopeInstance;
import org.protobee.guice.example.scopes.ExampleScopes;
import org.protobee.guice.example.scopes.NewBattlestarScopeInstance;

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
  public void testScopeHolder() {
    Injector inj = Guice.createInjector(new ExamplesGuiceModule());

    ScopeInstance instance =
        inj.getInstance(Key.get(ScopeInstance.class, NewBattlestarScopeInstance.class));
    assertFalse(instance.isInScope());

    try {
      instance.enterScope();
      assertTrue(instance.isInScope());
      assertEquals(instance, inj.getInstance(Key.get(ScopeInstance.class, BattlestarScopeInstance.class)));
    } finally {
      instance.exitScope();
    }

    assertFalse(instance.isInScope());

    ScopeInstance instance2 =
        inj.getInstance(Key.get(ScopeInstance.class, NewBattlestarScopeInstance.class));

    assertNotSame(instance, instance2);
  }

  @Test
  public void testBasicScoping() {
    Injector inj = Guice.createInjector(new ExamplesGuiceModule());

    ScopeInstance instance =
        inj.getInstance(Key.get(ScopeInstance.class, NewBattlestarScopeInstance.class));

    try {
      instance.enterScope();
      CommandDeck deck = inj.getInstance(CommandDeck.class);
      assertNotNull(deck);
      assertTrue(deck == inj.getInstance(CommandDeck.class));
      instance.exitScope();
      instance.enterScope();
      assertTrue(deck == inj.getInstance(CommandDeck.class));
    } finally {
      instance.exitScope();
    }
  }
  
  @Test
  public void testTwoScopeInstances() {
    Injector inj = Guice.createInjector(new ExamplesGuiceModule());

    ScopeInstance instance =
        inj.getInstance(Key.get(ScopeInstance.class, NewBattlestarScopeInstance.class));
    ScopeInstance instance2 =
      inj.getInstance(Key.get(ScopeInstance.class, NewBattlestarScopeInstance.class));
    
    CommandDeck deck;
    try {
      instance.enterScope();
      deck = inj.getInstance(CommandDeck.class);
      assertNotNull(deck);
    } finally {
      instance.exitScope();
    }
    
    try {
      instance2.enterScope();
      assertNotSame(deck, inj.getInstance(CommandDeck.class));
    } finally {
      instance2.exitScope();
    } 
  }

  @Test
  public void testExceptionOnPreviouslyEnteredScope() {
    Injector inj = Guice.createInjector(new ExamplesGuiceModule());

    ScopeInstance instance =
        inj.getInstance(Key.get(ScopeInstance.class, NewBattlestarScopeInstance.class));

    boolean caught = false;
    try {
      instance.enterScope();
      instance.enterScope();
    } catch (IllegalStateException e) {
      caught = true;
    } finally {
      instance.exitScope();
    }
    assertTrue(caught);


    ScopeInstance instance2 =
        inj.getInstance(Key.get(ScopeInstance.class, NewBattlestarScopeInstance.class));

    caught = false;
    try {
      instance.enterScope();
      instance2.enterScope();
    } catch (IllegalStateException e) {
      caught = true;
    } finally {
      instance.exitScope();
    }
    assertTrue(caught);
  }

  @Test
  public void testExceptionWhenOutOfScope() {
    Injector inj = Guice.createInjector(new ExamplesGuiceModule());

    boolean caught = false;
    try {
      inj.getInstance(CommandDeck.class);
    } catch (ProvisionException e) {
      caught = true;
    }
    assertTrue(caught);
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

    ScopeInstance instance =
        inj.getInstance(Key.get(ScopeInstance.class, NewBattlestarScopeInstance.class));

    Captain captain = new Captain();
    instance.putInScope(Key.get(Captain.class), captain);

    try {
      instance.enterScope();
      assertEquals(captain, inj.getInstance(Captain.class));
    } finally {
      instance.exitScope();
    }

    boolean caught = false;
    try {
      inj.getInstance(Captain.class);
    } catch (ProvisionException e) {
      caught = true;
    }
    assertTrue(caught);
  }

  @Test
  public void testExceptionWhenNotPrescoped() {
    Injector inj = Guice.createInjector(new ExamplesGuiceModule(), new AbstractModule() {

      @Override
      protected void configure() {
        bind(Captain.class).toProvider(
            new PrescopedProvider<Captain>("Captain should have been prescoped")).in(
            BattlestarScope.class);
      }
    });

    boolean caught = false;
    ScopeInstance instance =
        inj.getInstance(Key.get(ScopeInstance.class, NewBattlestarScopeInstance.class));
    try {
      instance.enterScope();
      inj.getInstance(Captain.class);
    } catch (ProvisionException e) {
      caught = true;
    } finally {
      instance.exitScope();
    }
    assertTrue(caught);
  }
}
