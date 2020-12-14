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

import net.shadew.gametest.framework.GameTestRegistry;

public class NamespaceArgumentType implements ArgumentType<String> {
    private static final DynamicCommandExceptionType NO_SUCH_TEST_NAMESPACE = new DynamicCommandExceptionType(
        o -> new LiteralMessage("No test namespace with name '" + o + "'")
    );

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String ns = reader.readUnquotedString();
        if (!GameTestRegistry.hasNamespace(ns)) throw NO_SUCH_TEST_NAMESPACE.createWithContext(reader, ns);
        return ns;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        for(String ns : GameTestRegistry.getAllNamespaces()) {
            builder.suggest(ns);
        }
        return builder.buildFuture();
    }

    public static NamespaceArgumentType namespace() {
        return new NamespaceArgumentType();
    }

    public static String getNamespace(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, String.class);
    }
}
