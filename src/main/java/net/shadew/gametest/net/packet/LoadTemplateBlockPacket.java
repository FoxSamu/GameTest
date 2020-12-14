package net.shadew.gametest.net.packet;

import com.mojang.datafixers.util.Either;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;

import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.framework.GameTestFunction;
import net.shadew.gametest.net.NetContext;

public class LoadTemplateBlockPacket extends UpdateTemplateBlockPacket {
    public LoadTemplateBlockPacket(BlockPos pos, Either<ResourceLocation, GameTestFunction> name, int width, int height, int depth) {
        super(pos, name, width, height, depth);
    }

    public LoadTemplateBlockPacket() {
    }

    @Override
    protected void updateTemplateBlock(TemplateBlockTileEntity templateBlock, NetContext ctx) {
        updateTemplateBlock(templateBlock);

        int res = templateBlock.loadOrPlace();
        switch (res) {
            case 0:
                ctx.getPlayer().sendStatusMessage(new TranslationTextComponent("gametest.template_block.loaded"), true);
                break;
            case 1:
                ctx.getPlayer().sendStatusMessage(new TranslationTextComponent("gametest.template_block.not_loaded.error"), true);
                break;
            case 2:
                ctx.getPlayer().sendStatusMessage(new TranslationTextComponent("gametest.template_block.not_loaded.unknown"), true);
                break;
            case 3:
                ctx.getPlayer().sendStatusMessage(new TranslationTextComponent("gametest.template_block.not_placed"), true);
                break;
            case 4:
                ctx.getPlayer().sendStatusMessage(new TranslationTextComponent("gametest.template_block.placed"), true);
                break;
        }
    }
}
