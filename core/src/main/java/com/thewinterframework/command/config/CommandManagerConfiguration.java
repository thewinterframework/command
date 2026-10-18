package com.thewinterframework.command.config;

import org.incendo.cloud.CommandManager;

/**
 * Configures Cloud after component extensions are registered and before annotated commands are parsed.
 * @param <C> the command sender type
 */
@FunctionalInterface
public interface CommandManagerConfiguration<C> {
    /**
     * Configures the command manager.
     * @param commandManager the command manager
     */
    void configure(CommandManager<C> commandManager);
}
