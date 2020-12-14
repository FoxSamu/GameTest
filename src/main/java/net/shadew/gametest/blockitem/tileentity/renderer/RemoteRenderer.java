package net.shadew.gametest.blockitem.tileentity.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;

import net.shadew.gametest.blockitem.block.RemoteBlock;
import net.shadew.gametest.blockitem.block.props.RemoteState;
import net.shadew.gametest.blockitem.tileentity.RemoteTileEntity;
import net.shadew.gametest.util.RenderStates;

public class RemoteRenderer extends TileEntityRenderer<RemoteTileEntity> {
    private static final RenderType LAYER = getGlow(new ResourceLocation("gametest:textures/misc/test_remote_glow.png"));

    public RemoteRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(RemoteTileEntity remote, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int packedLight, int otherLight) {
        BlockState state = remote.getBlockState();
        boolean pressed = state.get(RemoteBlock.PRESSED);
        RemoteState remoteState = state.get(RemoteBlock.STATE);
        int col = remoteState.getColor();

        if (col != 0) {
            float glowHeight = (pressed ? 12 : 13) / 16f;

            stack.push();
            stack.translate(0.5, glowHeight, 0.5);
            stack.multiply(dispatcher.renderInfo.getRotation());

            Matrix4f mat = stack.peek().getModel();

            int a = col >>> 24 & 0xFF;
            int r = col >>> 16 & 0xFF;
            int g = col >>> 8 & 0xFF;
            int b = col & 0xFF;

            IVertexBuilder builder = buffer.getBuffer(LAYER);
            builder.vertex(mat, -0.5f, 0.5f, 0).color(r, g, b, a).texture(0, 0).endVertex();
            builder.vertex(mat, 0.5f, 0.5f, 0).color(r, g, b, a).texture(1, 0).endVertex();
            builder.vertex(mat, 0.5f, -0.5f, 0).color(r, g, b, a).texture(1, 1).endVertex();
            builder.vertex(mat, -0.5f, -0.5f, 0).color(r, g, b, a).texture(0, 1).endVertex();

            stack.pop();
        }
    }


    public static RenderType getGlow(ResourceLocation texture) {
        RenderType.State state = RenderType.State.builder()
                                                 .texture(new RenderState.TextureState(texture, false, false))
                                                 .transparency(RenderStates.getAdditiveTransparency())
                                                 .writeMaskState(RenderStates.getColorMask())
                                                 .fog(RenderStates.getNoFog())
                                                 .alpha(RenderStates.getOneTenthAlpha())
                                                 .build(false);

        return RenderType.of("gametest:test_remote_glow", DefaultVertexFormats.POSITION_COLOR_TEXTURE, 7, 131072, false, true, state);
    }
}
