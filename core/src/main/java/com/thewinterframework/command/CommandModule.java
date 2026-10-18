package com.thewinterframework.command;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.thewinterframework.command.config.CommandConfigurationRegistrar;
import com.thewinterframework.command.config.CommandManagerConfiguration;
import com.thewinterframework.command.decorator.AnnotationParserDecorator;
import com.thewinterframework.plugin.WinterPlugin;
import com.thewinterframework.wire.module.AbstractProcessorModule;
import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeToken;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.injection.ParameterInjector;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Shared component registration, specialized by each platform's sender type.
 * @param <C> the command sender type
 */
public abstract class CommandModule<C> extends AbstractProcessorModule {
    @Inject private CommandManager<C> commandManager;
    @Inject private CommandConfigurationRegistrar<C> configurationRegistrar;
    @Inject private AnnotationParser<C> annotationParser;
    @Inject private Injector injector;

    private final Set<Class<?>> toParse = new LinkedHashSet<>();

    protected CommandModule() {
        super(CommandComponent.class);
    }

    @Override
    public void configure(final Binder binder) {
        binder.bindScope(CommandComponent.class, Scopes.SINGLETON);
    }

    /**
     * Selects a component sender mapper, falling back to the platform default.
     * @param injector the plugin injector
     * @param fallback the platform mapper
     * @param <N> the native platform sender type
     * @return the selected mapper
     */
    @SuppressWarnings("unchecked")
    protected final <N> SenderMapper<N, C> senderMapper(
            final Injector injector, final SenderMapper<N, C> fallback
    ) {
        return activeComponents.stream()
                .filter(SenderMapper.class::isAssignableFrom)
                .sorted(Comparator.comparing(Class::getName))
                .findFirst()
                .map(clazz -> (SenderMapper<N, C>) injector.getInstance(clazz))
                .orElse(fallback);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void enableComponent(final WinterPlugin plugin, final Class<?> componentClass) {
        final var component = injector.getInstance(componentClass);
        if (component instanceof ArgumentParser<?, ?> parser) {
            final var type = componentType(componentClass, ArgumentParser.class);
            commandManager.parserRegistry().registerParser(
                    ParserDescriptor.of((ArgumentParser<C, Object>) parser, type));
        }
        if (component instanceof ParameterInjector<?, ?> parameterInjector) {
            final var type = componentType(componentClass, ParameterInjector.class);
            commandManager.parameterInjectorRegistry().registerInjector(
                    type, (ParameterInjector<C, Object>) parameterInjector);
        }
        if (component instanceof CommandManagerConfiguration<?> configuration) {
            configurationRegistrar.register((CommandManagerConfiguration<C>) configuration);
        }
        if (component instanceof AnnotationParserDecorator<?> decorator) {
            ((AnnotationParserDecorator<C>) decorator).decorate(annotationParser);
        }
        toParse.add(componentClass);
    }

    @Override
    public boolean onEnable(final WinterPlugin plugin) throws Exception {
        super.onEnable(plugin);
        configurationRegistrar.configure(commandManager);
        for (final var componentClass : toParse) {
            annotationParser.parse(injector.getInstance(componentClass));
        }
        return true;
    }

    @Override
    public boolean onDisable(final WinterPlugin plugin) throws Exception {
        try {
            toParse.clear();
            configurationRegistrar.clear();
        } finally {
            super.onDisable(plugin);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private static TypeToken<Object> componentType(final Class<?> componentClass, final Class<?> role) {
        final var type = GenericTypeReflector.getTypeParameter(componentClass, role.getTypeParameters()[1]);
        if (type == null) {
            throw new IllegalArgumentException("Missing generic type for " + role.getSimpleName()
                    + " on " + componentClass.getName());
        }
        return (TypeToken<Object>) TypeToken.get(type);
    }
}
