package org.protobee.guice.example;

import org.protobee.guice.ScopeHolder;
import org.protobee.guice.example.scopes.NewBattlestarScopeHolder;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Creates a {@link battlestarModel} with it's corresponding scope.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
@Singleton
public class BattlestarFactory {

  private final Provider<ScopeHolder> scopeProvider;
  private final Provider<Battlestar> battlestarProvider;

  @Inject
  public BattlestarFactory(@NewBattlestarScopeHolder Provider<ScopeHolder> scopeProvider,
      Provider<Battlestar> battlestarProvider) {
    this.scopeProvider = scopeProvider;
    this.battlestarProvider = battlestarProvider;
  }

  /**
   * Creates a {@link Battlestar}. If prescoped objects were needed for the battlestar scope, they
   * would probably be arguments of this method and put into the scope before the creation of the
   * model. <br/>
   * Preconditions: not in a battlestar scope
   */
  public Battlestar create() {

    ScopeHolder battlestarScope = scopeProvider.get();
    Battlestar model;
    try {
      battlestarScope.enterScope();
      model = battlestarProvider.get();
    } finally {
      battlestarScope.exitScope();
    }
    return model;
  }
}
