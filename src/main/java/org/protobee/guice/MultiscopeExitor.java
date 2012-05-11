package org.protobee.guice;

import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MultiscopeExitor {

  private final Set<Multiscope> multiscopes;

  @Inject
  public MultiscopeExitor(Set<Multiscope> multiscopes) {
    this.multiscopes = multiscopes;
  }

  public void exitAllScopes() {
    for (Multiscope scope : multiscopes) {
      scope.exitScope();
    }
  }
}
