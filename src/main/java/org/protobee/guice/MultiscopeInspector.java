package org.protobee.guice;

import java.lang.annotation.Annotation;

public interface MultiscopeInspector {
  
  void ensureExit();

  Class<? extends Annotation> getScopeBindingAnnotation();
  
  Multiscope getScope();
}
