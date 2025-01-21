package com.thewinterframework.command.config;

import com.google.inject.Singleton;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * Registers {@link CommandManagerConfiguration}s and applies them to a {@link PaperCommandManager}.
 */
@Singleton
public class CommandConfigurationRegistrar {

    private final List<CommandManagerConfiguration> configurations = new ArrayList<>();

    /**
     * Registers a configuration.
     *
     * @param configuration the configuration
     */
    public void register(CommandManagerConfiguration configuration) {
        this.configurations.add(configuration);
    }

    /**
     * Configures a {@link PaperCommandManager} with all registered configurations.
     *
     * @param commandManager the command manager
     */
    public void configure(PaperCommandManager<Source> commandManager) {
        for (final var configuration : this.configurations) {
            configuration.configure(commandManager);
        }
    }

}
