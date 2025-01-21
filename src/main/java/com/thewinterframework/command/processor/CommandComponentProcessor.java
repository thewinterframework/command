package com.thewinterframework.command.processor;

import com.thewinterframework.command.CommandComponent;
import com.thewinterframework.command.CommandModule;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.processor.provider.ClassListProviderAnnotationProcessor;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Annotation processor for {@link CommandComponent}.
 */
public class CommandComponentProcessor extends ClassListProviderAnnotationProcessor {

	@Override
	protected Set<Class<? extends Annotation>> getSupportedAnnotations() {
		return Set.of(CommandComponent.class);
	}

	@Override
	protected @Nullable Class<? extends PluginModule> requiredModule() {
		return CommandModule.class;
	}
}