package org.protobee.guice.example.scopes;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.ScopeAnnotation;

/**
 * Scopes an object in the fighter scope (ie, one object instance per fighter scope)
 * 
 * @author Daniel
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface FighterScope {}
