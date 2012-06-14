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
package org.protobee.guice.multiscopes.util;

/**
 * Descoper for exiting and re-entering one or more multiscopes. If injected with the scope instance
 * annotation of a class, this descoper applies to that multiscope. If injected with no binding
 * annotation, this descoper applies to all multiscopes.
 * <br/><br/>
 * If you need to just exit all scopes, try the {@link MultiscopeExitor}.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public interface Descoper {

  /**
   * Exits any scope or scopes this descoper corresponds to
   * 
   * @throws IllegalStateException if {@link #descope()} was already called without a matching
   *         {@link #rescope()}
   */
  void descope() throws IllegalStateException;

  /**
   * Re-enters scopes that were exited
   * 
   * @throws IllegalStateException if the corresponding scope or scopes are already entered
   */
  void rescope() throws IllegalStateException;
}
