package net.shadew.gametest.util;

import net.minecraft.client.renderer.RenderState;

public class RenderStates extends RenderState {
    private RenderStates(String name, Runnable beginAction, Runnable endAction) {
        super(name, beginAction, endAction);
    }

    public static RenderState.TransparencyState getNoTransparency() {
        return RenderState.NO_TRANSPARENCY;
    }

    public static RenderState.TransparencyState getCrumblingTransparency() {
        return RenderState.CRUMBLING_TRANSPARENCY;
    }

    public static RenderState.TransparencyState getAdditiveTransparency() {
        return RenderState.ADDITIVE_TRANSPARENCY;
    }

    public static RenderState.TransparencyState getLightningTransparency() {
        return RenderState.LIGHTNING_TRANSPARENCY;
    }

    public static RenderState.TransparencyState getTranslucentTransparency() {
        return RenderState.TRANSLUCENT_TRANSPARENCY;
    }

    public static RenderState.WriteMaskState getAllMask() {
        return RenderState.ALL_MASK;
    }

    public static RenderState.WriteMaskState getColorMask() {
        return RenderState.COLOR_MASK;
    }

    public static RenderState.FogState getNoFog() {
        return RenderState.NO_FOG;
    }

    public static RenderState.ShadeModelState getSmoothShadeModel() {
        return RenderState.SMOOTH_SHADE_MODEL;
    }

    public static RenderState.ShadeModelState getShadeModel() {
        return RenderState.SHADE_MODEL;
    }

    public static RenderState.AlphaState getOneTenthAlpha() {
        return RenderState.ONE_TENTH_ALPHA;
    }

    public static RenderState.DiffuseLightingState getEnableDiffuseLighting() {
        return RenderState.ENABLE_DIFFUSE_LIGHTING;
    }

    public static RenderState.LightmapState getEnableLightmap() {
        return RenderState.ENABLE_LIGHTMAP;
    }

    public static RenderState.OverlayState getEnableOverlayColor() {
        return RenderState.ENABLE_OVERLAY_COLOR;
    }
}
