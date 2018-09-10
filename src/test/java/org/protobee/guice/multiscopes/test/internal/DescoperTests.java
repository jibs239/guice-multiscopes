package org.protobee.guice.multiscopes.test.internal;

import com.google.inject.*;
import org.junit.After;
import org.junit.Test;
import org.protobee.guice.multiscopes.Multiscopes;
import org.protobee.guice.multiscopes.ScopeInstance;
import org.protobee.guice.multiscopes.util.CompleteDescoper;
import org.protobee.guice.multiscopes.util.Descoper;
import org.protobee.guice.multiscopes.util.MultiscopeExitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.*;

public class DescoperTests {

	Injector inj;

	@After public void clearScopes() {
		if (inj == null) {
			return;
		}
		MultiscopeExitor exitor = inj.getInstance(MultiscopeExitor.class);
		exitor.exitAllScopes();
	}

	@Test public void simpleDescoperTest() {
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

	@Test public void testTotalDescoper() {
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

	// scope binding annotation
	@Retention(RetentionPolicy.RUNTIME) @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) @BindingAnnotation public static @interface Table {
	}

	// scope annotation
	@Target({ ElementType.TYPE, ElementType.METHOD }) @Retention(RetentionPolicy.RUNTIME) @ScopeAnnotation public static @interface TableScope {
	}

	// new scope instance annotation
	@Retention(RetentionPolicy.RUNTIME) @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) @BindingAnnotation public static @interface NewTableInstance {
	}

	// scope binding annotation
	@Retention(RetentionPolicy.RUNTIME) @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) @BindingAnnotation public static @interface Chair {
	}

	// scope annotation
	@Target({ ElementType.TYPE, ElementType.METHOD }) @Retention(RetentionPolicy.RUNTIME) @ScopeAnnotation public static @interface ChairScope {
	}

	// new scope instance annotation
	@Retention(RetentionPolicy.RUNTIME) @Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) @BindingAnnotation public static @interface NewChairInstance {
	}

	@TableScope public static class Legs {
	}

	static class UnboundedModule extends AbstractModule {
		@Override protected void configure() {
			Multiscopes.newBinder(binder(), TableScope.class, Table.class, NewTableInstance.class);
			Multiscopes.newBinder(binder(), ChairScope.class, Chair.class, NewChairInstance.class);
		}
	}
}
