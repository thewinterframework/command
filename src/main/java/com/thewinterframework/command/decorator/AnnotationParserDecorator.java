package com.thewinterframework.command.decorator;

import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.paper.util.sender.Source;
import org.jetbrains.annotations.NotNull;

public interface AnnotationParserDecorator {

  void decorate(final @NotNull AnnotationParser<Source> annotationParser);

}
