package org.protobee.guice.multiscopes.test.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Test;
import org.protobee.guice.multiscopes.BoundedMultiscopeBinder;
import org.protobee.guice.multiscopes.BoundedMultiscopeBinder.PrescopeType;
import org.protobee.guice.multiscopes.Multiscope;
import org.protobee.guice.multiscopes.Multiscopes;
import org.protobee.guice.multiscopes.ScopeInstance;
import org.protobee.guice.multiscopes.util.MultiscopeExitor;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
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
          Multiscopes.newBoundedBinder(binder(), PlanetScope.class, Planet.class);
      binder.addInstance(Mercury.class).addInstance(Venus.class);
    }
  }

  static class EarthModule extends AbstractModule {
    @Override
    protected void configure() {
      Multiscopes.newBoundedBinder(binder(), PlanetScope.class, Planet.class).addInstance(
          Earth.class);
    }
  }

  static class PrecopedConstantsModule extends AbstractModule {
    @Override
    protected void configure() {
      BoundedMultiscopeBinder boundedScopes =
          Multiscopes.newBoundedBinder(binder(), PlanetScope.class, Planet.class);

      bindConstant().annotatedWith(Venus.class).to("Venus");
      bindConstant().annotatedWith(Mercury.class).to("Mercury");

      boundedScopes.prescopeInstance(Venus.class).addInstanceObject(
          Key.get(String.class, Venus.class));
      boundedScopes.prescopeInstance(Mercury.class).addInstanceObject(
          Key.get(String.class, Mercury.class));

      Multiscopes.bindAsPrescoped(binder(), PlanetScope.class, Planet.class, String.class);
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

  @Test
  public void testPrescoped() {
    inj = Guice.createInjector(new PlanetsModule(), new PrecopedConstantsModule());

    ScopeInstance mercury = inj.getInstance(Key.get(ScopeInstance.class, Mercury.class));
    ScopeInstance venus = inj.getInstance(Key.get(ScopeInstance.class, Venus.class));

    mercury.enterScope();
    assertEquals("Mercury", inj.getInstance(Key.get(String.class, Planet.class)));
    mercury.exitScope();

    venus.enterScope();
    assertEquals("Venus", inj.getInstance(Key.get(String.class, Planet.class)));
    venus.exitScope();
  }

  @Test
  public void testLazyPrescope() {
    final AtomicBoolean providedLazy = new AtomicBoolean(false);

    inj = Guice.createInjector(new PlanetsModule(), new AbstractModule() {
      @Override
      protected void configure() {
        BoundedMultiscopeBinder boundedScopes =
            Multiscopes.newBoundedBinder(binder(), PlanetScope.class, Planet.class);

        bind(Object.class).toProvider(new Provider<Object>() {
          @Override
          public Object get() {
            providedLazy.set(true);
            return new Object();
          }
        });
        boundedScopes.prescopeInstance(Mercury.class).addInstanceObject(Key.get(Object.class),
            PrescopeType.LAZY);

        Multiscopes.bindAsPrescoped(binder(), PlanetScope.class, Planet.class, Object.class);
      }
    });

    ScopeInstance mercury = inj.getInstance(Key.get(ScopeInstance.class, Mercury.class));

    assertFalse(providedLazy.get());
    mercury.enterScope();
    inj.getInstance(Key.get(Object.class, Planet.class));
    assertTrue(providedLazy.get());
    mercury.exitScope();
  }
}
