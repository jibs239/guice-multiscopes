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
package org.protobee.guice.multiscopes.example;

import org.protobee.guice.multicopes.ScopeInstance;
import org.protobee.guice.multiscopes.example.scopes.NewBattlestarScopeInstance;

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

  private final Provider<ScopeInstance> scopeProvider;
  private final Provider<Battlestar> battlestarProvider;

  @Inject
  public BattlestarFactory(@NewBattlestarScopeInstance Provider<ScopeInstance> scopeProvider,
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

    ScopeInstance battlestarScope = scopeProvider.get();
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