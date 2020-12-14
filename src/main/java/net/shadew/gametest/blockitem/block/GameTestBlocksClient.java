package net.shadew.gametest.blockitem.block;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GameTestBlocksClient {
    public static void setupClient() {
//        RenderTypeLookup.setRenderLayer(GameTestBlocks.TEST_MARKER_FRAME, RenderType.getTranslucent());
    }
}
