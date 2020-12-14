package net.shadew.gametest.testmc;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class TestClassArgument implements ArgumentType<String> {
   private static final Collection<String> EXAMPLES = Arrays.asList("techtests", "mobtests");

   @Override
   public String parse(StringReader reader) throws CommandSyntaxException {
      String cls = reader.readUnquotedString();
      if (Tests.hasTestClass(cls)) {
         return cls;
      } else {
         Message msg = new StringTextComponent("No such test class: " + cls);
         throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
      }
   }

   public static TestClassArgument testClass() {
      return new TestClassArgument();
   }

   public static String getTestClass(CommandContext<CommandSource> ctx, String name) {
      return ctx.getArgument(name, String.class);
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      return ISuggestionProvider.suggest(Tests.classes().stream(), builder);
   }

   @Override
   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
