package net.shadew.gametest.net.packet;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.net.NetContext;

public class PlatformTemplateBlockPacket extends TemplateBlockPacket {
    private CompoundNBT nbt;

    public PlatformTemplateBlockPacket(BlockPos pos, CompoundNBT nbt) {
        super(pos);
        this.nbt = nbt;
    }

    public PlatformTemplateBlockPacket() {
    }

    @Override
    public TemplateBlockPacket read(PacketBuffer buf) {
        super.read(buf);
        nbt = buf.readCompoundTag();
        return this;
    }

    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeCompoundTag(nbt);
    }

    @Override
    protected void updateTemplateBlock(TemplateBlockTileEntity templateBlock, NetContext ctx) {
        updateTemplateBlock(templateBlock);

        if (templateBlock.save()) {
            ctx.getPlayer().sendStatusMessage(new TranslationTextComponent("gametest.template_block.saved"), true);
        } else {
            ctx.getPlayer().sendStatusMessage(new TranslationTextComponent("gametest.template_block.not_saved"), true);
        }
    }

    @Override
    protected void updateTemplateBlock(TemplateBlockTileEntity templateBlock) {
        templateBlock.createPlatform(nbt);
    }
}
