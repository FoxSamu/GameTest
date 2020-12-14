package net.shadew.gametest.net.packet;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import net.shadew.gametest.blockitem.block.props.DiagonalDirection;
import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;

public class RotateTemplateBlockPacket extends TemplateBlockPacket {
    private DiagonalDirection direction;

    public RotateTemplateBlockPacket(BlockPos pos, DiagonalDirection direction) {
        super(pos);
        this.direction = direction;
    }

    public RotateTemplateBlockPacket() {
    }

    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeByte(direction.ordinal());
    }

    @Override
    public TemplateBlockPacket read(PacketBuffer buf) {
        super.read(buf);
        direction = DiagonalDirection.values()[buf.readByte()];
        return this;
    }

    @Override
    protected void updateTemplateBlock(TemplateBlockTileEntity templateBlock) {
        templateBlock.changeRotation(direction);
    }
}
