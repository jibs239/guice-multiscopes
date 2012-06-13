package org.protobee.guice.multiscopes.test.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.After;
import org.junit.Test;
import org.protobee.guice.multicopes.Descoper;
import org.protobee.guice.multicopes.MultiscopeBinder;
import org.protobee.guice.multicopes.MultiscopeExitor;
import org.protobee.guice.multicopes.ScopeInstance;
import org.protobee.guice.multicopes.CompleteDescoper;

import com.google.inject.AbstractModule;
import com.google.inject.BindingAnnotation;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.ScopeAnnotation;

public class DescoperTests {

  // scope binding annotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @BindingAnnotation
  public static @interface Table {}

  // scope annotation
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ScopeAnnotation
  public static @interface TableScope {}

  // new scope instance annotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @BindingAnnotation
  public static @interface NewTableInstance {}

  // scope binding annotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @BindingAnnotation
  public static @interface Chair {}

  // scope annotation
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @ScopeAnnotation
  public static @interface ChairScope {}

  // new scope instance annotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
  @BindingAnnotation
  public static @interface NewChairInstance {}

  @TableScope
  public static class Legs {}

  static class UnboundedModule extends AbstractModule {
    @Override
    protected void configure() {
      MultiscopeBinder.newBinder(binder(), TableScope.class, Table.class, NewTableInstance.class);
      MultiscopeBinder.newBinder(binder(), ChairScope.class, Chair.class, NewChairInstance.class);
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
  public void simpleDescoperTest() {
    inj = Guice.createInjector(new UnboundedModule());

    ScopeInstance table = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));
    Descoper descoper = inj.getInstance(Key.get(Descoper.class, Table.class));

    try {
      table.enterScope();
      assertTrue(table.isInScope());

      descoper.descope();
      assertFalse(table.isInScope());

      descoper.rescope();
      assertTrue(table.isInScope());

      assertEquals(table, inj.getInstance(Key.get(ScopeInstance.class, Table.class)));
    } finally {
      table.exitScope();
    }
  }

  @Test
  public void testTotalDescoper() {
    inj = Guice.createInjector(new UnboundedModule());

    ScopeInstance table = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));
    ScopeInstance chair = inj.getInstance(Key.get(ScopeInstance.class, NewChairInstance.class));

    CompleteDescoper descoper = inj.getInstance(CompleteDescoper.class);

    try {
      table.enterScope();
      chair.enterScope();
      assertTrue(table.isInScope());
      assertTrue(chair.isInScope());

      descoper.descope();
      assertFalse(table.isInScope());
      assertFalse(chair.isInScope());

      descoper.rescope();
      assertTrue(table.isInScope());
      assertTrue(chair.isInScope());

      assertEquals(table, inj.getInstance(Key.get(ScopeInstance.class, Table.class)));
      assertEquals(chair, inj.getInstance(Key.get(ScopeInstance.class, Chair.class)));
    } finally {
      table.exitScope();
      chair.exitScope();
    }
  }
}
