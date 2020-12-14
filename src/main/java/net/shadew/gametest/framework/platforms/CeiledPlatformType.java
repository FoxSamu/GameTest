package net.shadew.gametest.framework.platforms;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;

import net.shadew.gametest.framework.command.arguments.BlockStateArgumentType;
import net.shadew.gametest.util.FallbackI18nTextComponent;

public class CeiledPlatformType extends AbstractPlatformType {
    @Override
    public ArgumentBuilder<CommandSource, ?> createArgument(Command<CommandSource> cmd) {
        return Commands.literal("platform-ceiled").executes(cmd).then(
            Commands.argument("floor", BlockStateArgumentType.blockState()).executes(cmd).then(
                Commands.argument("ceil", BlockStateArgumentType.blockState()).executes(cmd)
            )
        );
    }

    @Override
    public IPlatform makePlatform(CommandContext<CommandSource> ctx) {
        BlockState floor = getProperty(ctx, "floor", BlockState.class, ANDESITE);
        BlockState ceil = getProperty(ctx, "ceil", BlockState.class, floor);
        return IPlatform.ceiled(floor, ceil);
    }

    @Override
    public ITextComponent getName() {
        return new FallbackI18nTextComponent("gametest.platform_type.platform_ceiled", "Platform With Ceiling");
    }
}
