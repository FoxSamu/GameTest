package net.shadew.gametest.blockitem.tileentity.renderer;

import net.minecraftforge.fml.client.registry.ClientRegistry;

import net.shadew.gametest.blockitem.tileentity.GameTestTileEntityTypes;

public abstract class TileEntityRenderRegistry {
    public static void setup() {
        ClientRegistry.bindTileEntityRenderer(GameTestTileEntityTypes.TEMPLATE_BLOCK, TemplateBlockRenderer::new);
        ClientRegistry.bindTileEntityRenderer(GameTestTileEntityTypes.TEST_BLOCK, TestBlockRenderer::new);
//        ClientRegistry.bindTileEntityRenderer(GameTestTileEntityTypes.REMOTE, RemoteRenderer::new);
    }
}
