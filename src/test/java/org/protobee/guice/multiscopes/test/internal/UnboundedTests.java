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
package org.protobee.guice.multiscopes.test.internal;

import com.google.common.collect.Iterables;
import com.google.inject.*;
import org.junit.After;
import org.junit.Test;
import org.protobee.guice.multiscopes.Multiscope;
import org.protobee.guice.multiscopes.Multiscopes;
import org.protobee.guice.multiscopes.PrescopedProvider;
import org.protobee.guice.multiscopes.ScopeInstance;
import org.protobee.guice.multiscopes.util.MultiscopeExitor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import static org.junit.Assert.*;

public class UnboundedTests {

	Injector inj;

	@After public void clearScopes() {
		if (inj == null) {
			return;
		}
		MultiscopeExitor exitor = inj.getInstance(MultiscopeExitor.class);
		exitor.exitAllScopes();
	}

	@Test public void testScopePresentAndExitor() {
		inj = Guice.createInjector(new UnboundedModule());

		TypeLiteral<Set<Multiscope>> multiscopesType = new TypeLiteral<Set<Multiscope>>() {
		};
		Set<Multiscope> multiscopes = inj.getInstance(Key.get(multiscopesType));

		assertEquals(1, multiscopes.size());
		Multiscope scope = inj.getInstance(Key.get(Multiscope.class, Table.class));
		assertNotNull(scope);
		assertSame(scope, inj.getInstance(Key.get(Multiscope.class, Table.class)));
		assertEquals(scope, Iterables.getOnlyElement(multiscopes));

		MultiscopeExitor exitor = inj.getInstance(MultiscopeExitor.class);

		ScopeInstance table = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));
		table.enterScope();
		exitor.exitAllScopes();
		assertFalse(table.isInScope());
	}

	@Test public void testScopeHolder() {
		inj = Guice.createInjector(new UnboundedModule());

		ScopeInstance table = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));
		assertFalse(table.isInScope());

		try {
			table.enterScope();
			assertTrue(table.isInScope());
			assertEquals(table, inj.getInstance(Key.get(ScopeInstance.class, Table.class)));
		} finally {
			table.exitScope();
		}

		assertFalse(table.isInScope());

		ScopeInstance instance2 = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));

		assertNotSame(table, instance2);
	}

	@Test public void testMultiscope() {
		inj = Guice.createInjector(new UnboundedModule());

		ScopeInstance table = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));
		Multiscope scope = inj.getInstance(Key.get(Multiscope.class, Table.class));
		assertFalse(scope.isInScope());
		assertEquals(Table.class, scope.getBindingAnnotation());

		try {
			table.enterScope();
			assertTrue(scope.isInScope());
		} finally {
			scope.exitScope();
			assertFalse(scope.isInScope());
			assertFalse(table.isInScope());
		}

		assertFalse(table.isInScope());

		ScopeInstance instance2 = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));

		assertNotSame(table, instance2);
	}

	@Test public void testBasicScoping() {
		inj = Guice.createInjector(new UnboundedModule());

		ScopeInstance table = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));

		try {
			table.enterScope();
			Legs deck = inj.getInstance(Legs.class);
			assertNotNull(deck);
			assertTrue(deck == inj.getInstance(Legs.class));
			table.exitScope();
			table.enterScope();
			assertTrue(deck == inj.getInstance(Legs.class));
		} finally {
			table.exitScope();
		}
	}

	@Test public void testTwoScopeInstances() {
		inj = Guice.createInjector(new UnboundedModule());

		ScopeInstance table1 = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));
		ScopeInstance table2 = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));

		Legs deck;
		try {
			table1.enterScope();
			assertFalse(table2.isInScope());
			deck = inj.getInstance(Legs.class);
			assertNotNull(deck);
		} finally {
			table1.exitScope();
		}

		try {
			table2.enterScope();
			assertFalse(table1.isInScope());
			assertNotSame(deck, inj.getInstance(Legs.class));
		} finally {
			table2.exitScope();
		}
	}

	@Test public void testExceptionOnPreviouslyEnteredScope() {
		inj = Guice.createInjector(new UnboundedModule());

		ScopeInstance table1 = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));

		boolean caught = false;
		try {
			table1.enterScope();
			table1.enterScope();
		} catch (IllegalStateException e) {
			caught = true;
		} finally {
			table1.exitScope();
		}
		assertTrue(caught);

		ScopeInstance table2 = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));

		caught = false;
		try {
			table1.enterScope();
			table2.enterScope();
		} catch (IllegalStateException e) {
			caught = true;
		} finally {
			table1.exitScope();
		}
		assertTrue(caught);
	}

	@Test public void testExceptionWhenOutOfScope() {
		inj = Guice.createInjector(new UnboundedModule());

		boolean caught = false;
		try {
			inj.getInstance(Legs.class);
		} catch (ProvisionException e) {
			caught = true;
		}
		assertTrue(caught);
	}

	@Test public void testPrescope() {
		inj = Guice.createInjector(new UnboundedModule(), new AbstractModule() {

			@Override protected void configure() {
				bind(Tablecloth.class).toProvider(new PrescopedProvider<Tablecloth>("Captain should have been prescoped")).in(TableScope.class);
			}
		});

		ScopeInstance table = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));

		Tablecloth captain = new Tablecloth();
		table.putInScope(Key.get(Tablecloth.class), captain);

		try {
			table.enterScope();
			assertEquals(captain, inj.getInstance(Tablecloth.class));
		} finally {
			table.exitScope();
		}

		boolean caught = false;
		try {
			inj.getInstance(Tablecloth.class);
		} catch (ProvisionException e) {
			caught = true;
		}
		assertTrue(caught);
	}

	@Test public void testExceptionWhenNotPrescoped() {
		inj = Guice.createInjector(new UnboundedModule(), new AbstractModule() {

			@Override protected void configure() {
				bind(Tablecloth.class).toProvider(new PrescopedProvider<Tablecloth>("Captain should have been prescoped")).in(TableScope.class);
			}
		});

		boolean caught = false;
		ScopeInstance table = inj.getInstance(Key.get(ScopeInstance.class, NewTableInstance.class));
		try {
			table.enterScope();
			inj.getInstance(Tablecloth.class);
		} catch (ProvisionException e) {
			caught = true;
		} finally {
			table.exitScope();
		}
		assertTrue(caught);
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

	@TableScope public static class Legs {
	}

	static class UnboundedModule extends AbstractModule {
		@Override protected void configure() {
			Multiscopes.newBinder(binder(), TableScope.class, Table.class, NewTableInstance.class);
		}
	}

	@TableScope public static class Tablecloth {
	}
}
