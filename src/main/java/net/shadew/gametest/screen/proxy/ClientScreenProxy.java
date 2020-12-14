package net.shadew.gametest.screen.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

import net.shadew.gametest.blockitem.entity.FrameEntity;
import net.shadew.gametest.screen.EditFrameScreen;
import net.shadew.gametest.screen.EditTemplateScreen;

public class ClientScreenProxy extends ScreenProxy {
    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void openTemplateBlock(PlayerEntity player, BlockPos pos) {
        mc.displayGuiScreen(new EditTemplateScreen().loadTileEntity(pos));
    }

    @Override
    public void openFrame(PlayerEntity player, FrameEntity frame) {
        mc.displayGuiScreen(new EditFrameScreen(frame));
    }
}
