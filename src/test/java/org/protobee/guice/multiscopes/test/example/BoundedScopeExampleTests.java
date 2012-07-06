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
package org.protobee.guice.multiscopes.test.example;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;
import org.protobee.guice.multiscopes.Multiscope;
import org.protobee.guice.multiscopes.ScopeInstance;
import org.protobee.guice.multiscopes.example.ExamplePluginModule;
import org.protobee.guice.multiscopes.example.ExamplesGuiceModule;
import org.protobee.guice.multiscopes.example.scoped.GalaxyCenter;
import org.protobee.guice.multiscopes.example.scoped.GalaxyProperties;
import org.protobee.guice.multiscopes.example.scopes.AndromedaGalaxy;
import org.protobee.guice.multiscopes.example.scopes.Galaxy;
import org.protobee.guice.multiscopes.example.scopes.MilkyWayGalaxy;
import org.protobee.guice.multiscopes.example.scopes.NewBattlestar;
import org.protobee.guice.multiscopes.test.AbstractMultiscopeTest;
import org.protobee.guice.multiscopes.util.CompleteDescoper;
import org.protobee.guice.multiscopes.util.Descoper;
import org.protobee.guice.multiscopes.util.MultiscopeExitor;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

public class BoundedScopeExampleTests extends AbstractMultiscopeTest {

  @Test
  public void testBoundedScope() {
    ScopeInstance milkyWay =
        injector.getInstance(Key.get(ScopeInstance.class, MilkyWayGalaxy.class));
    assertNotNull(milkyWay);

    TypeLiteral<Set<ScopeInstance>> galaxySetType = new TypeLiteral<Set<ScopeInstance>>() {};
    Set<ScopeInstance> galaxies = injector.getInstance(Key.get(galaxySetType, Galaxy.class));
    assertEquals(1, galaxies.size());
    assertEquals(milkyWay, Iterables.getOnlyElement(galaxies));

    Key<GalaxyProperties> propertiesKey = Key.get(GalaxyProperties.class, Galaxy.class);

    Multiscope galaxyScope = injector.getInstance(Key.get(Multiscope.class, Galaxy.class));
    try {
      milkyWay.enterScope();
      assertTrue(galaxyScope.isInScope());
      assertTrue(milkyWay.isInScope());
      assertSame(milkyWay, injector.getInstance(Key.get(ScopeInstance.class, Galaxy.class)));

      GalaxyProperties properties = injector.getInstance(propertiesKey);
      assertNotNull(properties);
      assertEquals(200000000000l, properties.getTotalSolarMass());
      assertEquals(40000l, properties.getRadiusLightYears());

      assertSame(properties, injector.getInstance(propertiesKey));

      assertSame(injector.getInstance(GalaxyCenter.class), injector.getInstance(GalaxyCenter.class));
    } finally {
      milkyWay.exitScope();
    }

  }

  @Test
  public void testPluginModule() {
    injector = Guice.createInjector(new ExamplesGuiceModule(), new ExamplePluginModule());

    ScopeInstance milkyWay =
        injector.getInstance(Key.get(ScopeInstance.class, MilkyWayGalaxy.class));
    assertNotNull(milkyWay);
    ScopeInstance andromeda =
        injector.getInstance(Key.get(ScopeInstance.class, AndromedaGalaxy.class));
    assertNotNull(andromeda);

    TypeLiteral<Set<ScopeInstance>> galaxySetType = new TypeLiteral<Set<ScopeInstance>>() {};
    Set<ScopeInstance> galaxies = injector.getInstance(Key.get(galaxySetType, Galaxy.class));
    assertEquals(2, galaxies.size());
    assertEquals(Sets.newHashSet(milkyWay, andromeda), galaxies);

    Key<GalaxyProperties> propertiesKey = Key.get(GalaxyProperties.class, Galaxy.class);

    Multiscope galaxyScope = injector.getInstance(Key.get(Multiscope.class, Galaxy.class));
    try {
      andromeda.enterScope();
      assertTrue(galaxyScope.isInScope());
      GalaxyProperties properties = injector.getInstance(propertiesKey);
      assertNotNull(properties);
      assertEquals(710000000000l, properties.getTotalSolarMass());
      assertEquals(33000l, properties.getRadiusLightYears());
      assertSame(injector.getInstance(GalaxyCenter.class), injector.getInstance(GalaxyCenter.class));
    } finally {
      andromeda.exitScope();
    }

    assertFalse(andromeda.isInScope());
  }

  @Test
  public void testUtilityClasses() {
    injector = Guice.createInjector(new ExamplesGuiceModule(), new ExamplePluginModule());

    ScopeInstance andromeda =
        injector.getInstance(Key.get(ScopeInstance.class, AndromedaGalaxy.class));
    ScopeInstance battlestar =
        injector.getInstance(Key.get(ScopeInstance.class, NewBattlestar.class));

    Multiscope galaxyScope = injector.getInstance(Key.get(Multiscope.class, Galaxy.class));
    CompleteDescoper completeDescoper = injector.getInstance(CompleteDescoper.class);
    Descoper galaxyDescoper = injector.getInstance(Key.get(Descoper.class, Galaxy.class));
    MultiscopeExitor exitor = injector.getInstance(Key.get(MultiscopeExitor.class));

    try {
      andromeda.enterScope();
      galaxyDescoper.descope();
      assertFalse(andromeda.isInScope());
      assertFalse(galaxyScope.isInScope());

      galaxyDescoper.rescope();
      assertTrue(andromeda.isInScope());
      assertTrue(galaxyScope.isInScope());

      battlestar.enterScope();
      completeDescoper.descope();
      assertFalse(andromeda.isInScope());
      assertFalse(battlestar.isInScope());

      completeDescoper.rescope();
      assertTrue(andromeda.isInScope());
      assertTrue(battlestar.isInScope());

      exitor.exitAllScopes();
      assertFalse(andromeda.isInScope());
      assertFalse(battlestar.isInScope());
      assertFalse(galaxyScope.isInScope());
    } finally {
      battlestar.exitScope();
      andromeda.exitScope();
    }

    assertFalse(andromeda.isInScope());
    assertFalse(battlestar.isInScope());
  }
}
