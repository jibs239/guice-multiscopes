package org.protobee.guice.test.internal;

import static org.junit.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.protobee.guice.BoundedMultiscopeBinder;
import org.protobee.guice.Multiscope;
import org.protobee.guice.MultiscopeExitor;
import org.protobee.guice.ScopeInstance;
import org.protobee.guice.test.internal.UnboundedMultiscopeTests.NewTableInstance;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ScopeAnnotation;
import com.google.inject.TypeLiteral;

public class BoundedTests {

  // scope binding annotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @BindingAnnotation
  public static @interface Planet {}

  // scope annotation
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ScopeAnnotation
  public static @interface PlanetScope {}

  // planets:
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @BindingAnnotation
  public static @interface Mercury {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @BindingAnnotation
  public static @interface Venus {}

  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @BindingAnnotation
  public static @interface Earth {}
  // enough

  static class PlanetsModule extends AbstractModule {
    @Override
    protected void configure() {
      BoundedMultiscopeBinder binder =
          BoundedMultiscopeBinder.newBoundedBinder(binder(), PlanetScope.class, Planet.class);
      binder.addInstance(Mercury.class).addInstance(Venus.class);
    }
  }

  static class EarthModule extends AbstractModule {
    @Override
    protected void configure() {
      BoundedMultiscopeBinder.newBoundedBinder(binder(), PlanetScope.class, Planet.class)
          .addInstance(Earth.class);
    }
  }

  Injector inj;

  @After
  public void clearScopes() {
    if (inj == null) {
      return;
    }
    MultiscopeExitor exitor = inj.getInstance(MultiscopeExitor.class);
    exitor.exitAllScopes();
  }

  @Test
  public void testScopesPresent() {
    inj = Guice.createInjector(new PlanetsModule());

    Multiscope mercury = inj.getInstance(Key.get(Multiscope.class, Planet.class));
    assertNotNull(mercury);
    assertSame(mercury, inj.getInstance(Key.get(Multiscope.class, Planet.class)));

    TypeLiteral<Set<Multiscope>> multiscopesType = new TypeLiteral<Set<Multiscope>>() {};
    Set<Multiscope> multiscopes = inj.getInstance(Key.get(multiscopesType));

    assertEquals(1, multiscopes.size());
    assertEquals(Sets.newHashSet(mercury), multiscopes);
  }

  @Test
  public void testInstancesPresent() {
    inj = Guice.createInjector(new PlanetsModule());

    ScopeInstance mercury = inj.getInstance(Key.get(ScopeInstance.class, Mercury.class));
    assertNotNull(mercury);
    assertSame(mercury, inj.getInstance(Key.get(ScopeInstance.class, Mercury.class)));

    ScopeInstance venus = inj.getInstance(Key.get(ScopeInstance.class, Venus.class));
    assertNotNull(venus);
    assertSame(venus, inj.getInstance(Key.get(ScopeInstance.class, Venus.class)));

    assertNotSame(mercury, venus);

    TypeLiteral<Set<ScopeInstance>> multiscopesType = new TypeLiteral<Set<ScopeInstance>>() {};
    Set<ScopeInstance> multiscopes = inj.getInstance(Key.get(multiscopesType, Planet.class));

    assertEquals(2, multiscopes.size());
    assertEquals(Sets.newHashSet(mercury, venus), multiscopes);
  }
}
