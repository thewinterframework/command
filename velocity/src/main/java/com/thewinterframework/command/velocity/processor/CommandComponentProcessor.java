package com.thewinterframework.command.velocity.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.command.processor.CommandWireProcessor;
import com.thewinterframework.processor.extension.WinterProcessorExtension;

/** Generates the velocity command module and component list. */
@AutoService(WinterProcessorExtension.class)
public final class CommandComponentProcessor extends CommandWireProcessor {
    @Override
    protected String platformModule() {
        return "com.thewinterframework.command.velocity.VelocityCommandModule";
    }
}
