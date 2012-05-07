package org.protobee.guice.example;

import org.protobee.guice.ScopeHolder;
import org.protobee.guice.example.scopes.FighterScope;
import org.protobee.guice.example.scopes.FighterScopeHolder;

import com.google.inject.Inject;

/**
 * Represents a Fighter and hold it's scope. Like the {@link Battlestar}, we use this model to hold our scope in a
 * friendly way. This also allows us to easily store our parent {@link Battlestar} scope (this
 * hierarchical scoping is only facilitated through the {@link FighterFactory} and our injected
 * {@link Battlestar}.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
@FighterScope
public class Fighter {

  private final ScopeHolder scope;
  private final Battlestar battlestar;

  @Inject
  public Fighter(@FighterScopeHolder ScopeHolder scope, Battlestar battlestar) {
    this.scope = scope;
    this.battlestar = battlestar;
  }

  public Battlestar getBattlestar() {
    return battlestar;
  }

  public ScopeHolder getScope() {
    return scope;
  }

  /**
   * Enters the fighter scope. Calls should be in a try-finally statement, with {@link #exitScope()}
   * in the finally clause
   * 
   * @throws IllegalStateException if we're already in a figher scope
   * @see ScopeHolder#enterScope()
   */
  public void enterScope() throws IllegalStateException {
    scope.enterScope();
  }

  public boolean isInScope() {
    return scope.isInScope();
  }

  /**
   * Exits the fighter scope.
   * @see ScopeHolder#exitScope()
   */
  public void exitScope() {
    scope.exitScope();
  }
}
