package net.shadew.gametest.blockitem.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import net.shadew.gametest.blockitem.block.props.TestBlockState;
import net.shadew.gametest.blockitem.tileentity.TestBlockTileEntity;

public class TestBlock extends Block {
    public static final EnumProperty<TestBlockState> STATE = EnumProperty.create("state", TestBlockState.class);

    public TestBlock(String id) {
        super(Properties.create(Material.ROCK, MaterialColor.FOLIAGE).luminance(state -> 15)
                        .sound(SoundType.NETHERITE).noDrops().hardnessAndResistance(-1, 3600000));

        setRegistryName(id);

        setDefaultState(getBlockState(TestBlockState.OFF));
    }

    public BlockState getBlockState(TestBlockState state) {
        return stateContainer.getBaseState().with(STATE, state);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(STATE);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TestBlockTileEntity();
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rtr) {
        if(world.isRemote) return ActionResultType.SUCCESS;
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof TestBlockTileEntity) {
            ((TestBlockTileEntity) te).sendTestInfo(player);
        }
        return ActionResultType.SUCCESS;
    }
}
