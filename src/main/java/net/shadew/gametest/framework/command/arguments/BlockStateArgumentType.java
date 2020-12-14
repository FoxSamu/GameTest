package net.shadew.gametest.framework.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.BlockStateParser;
import net.minecraft.tags.BlockTags;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BlockStateArgumentType implements ArgumentType<BlockState> {
    private static final Collection<String> EXAMPLES = Arrays.asList("stone", "minecraft:stone", "stone[foo=bar]");

    @Override
    public BlockState parse(StringReader reader) throws CommandSyntaxException {
        BlockStateParser parser = new BlockStateParser(reader, false).parse(false);
        return parser.getState();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());

        BlockStateParser parser = new BlockStateParser(reader, false);

        try {
            parser.parse(true);
        } catch (CommandSyntaxException ignored) {
        }

        return parser.getSuggestions(builder, BlockTags.getCollection());
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static BlockStateArgumentType blockState() {
        return new BlockStateArgumentType();
    }

    public static BlockState getBlockState(CommandContext<CommandSource> ctx, String name) {
        return ctx.getArgument(name, BlockState.class);
    }
}
