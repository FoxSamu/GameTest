package net.shadew.gametest.blockitem.block;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public final class TestItemGroup {
    public static final ItemGroup GAMETEST = new ItemGroup("gametest") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(GameTestBlocks.TEST_BLOCK);
        }
    };
}
