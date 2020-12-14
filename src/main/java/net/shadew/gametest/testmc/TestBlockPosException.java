package net.shadew.gametest.testmc;

import net.minecraft.util.math.BlockPos;

public class TestBlockPosException extends TestRuntimeException {
   private final BlockPos pos;
   private final BlockPos relativePos;
   private final long time;

   public TestBlockPosException(String message, BlockPos pos, BlockPos relativePos, long time) {
      super(message);
      this.pos = pos;
      this.relativePos = relativePos;
      this.time = time;
   }

   @Override
   public String getMessage() {
      String coords = pos.getX() + "," + pos.getY() + "," + pos.getZ() + " (relative: " + relativePos.getX() + "," + relativePos.getY() + "," + relativePos.getZ() + ")";
      return super.getMessage() + " at " + coords + " (t=" + this.time + ")";
   }

   public String getMarkerMessage() {
      return super.getMessage() + " here";
   }

   public BlockPos getPos() {
      return pos;
   }
}
