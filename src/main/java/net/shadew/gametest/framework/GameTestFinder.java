package net.shadew.gametest.framework;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.server.ServerWorld;

import java.util.*;

import net.shadew.gametest.blockitem.tileentity.TestBlockTileEntity;
import net.shadew.gametest.util.Utils;

public final class GameTestFinder {
    public static Collection<TestBlockTileEntity> findTestBlocks(ServerWorld world, MutableBoundingBox box) {
        int nx = box.minX >> 4;
        int nz = box.minZ >> 4;
        int px = (box.maxX + 1 >> 4) + 1;
        int pz = (box.maxZ + 1 >> 4) + 1;

        List<TestBlockTileEntity> out = new ArrayList<>();

        for (int cx = nx; cx <= px; cx++) {
            for (int cz = nz; cz <= pz; cz++) {
                ChunkPos cpos = new ChunkPos(cx, cz);
                if (world.getChunkProvider().isChunkLoaded(cpos)) {
                    Map<BlockPos, TileEntity> tileEntityMap = world.getChunk(cx, cz).getTileEntityMap();
                    for (TileEntity te : tileEntityMap.values()) {
                        if (te instanceof TestBlockTileEntity && box.isVecInside(te.getPos())) {
                            out.add((TestBlockTileEntity) te);
                        }
                    }
                }
            }
        }

        return out;
    }

    public static Collection<TestBlockTileEntity> findTestBlocks(ServerWorld world, BlockPos origin, int radius) {
        return findTestBlocks(world, Utils.areaAround(origin, radius));
    }

    public static Optional<TestBlockTileEntity> findNearestTestBlock(ServerWorld world, BlockPos pos, int searchRadius) {
        return findTestBlocks(world, pos, searchRadius)
                   .stream()
                   .min(Comparator.comparingInt(te -> te.getPos().manhattanDistance(pos)));
    }

    public static Optional<TestBlockTileEntity> findTestBlockContaining(ServerWorld world, BlockPos pos, int searchRadius) {
        AxisAlignedBB posAabb = new AxisAlignedBB(pos);
        return findTestBlocks(world, pos, searchRadius)
                    .stream()
                    .filter(te -> te.getTemplateBlock() != null)
                    .filter(te -> te.getTemplateBlock().getBox().grow(2.5).intersects(posAabb))
                    .min(Comparator.comparingInt(te -> te.getPos().manhattanDistance(pos)));
    }

    public static Optional<TestBlockTileEntity> findTestBlockContaining(ServerWorld world, BlockPos pos) {
        return findTestBlockContaining(world, pos, 55);
    }
}
