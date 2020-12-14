package net.shadew.gametest.framework;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;

import net.shadew.gametest.blockitem.block.GameTestBlocks;
import net.shadew.gametest.blockitem.block.TemplateBlock;
import net.shadew.gametest.blockitem.block.props.DiagonalDirection;
import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.blockitem.tileentity.TestBlockTileEntity;
import net.shadew.gametest.framework.platforms.IPlatform;

public final class GameTestBuilder {
    public static TestBuildResult buildTest(ServerWorld world, BlockPos pos, DiagonalDirection direction, ResourceLocation location) {
        BlockPos off1 = pos.offset(direction.getXAxisDir(), 2).offset(direction.getZAxisDir(), 2);
        BlockPos off2 = pos.offset(direction.getXAxisDir(), 3).offset(direction.getZAxisDir(), 3);

        world.setBlockState(off1, GameTestBlocks.TEST_BLOCK.getDefaultState());
        world.setBlockState(off2, GameTestBlocks.TEST_TEMPLATE_BLOCK.getDefaultState().with(TemplateBlock.DIRECTION, direction));

        TileEntity testBlockTe = world.getTileEntity(off1);
        TileEntity templateBlockTe = world.getTileEntity(off2);

        assert testBlockTe instanceof TestBlockTileEntity;
        assert templateBlockTe instanceof TemplateBlockTileEntity;

        TestBlockTileEntity testBlock = (TestBlockTileEntity) testBlockTe;
        TemplateBlockTileEntity templateBlock = (TemplateBlockTileEntity) templateBlockTe;

        templateBlock.setName(location);
        int load = templateBlock.load();
        if (load != 0) {
            return new TestBuildResult(location, new MutableBoundingBox(), off1, off2, testBlock, templateBlock, false);
        }

        MutableBoundingBox box = templateBlock.getMBox();
        box.minX -= 3;
        box.minY -= 3;
        box.minZ -= 3;
        box.maxX += 3;
        box.maxY += 3;
        box.maxZ += 3;

        int h = pos.getY() - 1;
        BlockPos.stream(box).forEach(p -> {
            if(p.equals(off1) || p.equals(off2)) return;
            int y = p.getY();
            BlockState state = Blocks.AIR.getDefaultState();
            Biome biome = world.getBiome(p);

            if(y < h) state = biome.getGenerationSettings().getSurfaceConfig().getUnder();
            if(y == h) state = biome.getGenerationSettings().getSurfaceConfig().getTop();

            world.setBlockState(p, state);
        });

        AxisAlignedBB aabb = templateBlock.getBox().grow(3);
        world.getEntitiesInAABBexcluding(null, aabb, e -> !(e instanceof PlayerEntity)).forEach(Entity::remove);
        world.getPendingBlockTicks().getPending(box, true, false);
        world.clearUpdatesInArea(box);

        if(!templateBlock.place()) {
            return new TestBuildResult(location, box, off1, off2, testBlock, templateBlock, false);
        }

        return new TestBuildResult(location, box, off1, off2, testBlock, templateBlock, true);
    }

    public static TestBuildResult buildNewTest(ServerWorld world, BlockPos pos, DiagonalDirection direction, ResourceLocation location, int wdt, int hgt, int dpt) {
        BlockPos off1 = pos.offset(direction.getXAxisDir(), 2).offset(direction.getZAxisDir(), 2);
        BlockPos off2 = pos.offset(direction.getXAxisDir(), 3).offset(direction.getZAxisDir(), 3);

        world.setBlockState(off1, GameTestBlocks.TEST_BLOCK.getDefaultState());
        world.setBlockState(off2, GameTestBlocks.TEST_TEMPLATE_BLOCK.getDefaultState().with(TemplateBlock.DIRECTION, direction));

        TileEntity testBlockTe = world.getTileEntity(off1);
        TileEntity templateBlockTe = world.getTileEntity(off2);

        assert testBlockTe instanceof TestBlockTileEntity;
        assert templateBlockTe instanceof TemplateBlockTileEntity;

        TestBlockTileEntity testBlock = (TestBlockTileEntity) testBlockTe;
        TemplateBlockTileEntity templateBlock = (TemplateBlockTileEntity) templateBlockTe;

        templateBlock.setName(location);
        templateBlock.setWidth(wdt);
        templateBlock.setHeight(hgt);
        templateBlock.setDepth(dpt);

        MutableBoundingBox box = templateBlock.getMBox();
        box.minX -= 3;
        box.minY -= 3;
        box.minZ -= 3;
        box.maxX += 3;
        box.maxY += 3;
        box.maxZ += 3;

        int h = pos.getY() - 1;
        BlockPos.stream(box).forEach(p -> {
            if(p.equals(off1) || p.equals(off2)) return;
            int y = p.getY();
            BlockState state = Blocks.AIR.getDefaultState();
            Biome biome = world.getBiome(p);

            if(y < h) state = biome.getGenerationSettings().getSurfaceConfig().getUnder();
            if(y == h) state = biome.getGenerationSettings().getSurfaceConfig().getTop();

            world.setBlockState(p, state);
        });

        AxisAlignedBB aabb = templateBlock.getBox().grow(3);
        world.getEntitiesInAABBexcluding(null, aabb, e -> !(e instanceof PlayerEntity)).forEach(Entity::remove);
        world.getPendingBlockTicks().getPending(box, true, false);
        world.clearUpdatesInArea(box);

        templateBlock.createPlatform(IPlatform.platform(Blocks.POLISHED_ANDESITE.getDefaultState()));

        return new TestBuildResult(location, box, off1, off2, testBlock, templateBlock, true);
    }

    public static class TestBuildResult {
        public final ResourceLocation id;
        public final MutableBoundingBox box;
        public final BlockPos testBlockPos;
        public final BlockPos templateBlockPos;
        public final TestBlockTileEntity testBlock;
        public final TemplateBlockTileEntity templateBlock;
        public final boolean loaded;

        public TestBuildResult(ResourceLocation id, MutableBoundingBox box, BlockPos testBlockPos, BlockPos templateBlockPos, TestBlockTileEntity testBlock, TemplateBlockTileEntity templateBlock, boolean loaded) {
            this.id = id;
            this.box = box;
            this.testBlockPos = testBlockPos;
            this.templateBlockPos = templateBlockPos;
            this.testBlock = testBlock;
            this.templateBlock = templateBlock;
            this.loaded = loaded;
        }
    }
}
