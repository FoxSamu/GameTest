package net.shadew.gametest.framework.command.arguments;

import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;

public abstract class GameTestArguments {
    public static void setup() {
        ArgumentTypes.register("gametest:blockstate", BlockStateArgumentType.class, new ArgumentSerializer<>(BlockStateArgumentType::new));
        ArgumentTypes.register("gametest:boolean", BooleanArgumentType.class, new ArgumentSerializer<>(BooleanArgumentType::new));
        ArgumentTypes.register("gametest:function", FunctionArgumentType.class, new ArgumentSerializer<>(FunctionArgumentType::new));
        ArgumentTypes.register("gametest:batch", BatchArgumentType.class, new ArgumentSerializer<>(BatchArgumentType::new));
        ArgumentTypes.register("gametest:class", ClassArgumentType.class, new ArgumentSerializer<>(ClassArgumentType::new));
        ArgumentTypes.register("gametest:namespace", NamespaceArgumentType.class, new ArgumentSerializer<>(NamespaceArgumentType::new));
        ArgumentTypes.register("gametest:size", SizeArgumentType.class, new ArgumentSerializer<>(SizeArgumentType::new));
    }
}
