package com.thewinterframework.command.velocity;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.proxy.ProxyServer;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.annotations.AnnotationParser;
import org.incendo.cloud.velocity.VelocityCommandManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VelocityCommandModuleTest {
    @Test
    void inheritsVelocityBindingsAndSharesTheCommandManager() {
        final var server = mock(ProxyServer.class);
        final var eventManager = mock(EventManager.class);
        when(server.getEventManager()).thenReturn(eventManager);
        final var plugin = mock(PluginContainer.class);
        final var parent = Guice.createInjector(binder -> {
            binder.bind(ProxyServer.class).toInstance(server);
            binder.bind(PluginContainer.class).toInstance(plugin);
        });
        final var module = new VelocityCommandModule();
        final var injector = parent.createChildInjector(module);
        injector.injectMembers(module);
        final var manager = injector.getInstance(Key.get(new TypeLiteral<VelocityCommandManager<CommandSource>>() {}));
        assertSame(manager, injector.getInstance(Key.get(new TypeLiteral<CommandManager<CommandSource>>() {})));
        assertSame(manager, injector.getInstance(Key.get(new TypeLiteral<VelocityCommandManager<CommandSource>>() {})));
        assertNotNull(injector.getInstance(Key.get(new TypeLiteral<AnnotationParser<CommandSource>>() {})));
        final var sender = mock(CommandSource.class);
        assertSame(sender, manager.senderMapper().map(sender));
        assertSame(sender, manager.senderMapper().reverse(sender));
        when(sender.hasPermission("command.test")).thenReturn(true);
        assertTrue(manager.hasPermission(sender, "command.test"));
        verify(eventManager).register(eq(plugin), eq(com.velocitypowered.api.event.player.ServerPreConnectEvent.class), any());
    }
}
