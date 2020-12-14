package net.shadew.gametest.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.sun.javafx.geom.Vec3d;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraftforge.eventbus.api.Event;

public class DebugRenderEvent extends Event {
    private final DebugRenderer renderer;
    private final MatrixStack matrices;
    private final IRenderTypeBuffer.Impl buffer;
    private final Vec3d camera;

    public DebugRenderEvent(DebugRenderer renderer, MatrixStack matrices, IRenderTypeBuffer.Impl buffer, Vec3d camera) {
        this.renderer = renderer;
        this.matrices = matrices;
        this.buffer = buffer;
        this.camera = camera;
    }

    public DebugRenderer getRenderer() {
        return renderer;
    }

    public MatrixStack getMatrices() {
        return matrices;
    }

    public IRenderTypeBuffer.Impl getBuffer() {
        return buffer;
    }

    public Vec3d getCamera() {
        return camera;
    }
}
