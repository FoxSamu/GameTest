package net.shadew.gametest.framework.platforms;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

@FunctionalInterface
public interface IPlatform {
    IPlatform EMPTY = (pos, size) -> Blocks.AIR.getDefaultState();

    BlockState getStateAt(BlockPos pos, BlockPos size);

    static IPlatform platform(BlockState block) {
        return (pos, size) -> pos.getY() == 0 ? block : Blocks.AIR.getDefaultState();
    }

    static IPlatform ceiled(BlockState floor, BlockState ceil) {
        return (pos, size) -> {
            if(pos.getY() == 0) return floor;
            if(pos.getY() == size.getY() - 1) return ceil;
            return Blocks.AIR.getDefaultState();
        };
    }

    static IPlatform ceiled(BlockState block) {
        return ceiled(block, block);
    }

    static IPlatform box(BlockState floor, BlockState ceil, BlockState wall, boolean door) {
        return (pos, size) -> {
            if(door && pos.getX() == 1 && pos.getZ() == 0) {
                if(pos.getY() == 1) {
                    return Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH);
                }
                if(pos.getY() == 2) {
                    return Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH);
                }
            }
            if(pos.getY() == 0) return floor;
            if(pos.getY() == size.getY() - 1) return ceil;
            if(pos.getX() == 0 || pos.getZ() == 0) return wall;
            if(pos.getX() == size.getX() - 1 || pos.getZ() == size.getZ() - 1) return wall;
            return Blocks.AIR.getDefaultState();
        };
    }

    static IPlatform box(BlockState block, boolean door) {
        return box(block, block, block, door);
    }

    static IPlatform pool(BlockState floor, BlockState wall, BlockState inner, int depth, int wallExtra) {
        return (pos, size) -> {
            if(pos.getY() == 0) return floor;
            if(pos.getY() <= depth + wallExtra) {
                if(pos.getX() == 0 || pos.getZ() == 0) return wall;
                if(pos.getX() == size.getX() - 1 || pos.getZ() == size.getZ() - 1) return wall;
                if(pos.getY() <= depth) return inner;
            }
            return Blocks.AIR.getDefaultState();
        };
    }

    static IPlatform ceiledPool(BlockState floor, BlockState ceil, BlockState wall, BlockState inner, int depth, int wallExtra) {
        return (pos, size) -> {
            if(pos.getY() == 0) return floor;
            if(pos.getY() == size.getY() - 1) return ceil;
            if(pos.getY() <= depth + wallExtra) {
                if(pos.getX() == 0 || pos.getZ() == 0) return wall;
                if(pos.getX() == size.getX() - 1 || pos.getZ() == size.getZ() - 1) return wall;
                if(pos.getY() <= depth) return inner;
            }
            return Blocks.AIR.getDefaultState();
        };
    }

    static IPlatform boxedPool(BlockState floor, BlockState ceil, BlockState wall, BlockState inner, int depth, boolean door) {
        return (pos, size) -> {
            if(door && pos.getX() == 1 && pos.getZ() == 0) {
                int doorHeight = 1 + depth;
                if(doorHeight + 1 >= size.getY() - 1) {
                    doorHeight = size.getY() - 3;
                }
                if(pos.getY() == doorHeight) {
                    return Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.LOWER).with(DoorBlock.FACING, Direction.SOUTH);
                }
                if(pos.getY() == doorHeight + 1) {
                    return Blocks.OAK_DOOR.getDefaultState().with(DoorBlock.HALF, DoubleBlockHalf.UPPER).with(DoorBlock.FACING, Direction.SOUTH);
                }
            }
            if(pos.getY() == 0) return floor;
            if(pos.getY() == size.getY() - 1) return ceil;
            if(pos.getX() == 0 || pos.getZ() == 0) return wall;
            if(pos.getX() == size.getX() - 1 || pos.getZ() == size.getZ() - 1) return wall;
            if(pos.getY() <= depth) return inner;
            return Blocks.AIR.getDefaultState();
        };
    }
}
