package com.thewinterframework.command.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.command.CommandModule;
import com.thewinterframework.processor.clazz.ClassWireProcessor;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.handler.WinterAnnotationProcessor;

import java.lang.annotation.Annotation;

/**
 * Annotation processor for {@link CommandComponent}.
 */
@AutoService(WinterAnnotationProcessor.class)
public class CommandComponentProcessor extends ClassWireProcessor {
    @Override
    public void onRoundStart(final ProcessorContext ctx) {
        ctx.wireModule(CommandModule.class);
    }

    @Override
    protected Class<? extends Annotation> wiredAnnotation() {
        return CommandComponent.class;
    }
}