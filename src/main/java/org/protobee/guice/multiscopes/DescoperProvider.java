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

import java.util.Set;

import org.protobee.guice.multiscopes.util.Descoper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.spi.Dependency;
import com.google.inject.spi.HasDependencies;
import com.google.inject.spi.Toolable;

class DescoperProvider implements Provider<Descoper>, HasDependencies {

  private final Multiscope scope;
  private Provider<ScopeInstance> instanceProvider;

  public DescoperProvider(Multiscope scope) {
    this.scope = scope;
  }

  @Inject
  @Toolable
  void init(Injector inj) {
    instanceProvider = inj.getProvider(Key.get(ScopeInstance.class, scope.getBindingAnnotation()));
  }

  @Override
  public Set<Dependency<?>> getDependencies() {
    return ImmutableSet.<Dependency<?>>of(Dependency.get(Key.get(ScopeInstance.class,
        scope.getBindingAnnotation())));
  }
  
  @Override
  public String toString() {
    return scope.getBindingAnnotation()+"-DescoperProvider";
  }

  @Override
  public Descoper get() {
    return new Descoper() {
      private ScopeInstance instance = null;

      @Override
      public void descope() throws IllegalStateException {
        Preconditions.checkState(instance == null,
            "Can't call descope() twice in a row, must call rescope() first.");
        if (scope.isInScope()) {
          instance = instanceProvider.get();
          instance.exitScope();
        }
      }

      @Override
      public void rescope() throws IllegalStateException {
        Preconditions.checkState(!scope.isInScope(), "Cannot rescope when we're already in "
            + scope);
        if (instance != null) {
          instance.enterScope();
          instance = null;
        }
      }
    };
  }
}
