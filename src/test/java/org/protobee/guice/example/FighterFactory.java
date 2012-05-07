package org.protobee.guice.example;

import org.protobee.guice.ScopeHolder;
import org.protobee.guice.example.scopes.ExampleScopes;
import org.protobee.guice.example.scopes.NewFighterScopeHolder;

import com.google.common.base.Preconditions;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Creates a {@link Fighter} with it's corresponding scope.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
@Singleton
public class FighterFactory {

  private final Provider<ScopeHolder> scopeProvider;
  private final Provider<Fighter> fighterProvider;

  public FighterFactory(@NewFighterScopeHolder Provider<ScopeHolder> scopeProvider,
      Provider<Fighter> fighterProvider) {
    this.scopeProvider = scopeProvider;
    this.fighterProvider = fighterProvider;
  }

  /**
   * Creates a fighter scope in a {@link Fighter}. If prescoped objects were needed for the
   * fighter scope, they would probably be arguments of this method and put into the scope before
   * the creation of the model. <br/>
   * Preconditions: not in a fighter scope, and we need to be in the parent battlestar scope.
   */
  public Fighter create() {
    Preconditions.checkState(ExampleScopes.BATTLESTAR.isInScope(), "Not in Battlestar scope");

    ScopeHolder fighterScope = scopeProvider.get();
    Fighter model;
    try {
      fighterScope.enterScope();
      model = fighterProvider.get();
    } finally {
      fighterScope.exitScope();
    }
    return model;
  }
}
