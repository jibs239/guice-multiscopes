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

import com.google.inject.Key;

import javax.annotation.Nullable;

/**
 * An instance of a scope
 *
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public interface ScopeInstance {

	/**
	 * Enters the scope of this object for the calling thread. All injections of objects that are
	 * under the scope this object describes will be held by this object.
	 * <p>
	 * A call to {@link #enterScope()} should always be in a try-finally block, where
	 * {@link #exitScope()} is called in the finally block.
	 *
	 * @throws IllegalStateException if we are already in the scope described by this object
	 */
	void enterScope() throws IllegalStateException;

	/**
	 * true if the calling thread is currently in this object's scope instance (if we're in the same
	 * scope but in a different scope instance and this will be false)
	 */
	boolean isInScope();

	/**
	 * Exits the scope this object holds. Will not throw exceptions (safe for finally clauses)
	 */
	void exitScope();

	/**
	 * Puts the object in this scope. This should only be done when completely unavoidable, ie,
	 * working with other non-guice code.
	 *
	 * @param key
	 * @param object
	 * @throws IllegalArgumentException if the key does not match the object
	 * @throws NullPointerException     if the key is null
	 */
	void putInScope(Key<?> key, @Nullable Object object) throws IllegalArgumentException;

	/**
	 * @return the unique id of this instance in it's respective scope
	 */
	int getInstanceId();
}
