package com.thewinterframework.command;

import com.google.inject.*;
import com.thewinterframework.command.config.CommandConfigurationRegistrar;
import com.thewinterframework.command.config.CommandManagerConfiguration;
import com.thewinterframework.command.decorator.AnnotationParserDecorator;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.utils.reflect.Reflections;
import com.thewinterframework.wire.module.AbstractProcessorModule;
import io.leangen.geantyref.TypeToken;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.injection.ParameterInjector;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public class CommandModule extends AbstractProcessorModule {

    public CommandModule() {
        super(CommandComponent.class);
    }

    @Override
	public void configure(final Binder binder) {
		binder.bindScope(CommandComponent.class, Scopes.SINGLETON);
	}

	@Provides
	@Singleton
	PaperCommandManager<Source> commandManager(final JavaPlugin plugin, final Injector injector) {
		final var senderMapper = this.activeComponents.stream()
				.filter(SenderMapper.class::isAssignableFrom)
				.findFirst()
				.map(clazz -> (SenderMapper<CommandSourceStack, Source>) injector.getInstance(clazz))
				.orElse(PaperSimpleSenderMapper.simpleSenderMapper());
		return PaperCommandManager
				.builder(senderMapper)
				.executionCoordinator(ExecutionCoordinator.simpleCoordinator())
				.buildOnEnable(plugin);
	}

	@Provides
	@Singleton
	AnnotationParser<Source> annotationParser(final PaperCommandManager<Source> commandManager) {
		return new AnnotationParser<>(commandManager, Source.class);
	}

    @Inject private PaperCommandManager<Source> commandManager;
    @Inject private CommandConfigurationRegistrar configurationRegistrar;
    @Inject private AnnotationParser<Source> annotationParser;
    @Inject private Injector injector;

    private final Set<Class<?>> toParse = new HashSet<>();

    @Override
    protected void enableComponent(final WinterPlugin plugin, final Class<?> commandComponent) {
        if (ArgumentParser.class.isAssignableFrom(commandComponent)) {
            registerArgumentParser(commandManager, commandComponent);
        }
        if (ParameterInjector.class.isAssignableFrom(commandComponent)) {
            registerParameterInjector(commandManager, commandComponent);
        }
        if (CommandManagerConfiguration.class.isAssignableFrom(commandComponent)) {
            registerCommandManagerConfiguration(configurationRegistrar, commandComponent);
        }
        if (AnnotationParserDecorator.class.isAssignableFrom(commandComponent)) {
            decorateAnnotationParser(annotationParser, commandComponent);
        }

        toParse.add(commandComponent);
    }

    @Override
	public boolean onEnable(final WinterPlugin plugin) throws Exception {
		super.onEnable(plugin);

		configurationRegistrar.configure(commandManager);
		for (final var commandComponent : toParse) {
			annotationParser.parse(injector.getInstance(commandComponent));
		}
		return true;
	}

	private void registerCommandManagerConfiguration(
            final CommandConfigurationRegistrar configurationRegistrar,
            final Class<?> clazz
	) {
		configurationRegistrar.register((CommandManagerConfiguration) injector.getInstance(clazz));
	}

	@SuppressWarnings("unchecked")
	private void registerArgumentParser(
            final PaperCommandManager<Source> commandManager,
            final Class<?> clazz
	) {
		final var type = (TypeToken<Object>) TypeToken.get(Reflections.getGenericType(clazz, ArgumentParser.class, 1));
		final var parser = (ArgumentParser<Source, Object>) injector.getInstance(clazz);

		commandManager.parserRegistry()
				.registerParser(ParserDescriptor.of(parser, type));
	}

	@SuppressWarnings("unchecked")
	private void registerParameterInjector(
            final PaperCommandManager<Source> commandManager,
            final Class<?> clazz
	) {
		final var type = (TypeToken<Object>) TypeToken.get(Reflections.getGenericType(clazz, ParameterInjector.class, 1));
		final var parameterInjector = (ParameterInjector<Source, Object>) injector.getInstance(clazz);
		commandManager.parameterInjectorRegistry()
				.registerInjector(type, parameterInjector);
	}

	private void decorateAnnotationParser(
            final AnnotationParser<Source> annotationParser,
            final Class<?> clazz
	) {
		final var decorator = (AnnotationParserDecorator) injector.getInstance(clazz);
		decorator.decorate(annotationParser);
	}

}
