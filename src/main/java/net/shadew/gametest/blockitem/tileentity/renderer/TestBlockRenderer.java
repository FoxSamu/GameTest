package net.shadew.gametest.blockitem.tileentity.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

import net.shadew.gametest.blockitem.block.props.TestBlockState;
import net.shadew.gametest.blockitem.tileentity.TestBlockTileEntity;
import net.shadew.gametest.framework.api.Marker;
import net.shadew.gametest.util.RenderLayerUtil;
import net.shadew.gametest.util.RenderStates;

public class TestBlockRenderer extends TileEntityRenderer<TestBlockTileEntity> {
    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("gametest:textures/misc/test_block_beam.png");
    public static final RenderType BEAM_LAYER = getBeam(BEAM_TEXTURE, false);

    private static final ResourceLocation EMPTY_MARKER_TEXTURE = new ResourceLocation("gametest:textures/block/test_marker.png");
    public static final RenderType EMPTY_MARKER_LAYER = getEntityTranslucentCull("empty", EMPTY_MARKER_TEXTURE);

    private static final ResourceLocation ERROR_MARKER_TEXTURE = new ResourceLocation("gametest:textures/block/test_marker_error.png");
    public static final RenderType ERROR_MARKER_LAYER = getEntityTranslucentCull("error", ERROR_MARKER_TEXTURE);

    private static final ResourceLocation OK_MARKER_TEXTURE = new ResourceLocation("gametest:textures/block/test_marker_ok.png");
    public static final RenderType OK_MARKER_LAYER = getEntityTranslucentCull("ok", OK_MARKER_TEXTURE);

    private static final ResourceLocation WORKING_MARKER_TEXTURE = new ResourceLocation("gametest:textures/block/test_marker_working.png");
    public static final RenderType WORKING_MARKER_LAYER = getEntityTranslucentCull("working", WORKING_MARKER_TEXTURE);

