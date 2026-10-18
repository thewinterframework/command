package com.thewinterframework.command.velocity;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.thewinterframework.command.CommandModule;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.velocity.VelocityCommandManager;

/** Winter command integration using Velocity's native command sources. */
public final class VelocityCommandModule extends CommandModule<CommandSource> {
    @Provides
    @Singleton
    VelocityCommandManager<CommandSource> velocityCommandManager(
            final PluginContainer plugin, final ProxyServer server, final Injector injector
    ) {
        return new VelocityCommandManager<>(plugin, server, ExecutionCoordinator.simpleCoordinator(),
                senderMapper(injector, SenderMapper.identity()));
    }

    @Provides
    CommandManager<CommandSource> commandManager(final VelocityCommandManager<CommandSource> commandManager) {
        return commandManager;
    }

    @Provides
    @Singleton
    AnnotationParser<CommandSource> annotationParser(final VelocityCommandManager<CommandSource> commandManager) {
        return new AnnotationParser<>(commandManager, CommandSource.class);
    }
}
