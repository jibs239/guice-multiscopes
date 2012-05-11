package org.protobee.guice;

import java.lang.annotation.Annotation;

import com.google.inject.Scope;


public interface Multiscope extends Scope {

  Class<? extends Annotation> getBindingAnnotation();

 
  boolean isInScope();

  
  void exitScope();
}
