package org.protobee.guice.example.scopes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.protobee.guice.ScopeHolder;

import com.google.inject.BindingAnnotation;

/**
 * Specifies a new {@link ScopeHolder} for the {@link BattlestarScope} (basically, a new
 * {@link BattlestarScope} is created). Also specifies a new scope map for the
 * {@link BattlestarScope}.
 * 
 * @author Daniel Murphy (daniel@dmurph.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@BindingAnnotation
public @interface NewBattlestarScopeHolder {}
