package net.shadew.gametest.blockitem.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

import net.shadew.gametest.blockitem.block.props.RemoteState;
import net.shadew.gametest.blockitem.block.props.TestBlockState;
import net.shadew.gametest.blockitem.tileentity.RemoteTileEntity;

public class RemoteBlock extends Block {
    private static final VoxelShape SHAPE = makeCuboidShape(0, 0, 0, 16, 8, 16);

    public static final BooleanProperty PRESSED = BooleanProperty.create("pressed");
    public static final EnumProperty<RemoteState> STATE = EnumProperty.create("state", RemoteState.class);

    public RemoteBlock(String id) {
        super(Properties.create(Material.ROCK, MaterialColor.FOLIAGE).luminance(state -> 15).nonOpaque()
                        .sound(SoundType.NETHERITE).noDrops().hardnessAndResistance(-1, 3600000));

        setRegistryName(id);

        setDefaultState(getBlockState(RemoteState.INACTIVE).with(PRESSED, false));
    }

    public BlockState getBlockState(RemoteState state) {
        return stateContainer.getBaseState().with(STATE, state);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(STATE, PRESSED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext ctx) {
        return SHAPE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new RemoteTileEntity();
    }
}
