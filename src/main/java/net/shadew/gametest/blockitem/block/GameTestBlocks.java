package net.shadew.gametest.blockitem.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import net.shadew.gametest.blockitem.item.FrameItem;

@ObjectHolder("gametest")
public abstract class GameTestBlocks {
    public static final Block TEST_BLOCK = Blocks.AIR;
    public static final Block TEST_TEMPLATE_BLOCK = Blocks.AIR;
    public static final Block TEST_REMOTE = Blocks.AIR;

    public static void register(IForgeRegistry<Block> registry) {
        registry.registerAll(
            new TestBlock("gametest:test_block"),
            new TemplateBlock("gametest:test_template_block"),
            new RemoteBlock("gametest:test_remote")
        );
    }

    public static void registerItems(IForgeRegistry<Item> registry) {
        registry.registerAll(
            createBlockItem(TEST_BLOCK).setRegistryName("gametest:test_block"),
            createBlockItem(TEST_TEMPLATE_BLOCK).setRegistryName("gametest:test_template_block"),
            createBlockItem(TEST_REMOTE).setRegistryName("gametest:test_remote")
        );
    }

    private static BlockItem createBlockItem(Block block) {
        return new BlockItem(block, new Item.Properties().group(TestItemGroup.GAMETEST));
    }
}
