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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class TestFunctionArgument implements ArgumentType<TestFunction> {
   private static final Collection<String> EXAMPLES = Arrays.asList("techtests.piston", "techtests");

   @Override
   public TestFunction parse(StringReader reader) throws CommandSyntaxException {
      String name = reader.readUnquotedString();
      Optional<TestFunction> test = Tests.byNameOptional(name);
      if (test.isPresent()) {
         return test.get();
      } else {
         Message msg = new StringTextComponent("No such test: " + name);
         throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
      }
   }

   public static TestFunctionArgument testFunction() {
      return new TestFunctionArgument();
   }

   public static TestFunction getFunction(CommandContext<CommandSource> ctx, String argumentName) {
      return ctx.getArgument(argumentName, TestFunction.class);
   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
      Stream<String> funcs = Tests.functions().stream().map(TestFunction::id);
      return ISuggestionProvider.suggest(funcs, builder);
   }

   @Override
   public Collection<String> getExamples() {
      return EXAMPLES;
   }
}
