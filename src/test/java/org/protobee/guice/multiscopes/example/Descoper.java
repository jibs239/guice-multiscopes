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

import org.protobee.guice.multiscopes.example.scopes.ExampleScopes;

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
