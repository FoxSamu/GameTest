package net.shadew.gametest.blockitem.tileentity;

import com.google.common.collect.Sets;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.IForgeRegistry;

import net.shadew.gametest.blockitem.block.GameTestBlocks;

public abstract class GameTestTileEntityTypes {
    public static final TileEntityType<TemplateBlockTileEntity> TEMPLATE_BLOCK = new TileEntityType<>(
        TemplateBlockTileEntity::new,
        Sets.newHashSet(GameTestBlocks.TEST_TEMPLATE_BLOCK),
        null
    );
    public static final TileEntityType<TestBlockTileEntity> TEST_BLOCK = new TileEntityType<>(
        TestBlockTileEntity::new,
        Sets.newHashSet(GameTestBlocks.TEST_BLOCK),
        null
    );
    public static final TileEntityType<RemoteTileEntity> REMOTE = new TileEntityType<>(
        RemoteTileEntity::new,
        Sets.newHashSet(GameTestBlocks.TEST_REMOTE),
        null
    );

    public static void register(IForgeRegistry<TileEntityType<?>> registry) {
        registry.registerAll(
            TEMPLATE_BLOCK.setRegistryName("gametest:template_block"),
            TEST_BLOCK.setRegistryName("gametest:test_block"),
            REMOTE.setRegistryName("gametest:remote")
        );
    }
}
