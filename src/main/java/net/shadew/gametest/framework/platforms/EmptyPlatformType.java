package net.shadew.gametest.framework.platforms;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.ITextComponent;

import net.shadew.gametest.util.FallbackI18nTextComponent;

public class EmptyPlatformType implements IPlatformType {
    @Override
    public ArgumentBuilder<CommandSource, ?> createArgument(Command<CommandSource> cmd) {
        return Commands.literal("empty").executes(cmd);
    }

    @Override
    public IPlatform makePlatform(CommandContext<CommandSource> ctx) {
        return IPlatform.EMPTY;
    }

    @Override
    public ITextComponent getName() {
        return new FallbackI18nTextComponent("gametest.platform_type.empty", "Empty");
    }
}
