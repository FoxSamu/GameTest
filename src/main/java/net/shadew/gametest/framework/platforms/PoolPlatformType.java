package net.shadew.gametest.framework.platforms;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;

import net.shadew.gametest.framework.command.arguments.BlockStateArgumentType;
import net.shadew.gametest.framework.command.CommandChainBuilder;
import net.shadew.gametest.util.FallbackI18nTextComponent;

public class PoolPlatformType extends AbstractPlatformType {
    @Override
    public ArgumentBuilder<CommandSource, ?> createArgument(Command<CommandSource> cmd) {
        return new CommandChainBuilder()
                .required(Commands.literal("pool").executes(cmd))
                .optional(
                    new CommandChainBuilder()
                        .required(Commands.argument("inner", BlockStateArgumentType.blockState()).executes(cmd))
                        .required(Commands.argument("floor", BlockStateArgumentType.blockState()).executes(cmd))
                        .required(Commands.argument("wall", BlockStateArgumentType.blockState()).executes(cmd))
                        .build().executes(cmd)
                )
                .required(Commands.argument("depth", IntegerArgumentType.integer()).executes(cmd))
                .required(Commands.argument("wallextra", IntegerArgumentType.integer()).executes(cmd))
                .build();
    }

    @Override
    public IPlatform makePlatform(CommandContext<CommandSource> ctx) {
        BlockState inner = getProperty(ctx, "inner", BlockState.class, WATER);
        BlockState floor = getProperty(ctx, "floor", BlockState.class, ANDESITE);
        BlockState wall = getProperty(ctx, "wall", BlockState.class, floor);
        int depth = getProperty(ctx, "depth", Integer.class, 1);
        int wallextra = getProperty(ctx, "wallextra", Integer.class, 0);

        return IPlatform.pool(floor, wall, inner, depth, wallextra);
    }

    @Override
    public ITextComponent getName() {
        return new FallbackI18nTextComponent("gametest.platform_type.pool", "Pool");
    }
}
