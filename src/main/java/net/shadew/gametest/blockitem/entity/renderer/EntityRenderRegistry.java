package net.shadew.gametest.blockitem.entity.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;

import net.shadew.gametest.blockitem.entity.GameTestEntityTypes;

public abstract class EntityRenderRegistry {
    public static void setup() {
        EntityRendererManager rendererManager = Minecraft.getInstance().getRenderManager();
        rendererManager.register(GameTestEntityTypes.FRAME, new FrameRenderer(rendererManager));
    }
}
