package org.protobee.guice.multicopes;

import java.lang.annotation.Annotation;
import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.inject.Key;
import com.google.inject.Scope;

/**
 * A scope that supports multiple instances of the scope itself. After binding your multiscope with
 * {@link Multiscopes#bindMultiscope(com.google.inject.Binder, SimpleMultiscope, Class, Class)} or
 * {@link Multiscopes#bindMultiscope(com.google.inject.Binder, SimpleMultiscope, Class, Class, Class)}
 * , you can create new instances of your scope by injecting a {@link ScopeInstance} annotated with
 * the annotation 'newInstanceAnnotation' given when you bind this scope. This instance is then used
 * to enter and exit the scope.<br/>
 * <br/>
 * So there are two annotations at work here, the 'scope instance' binding annotation and the 'new
 * scope instance' binding annotation. The 'new scope instance' annotation is to specify the
 * creation of a new {@link ScopeInstance} for the corresponding scope. When the new
 * {@link ScopeInstance} is created, it is is also bound in that scope with the 'scope instance'
 * annotation. This allows objects to inject the scope instance of the current scope. This is used
 * when making {@link ScopeInstance} holder classes, which is outlined in the wiki <a
 * href="http://code.google.com/p/guice-multiscopes/wiki/BestPractices">Best Practices</a> page, and
 * shown in the examples.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
public abstract class Multiscope implements Scope {

  private final Class<? extends Annotation> bindingAnnotation;

  public Multiscope(Class<? extends Annotation> bindingAnnotation) {
    this.bindingAnnotation = Preconditions.checkNotNull(bindingAnnotation, "bindingAnnotation");
  }

  public abstract boolean isInScope();

  public abstract void exitScope();

  public Class<? extends Annotation> getBindingAnnotation() {
    return bindingAnnotation;
  }

  protected abstract ScopeInstance createScopeInstance(final Map<Key<?>, Object> scopeMap);
}
