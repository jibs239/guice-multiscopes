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
        if (instance != null) {
          Preconditions.checkState(!scope.isInScope(), "Cannot rescope when we're already in "
              + scope);
          instance.enterScope();
          instance = null;
        }
      }
    };
  }
}
