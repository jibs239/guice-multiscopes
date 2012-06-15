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

import org.protobee.guice.multiscopes.BoundedMultiscopeBinder;
import org.protobee.guice.multiscopes.BoundedMultiscopeBinder.PrescopeType;
import org.protobee.guice.multiscopes.Multiscopes;
import org.protobee.guice.multiscopes.example.scoped.BattlestarFighterRoster;
import org.protobee.guice.multiscopes.example.scoped.CommandDeck;
import org.protobee.guice.multiscopes.example.scoped.FighterWeapons;
import org.protobee.guice.multiscopes.example.scoped.GalaxyCenter;
import org.protobee.guice.multiscopes.example.scoped.GalaxyProperties;
import org.protobee.guice.multiscopes.example.scoped.Pilot;
import org.protobee.guice.multiscopes.example.scoped.Star;
import org.protobee.guice.multiscopes.example.scopes.Battlestar;
import org.protobee.guice.multiscopes.example.scopes.BattlestarScope;
import org.protobee.guice.multiscopes.example.scopes.Fighter;
import org.protobee.guice.multiscopes.example.scopes.FighterScope;
import org.protobee.guice.multiscopes.example.scopes.Galaxy;
import org.protobee.guice.multiscopes.example.scopes.GalaxyScope;
import org.protobee.guice.multiscopes.example.scopes.MilkyWayGalaxy;
import org.protobee.guice.multiscopes.example.scopes.NewBattlestar;
import org.protobee.guice.multiscopes.example.scopes.NewFighter;
import org.protobee.guice.multiscopes.util.CompleteDescoper;
import org.protobee.guice.multiscopes.util.MultiscopeExitor;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

public class ExamplesGuiceModule extends AbstractModule {

  @Override
  protected void configure() {
    Multiscopes.newBinder(binder(), BattlestarScope.class, Battlestar.class, NewBattlestar.class);
    Multiscopes.newBinder(binder(), FighterScope.class, Fighter.class, NewFighter.class);

    // bind our scoped objects
    bind(BattlestarFighterRoster.class).in(BattlestarScope.class);
    bind(CommandDeck.class).in(BattlestarScope.class);
    bind(FighterWeapons.class).in(FighterScope.class);
    bind(Pilot.class).in(FighterScope.class);
    bind(CompleteDescoper.class);
    bind(MultiscopeExitor.class);

    // bind our custom holder and factory
    bind(FighterHolder.class).in(FighterScope.class);
    bind(FighterFactory.class).in(Singleton.class);


    // bind our bounded multiscope
    Multiscopes.newBoundedBinder(binder(), GalaxyScope.class, Galaxy.class);

    // bind required prescoped items
    Multiscopes.bindAsPrescoped(binder(), GalaxyScope.class, Galaxy.class, GalaxyProperties.class);
    TypeLiteral<Star[]> starArrayType = new TypeLiteral<Star[]>() {};
    Multiscopes.bindAsPrescoped(binder(), GalaxyScope.class, Galaxy.class, starArrayType);
    bind(GalaxyCenter.class).in(GalaxyScope.class);

    // install our milkly way module to define the milky way galaxy scope
    install(new MilklyWayModule());
  }

  // separated into separate module as an example, would usually be in a separate file
  public class MilklyWayModule extends AbstractModule {
    @Override
    protected void configure() {
      BoundedMultiscopeBinder galaxyScopes =
          Multiscopes.newBoundedBinder(binder(), GalaxyScope.class, Galaxy.class);
      galaxyScopes.addInstance(MilkyWayGalaxy.class);

      TypeLiteral<Star[]> starArrayType = new TypeLiteral<Star[]>() {};

      galaxyScopes
          .prescopeInstance(MilkyWayGalaxy.class)
          .addInstanceObject(Key.get(starArrayType, MilkyWayGalaxy.class))
          .addInstanceObject(Key.get(GalaxyProperties.class, MilkyWayGalaxy.class),
              PrescopeType.EAGER);
    }

    @Provides
    @MilkyWayGalaxy
    public GalaxyProperties getProperties() {
      return new GalaxyProperties(200000000000l, 40000l);
    }

    @Provides
    @MilkyWayGalaxy
    public Star[] getStars() {
      // just a couple blank ones for example
      return new Star[] {new Star() {}, new Star() {}};
    }
  }
}
