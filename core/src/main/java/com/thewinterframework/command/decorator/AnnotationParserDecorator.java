package com.thewinterframework.command.decorator;

import org.incendo.cloud.annotations.AnnotationParser;

/**
 * Customizes annotation parsing before commands are registered.
 * @param <C> the command sender type
 */
@FunctionalInterface
public interface AnnotationParserDecorator<C> {
    /**
     * Decorates the annotation parser.
     * @param annotationParser the annotation parser
     */
    void decorate(AnnotationParser<C> annotationParser);
}
