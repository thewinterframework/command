package com.thewinterframework.command.paper;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.util.Modules;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.Source;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PaperCommandModuleTest {
    @Test
    @SuppressWarnings("unchecked")
    void createsThePaperManagerOnEnableAndSharesItsGenericBinding() {
        final var plugin = mock(JavaPlugin.class);
        final PaperCommandManager<Source> manager = mock(PaperCommandManager.class);
        final PaperCommandManager.Builder<Source> builder = mock(PaperCommandManager.Builder.class);
        final PaperCommandManager.CoordinatedBuilder<Source> coordinated = mock(PaperCommandManager.CoordinatedBuilder.class);
        when(builder.executionCoordinator(any())).thenReturn(coordinated);
        when(coordinated.buildOnEnable(plugin)).thenReturn(manager);
        final AnnotationParser<Source> parser = mock(AnnotationParser.class);
        final var module = new PaperCommandModule();
        try (final var factory = mockStatic(PaperCommandManager.class)) {
            factory.when(() -> PaperCommandManager.builder(any())).thenReturn(builder);
            final var injector = Guice.createInjector(Modules.override(module).with(binder -> {
                binder.bind(JavaPlugin.class).toInstance(plugin);
                binder.bind(new TypeLiteral<AnnotationParser<Source>>() {}).toInstance(parser);
            }));
            injector.injectMembers(module);
            assertSame(manager, injector.getInstance(Key.get(new TypeLiteral<PaperCommandManager<Source>>() {})));
            assertSame(manager, injector.getInstance(Key.get(new TypeLiteral<CommandManager<Source>>() {})));
            verify(coordinated).buildOnEnable(plugin);
            factory.verify(() -> PaperCommandManager.builder(any()));
        }
    }
}
