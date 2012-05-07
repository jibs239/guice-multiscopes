package org.protobee.guice;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.protobee.guice.example.Battlestar;
import org.protobee.guice.example.BattlestarFactory;
import org.protobee.guice.example.scopedClasses.CommandDeck;

import com.google.inject.ProvisionException;

public class SingleScopeExampleTests extends AbstractMultiscopeTest {

  @Test
  public void testSingleScoping() {

    BattlestarFactory factory = injector.getInstance(BattlestarFactory.class);
    Battlestar battlestar = factory.create();
    assertFalse(battlestar.isInScope());

    CommandDeck deck;
    try {
      battlestar.enterScope();
      assertTrue(battlestar.isInScope());
      assertSame(battlestar, injector.getInstance(Battlestar.class));
      deck = injector.getInstance(CommandDeck.class);
      deck.setCapacity(1);
      deck.setName("small deck");

      battlestar.exitScope();
      battlestar.enterScope();
      assertSame(deck, injector.getInstance(CommandDeck.class));
    } finally {
      battlestar.exitScope();
    }

    boolean caught = false;
    try {
      deck = injector.getInstance(CommandDeck.class);
    } catch (ProvisionException exception) {
      caught = true;
    }
    assertTrue("Deck was created outside of scope", caught);
  }

  @Test
  public void testMultipleScopeInstances() {

    BattlestarFactory factory = injector.getInstance(BattlestarFactory.class);
    Battlestar battlestar1 = factory.create();
    Battlestar battlestar2 = factory.create();

    CommandDeck deck1;
    try {
      battlestar1.enterScope();
      deck1 = injector.getInstance(CommandDeck.class);
      deck1.setCapacity(10);
      deck1.setName("Deck1");

    } finally {
      battlestar1.exitScope();
    }

    CommandDeck deck2;
    try {
      battlestar2.enterScope();
      deck2 = injector.getInstance(CommandDeck.class);
      deck2.setCapacity(12);
      deck2.setName("Deck2");

    } finally {
      battlestar2.exitScope();
    }

    try {
      battlestar1.enterScope();
      CommandDeck deck = injector.getInstance(CommandDeck.class);
      assertSame(deck1, deck);
    } finally {
      battlestar1.exitScope();
    }

    try {
      battlestar2.enterScope();
      CommandDeck deck = injector.getInstance(CommandDeck.class);
      assertSame(deck2, deck);
    } finally {
      battlestar2.exitScope();
    }
  }
}
