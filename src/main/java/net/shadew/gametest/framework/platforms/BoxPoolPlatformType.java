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
import net.shadew.gametest.framework.command.arguments.BooleanArgumentType;
import net.shadew.gametest.framework.command.CommandChainBuilder;
import net.shadew.gametest.util.FallbackI18nTextComponent;

public class BoxPoolPlatformType extends AbstractPlatformType {
    @Override
    public ArgumentBuilder<CommandSource, ?> createArgument(Command<CommandSource> cmd) {
        return new CommandChainBuilder()
                .required(Commands.literal("pool-box").executes(cmd))
                .optional(
                    new CommandChainBuilder()
                        .required(Commands.argument("inner", BlockStateArgumentType.blockState()).executes(cmd))
                        .required(Commands.argument("floor", BlockStateArgumentType.blockState()).executes(cmd))
                        .required(Commands.argument("wall", BlockStateArgumentType.blockState()).executes(cmd))
                        .required(Commands.argument("ceil", BlockStateArgumentType.blockState()).executes(cmd))
                        .build().executes(cmd)
                )
                .required(Commands.argument("depth", IntegerArgumentType.integer()).executes(cmd))
                .required(Commands.argument("door", BooleanArgumentType.booleanArgument()).executes(cmd))
                .build();
    }

    @Override
    public IPlatform makePlatform(CommandContext<CommandSource> ctx) {
        BlockState inner = getProperty(ctx, "inner", BlockState.class, WATER);
        BlockState floor = getProperty(ctx, "floor", BlockState.class, ANDESITE);
        BlockState wall = getProperty(ctx, "wall", BlockState.class, floor);
        BlockState ceil = getProperty(ctx, "ceil", BlockState.class, floor);
        int depth = getProperty(ctx, "depth", Integer.class, 1);
        boolean door = getProperty(ctx, "door", Boolean.class, true);

        return IPlatform.boxedPool(floor, ceil, wall, inner, depth, door);
    }

    @Override
    public ITextComponent getName() {
        return new FallbackI18nTextComponent("gametest.platform_type.box_pool", "Box With Pool");
    }
}
