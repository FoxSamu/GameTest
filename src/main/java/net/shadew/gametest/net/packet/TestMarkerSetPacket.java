package net.shadew.gametest.net.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.blockitem.tileentity.TestBlockTileEntity;
import net.shadew.gametest.framework.api.Marker;
import net.shadew.gametest.net.INetPacket;
import net.shadew.gametest.net.NetContext;

public class TestMarkerSetPacket implements INetPacket {
    private BlockPos originPos;
    private BlockPos markerPos;
    private Marker type;
    private String message;

    public TestMarkerSetPacket(BlockPos originPos, BlockPos markerPos, Marker type, String message) {
        this.originPos = originPos;
        this.markerPos = markerPos;
        this.type = type;
        this.message = message;
    }

    public TestMarkerSetPacket() {
    }


    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockPos(originPos);
        buf.writeBlockPos(markerPos);
        buf.writeByte(type.ordinal());
        buf.writeString(message);
    }

    @Override
    public INetPacket read(PacketBuffer buf) {
        originPos = buf.readBlockPos();
        markerPos = buf.readBlockPos();
        type = Marker.values()[buf.readByte()];
        message = buf.readString();
        return this;
    }

    @Override
    public void handle(NetContext ctx) {
        ctx.ensureMainThread();

        World world = ctx.getWorld();
        TileEntity tileEntity = world.getTileEntity(originPos);
        if(tileEntity instanceof TemplateBlockTileEntity) {
            TestBlockTileEntity testBlock = (TestBlockTileEntity) tileEntity;
            testBlock.addMarker(markerPos, type, message);
        }
    }
}
