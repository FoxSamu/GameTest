package net.shadew.gametest.blockitem.tileentity.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

import net.shadew.gametest.blockitem.block.TemplateBlock;
import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;

public class TemplateBlockRenderer extends TileEntityRenderer<TemplateBlockTileEntity> {
    public TemplateBlockRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(TemplateBlockTileEntity templateBlock, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, int otherLight) {
        if (Minecraft.getInstance().player.canUseCommandBlock() || Minecraft.getInstance().player.isSpectator()) {
            double xs = templateBlock.getWidth();
            double ys = templateBlock.getHeight();
            double zs = templateBlock.getDepth();

            if (xs >= 1 && ys >= 1 && zs >= 1) {
                double y1 = 1, y2 = y1 + ys;
                double x1, z1, x2, z2;

                switch (templateBlock.getBlockState().get(TemplateBlock.DIRECTION).getRotation()) {
                    case CLOCKWISE_90:
                        x1 = 1;
                        z1 = 0;
                        x2 = x1 - zs;
                        z2 = z1 + xs;
                        break;
                    case CLOCKWISE_180:
                        x1 = 1;
                        z1 = 1;
                        x2 = x1 - xs;
                        z2 = z1 - zs;
                        break;
                    case COUNTERCLOCKWISE_90:
                        x1 = 0;
                        z1 = 1;
                        x2 = x1 + zs;
                        z2 = z1 - xs;
                        break;
                    default:
                        x1 = 0;
                        z1 = 0;
                        x2 = x1 + xs;
                        z2 = z1 + zs;
                }

                IVertexBuilder builder = buffer.getBuffer(RenderType.getLines());
                WorldRenderer.drawBox(matrixStack, builder, x1, y1, z1, x2, y2, z2, .9f, .9f, .9f, 1, .5f, .5f, .5f);
            }
        }
    }

    @Override
    public boolean isGlobalRenderer(TemplateBlockTileEntity templateBlock) {
        return true;
    }
}
