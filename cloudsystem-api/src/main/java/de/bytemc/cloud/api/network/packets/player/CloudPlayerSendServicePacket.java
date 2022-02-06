package de.bytemc.cloud.api.network.packets.player;

import de.bytemc.network.packets.IPacket;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CloudPlayerSendServicePacket implements IPacket {

    private UUID uuid;
    private String service;

    @Override
    public void read(ByteBuf byteBuf) {
        this.uuid = readUUID(byteBuf);
        this.service = readString(byteBuf);
    }

    @Override
    public void write(ByteBuf byteBuf) {
        writeUUID(byteBuf, uuid);
        writeString(byteBuf, service);
    }
}