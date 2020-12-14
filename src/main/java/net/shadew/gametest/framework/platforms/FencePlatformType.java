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

public class FencePlatformType extends AbstractPlatformType {
    @Override
    public ArgumentBuilder<CommandSource, ?> createArgument(Command<CommandSource> cmd) {
        return new CommandChainBuilder()
                .required(Commands.literal("fence").executes(cmd))
                .optional(
                    new CommandChainBuilder()
                        .required(Commands.argument("fence", BlockStateArgumentType.blockState()).executes(cmd))
                        .required(Commands.argument("floor", BlockStateArgumentType.blockState()).executes(cmd))
                        .build().executes(cmd)
                )
                .required(Commands.argument("height", IntegerArgumentType.integer()).executes(cmd))
                .build();
    }

    @Override
    public IPlatform makePlatform(CommandContext<CommandSource> ctx) {
        BlockState floor = getProperty(ctx, "floor", BlockState.class, ANDESITE);
        BlockState fence = getProperty(ctx, "fence", BlockState.class, FENCE);
        int depth = getProperty(ctx, "height", Integer.class, 1);

        return IPlatform.pool(floor, fence, AIR, depth, 0);
    }

    @Override
    public ITextComponent getName() {
        return new FallbackI18nTextComponent("gametest.platform_type.fence", "Fenced Platform");
    }
}
