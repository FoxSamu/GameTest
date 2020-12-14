package net.shadew.gametest.testmc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class Tests {
    private static final Collection<TestFunction> FUNCTIONS = Lists.newArrayList();
    private static final Set<String> CLASSES = Sets.newHashSet();
    private static final Map<String, Consumer<ServerWorld>> BEFORE_BATCH = Maps.newHashMap();
    private static final Collection<TestFunction> FAILED = Sets.newHashSet();

    public static Collection<TestFunction> functions(String cls) {
        return FUNCTIONS.stream()
                        .filter(func -> isInClass(func, cls))
                        .collect(Collectors.toList());
    }

    public static Collection<TestFunction> functions() {
        return FUNCTIONS;
    }

    public static Collection<String> classes() {
        return CLASSES;
    }

    public static boolean hasTestClass(String name) {
        return CLASSES.contains(name);
    }

    @Nullable
    public static Consumer<ServerWorld> beforeBatch(String name) {
        return BEFORE_BATCH.get(name);
    }

    public static Optional<TestFunction> byNameOptional(String name) {
        return functions().stream()
                          .filter(function -> function.id().equalsIgnoreCase(name))
                          .findFirst();
    }

    public static TestFunction byName(String name) {
        Optional<TestFunction> opt = byNameOptional(name);
        if (!opt.isPresent()) {
            throw new IllegalArgumentException("Can't find the test function for " + name);
        } else {
            return opt.get();
        }
    }

    private static boolean isInClass(TestFunction func, String cls) {
        return func.id().toLowerCase().startsWith(cls.toLowerCase() + ".");
    }

    public static void addTestClass(Class<?> cls) {
        String name = cls.getSimpleName().toLowerCase();

        if(CLASSES.contains(name))
            return;

        for (Method method : cls.getMethods()) {
            if (method.isAnnotationPresent(GameTest.class)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException(
                        "Test method " + cls.getName() + "." + method.getName() + " is not static"
                    );
                }
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1 || params[0] != TestHelper.class || method.getReturnType() != void.class) {
                    throw new IllegalArgumentException(
                        "Test method " + cls.getName() + "." + method.getName() + " has incorrect signature"
                    );
                }

                TestFunction function = new TestFunction(
                    name,
                    method.getName().toLowerCase(),
                    method.getAnnotation(GameTest.class),
                    helper -> {
                        try {
                            method.invoke(null, helper);
                        } catch (IllegalAccessException | InvocationTargetException exc) {
                            throw new RuntimeException(exc);
                        }
                    }
                );

                FUNCTIONS.add(function);
            } else if(method.isAnnotationPresent(BeforeBatch.class)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException(
                        "Before-batch method " + cls.getName() + "." + method.getName() + " is not static"
                    );
                }
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1 || params[0] != ServerWorld.class || method.getReturnType() != void.class) {
                    throw new IllegalArgumentException(
                        "Before-batch method " + cls.getName() + "." + method.getName() + " has incorrect signature"
                    );
                }

                BeforeBatch annotation = method.getAnnotation(BeforeBatch.class);

                Consumer<ServerWorld> func = world -> {
                    try {
                        method.invoke(null, world);
                    } catch (IllegalAccessException | InvocationTargetException exc) {
                        throw new RuntimeException(exc);
                    }
                };

                BEFORE_BATCH.put(annotation.value(), func);
            }
        }

        CLASSES.add(name);
    }

    public static Collection<TestFunction> failed() {
        return FAILED;
    }

    public static void addFailed(TestFunction func) {
        FAILED.add(func);
    }

    public static void clearFailed() {
        FAILED.clear();
    }
}
