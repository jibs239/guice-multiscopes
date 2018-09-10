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

import com.google.inject.BindingAnnotation;
import com.google.inject.Key;

import java.lang.annotation.Annotation;

/**
 * Created by {@link Multiscopes#newBoundedBinder(com.google.inject.Binder, Class, Class)}. Bounded
 * multiscopes have a discrete numer of instances that are determined at injection-time (similar to
 * a multiset). Each instance is tied to a binding annotation, which can be used to specify that
 * specify that instance. Because of this, you can "prescope" objects as well using
 * {@link #prescopeInstance(Class)}. This binds an arbitrary binding to the given scope.
 *
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public interface BoundedMultiscopeBinder extends MultiscopeBinder {

	/**
	 * Adds an instance of this multiscope with thiven instance annotation (must be a
	 * {@link BindingAnnotation}).
	 */
	BoundedMultiscopeBinder addInstance(Class<? extends Annotation> instanceAnnotation);

	/**
	 * Returns an {@link InstancePrescoper} to prescope the given instance. This binds arbitrary keys
	 * to the scope instance specified, and will appear when in the specified scope instance and when
	 * using this scope's binding annotation with the key's type (not the instance binding annotation)
	 */
	InstancePrescoper prescopeInstance(Class<? extends Annotation> instanceAnnotation);

	/**
	 * Specifies how the prescoped object will be populated in the scope.
	 */
	public static enum PrescopeType {
		/**
		 * The object is fetched + populated in the scope on scope creation.
		 */
		EAGER,
		/**
		 * The object is fetched only when requested (on injection).
		 */
		LAZY
	}

	/**
	 * Facilitates adding prescoped objects to a multiscope instance.
	 */
	public static interface InstancePrescoper {
		/**
		 * Prescopes the given key in this multiscope instance, with default type of
		 * {@link PrescopeType#LAZY}.
		 */
		<T> InstancePrescoper addInstanceObject(Key<T> key);

		/**
		 * Prescopes the given key with the given {@link PrescopeType}.
		 */
		<T> InstancePrescoper addInstanceObject(Key<T> key, PrescopeType type);
	}
}
