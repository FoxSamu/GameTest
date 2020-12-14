package net.shadew.gametest.framework.command.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SizeArgumentType implements ArgumentType<BlockPos> {
    private static final List<String> EXAMPLES = Arrays.asList("10", "10 5", "10 5 8", "");

    @Override
    public BlockPos parse(StringReader reader) throws CommandSyntaxException {
        int w = reader.readInt();

        BlockPos pos;
        int cursor = reader.getCursor();
        try {
            reader.expect(' ');
            int h = reader.readInt();
            int cursor1 = reader.getCursor();
            try {
                reader.expect(' ');
                int d = reader.readInt();
                pos = new BlockPos(w, h, d);
            } catch (CommandSyntaxException exc) {
                reader.setCursor(cursor1);
                pos = new BlockPos(w, h, w);
            }
        } catch (CommandSyntaxException exc) {
            reader.setCursor(cursor);
            pos = new BlockPos(w, w, w);
        }

        checkRange(pos.getX(), reader);
        checkRange(pos.getY(), reader);
        checkRange(pos.getZ(), reader);

        return pos;
    }

    private static void checkRange(int v, StringReader context) throws CommandSyntaxException {
        if (v < 1) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().createWithContext(context, v, 1);
        }
        if (v > 45) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().createWithContext(context, v, 45);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        try {
            StringReader reader = new StringReader(context.getInput());
            reader.setCursor(builder.getStart());

            int w = reader.readInt();
            checkRange(w, reader);
            try {
                reader.expect(' ');
                int h = reader.readInt();
                checkRange(h, reader);
                try {
                    reader.expect(' ');
                    int d = reader.readInt();
                    checkRange(d, reader);
                    return builder.suggest(w + " " + h + " " + d, new LiteralMessage("<width> <heigth> <depth>"))
                                  .buildFuture();
                } catch (CommandSyntaxException exception) {
                    return builder.suggest(w + " " + h + " " + w, new LiteralMessage("<width> <heigth> <depth>"))
                                  .suggest(w + " " + h, new LiteralMessage("<hsize> <heigth>"))
                                  .buildFuture();
                }
            } catch (CommandSyntaxException exception) {
                return builder.suggest(w + " " + w + " " + w, new LiteralMessage("<width> <heigth> <depth>"))
                              .suggest(w + " " + w, new LiteralMessage("<hsize> <heigth>"))
                              .suggest(w, new LiteralMessage("<size>"))
                              .buildFuture();
            }
        } catch (CommandSyntaxException exc) {
            return Suggestions.empty();
        }
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static SizeArgumentType size() {
        return new SizeArgumentType();
    }

    public static BlockPos getSize(CommandContext<?> ctx, String arg) {
        return ctx.getArgument(arg, BlockPos.class);
    }
}
