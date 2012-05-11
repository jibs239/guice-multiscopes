package org.protobee.guice;

/**
 * Descoper for exiting and re-entering one or more multiscopes. If injected with the scope instance
 * annotation of a class, this descoper applies to that multiscope. If injected with no binding
 * annotation, this descoper applies to all multiscopes.
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
   */
  void rescope();
}
