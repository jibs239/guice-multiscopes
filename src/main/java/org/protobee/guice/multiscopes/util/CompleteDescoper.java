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

import java.util.Set;

import org.protobee.guice.multiscopes.Multiscope;


import com.google.inject.Inject;

/**
 * This {@link Descoper} applies to all {@link Multiscope}s.
 * 
 * @author Daniel
 */
public class CompleteDescoper implements Descoper {

  private final Descoper[] descopers;

  @Inject
  public CompleteDescoper(Set<Descoper> descopers) {
    this.descopers = new Descoper[descopers.size()];
    int i = 0;
    for (Descoper descoper : descopers) {
      this.descopers[i++] = descoper;
    }
  }

  @Override
  public void descope() throws IllegalStateException {
    for (Descoper descoper : descopers) {
      descoper.descope();
    }
  }

  @Override
  public void rescope() throws IllegalStateException {
    for (Descoper descoper : descopers) {
      descoper.rescope();
    }
  }
}
