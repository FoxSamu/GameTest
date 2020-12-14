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
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.CompletableFuture;

import net.shadew.gametest.framework.GameTestFunction;
import net.shadew.gametest.framework.GameTestRegistry;

public class FunctionArgumentType implements ArgumentType<GameTestFunction> {
    private static final DynamicCommandExceptionType NO_SUCH_TEST = new DynamicCommandExceptionType(
        o -> new LiteralMessage("No test function with name '" + o + "'")
    );

    @Override
    public GameTestFunction parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation loc = ResourceLocation.read(reader);
        GameTestFunction function = GameTestRegistry.getFunction(loc);
        if (function == null) throw NO_SUCH_TEST.createWithContext(reader, loc);
        return function;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ISuggestionProvider.func_201725_a(GameTestRegistry.getAllFunctionNames().stream(), builder, t -> t, t -> new LiteralMessage(t.toString()));
    }

    public static FunctionArgumentType function() {
        return new FunctionArgumentType();
    }

    public static GameTestFunction getFunction(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, GameTestFunction.class);
    }
}
