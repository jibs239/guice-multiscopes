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
