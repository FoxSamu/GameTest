package net.shadew.gametest.framework.command.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.CompletableFuture;

import net.shadew.gametest.framework.GameTestFunction;
import net.shadew.gametest.framework.GameTestRegistry;

public class FunctionOrTemplateArgumentType implements ArgumentType<Either<ResourceLocation, GameTestFunction>> {
    private static final DynamicCommandExceptionType NO_SUCH_TEST = new DynamicCommandExceptionType(
        o -> new LiteralMessage("No test function with name '" + o + "'")
    );

    @Override
    public Either<ResourceLocation, GameTestFunction> parse(StringReader reader) throws CommandSyntaxException {
        if(reader.canRead() && reader.peek() == '#') {
            reader.skip();
            ResourceLocation loc = ResourceLocation.read(reader);
            GameTestFunction function = GameTestRegistry.getFunction(loc);
            if (function == null) throw NO_SUCH_TEST.createWithContext(reader, loc);
            return Either.right(function);
        }
        ResourceLocation loc = ResourceLocation.read(reader);
        return Either.left(loc);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());

        if(reader.canRead() && reader.peek() == '#') {
            return ISuggestionProvider.func_201725_a(
                GameTestRegistry.getAllFunctionNames().stream(),
                builder.createOffset(builder.getStart() + 1),
                t -> t,
                t -> new LiteralMessage(t.toString())
            );
        }

        return Suggestions.empty();
    }

    public static FunctionOrTemplateArgumentType functionOrTemplate() {
        return new FunctionOrTemplateArgumentType();
    }

    @SuppressWarnings("unchecked")
    public static Either<ResourceLocation, GameTestFunction> getFunctionOrTemplate(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, Either.class);
    }
}
