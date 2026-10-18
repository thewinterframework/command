package com.thewinterframework.command;

import com.google.inject.Guice;
import com.google.inject.TypeLiteral;
import com.thewinterframework.command.config.CommandManagerConfiguration;
import com.thewinterframework.command.decorator.AnnotationParserDecorator;
import com.thewinterframework.plugin.WinterPlugin;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.injection.ParameterInjector;
import org.incendo.cloud.internal.CommandRegistrationHandler;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.NOPLogger;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandModuleTest {
    @Test
    @SuppressWarnings("unchecked")
    void selectsAComponentMapperAndFallsBackToThePlatformMapper() {
        final var module = new TestModule();
        final var injector = Guice.createInjector(module);
        final SenderMapper<Integer, Sender> fallback = mock(SenderMapper.class);
        assertSame(fallback, module.selectMapper(injector, fallback));
        module.activate(CustomMapper.class);
        assertSame(injector.getInstance(CustomMapper.class), module.selectMapper(injector, fallback));
    }

    @Test
    void registersExtensionsAndExecutesAnAnnotatedCommand() throws Exception {
        final var manager = new TestManager();
        final var parser = new AnnotationParser<>(manager, Sender.class);
        final var module = new TestModule();
        module.activate(ValueParser.class, ValueInjector.class, Configuration.class, Decorator.class, TestCommand.class);
        final var injector = Guice.createInjector(module, binder -> {
            binder.bind(new TypeLiteral<CommandManager<Sender>>() {}).toInstance(manager);
            binder.bind(new TypeLiteral<AnnotationParser<Sender>>() {}).toInstance(parser);
        });
        injector.injectMembers(module);
        final var plugin = mock(WinterPlugin.class);
        when(plugin.getInjector()).thenReturn(injector);
        when(plugin.getSLF4JLogger()).thenReturn(NOPLogger.NOP_LOGGER);

        assertTrue(module.onEnable(plugin));
        assertTrue(injector.getInstance(Configuration.class).configured);
        assertTrue(injector.getInstance(Decorator.class).decorated);
        assertSame(injector.getInstance(TestCommand.class), injector.getInstance(TestCommand.class));
        final var sender = new Sender();
        manager.commandExecutor().executeCommand(sender, "echo hello").join();
        assertEquals("hello:injected", sender.result);
        assertTrue(module.onDisable(plugin));
    }

    static final class Sender { String result; }
    record Value(String text) {}
    record InjectedValue(String text) {}

    static final class TestModule extends CommandModule<Sender> {
        void activate(final Class<?>... components) { activeComponents.addAll(List.of(components)); }
        SenderMapper<Integer, Sender> selectMapper(final com.google.inject.Injector injector,
                                                   final SenderMapper<Integer, Sender> fallback) {
            return senderMapper(injector, fallback);
        }
    }

    static final class TestManager extends CommandManager<Sender> {
        TestManager() {
            super(ExecutionCoordinator.simpleCoordinator(), CommandRegistrationHandler.nullCommandRegistrationHandler());
        }
        @Override public boolean hasPermission(final Sender sender, final String permission) { return true; }
    }

    @CommandComponent
    public static final class CustomMapper implements SenderMapper<Integer, Sender> {
        @Override public Sender map(final Integer nativeSender) { return new Sender(); }
        @Override public Integer reverse(final Sender sender) { return 0; }
    }

    // Inheritance verifies that generic extension types survive extraction into the shared module.
    abstract static class BaseParser<T> implements ArgumentParser<Sender, T> {}

    @CommandComponent
    public static final class ValueParser extends BaseParser<Value> {
        @Override
        public ArgumentParseResult<Value> parse(final CommandContext<Sender> context, final CommandInput input) {
            return ArgumentParseResult.success(new Value(input.readString()));
        }
    }

    @CommandComponent
    public static final class ValueInjector implements ParameterInjector<Sender, InjectedValue> {
        @Override
        public InjectedValue create(final CommandContext<Sender> context, final org.incendo.cloud.util.annotation.AnnotationAccessor annotations) {
            return new InjectedValue("injected");
        }
    }

    @CommandComponent
    public static final class Configuration implements CommandManagerConfiguration<Sender> {
        boolean configured;
        @Override public void configure(final CommandManager<Sender> manager) { configured = true; }
    }

    @CommandComponent
    public static final class Decorator implements AnnotationParserDecorator<Sender> {
        boolean decorated;
        @Override public void decorate(final AnnotationParser<Sender> parser) { decorated = true; }
    }

    @CommandComponent
    public static final class TestCommand {
        @Command("echo <value>")
        public void echo(final Sender sender, @Argument("value") final Value value, final InjectedValue injected) {
            sender.result = value.text() + ":" + injected.text();
        }
    }
}
