package net.shadew.gametest.net.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.blockitem.tileentity.TestBlockTileEntity;
import net.shadew.gametest.net.INetPacket;
import net.shadew.gametest.net.NetContext;

public class TestMarkerClearPacket implements INetPacket {
    private BlockPos originPos;

    public TestMarkerClearPacket(BlockPos originPos) {
        this.originPos = originPos;
    }

    public TestMarkerClearPacket() {
    }


    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockPos(originPos);
    }

    @Override
    public INetPacket read(PacketBuffer buf) {
        originPos = buf.readBlockPos();
        return this;
    }

    @Override
    public void handle(NetContext ctx) {
        ctx.ensureMainThread();

        World world = ctx.getWorld();
        TileEntity tileEntity = world.getTileEntity(originPos);
        if(tileEntity instanceof TemplateBlockTileEntity) {
            TestBlockTileEntity testBlock = (TestBlockTileEntity) tileEntity;
            testBlock.clearMarkers();
        }
    }
}
