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

import org.protobee.guice.multicopes.Multiscopes;
import org.protobee.guice.multiscopes.example.scoped.BattlestarFighterRoster;
import org.protobee.guice.multiscopes.example.scoped.CommandDeck;
import org.protobee.guice.multiscopes.example.scoped.FighterWeapons;
import org.protobee.guice.multiscopes.example.scoped.Pilot;
import org.protobee.guice.multiscopes.example.scopes.BattlestarScope;
import org.protobee.guice.multiscopes.example.scopes.ExampleScopes;
import org.protobee.guice.multiscopes.example.scopes.FighterScope;
import org.protobee.guice.multiscopes.example.scopes.NewBattlestarScopeInstance;
import org.protobee.guice.multiscopes.example.scopes.NewFighterScopeInstance;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

public class ExamplesGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    Multiscopes.bindMultiscope(binder(), ExampleScopes.BATTLESTAR, BattlestarScope.class,
        NewBattlestarScopeInstance.class);
    Multiscopes.bindMultiscope(binder(), ExampleScopes.FIGHTER, FighterScope.class,
        NewFighterScopeInstance.class);

    bind(BattlestarFighterRoster.class).in(BattlestarScope.class);
    bind(CommandDeck.class).in(BattlestarScope.class);
    bind(FighterWeapons.class).in(FighterScope.class);
    bind(Pilot.class).in(FighterScope.class);

    bind(Descoper.class);

    bind(Battlestar.class).in(BattlestarScope.class);
    bind(BattlestarFactory.class).in(Singleton.class);
    bind(Fighter.class).in(FighterScope.class);
    bind(FighterFactory.class).in(Singleton.class);
  }
}
