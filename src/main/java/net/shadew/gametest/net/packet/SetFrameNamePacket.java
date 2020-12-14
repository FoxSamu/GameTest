package net.shadew.gametest.net.packet;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.blockitem.entity.FrameEntity;
import net.shadew.gametest.net.INetPacket;
import net.shadew.gametest.net.NetContext;

public class SetFrameNamePacket implements INetPacket {
    private int id;
    private String name;

    public SetFrameNamePacket(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public SetFrameNamePacket() {
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeInt(id);
        buf.writeString(name);
    }

    @Override
    public INetPacket read(PacketBuffer buf) {
        id = buf.readInt();
        name = buf.readString();
        return this;
    }

    @Override
    public void handle(NetContext ctx) {
        ctx.ensureMainThread();

        World world = ctx.getWorld();
        Entity entity = world.getEntityByID(id);
        if(!(entity instanceof FrameEntity)) {
            GameTestMod.LOGGER.error("Received invalid SetFrameName packet");
            return;
        }

        ((FrameEntity) entity).setFrameName(name);
    }
}
