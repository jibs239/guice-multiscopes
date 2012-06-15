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
import org.protobee.guice.multiscopes.test.AbstractMultiscopeTest;

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
}
