package net.shadew.gametest.blockitem.item;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import net.shadew.gametest.blockitem.block.TestItemGroup;

@ObjectHolder("gametest")
public abstract class GameTestItems {
    public static final Item TEST_MARKER_FRAME = Items.AIR;

    public static void register(IForgeRegistry<Item> registry) {
        registry.registerAll(
            createFrameItem().setRegistryName("gametest:test_marker_frame")
        );
    }

    private static FrameItem createFrameItem() {
        return new FrameItem(new Item.Properties().group(TestItemGroup.GAMETEST));
    }
}
