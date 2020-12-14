package net.shadew.gametest.net.protocol;

import net.shadew.gametest.net.INetProtocol;
import net.shadew.gametest.net.NetPacketCodec;
import net.shadew.gametest.net.packet.*;

public class V1Protocol implements INetProtocol {
    @Override
    public String getVersion() {
        return "1";
    }

    @Override
    public void registerToServer(NetPacketCodec codec) {
        codec.register(0, UpdateTemplateBlockPacket.class);
        codec.register(1, RotateTemplateBlockPacket.class);
        codec.register(2, SaveTemplateBlockPacket.class);
        codec.register(3, LoadTemplateBlockPacket.class);
        codec.register(4, PlatformTemplateBlockPacket.class);
        codec.register(5, SetFrameNamePacket.class);
    }

    @Override
    public void registerToClient(NetPacketCodec codec) {
        codec.register(0, UpdateTemplateBlockPacket.class);
        codec.register(1, TestMarkerSetPacket.class);
        codec.register(2, TestMarkerClearPacket.class);
    }
}
