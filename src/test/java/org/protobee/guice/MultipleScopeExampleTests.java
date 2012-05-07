package org.protobee.guice;

import static org.junit.Assert.*;

import org.junit.Test;
import org.protobee.guice.example.Battlestar;
import org.protobee.guice.example.BattlestarFactory;
import org.protobee.guice.example.Fighter;
import org.protobee.guice.example.FighterFactory;

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
