package net.shadew.gametest.hooks;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.sun.javafx.geom.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.event.DebugRenderEvent;
import net.shadew.gametest.renderer.FluidDebugRenderer;

@OnlyIn(Dist.CLIENT)
public final class DebugRenderHook {
    public static boolean showPathfinding;
    public static boolean showNeighborsUpdate;
    public static boolean showFluids;

    // Ripoff of water debug renderer, with some tweaks
    // Since it doesn't receive any packets from the server we don't need hacky ATs to inject it
    private static final FluidDebugRenderer FLUID_DEBUG_RENDERER = new FluidDebugRenderer(Minecraft.getInstance());

    public static void onRenderDebug(DebugRenderer renderer, MatrixStack matrices, IRenderTypeBuffer.Impl buf, double camx, double camy, double camz) {
        if(showPathfinding) renderer.pathfinding.render(matrices, buf, camx, camy, camz);
        if(showNeighborsUpdate) renderer.neighborsUpdate.render(matrices, buf, camx, camy, camz);
        if(showFluids) FLUID_DEBUG_RENDERER.render(matrices, buf, camx, camy, camz);

        GameTestMod.DEBUG_EVENT_BUS.post(new DebugRenderEvent(renderer, matrices, buf, new Vec3d(camx, camy, camz)));
    }
}
