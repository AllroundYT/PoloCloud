package de.bytemc.cloud.services;

import de.bytemc.cloud.Base;
import de.bytemc.cloud.api.CloudAPI;
import de.bytemc.cloud.api.network.packets.QueryPacket;
import de.bytemc.cloud.api.network.packets.services.ServiceRequestShutdownPacket;
import de.bytemc.cloud.api.network.packets.services.ServiceShutdownPacket;
import de.bytemc.cloud.api.network.packets.services.ServiceUpdatePacket;
import de.bytemc.cloud.api.services.IService;
import de.bytemc.cloud.api.services.impl.AbstractSimpleServiceManager;
import de.bytemc.cloud.services.process.ProcessServiceStarter;
import de.bytemc.network.cluster.types.NetworkType;
import de.bytemc.network.packets.IPacket;
import de.bytemc.network.promise.ICommunicationPromise;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class ServiceManager extends AbstractSimpleServiceManager {

    public ServiceManager() {
        CloudAPI.getInstance().getNetworkHandler().registerPacketListener(ServiceRequestShutdownPacket.class,
            (channelHandlerContext, serviceRequestShutdownPacket) ->
                shutdownService(Objects.requireNonNull(CloudAPI.getInstance().getServiceManager().getServiceByNameOrNull(serviceRequestShutdownPacket.getService()))));
    }

    public void start(final IService service) {
        this.startService(service).addResultListener(it ->
            CloudAPI.getInstance().getLoggerProvider()
                .logMessage("The service '§b" + service.getName() + "§7' selected and will now started.")).addFailureListener(Throwable::printStackTrace);
    }

    public ICommunicationPromise<IService> startService(final @NotNull IService service) {
        return new ProcessServiceStarter(service).start();
    }

    public void sendPacketToService(final IService service, final IPacket packet) {
        Base.getInstance().getNode().getAllCachedConnectedClients().stream().filter(it -> it.getName().equals(service.getName())).findAny().ifPresent(it -> it.sendPacket(packet));
    }

    public void shutdownService(final IService service) {
        service.sendPacket(new ServiceShutdownPacket(service.getName()));
    }


    @Override
    public void updateService(@NotNull IService service) {
        ServiceUpdatePacket packet = new ServiceUpdatePacket(service);
        //update all other nodes and this connected services
        Base.getInstance().getNode().sendPacketToType(new QueryPacket(packet, QueryPacket.QueryState.SECOND_RESPONSE), NetworkType.NODE);
        //update own service caches
        Base.getInstance().getNode().sendPacketToType(packet, NetworkType.SERVICE);
    }
}
