package net.shadew.gametest.net;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public interface INetPacket {
    void write(PacketBuffer buf);
    INetPacket read(PacketBuffer buf);
    void handle(NetContext ctx);
}
