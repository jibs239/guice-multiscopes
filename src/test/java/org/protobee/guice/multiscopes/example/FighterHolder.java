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

import com.google.inject.Inject;
import org.protobee.guice.multiscopes.ScopeInstance;
import org.protobee.guice.multiscopes.example.scopes.Battlestar;
import org.protobee.guice.multiscopes.example.scopes.Fighter;
import org.protobee.guice.multiscopes.example.scopes.FighterScope;

/**
 * Represents a Fighter and hold it's scope. We use this holder to facilitate a hierarchy, where we
 * have a parent {@link Battlestar}. This hierarchical scoping is only facilitated through the
 * {@link FighterFactory} and our injected {@link Battlestar} {@link ScopeInstance}.
 *
 * @author Daniel Murphy (daniel@dmurph.com)
 */
@FighterScope public class FighterHolder {

	private final ScopeInstance scope;
	private final ScopeInstance battlestar;

	@Inject public FighterHolder(@Fighter ScopeInstance scope, @Battlestar ScopeInstance battlestar) {
		this.scope = scope;
		this.battlestar = battlestar;
	}

	public ScopeInstance getBattlestar() {
		return battlestar;
	}

	public ScopeInstance getScope() {
		return scope;
	}

	/**
	 * Enters the fighter scope. Calls should be in a try-finally statement, with {@link #exitScope()}
	 * in the finally clause
	 *
	 * @throws IllegalStateException if we're already in a figher scope
	 * @see ScopeInstance#enterScope()
	 */
	public void enterScope() throws IllegalStateException {
		scope.enterScope();
	}

	public boolean isInScope() {
		return scope.isInScope();
	}

	/**
	 * Exits the fighter scope.
	 *
	 * @see ScopeInstance#exitScope()
	 */
	public void exitScope() {
		scope.exitScope();
	}
}
