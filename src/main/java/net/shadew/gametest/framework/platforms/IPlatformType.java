package net.shadew.gametest.framework.platforms;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.ITextComponent;

public interface IPlatformType {
    ArgumentBuilder<CommandSource, ?> createArgument(Command<CommandSource> cmd);
    IPlatform makePlatform(CommandContext<CommandSource> ctx);
    ITextComponent getName();
}
