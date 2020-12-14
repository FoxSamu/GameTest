package net.shadew.gametest.framework.command.arguments;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class BooleanArgumentType implements ArgumentType<Boolean> {
    private static final Collection<String> EXAMPLES = Arrays.asList("true", "false");
    private static final SimpleCommandExceptionType INVALID_BOOLEAN = new SimpleCommandExceptionType(
        new LiteralMessage("Invalid boolean")
    );

    @Override
    public Boolean parse(StringReader reader) throws CommandSyntaxException {
        String str = reader.readUnquotedString();
        if (str.equals("true")) return true;
        if (str.equals("false")) return false;
        throw INVALID_BOOLEAN.createWithContext(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return builder.suggest("true")
                      .suggest("false")
                      .buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static BooleanArgumentType booleanArgument() {
        return new BooleanArgumentType();
    }

    public static boolean getBoolean(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, Boolean.class);
    }
}
