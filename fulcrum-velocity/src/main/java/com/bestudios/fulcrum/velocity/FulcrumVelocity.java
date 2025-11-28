package com.bestudios.fulcrum.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

@Plugin(
    id = "fulcrum-velocity",
    name = "fulcrum-velocity",
    version = BuildConstants.VERSION
    ,description = "Comprehensive framework and API for Minecraft plugin development that streamlines the creation of Paper and Spigot plugins"
    ,url = "https://github.com/BlackEden-Studios"
    ,authors = {"Nico Visci"}
)
public class FulcrumVelocity {

    @Inject private Logger logger;

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
    }
}
