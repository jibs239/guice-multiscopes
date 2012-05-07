package org.protobee.guice.example;

import org.protobee.guice.ScopeHolder;
import org.protobee.guice.example.scopedClasses.BattlestarFighterRoster;
import org.protobee.guice.example.scopes.ExampleScopes;
import org.protobee.guice.example.scopes.NewFighterScopeHolder;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Creates a {@link Fighter} with it's corresponding scope, and adds it to the
 * {@link BattlestarFighterRoster}.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
@Singleton
public class FighterFactory {

  private final Provider<ScopeHolder> scopeProvider;
  private final Provider<Fighter> fighterProvider;
  private final Provider<BattlestarFighterRoster> rosterProvider;

  @Inject
  public FighterFactory(@NewFighterScopeHolder Provider<ScopeHolder> scopeProvider,
      Provider<Fighter> fighterProvider, Provider<BattlestarFighterRoster> rosterProvider) {
    this.scopeProvider = scopeProvider;
    this.fighterProvider = fighterProvider;
    this.rosterProvider = rosterProvider;
  }

  /**
   * Creates a fighter scope in a {@link Fighter}, and adds the fighter to the
   * {@link BattlestarFighterRoster} from the battlestar scope. If prescoped objects were needed for
   * the fighter scope, they would probably be arguments of this method and put into the scope
   * before the creation of the model. <br/>
   * Preconditions: not in a fighter scope, and we need to be in the parent battlestar scope.
   */
  public Fighter create() {
    Preconditions.checkState(ExampleScopes.BATTLESTAR.isInScope(), "Not in Battlestar scope");

    ScopeHolder fighterScope = scopeProvider.get();
    Fighter fighter;
    try {
      fighterScope.enterScope();
      fighter = fighterProvider.get();
    } finally {
      fighterScope.exitScope();
    }
    rosterProvider.get().addFighter(fighter);
    return fighter;
  }
}
