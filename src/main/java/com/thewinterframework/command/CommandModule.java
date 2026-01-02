package com.thewinterframework.command;

import com.google.inject.*;
import com.thewinterframework.command.config.CommandConfigurationRegistrar;
import com.thewinterframework.command.config.CommandManagerConfiguration;
import com.thewinterframework.command.decorator.AnnotationParserDecorator;
import com.thewinterframework.command.processor.CommandComponentProcessor;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.plugin.module.PluginModule;
import com.thewinterframework.utils.Reflections;
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class CommandModule implements PluginModule {

	private final List<Class<?>> commandComponents = new ArrayList<>();

	@Override
	public void configure(Binder binder) {
		binder.bindScope(CommandComponent.class, Scopes.SINGLETON);
	}

	@Override
	public boolean onLoad(WinterPlugin plugin) {
		try {
			final var components = CommandComponentProcessor.scan(plugin.getClass(), CommandComponent.class).getClassList();
			commandComponents.addAll(components);
			return true;
		} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			plugin.getSLF4JLogger().error("Failed to scan command components", e);
			return false;
		}
	}

	@Provides
	@Singleton
	PaperCommandManager<Source> commandManager(JavaPlugin plugin, Injector injector) {
		final var senderMapper = this.commandComponents.stream()
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
	AnnotationParser<Source> annotationParser(PaperCommandManager<Source> commandManager) {
		return new AnnotationParser<>(commandManager, Source.class);
	}

	@Override
	public boolean onEnable(WinterPlugin plugin) {
		final var injector = plugin.getInjector();
		final var commandManager = injector.getInstance(Key.get(new TypeLiteral<PaperCommandManager<Source>>() {}));
		final var configurationRegistrar = injector.getInstance(CommandConfigurationRegistrar.class);
		final var annotationParser = injector.getInstance(Key.get(new TypeLiteral<AnnotationParser<Source>>() {}));
		for (final var commandComponent : commandComponents) {
			if (ArgumentParser.class.isAssignableFrom(commandComponent)) {
				registerArgumentParser(commandManager, injector, commandComponent);
			}
			if (ParameterInjector.class.isAssignableFrom(commandComponent)) {
				registerParameterInjector(commandManager, injector, commandComponent);
			}
			if (CommandManagerConfiguration.class.isAssignableFrom(commandComponent)) {
				registerCommandManagerConfiguration(configurationRegistrar, injector, commandComponent);
			}
			if (AnnotationParserDecorator.class.isAssignableFrom(commandComponent)) {
				decorateAnnotationParser(annotationParser, injector, commandComponent);
			}
		}
		configurationRegistrar.configure(commandManager);
		for (final var commandComponent : commandComponents) {
			annotationParser.parse(injector.getInstance(commandComponent));
		}
		return true;
	}

	private void registerCommandManagerConfiguration(
			CommandConfigurationRegistrar configurationRegistrar,
			Injector injector,
			Class<?> clazz
	) {
		configurationRegistrar.register((CommandManagerConfiguration) injector.getInstance(clazz));
	}

	@SuppressWarnings("unchecked")
	private void registerArgumentParser(
			PaperCommandManager<Source> commandManager,
			Injector injector,
			Class<?> clazz
	) {
		final var type = (TypeToken<Object>) TypeToken.get(Reflections.getGenericType(clazz, ArgumentParser.class, 1));
		final var parser = (ArgumentParser<Source, Object>) injector.getInstance(clazz);

		commandManager.parserRegistry()
				.registerParser(ParserDescriptor.of(parser, type));
	}

	@SuppressWarnings("unchecked")
	private void registerParameterInjector(
			PaperCommandManager<Source> commandManager,
			Injector injector,
			Class<?> clazz
	) {
		final var type = (TypeToken<Object>) TypeToken.get(Reflections.getGenericType(clazz, ParameterInjector.class, 1));
		final var parameterInjector = (ParameterInjector<Source, Object>) injector.getInstance(clazz);
		commandManager.parameterInjectorRegistry()
				.registerInjector(type, parameterInjector);
	}

	private void decorateAnnotationParser(
			AnnotationParser<Source> annotationParser,
			Injector injector,
			Class<?> clazz
	) {
		final var decorator = (AnnotationParserDecorator) injector.getInstance(clazz);
		decorator.decorate(annotationParser);
	}

}
