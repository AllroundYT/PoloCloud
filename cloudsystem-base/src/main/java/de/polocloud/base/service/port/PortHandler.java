package de.polocloud.base.service.port;

import de.polocloud.base.Base;
import de.polocloud.api.groups.ServiceGroup;
import de.polocloud.api.service.CloudService;

import java.net.InetSocketAddress;
import java.net.ServerSocket;

public final class PortHandler {

    private static final int PORTS_BOUNCE_PROXY = 25565;
    private static final int PORTS_BOUNCE = 30000;

    public static int getNextPort(ServiceGroup service) {
        int port = service.getGameServerVersion().isProxy() ? PORTS_BOUNCE_PROXY : PORTS_BOUNCE;
        while (isPortUsed(port)) {
            port++;
        }
        return port;
    }

    private static boolean isPortUsed(int port) {
        for (final CloudService service : Base.getInstance().getServiceManager().getAllCachedServices()) {
            if (service.getNode().equals(Base.getInstance().getNode().getName())) {
                if (service.getPort() == port) return true;
            }
        }
        try (final ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.bind(new InetSocketAddress(port));
            return false;
        } catch (Exception exception) {
            return true;
        }
    }

}
