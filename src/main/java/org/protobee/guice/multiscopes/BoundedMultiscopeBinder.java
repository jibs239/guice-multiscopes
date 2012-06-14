package org.protobee.guice.multiscopes;

import java.lang.annotation.Annotation;

import com.google.inject.Key;

public interface BoundedMultiscopeBinder extends MultiscopeBinder {

  public static enum PrescopeType {
    EAGER, LAZY
  }

  public static interface InstancePrescoper {
    <T> InstancePrescoper addInstanceObject(Key<T> key);

    <T> InstancePrescoper addInstanceObject(Key<T> key, PrescopeType type);
  }

  BoundedMultiscopeBinder addInstance(Class<? extends Annotation> instanceAnnotation);

  InstancePrescoper prescopeInstance(Class<? extends Annotation> instanceAnnotation);
}
