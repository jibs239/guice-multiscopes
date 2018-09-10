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

import com.google.inject.Provider;
import com.google.inject.ProvisionException;

import javax.annotation.Nullable;

/**
 * A provider that always throws an exception when {@link #get()} is called. Meant to be used for
 * prescoped bindings (and is used when either
 * {@link Multiscopes#bindAsPrescoped(com.google.inject.Binder, Class, Class, Class)} or
 * {@link Multiscopes#bindAsPrescoped(com.google.inject.Binder, Class, Class, com.google.inject.TypeLiteral)}
 * are called).
 *
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public class PrescopedProvider<T> implements Provider<T> {

	private final String exceptionMessage;
	private final String description;

	public PrescopedProvider() {
		this(null, null);
	}

	/**
	 * @param exceptionMessage the message for the exception when {@link #get()} is called
	 */
	public PrescopedProvider(@Nullable String exceptionMessage) {
		this(exceptionMessage, null);
	}

	/**
	 * @param exceptionMessage the message for the exception when {@link #get()} is called
	 * @param description      what's returned when {@link #toString()} is called
	 */
	public PrescopedProvider(@Nullable String exceptionMessage, @Nullable String description) {
		this.description = description;
		this.exceptionMessage = exceptionMessage;
	}

	@Override public T get() {
		throw new ProvisionException(exceptionMessage != null ? exceptionMessage : "Prescoped object, this provider should never be called.");
	}

	@Override public String toString() {
		if (description == null) {
			return "PrescopedProvider";
		}
		return description;
	}
}
