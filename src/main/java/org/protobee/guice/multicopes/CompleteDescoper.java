package org.protobee.guice.multicopes;

import java.util.Set;

import com.google.inject.Inject;

/**
 * This {@link Descoper} applies to all multiscopes.
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
  public void rescope() {
    for (Descoper descoper : descopers) {
      descoper.rescope();
    }
  }
}
