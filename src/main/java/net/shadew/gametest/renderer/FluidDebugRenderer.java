package net.shadew.gametest.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class FluidDebugRenderer implements DebugRenderer.IDebugRenderer {
    private final Minecraft minecraft;

    public FluidDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void render(MatrixStack matrices, IRenderTypeBuffer buffer, double camX, double camY, double camZ) {
        assert minecraft.player != null;
        BlockPos center = minecraft.player.getBlockPos();
        IWorldReader world = minecraft.player.world;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(0.0F, 1.0F, 0.0F, 0.75F);
        RenderSystem.disableTexture();
        RenderSystem.lineWidth(6.0F);

        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-10, -10, -10), center.add(10, 10, 10))) {
            FluidState fluid = world.getFluidState(pos);
            if(fluid.isEmpty()) continue;
            double height = (float) pos.getY() + fluid.getActualHeight(world, pos);
            DebugRenderer.func_217730_a(new AxisAlignedBB(
                pos.getX() + 0.01,
                pos.getY() + 0.01,
                pos.getZ() + 0.01,
                pos.getX() + 0.99,
                height,
                pos.getZ() + 0.99
            ).offset(-camX, -camY, -camZ), fluid.isSource() ? 1 : 0, 1, fluid.isSource() ? 0 : 1, 0.2F);
        }

        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-10, -10, -10), center.add(10, 10, 10))) {
            FluidState fluid = world.getFluidState(pos);
            if(fluid.isEmpty()) continue;
            DebugRenderer.func_217732_a(
                String.valueOf(fluid.getLevel()),
                pos.getX() + 0.5,
                pos.getY() + fluid.getActualHeight(world, pos),
                pos.getZ() + 0.5,
                0xff000000
            );
        }

        RenderSystem.disableDepthTest();

        for (BlockPos pos : BlockPos.getAllInBoxMutable(center.add(-10, -10, -10), center.add(10, 10, 10))) {
            FluidState fluid = world.getFluidState(pos);
            if(fluid.isEmpty()) continue;
            Vector3d flow = fluid.getFlow(world, pos);

            float centerX = pos.getX() + 0.5f - (float) camX;
            float centerY = pos.getY() + fluid.getActualHeight(world, pos) - (float) camY;
            float centerZ = pos.getZ() + 0.5f - (float) camZ;
            float offsetX = centerX + (float) flow.x * 0.3f;
            float offsetY = centerY + (float) flow.y * 0.3f;
            float offsetZ = centerZ + (float) flow.z * 0.3f;

            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buff = tess.getBuffer();

            buff.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            buff.vertex(centerX, centerY, centerZ).color(0, 255, 255, 255).endVertex();
            buff.vertex(offsetX, offsetY, offsetZ).color(0, 255, 255, 255).endVertex();
            tess.draw();
        }

        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }
}
