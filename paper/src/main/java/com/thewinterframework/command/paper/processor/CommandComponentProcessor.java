package com.thewinterframework.command.paper.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.processor.clazz.ClassWireProcessor;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.handler.WinterAnnotationProcessor;

import java.lang.annotation.Annotation;

/** Generates the Paper command module and component list. */
@AutoService(WinterAnnotationProcessor.class)
public final class CommandComponentProcessor extends ClassWireProcessor {
    @Override
    public void onRoundStart(final ProcessorContext context) {
        context.wireModule("com.thewinterframework.command.paper.PaperCommandModule");
    }

    @Override
    protected Class<? extends Annotation> wiredAnnotation() {
        return CommandComponent.class;
    }
}
