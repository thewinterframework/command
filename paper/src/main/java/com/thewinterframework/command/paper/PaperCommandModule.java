package com.thewinterframework.command.paper;

import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.thewinterframework.command.CommandModule;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.PaperSimpleSenderMapper;
import org.incendo.cloud.paper.util.sender.Source;

/** Winter command integration using Cloud's Paper sender wrappers. */
public final class PaperCommandModule extends CommandModule<Source> {
    @Provides
    @Singleton
    PaperCommandManager<Source> paperCommandManager(final JavaPlugin plugin, final Injector injector) {
        return PaperCommandManager.builder(senderMapper(injector, PaperSimpleSenderMapper.simpleSenderMapper()))
                .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
                .buildOnEnable(plugin);
    }

    @Provides
    CommandManager<Source> commandManager(final PaperCommandManager<Source> commandManager) {
        return commandManager;
    }

    @Provides
    @Singleton
    AnnotationParser<Source> annotationParser(final PaperCommandManager<Source> commandManager) {
        return new AnnotationParser<>(commandManager, Source.class);
    }
}
