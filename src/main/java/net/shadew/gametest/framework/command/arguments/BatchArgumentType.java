package net.shadew.gametest.framework.command.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.CompletableFuture;

import net.shadew.gametest.framework.GameTestFunction;
import net.shadew.gametest.framework.GameTestRegistry;

public class BatchArgumentType implements ArgumentType<ResourceLocation> {
    private static final DynamicCommandExceptionType NO_SUCH_TEST_BATCH = new DynamicCommandExceptionType(
        o -> new LiteralMessage("No test batch with name '" + o + "'")
    );

    @Override
    public ResourceLocation parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation loc = ResourceLocation.read(reader);
        if (!GameTestRegistry.hasBatch(loc)) throw NO_SUCH_TEST_BATCH.createWithContext(reader, loc);
        return loc;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ISuggestionProvider.func_201725_a(GameTestRegistry.getAllBatchNames().stream(), builder, t -> t, t -> new LiteralMessage(t.toString()));
    }

    public static BatchArgumentType batch() {
        return new BatchArgumentType();
    }

    public static ResourceLocation getBatch(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, ResourceLocation.class);
    }
}
