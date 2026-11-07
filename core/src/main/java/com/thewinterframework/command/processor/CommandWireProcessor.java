package com.thewinterframework.command.processor;

import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.processor.WinterProcessor;
import com.thewinterframework.processor.context.ProcessorContext;
import com.thewinterframework.processor.extension.WinterProcessorExtension;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.TreeSet;

/** Generates command wires through Winter 3's processor extension SPI. */
public abstract class CommandWireProcessor implements WinterProcessorExtension {
    private final Set<String> classes = new TreeSet<>();

    protected abstract String platformModule();

    @Override
    public final Set<String> supportedAnnotationNames() {
        return Set.of(CommandComponent.class.getCanonicalName());
    }

    @Override
    public final void onRoundStarted(final ProcessorContext context) {
        context.wireModule(platformModule());
    }

    @Override
    public final void process(final TypeElement annotation, final Set<? extends Element> elements,
                              final ProcessorContext context) {
        elements.stream().filter(element -> element.getKind().isClass())
                .map(element -> element.asType() + ".class").forEach(classes::add);
    }

    @Override
    public final void onProcessingFinished(final ProcessorContext context) {
        WinterProcessor.writeClassWire(context, "CommandComponentListWire",
                context.getWinterModulePackageString(), classes);
    }
}
