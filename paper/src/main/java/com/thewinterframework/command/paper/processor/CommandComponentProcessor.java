package com.thewinterframework.command.paper.processor;

import com.google.auto.service.AutoService;
import com.thewinterframework.command.processor.CommandWireProcessor;
import com.thewinterframework.processor.extension.WinterProcessorExtension;

/** Generates the paper command module and component list. */
@AutoService(WinterProcessorExtension.class)
public final class CommandComponentProcessor extends CommandWireProcessor {
    @Override
    protected String platformModule() {
        return "com.thewinterframework.command.paper.PaperCommandModule";
    }
}
