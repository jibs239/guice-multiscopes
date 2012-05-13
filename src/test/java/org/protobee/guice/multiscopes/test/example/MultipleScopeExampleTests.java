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
package org.protobee.guice.multiscopes.test.example;

import static org.junit.Assert.*;

import org.junit.Test;
import org.protobee.guice.multiscopes.example.Battlestar;
import org.protobee.guice.multiscopes.example.BattlestarFactory;
import org.protobee.guice.multiscopes.example.Fighter;
import org.protobee.guice.multiscopes.example.FighterFactory;
import org.protobee.guice.multiscopes.test.AbstractMultiscopeTest;

public class MultipleScopeExampleTests extends AbstractMultiscopeTest {

  @Test
  public void testFighterHasBattleship() {

    BattlestarFactory battlestarFactory = injector.getInstance(BattlestarFactory.class);
    FighterFactory fighterFactory = injector.getInstance(FighterFactory.class);
    Battlestar battlestar = battlestarFactory.create();

    Fighter fighter;
    try {
      battlestar.enterScope();

      fighter = fighterFactory.create();
      assertSame(battlestar, fighter.getBattlestar());
    } finally {
      battlestar.exitScope();
    }

    try {
      fighter.enterScope();
      assertSame(fighter, injector.getInstance(Fighter.class));
    } finally {
      fighter.exitScope();
    }
  }

  @Test
  public void testEmbeddedScoping() {
    BattlestarFactory battlestarFactory = injector.getInstance(BattlestarFactory.class);
    FighterFactory fighterFactory = injector.getInstance(FighterFactory.class);
    Battlestar battlestar = battlestarFactory.create();

    Fighter fighter;
    try {
      battlestar.enterScope();
      fighter = fighterFactory.create();
    } finally {
      battlestar.exitScope();
    }

    try {
      battlestar.enterScope();
      fighter.enterScope();
      assertTrue(battlestar.isInScope());
      assertTrue(fighter.isInScope());
      assertSame(battlestar, injector.getInstance(Battlestar.class));
      assertSame(fighter, injector.getInstance(Fighter.class));
    } finally {
      fighter.exitScope();
      battlestar.exitScope();
    }
  }
}
