package net.shadew.gametest.net.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSide;

import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.net.INetPacket;
import net.shadew.gametest.net.NetContext;

public abstract class TemplateBlockPacket implements INetPacket {
    protected BlockPos pos;

    protected TemplateBlockPacket(BlockPos pos) {
        this.pos = pos;
    }

    protected TemplateBlockPacket() {
    }

    @Override
    public void write(PacketBuffer buf) {
        buf.writeBlockPos(pos);
    }

    @Override
    public TemplateBlockPacket read(PacketBuffer buf) {
        pos = buf.readBlockPos();
        return this;
    }

    @Override
    public void handle(NetContext ctx) {
        ctx.ensureMainThread();

        World world = ctx.getWorld();
        TileEntity e = world.getTileEntity(pos);
        if (!(e instanceof TemplateBlockTileEntity)) {
            if (ctx.getArrivalSide() == LogicalSide.SERVER)
                // Only on server this makes sense, if the TE does not exist on client yet it will receive a TE update
                // packet later
                GameTestMod.LOGGER.error("Received invalid template block update packet");
            return;
        }

        TemplateBlockTileEntity templateBlock = (TemplateBlockTileEntity) e;
        updateTemplateBlock(templateBlock, ctx);
    }

    protected void updateTemplateBlock(TemplateBlockTileEntity templateBlock, NetContext ctx) {
        updateTemplateBlock(templateBlock);
    }

    protected abstract void updateTemplateBlock(TemplateBlockTileEntity templateBlock);
}
