package de.bytemc.cloud.plugin.bootstrap.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.bytemc.cloud.api.CloudAPI;
import de.bytemc.cloud.api.services.IService;
import de.bytemc.cloud.api.services.utils.ServiceState;
import de.bytemc.cloud.api.services.utils.ServiceVisibility;
import de.bytemc.cloud.plugin.bootstrap.velocity.listener.VelocityCloudListener;
import de.bytemc.cloud.plugin.bootstrap.velocity.listener.VelocityListener;

import java.util.Comparator;
import java.util.Optional;

@Plugin(id = "cloudplugin", name = "CloudPlugin", authors = "HttpMarco", version = "1.0")
public final class VelocityBootstrap {

    private final ProxyServer proxyServer;

    @Inject
    public VelocityBootstrap(final ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    @Subscribe
    public void handle(final ProxyInitializeEvent event) {
        new VelocityCloudListener(this.proxyServer);

        this.proxyServer.getEventManager().register(this, new VelocityListener(this, this.proxyServer));
    }

    public Optional<IService> getFallback(final Player player) {
        return CloudAPI.getInstance().getServiceManager().getAllCachedServices().stream()
            .filter(service -> service.getServiceState() == ServiceState.ONLINE)
            .filter(service -> service.getServiceVisibility() == ServiceVisibility.VISIBLE)
            .filter(service -> !service.getGroup().getGameServerVersion().isProxy())
            .filter(service -> service.getGroup().isFallbackGroup())
            .filter(service -> (player.getCurrentServer().isEmpty()
                || !player.getCurrentServer().get().getServerInfo().getName().equals(service.getName())))
            .min(Comparator.comparing(IService::getOnlinePlayers));
    }

}
