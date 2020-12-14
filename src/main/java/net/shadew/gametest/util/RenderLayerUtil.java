package net.shadew.gametest.util;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.Util;

import net.shadew.gametest.blockitem.tileentity.renderer.TestBlockRenderer;

public final class RenderLayerUtil {
    private static IRenderTypeBuffer.Impl buf;

    public static void init() {
        buf = IRenderTypeBuffer.immediate(Util.make(new Object2ObjectLinkedOpenHashMap<>(), map -> {
            map.put(TestBlockRenderer.BEAM_LAYER, new BufferBuilder(TestBlockRenderer.BEAM_LAYER.getExpectedBufferSize()));
            map.put(TestBlockRenderer.EMPTY_MARKER_LAYER, new BufferBuilder(TestBlockRenderer.EMPTY_MARKER_LAYER.getExpectedBufferSize()));
            map.put(TestBlockRenderer.ERROR_MARKER_LAYER, new BufferBuilder(TestBlockRenderer.ERROR_MARKER_LAYER.getExpectedBufferSize()));
            map.put(TestBlockRenderer.OK_MARKER_LAYER, new BufferBuilder(TestBlockRenderer.OK_MARKER_LAYER.getExpectedBufferSize()));
            map.put(TestBlockRenderer.WORKING_MARKER_LAYER, new BufferBuilder(TestBlockRenderer.WORKING_MARKER_LAYER.getExpectedBufferSize()));
        }), new BufferBuilder(256));
    }

    public static IRenderTypeBuffer getBuf() {
        return buf;
    }

    public static void renderPost() {
        buf.draw();
    }
}
