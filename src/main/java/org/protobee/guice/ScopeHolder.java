package org.protobee.guice;

import com.google.inject.Key;

/**
 * Holds a scope which can be entered and exited.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public interface ScopeHolder {

  /**
   * Enters the scope of this object for the calling thread. All injections of objects that are
   * under the scope this object describes will be held by this object.
   * 
   * A call to {@link #enterScope()} should always be in a try-finally block, where
   * {@link #exitScope()} is called in the finally block.
   * 
   * @throws IllegalStateException if we are already in the scope described by this object
   */
  void enterScope() throws IllegalStateException;

  /**
   * @return if the calling thread is currently in this object's scope
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
   */
  void putInScope(Key<?> key, Object object) throws IllegalArgumentException;

  /**
   * @return the unique id of the holder for it's respective scope
   */
  int getHolderId();
}
