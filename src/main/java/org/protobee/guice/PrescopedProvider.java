package org.protobee.guice;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;

/**
 * A provider that always throws an exception when {@link #get()} is called. Meant to be used for
 * prescoped bindings.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public class PrescopedProvider<T> implements Provider<T> {

  private final String exceptionMessage;
  private final String description;

  /**
   * @param exceptionMessage the message for the exception when {@link #get()} is called
   */
  public PrescopedProvider(String exceptionMessage) {
    this(exceptionMessage, null);
  }

  /**
   * @param exceptionMessage the message for the exception when {@link #get()} is called
   * @param description what's returned when {@link #toString()} is called
   */
  public PrescopedProvider(String exceptionMessage, @Nullable String description) {
    Preconditions.checkNotNull(exceptionMessage, "typeClass is null");
    this.description = description;
    this.exceptionMessage = exceptionMessage;
  }

  @Override
  public T get() {
    throw new ProvisionException("Object wasn't prescoped.  " + exceptionMessage);
  }

  @Override
  public String toString() {
    if (description == null) {
      return super.toString();
    }
    return description;
  }
}
