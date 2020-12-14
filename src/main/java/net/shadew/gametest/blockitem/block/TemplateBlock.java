package net.shadew.gametest.blockitem.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.blockitem.block.props.DiagonalDirection;
import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;

public class TemplateBlock extends Block {
    public static final EnumProperty<DiagonalDirection> DIRECTION = EnumProperty.create("direction", DiagonalDirection.class);

    public TemplateBlock(String id) {
        super(Properties.create(Material.ROCK, MaterialColor.FOLIAGE)
                        .sound(SoundType.NETHERITE).noDrops().hardnessAndResistance(-1, 3600000));

        setRegistryName(id);

        setDefaultState(getRotatedState(DiagonalDirection.NW));
    }

    public BlockState getRotatedState(DiagonalDirection dir) {
        return stateContainer.getBaseState().with(DIRECTION, dir);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(DIRECTION);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext ctx) {
        DiagonalDirection dir = DiagonalDirection.fromEntityFacing(ctx.getPlayer());
        return getDefaultState().with(DIRECTION, dir);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rtr) {
        if(rtr.getFace() == Direction.UP) {
            world.setBlockState(pos, state.cycle(DIRECTION));
            if(world.isRemote)
                world.playEvent(null, Constants.WorldEvents.IRON_TRAPDOOR_OPEN_SOUND, pos, 0);
            else
                world.playEvent(player, Constants.WorldEvents.IRON_TRAPDOOR_OPEN_SOUND, pos, 0);
        } else {
            if(world.isRemote) {
                GameTestMod.getScreenProxy().openTemplateBlock(player, pos);
            }
        }
        return ActionResultType.SUCCESS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TemplateBlockTileEntity();
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.with(DIRECTION, state.get(DIRECTION).mirror(mirror));
    }

    @Override
    public BlockState rotate(BlockState state, IWorld world, BlockPos pos, Rotation rotation) {
        return state.with(DIRECTION, state.get(DIRECTION).rotate(rotation));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.with(DIRECTION, state.get(DIRECTION).rotate(rotation));
    }
}
