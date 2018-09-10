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
package org.protobee.guice.multiscopes;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.Scope;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * A scope that supports multiple instances of the scope itself. After binding your multiscope with
 * {@link Multiscopes#newBinder(com.google.inject.Binder, Class, Class, Class)} or
 * {@link Multiscopes#newBoundedBinder(com.google.inject.Binder, Class, Class)} , you can create new
 * instances of your scope by injecting a {@link ScopeInstance} annotated with the annotation
 * 'newInstanceAnnotation' in the case of the regular binder, or use the bound annotations for the
 * bounded binder. This instance is then used to enter and exit the scope.<br/>
 * <br/>
 * So for unbounded multiscopes, there are two annotations at work here, the 'scope instance'
 * binding annotation and the 'new scope instance' binding annotation. The 'new scope instance'
 * annotation is to specify the creation of a new {@link ScopeInstance} for the corresponding scope.
 * When the new {@link ScopeInstance} is created, it is is also bound in that scope with the 'scope
 * instance' annotation. This allows objects to inject the scope instance of the current scope. This
 * is used when making {@link ScopeInstance} holder classes for hierarchical scopes, which is
 * outlined in the wiki <a href="http://code.google.com/p/guice-multiscopes/wiki/BestPractices">Best
 * Practices</a> page, and shown in the examples.<br/>
 * <br/>
 * bounded multiscopes are similar, except instead of having a 'newScopeInstance' annoatation, they
 * have a bound set of instances that is decided during injection (similar to a multiset, so other
 * guice modules can add instances)
 *
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public abstract class Multiscope implements Scope {

	private final Class<? extends Annotation> bindingAnnotation;

	public Multiscope(Class<? extends Annotation> bindingAnnotation) {
		this.bindingAnnotation = Preconditions.checkNotNull(bindingAnnotation, "bindingAnnotation");
	}

	/**
	 * If we're in this multiscope on the current thread.
	 */
	public abstract boolean isInScope();

	/**
	 * Exits this multiscope on the current thread.
	 */
	public abstract void exitScope();

	/**
	 * The binding annotation given on configuration, used to specify this multiscope.
	 */
	public Class<? extends Annotation> getBindingAnnotation() {
		return bindingAnnotation;
	}

	protected abstract ScopeInstance createScopeInstance(final Map<Key<?>, Object> scopeMap);
}