    public TestBlockRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TestBlockTileEntity testBlock, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int packedLight, int otherLight) {
        TestBlockState state = testBlock.getState();
        buffer = RenderLayerUtil.getBuf();

        if (state.getColor() != 0) {
            float texOffset = (float) (System.nanoTime() / 5000000000d % 1);
            IVertexBuilder builder = buffer.getBuffer(BEAM_LAYER);

            stack.push();
            stack.translate(0.5, 0.5, 0.5);

            Matrix4f mat = stack.peek().getModel();
            Matrix3f nmat = stack.peek().getNormal();

            int argb = state.getColor();
            int a = argb >>> 24 & 0xFF;
            int r = argb >>> 16 & 0xFF;
            int g = argb >>> 8 & 0xFF;
            int b = argb & 0xFF;

            BlockPos ipos = testBlock.getPos();
            Vector3d pos = new Vector3d(ipos.getX() + 0.5, ipos.getY() + 0.5, ipos.getZ() + 0.5);
            Vector3d cameraPos = Minecraft.getInstance().renderViewEntity.getEyePosition(partialTicks);
            Vector3d distPos = new Vector3d(cameraPos.x, pos.y, cameraPos.z);
            double dist = distPos.squareDistanceTo(pos);
            boolean thick = dist > 64 * 64;
            float width = thick ? 1 : 0.5f;

            for (Direction faceDir : Direction.Plane.HORIZONTAL) {
                for (int i = 0; i < 10; i++) {
                    float h = i * 200 + 0.5f;

                    face(
                        faceDir,
                        (x, y, z, u, v, nx, ny, nz) ->
                            builder.vertex(mat, x * width / 2f, y < 0 ? h : h + 200f, z * width / 2f)
                                   .color(r, g, b, a)
                                   .texture(u * width, (v == 0 ? 0f : 200f) + texOffset)
                                   .light(0x00F000F0)
                                   .normal(nmat, nx, ny, nz)
                                   .endVertex()
                    );
                }
            }

            stack.pop();

            stack.push();
            for (TestBlockTileEntity.MarkerHolder marker : testBlock.getMarkers()) {
                BlockPos markerPos = marker.getPos();
                BlockPos local = markerPos.subtract(ipos);
                stack.translate(local.getX(), local.getY(), local.getZ());
                stack.translate(0.5f, 0.5f, 0.5f);

                int light = 0x00F000F0;

                renderMarker(marker.getType(), marker.getMessage(), stack, buffer, light);
            }

            stack.pop();
        }
    }

    private static final float OFF = 0.502f;

    private void renderMarker(Marker type, String message, MatrixStack matrix, IRenderTypeBuffer renderBuffer, int light) {
        Matrix4f mat = matrix.peek().getModel();
        Matrix3f nmat = matrix.peek().getNormal();

        IVertexBuilder builder = renderBuffer.getBuffer(getLayer(type));

        float v1 = 0, v2 = 1;

        if(type == Marker.WORKING) {
            long d = System.nanoTime() / 500000000L & 3;
            v1 = d * 0.25f;
            v2 = v1 + 0.25f;
        }

        float va = v1, vb = v2; // Reassign because of lambda

        for (Direction faceDir : Direction.values())
            face(
                faceDir,
                (x, y, z, u, v, nx, ny, nz) ->
                    builder.vertex(mat, x * OFF, y * OFF, z * OFF)
                           .color(1f, 1f, 1f, 1f)
                           .texture(u, v == 0 ? va : vb)
                           .overlay(OverlayTexture.DEFAULT_UV)
                           .light(light)
                           .normal(nmat, nx, ny, nz)
                           .endVertex()
            );

        if (!message.isEmpty())
            renderText(message, matrix, renderBuffer, 0x00F000F0);
    }

    protected void renderText(String text, MatrixStack matrix, IRenderTypeBuffer buf, int light) {
        matrix.push();
        matrix.translate(0, 0.7, 0);
        matrix.multiply(dispatcher.renderInfo.getRotation());
        matrix.scale(-0.015f, -0.015f, 0.015f);

        Matrix4f mat = matrix.peek().getModel();

        FontRenderer font = dispatcher.fontRenderer;
        float width = -font.getStringWidth(text) / 2f;
        font.draw(text, width, 0, 0xFFFFFFFF, false, mat, buf, false, 0, light);

        matrix.pop();
    }

    private RenderType getLayer(Marker type) {
        switch (type) {
            default:
            case EMPTY: return EMPTY_MARKER_LAYER;
            case ERROR: return ERROR_MARKER_LAYER;
            case OK: return OK_MARKER_LAYER;
            case WORKING: return WORKING_MARKER_LAYER;
        }
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

    public static RenderType getBeam(ResourceLocation texture, boolean translucent) {
        RenderType.State state = RenderType.State.builder()
                                                 .texture(new RenderState.TextureState(texture, false, false))
                                                 .transparency(translucent ? RenderStates.getTranslucentTransparency() : RenderStates.getNoTransparency())
                                                 .writeMaskState(translucent ? RenderStates.getColorMask() : RenderStates.getAllMask())
                                                 .fog(RenderStates.getNoFog())
                                                 .shadeModel(RenderStates.getSmoothShadeModel())
                                                 .alpha(RenderStates.getOneTenthAlpha())
                                                 .build(false);

        return RenderType.of("gametest:test_block_beam", DefaultVertexFormats.BLOCK, 7, 131072, false, true, state);
    }

    public static RenderType getEntityTranslucentCull(String id, ResourceLocation texture) {
        RenderType.State state = RenderType.State.builder()
                                                 .texture(new RenderState.TextureState(texture, false, false))
                                                 .transparency(RenderStates.getTranslucentTransparency())
                                                 .diffuseLighting(RenderStates.getEnableDiffuseLighting())
                                                 .alpha(RenderStates.getOneTenthAlpha())
                                                 .lightmap(RenderStates.getEnableLightmap())
                                                 .overlay(RenderStates.getEnableOverlayColor())
                                                 .build(true);
        return RenderType.of("gametest:frame_" + id, DefaultVertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, 7, 256, true, true, state);
    }

    @Override
    public boolean isGlobalRenderer(TestBlockTileEntity testBlock) {
        return testBlock.getState().getColor() != 0;
    }
}
