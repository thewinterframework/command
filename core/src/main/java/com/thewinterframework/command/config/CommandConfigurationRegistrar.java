package com.thewinterframework.command.config;

import com.google.inject.Singleton;
import org.incendo.cloud.CommandManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects configurations to apply before annotated commands are parsed.
 * @param <C> the command sender type
 */
@Singleton
public class CommandConfigurationRegistrar<C> {
    private final List<CommandManagerConfiguration<C>> configurations = new ArrayList<>();

    /**
     * Registers a configuration.
     * @param configuration the configuration
     */
    public void register(final CommandManagerConfiguration<C> configuration) {
        configurations.add(configuration);
    }

    /**
     * Applies all registered configurations.
     * @param commandManager the command manager
     */
    public void configure(final CommandManager<C> commandManager) {
        configurations.forEach(configuration -> configuration.configure(commandManager));
    }

    /** Clears configurations when the module stops. */
    public void clear() {
        configurations.clear();
    }
}
