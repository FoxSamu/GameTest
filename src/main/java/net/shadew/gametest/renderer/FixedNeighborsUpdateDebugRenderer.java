package net.shadew.gametest.renderer;

import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.client.renderer.debug.NeighborsUpdateDebugRenderer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class FixedNeighborsUpdateDebugRenderer extends NeighborsUpdateDebugRenderer implements DebugRenderer.IDebugRenderer {
   private final Minecraft minecraft;
   private final Map<Long, Map<BlockPos, Integer>> lastUpdate = Maps.newTreeMap(Ordering.natural().reverse());

   public FixedNeighborsUpdateDebugRenderer(Minecraft mc) {
      super(mc);
      this.minecraft = mc;
   }

   @Override
   public void addUpdate(long worldTime, BlockPos pos) {
      Map<BlockPos, Integer> updateMap = lastUpdate.computeIfAbsent(worldTime, k -> Maps.newHashMap());

      Integer time = updateMap.get(pos);
      if (time == null) {
         time = 0;
      }

      updateMap.put(pos, time + 1);
   }

   @Override
   public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, double camX, double camY, double camZ) {
      assert this.minecraft.world != null;
      long i = this.minecraft.world.getGameTime();
      Set<BlockPos> set = Sets.newHashSet();
      Map<BlockPos, Integer> map = Maps.newHashMap();
      IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getLines());
      Iterator<Map.Entry<Long, Map<BlockPos, Integer>>> iterator = this.lastUpdate.entrySet().iterator();

      while(iterator.hasNext()) {
         Map.Entry<Long, Map<BlockPos, Integer>> entry = iterator.next();
         Long olong = entry.getKey();
         Map<BlockPos, Integer> map1 = entry.getValue();
         long k = i - olong;
         if (k > 200L) {
            iterator.remove();
         } else {
            for(Map.Entry<BlockPos, Integer> entry1 : map1.entrySet()) {
               BlockPos blockpos = entry1.getKey();
               Integer integer = entry1.getValue();
               if (set.add(blockpos)) {
                  AxisAlignedBB axisalignedbb = new AxisAlignedBB(BlockPos.ZERO).grow(0.002D).shrink(0.0025D * (double)k).offset(blockpos.getX(), blockpos.getY(), blockpos.getZ()).offset(-camX, -camY, -camZ);
                  WorldRenderer.drawBox(matrixStackIn, ivertexbuilder, axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ, axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ, 1.0F, 1.0F, 1.0F, 1.0F);
                  map.put(blockpos, integer);
               }
            }
         }
      }

      for(Map.Entry<BlockPos, Integer> entry2 : map.entrySet()) {
         BlockPos blockpos1 = entry2.getKey();
         Integer integer1 = entry2.getValue();
         DebugRenderer.func_217731_a(String.valueOf(integer1), blockpos1.getX(), blockpos1.getY(), blockpos1.getZ(), 0xFFFFFFFF);
      }

   }
}
