package com.thewinterframework.command.config;

import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.paper.util.sender.Source;

/**
 * Configures the command manager.
 */
public interface CommandManagerConfiguration {

    /**
     * Configures the command manager.
     * This action will be executed after the command manager has been created but before commands, completions,
     * parameters, and conditions are registered.
     *
     * @param commandManager the command manager
     */
    void configure(PaperCommandManager<Source> commandManager);
}
