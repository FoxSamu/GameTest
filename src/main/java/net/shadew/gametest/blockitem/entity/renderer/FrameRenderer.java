package net.shadew.gametest.blockitem.entity.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.ITextComponent;

import net.shadew.gametest.blockitem.entity.FrameEntity;

public class FrameRenderer extends EntityRenderer<FrameEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("gametest:textures/block/test_marker_stripes.png");
    public static final RenderType LAYER = RenderType.getEntityTranslucentCull(TEXTURE);

    protected FrameRenderer(EntityRendererManager rendererManager) {
        super(rendererManager);
    }

    private static final float OFF = 0.501f;

    @Override
    public void render(FrameEntity entity, float yaw, float partialTicks, MatrixStack matrix, IRenderTypeBuffer renderBuffer, int light) {
        Direction mouseOverFace = null;

        Entity rvEntity = Minecraft.getInstance().getRenderViewEntity();
        if (rvEntity instanceof PlayerEntity && ((PlayerEntity) rvEntity).canUseCommandBlock()) {
            RayTraceResult objectMouseOver = Minecraft.getInstance().objectMouseOver;
            if (objectMouseOver instanceof EntityRayTraceResult) {
                EntityRayTraceResult entityMouseOver = (EntityRayTraceResult) objectMouseOver;

                if (entityMouseOver.getEntity() == entity) {
                    BlockRayTraceResult target = entity.target(rvEntity);
                    if (target != null) {
                        mouseOverFace = target.getFace();
                    }
                }
            }
        }

        Direction dir = mouseOverFace;

        Matrix4f mat = matrix.peek().getModel();
        Matrix3f nmat = matrix.peek().getNormal();

        IVertexBuilder builder = renderBuffer.getBuffer(LAYER);

        for (Direction faceDir : Direction.values())
            face(
                faceDir,
                (x, y, z, u, v, nx, ny, nz) ->
                    builder.vertex(mat, x * OFF, y * OFF, z * OFF)
                           .color(1f, 1f, 1f, 1f)
                           .texture(u, v)
                           .overlay(OverlayTexture.DEFAULT_UV)
                           .light(light)
                           .normal(nmat, nx, ny, nz)
                           .endVertex()
            );

        if (dir != null) {
            int dx = dir.getXOffset(), dy = dir.getYOffset(), dz = dir.getZOffset();

            matrix.push();
            matrix.translate(dx * 0.01, dy * 0.01, dz * 0.01);

            IVertexBuilder selectionBuilder = renderBuffer.getBuffer(RenderType.getLines());
            for (int i = 0, j = 1; i < 4; i = j++) {
                int s1 = n(i & 1), t1 = n(i >>> 1 & 1);
                int s2 = n(j & 1), t2 = n(j >>> 1 & 1);
                if (t1 > 0) s1 = -s1;
                if (t2 > 0) s2 = -s2;

                int x1 = dz != 0 ? s1 : dx == 0 ? s1 : dx;
                int y1 = dy == 0 ? t1 : dy;
                int z1 = dx != 0 ? s1 : dz == 0 ? t1 : dz;

                int x2 = dz != 0 ? s2 : dx == 0 ? s2 : dx;
                int y2 = dy == 0 ? t2 : dy;
                int z2 = dx != 0 ? s2 : dz == 0 ? t2 : dz;

                selectionBuilder.vertex(mat, x1 * OFF, y1 * OFF, z1 * OFF).color(255, 255, 255, 120).endVertex();
                selectionBuilder.vertex(mat, x2 * OFF, y2 * OFF, z2 * OFF).color(255, 255, 255, 120).endVertex();
            }

            matrix.pop();
        }

        if (!entity.getFrameName().isEmpty() && mouseOverFace != null)
            renderText(entity, entity.getFrameName(), matrix, renderBuffer, light, mouseOverFace);
    }

    private static int n(int n) {
        return n * 2 - 1;
    }

    private void face(Direction dir, IVertexConsumer consumer) {
        switch (dir) {
            case WEST:
                consumer.accept(-1, -1, -1, 0, 1, -1, 0, 0);
                consumer.accept(-1, -1, 1, 1, 1, -1, 0, 0);
                consumer.accept(-1, 1, 1, 1, 0, -1, 0, 0);
                consumer.accept(-1, 1, -1, 0, 0, -1, 0, 0);
                break;
            case EAST:
                consumer.accept(1, 1, -1, 1, 0, 1, 0, 0);
                consumer.accept(1, 1, 1, 0, 0, 1, 0, 0);
                consumer.accept(1, -1, 1, 0, 1, 1, 0, 0);
                consumer.accept(1, -1, -1, 1, 1, 1, 0, 0);
                break;
            case NORTH:
                consumer.accept(-1, 1, -1, 1, 0, 0, 0, -1);
                consumer.accept(1, 1, -1, 0, 0, 0, 0, -1);
                consumer.accept(1, -1, -1, 0, 1, 0, 0, -1);
                consumer.accept(-1, -1, -1, 1, 1, 0, 0, -1);
                break;
            case SOUTH:
                consumer.accept(-1, -1, 1, 0, 1, 0, 0, 1);
                consumer.accept(1, -1, 1, 1, 1, 0, 0, 1);
                consumer.accept(1, 1, 1, 1, 0, 0, 0, 1);
                consumer.accept(-1, 1, 1, 0, 0, 0, 0, 1);
                break;
            case DOWN:
                consumer.accept(-1, -1, -1, 1, 1, 0, -1, 0);
                consumer.accept(1, -1, -1, 0, 1, 0, -1, 0);
                consumer.accept(1, -1, 1, 0, 0, 0, -1, 0);
                consumer.accept(-1, -1, 1, 1, 0, 0, -1, 0);
                break;
            case UP:
                consumer.accept(-1, 1, 1, 1, 1, 0, 1, 0);
                consumer.accept(1, 1, 1, 0, 1, 0, 1, 0);
                consumer.accept(1, 1, -1, 0, 0, 0, 1, 0);
                consumer.accept(-1, 1, -1, 1, 0, 0, 1, 0);
                break;
        }
    }

    @FunctionalInterface
    private interface IVertexConsumer {
        void accept(int x, int y, int z, int u, int v, int nx, int ny, int nz);
    }

    @Override
    public ResourceLocation getEntityTexture(FrameEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void renderLabelIfPresent(FrameEntity frame, ITextComponent text, MatrixStack matrix, IRenderTypeBuffer buf, int light) {
        double sqDist = renderManager.getSquaredDistanceToCamera(frame);
        if (sqDist <= 4096) {
            matrix.push();
            matrix.translate(0, 0, 0);
            matrix.multiply(renderManager.getRotation());
            matrix.scale(-0.025f, -0.025f, 0.025f);

            Matrix4f mat = matrix.peek().getModel();

            FontRenderer font = getFontRendererFromRenderManager();
            float width = -font.getWidth(text) / 2f;
            font.draw(text, width, 0, 0xFFFFFFFF, false, mat, buf, true, 0, light);

            matrix.pop();
        }
    }

    protected void renderText(FrameEntity frame, String text, MatrixStack matrix, IRenderTypeBuffer buf, int light, Direction dir) {
        double sqDist = renderManager.getSquaredDistanceToCamera(frame);
        if (sqDist <= 4096) {
            matrix.push();
            matrix.translate(0, 0.7, 0);
            matrix.multiply(renderManager.getRotation());
            matrix.scale(-0.015f, -0.015f, 0.015f);

            Matrix4f mat = matrix.peek().getModel();

            FontRenderer font = getFontRendererFromRenderManager();
            float width = -font.getStringWidth(text) / 2f;
            font.draw(text, width, 0, 0xFFFFFFFF, false, mat, buf, false, 0, light);

            matrix.pop();
        }
    }
}
