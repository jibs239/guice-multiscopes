package org.protobee.guice.example;

import org.protobee.guice.example.scopes.ExampleScopes;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * This is an example utility class for exiting any current {@link Battlestar} or {@link Fighter}
 * scopes, and then re-entering them after performing any logic that needed any current scopes to be
 * exited. Calls should always be surrounded by a try-finally clause, where {@link #rescope()} is in
 * the finally clause.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public class Descoper {

  private final Provider<Battlestar> battlestarProvider;
  private final Provider<Fighter> fighterProvider;

  private Battlestar battlestar = null;
  private Fighter fighter = null;

  @Inject
  public Descoper(Provider<Battlestar> battlestarProvider, Provider<Fighter> fighterProvider) {
    this.battlestarProvider = battlestarProvider;
    this.fighterProvider = fighterProvider;
  }

  /**
   * Exits any current {@link Battlestar} or {@link Fighter} scopes. Should be in a try-finally
   * clause, with {@link #rescope()} in the finally clause.
   * 
   * @throws IllegalStateException if {@link #descope()} was already called without a matching
   *         {@link #rescope()}
   */
  public void descope() throws IllegalStateException {
    Preconditions.checkState(battlestar == null && fighter == null, "Already descoped.");
    if (ExampleScopes.BATTLESTAR.isInScope()) {
      battlestar = battlestarProvider.get();
      battlestar.exitScope();
    }
    if (ExampleScopes.FIGHTER.isInScope()) {
      fighter = fighterProvider.get();
      battlestar.exitScope();
    }
  }

  /**
   * Re-enters scopes that were exited
   */
  public void rescope() {
    if (fighter != null) {
      fighter.enterScope();
      fighter = null;
    }

    if (battlestar != null) {
      battlestar.enterScope();
      battlestar = null;
    }
  }
}
