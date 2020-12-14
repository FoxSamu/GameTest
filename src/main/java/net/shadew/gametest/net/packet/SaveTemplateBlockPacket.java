package net.shadew.gametest.net.packet;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.play.server.STitlePacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.framework.GameTestFunction;
import net.shadew.gametest.net.NetContext;

public class SaveTemplateBlockPacket extends UpdateTemplateBlockPacket {
    public SaveTemplateBlockPacket(BlockPos pos, Either<ResourceLocation, GameTestFunction> name, int width, int height, int depth) {
        super(pos, name, width, height, depth);
    }

    public SaveTemplateBlockPacket() {
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
}
