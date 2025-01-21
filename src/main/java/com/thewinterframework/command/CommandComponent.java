package com.thewinterframework.command;

import com.google.inject.ScopeAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a command component.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ScopeAnnotation
public @interface CommandComponent {
}
