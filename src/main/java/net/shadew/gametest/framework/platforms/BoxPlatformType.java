package net.shadew.gametest.framework.platforms;

import com.mojang.brigadier.Command;
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

public class BoxPlatformType extends AbstractPlatformType {
    @Override
    public ArgumentBuilder<CommandSource, ?> createArgument(Command<CommandSource> cmd) {
        return new CommandChainBuilder()
                   .required(Commands.literal("box").executes(cmd))
                   .optional(
                       new CommandChainBuilder()
                           .required(Commands.argument("floor", BlockStateArgumentType.blockState()).executes(cmd))
                           .required(Commands.argument("wall", BlockStateArgumentType.blockState()).executes(cmd))
                           .required(Commands.argument("ceil", BlockStateArgumentType.blockState()).executes(cmd))
                           .build().executes(cmd)
                   )
                   .required(Commands.argument("door", BooleanArgumentType.booleanArgument()).executes(cmd))
                   .build();
    }

    @Override
    public IPlatform makePlatform(CommandContext<CommandSource> ctx) {
        BlockState floor = getProperty(ctx, "floor", BlockState.class, ANDESITE);
        BlockState ceil = getProperty(ctx, "ceil", BlockState.class, floor);
        BlockState wall = getProperty(ctx, "wall", BlockState.class, floor);
        boolean door = getProperty(ctx, "door", Boolean.class, true);

        return IPlatform.box(floor, ceil, wall, door);
    }

    @Override
    public ITextComponent getName() {
        return new FallbackI18nTextComponent("gametest.platform_type.box", "Box");
    }
}
