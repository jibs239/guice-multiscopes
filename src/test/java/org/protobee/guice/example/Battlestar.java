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

import org.protobee.guice.ScopeInstance;
import org.protobee.guice.example.scopes.BattlestarScope;
import org.protobee.guice.example.scopes.BattlestarScopeInstance;

import com.google.inject.Inject;

/**
 * Represents a battlestar and holds it's scope. One could just use the
 * {@link BattlestarScopeInstance} {@link ScopeInstance} everywhere to reference a battlestar, but that
 * can get cumbersome and isn't very flexible.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
@BattlestarScope
public class Battlestar {

  private final ScopeInstance scope;

  @Inject
  public Battlestar(@BattlestarScopeInstance ScopeInstance scope) {
    this.scope = scope;
  }

  public ScopeInstance getScope() {
    return scope;
  }

  /**
   * Enters the battlestar scope. Calls should be in a try-finally statement, with
   * {@link #exitScope()} in the finally clause
   * 
   * @throws IllegalStateException if we're already in a battlestar scope
   * @see ScopeInstance#enterScope()
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
   * @see ScopeInstance#exitScope()
   */
  public void exitScope() {
    scope.exitScope();
  }
}
