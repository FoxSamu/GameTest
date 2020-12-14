package net.shadew.gametest.framework.platforms;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public abstract class AbstractPlatformType implements IPlatformType {
    protected static final BlockState ANDESITE = Blocks.POLISHED_ANDESITE.getDefaultState();
    protected static final BlockState WATER = Blocks.WATER.getDefaultState();
    protected static final BlockState FENCE = Blocks.DARK_OAK_FENCE.getDefaultState();
    protected static final BlockState AIR = Blocks.AIR.getDefaultState();

    protected static <T> T getProperty(CommandContext<?> ctx, String arg, Class<T> cls, T orElse) {
        try {
            return ctx.getArgument(arg, cls);
        } catch (IllegalArgumentException exc) {
            return orElse;
        }
    }
}
