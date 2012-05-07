package org.protobee.guice.example;

import org.protobee.guice.ScopeHolder;
import org.protobee.guice.example.scopes.BattlestarScope;
import org.protobee.guice.example.scopes.BattlestarScopeHolder;

import com.google.inject.Inject;

/**
 * Represents a battlestar and holds it's scope. One could just use the
 * {@link BattlestarScopeHolder} {@link ScopeHolder} everywhere to reference a battlestar, but that
 * can get cumbersome and isn't very flexible.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
@BattlestarScope
public class Battlestar {

  private final ScopeHolder scope;

  @Inject
  public Battlestar(@BattlestarScopeHolder ScopeHolder scope) {
    this.scope = scope;
  }

  public ScopeHolder getScope() {
    return scope;
  }

  /**
   * Enters the battlestar scope. Calls should be in a try-finally statement, with
   * {@link #exitScope()} in the finally clause
   * 
   * @throws IllegalStateException if we're already in a battlestar scope
   * @see ScopeHolder#enterScope()
   */
  public void enterScope() throws IllegalStateException {
    scope.enterScope();
  }

  public boolean isInScope() {
    return scope.isInScope();
  }

  /**
   * Exits the battlestar scope.
   * 
   * @see ScopeHolder#exitScope()
   */
  public void exitScope() {
    scope.exitScope();
  }
}
