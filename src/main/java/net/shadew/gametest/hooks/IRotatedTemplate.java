package net.shadew.gametest.hooks;

import net.minecraft.block.Block;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

@FunctionalInterface
public interface IRotatedTemplate {
    void takeBlocksFromWorldRotated(World world, BlockPos pos, BlockPos size, Rotation rot, boolean entities, @Nullable Block ignore);
}
