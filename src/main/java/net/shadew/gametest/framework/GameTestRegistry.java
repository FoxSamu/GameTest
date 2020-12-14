package net.shadew.gametest.framework;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.framework.api.annotation.BeforeBatch;
import net.shadew.gametest.framework.api.annotation.GameTest;
import net.shadew.gametest.framework.api.exception.TestException;

public final class GameTestRegistry {
    private static final Map<ResourceLocation, GameTestFunction> FUNCTIONS = new HashMap<>();
    private static final Map<ResourceLocation, ClassEntry> CLASSES = new HashMap<>();
    private static final Map<ResourceLocation, BatchEntry> BATCHES = new HashMap<>();
    private static final Map<String, NamespaceEntry> NAMESPACES = new HashMap<>();

    public static void registerTest(GameTestFunction function) {
        FUNCTIONS.put(function.getName(), function);

        BatchEntry batch = BATCHES.computeIfAbsent(function.getBatchId(), k -> new BatchEntry());
        batch.functions.add(function);

        ClassEntry cls = CLASSES.computeIfAbsent(function.getTestClassId(), k -> new ClassEntry());
        cls.functions.add(function);

        NamespaceEntry ns = NAMESPACES.computeIfAbsent(function.getTestClassId().getNamespace(), k -> new NamespaceEntry());
        ns.functions.add(function);
    }

    public static void registerBeforeBatch(String namespace, String id, Consumer<ServerWorld> func) {
        BatchEntry batch = BATCHES.computeIfAbsent(new ResourceLocation(namespace, id), k -> new BatchEntry());
        batch.beforeBatch = func;
    }

    public static GameTestFunction getFunction(ResourceLocation id) {
        return FUNCTIONS.get(id);
    }

    public static Collection<GameTestFunction> getAllOfBatch(ResourceLocation batchId) {
        return BATCHES.containsKey(batchId) ? BATCHES.get(batchId).functions : Collections.emptyList();
    }

    public static Collection<GameTestFunction> getAllOfClass(ResourceLocation classId) {
        return CLASSES.containsKey(classId) ? CLASSES.get(classId).functions : Collections.emptyList();
    }

    public static Collection<GameTestFunction> getAllOfNamespace(String namespace) {
        return NAMESPACES.containsKey(namespace) ? NAMESPACES.get(namespace).functions : Collections.emptyList();
    }

    public static Collection<GameTestFunction> getAll() {
        return FUNCTIONS.values();
    }

    public static Collection<ResourceLocation> getAllFunctionNames() {
        return FUNCTIONS.keySet();
    }

    public static Collection<ResourceLocation> getAllBatchNames() {
        return BATCHES.keySet();
    }

    public static Collection<ResourceLocation> getAllClassNames() {
        return CLASSES.keySet();
    }

    public static Collection<String> getAllNamespaces() {
        return NAMESPACES.keySet();
    }

    public static Consumer<ServerWorld> getBeforeBatchFunction(ResourceLocation batchId) {
        return BATCHES.containsKey(batchId) ? BATCHES.get(batchId).beforeBatch : world -> {};
    }

    public static boolean hasFunction(ResourceLocation id) {
        return FUNCTIONS.containsKey(id);
    }

    public static boolean hasBatch(ResourceLocation id) {
        return BATCHES.containsKey(id);
    }

    public static boolean hasClass(ResourceLocation id) {
        return CLASSES.containsKey(id);
    }

    public static boolean hasNamespace(String id) {
        return NAMESPACES.containsKey(id);
    }

    public static void registerClass(Class cls) {
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is abstract");
        }
        if (Modifier.isInterface(cls.getModifiers())) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is an interface");
        }
        if (cls.isPrimitive()) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is a primitive type");
        }
        if (cls.isArray()) {
            throw new IllegalArgumentException("Class " + cls.getName() + " is a array type");
        }
        if (!cls.isAnnotationPresent(GameTest.Class.class)) {
            throw new IllegalArgumentException("Class " + cls.getName() + " has no @GameTestClass annotation");
        }

        GameTest.Class classAnnotation = (GameTest.Class) cls.getAnnotation(GameTest.Class.class);
        String classIdString = classAnnotation.value();
        ResourceLocation classId;
        if (classIdString.contains(":")) {
            classId = new ResourceLocation(classIdString);
        } else {
            classId = new ResourceLocation(classIdString, cls.getSimpleName().toLowerCase());
        }

        for (Method method : cls.getMethods()) {
            if (method.isAnnotationPresent(GameTest.class)) {
                GameTest testAnnotation = method.getAnnotation(GameTest.class);

                GameTestFunction func = new GameTestFunction(classId.getNamespace(), cls, classId.getPath(), method, testAnnotation, classAnnotation);
                registerTest(func);
            }

            if (method.isAnnotationPresent(BeforeBatch.class)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalTestException("@BeforeBatch method is not static");
                }

                if (!Modifier.isPublic(method.getModifiers())) {
                    throw new IllegalTestException("@BeforeBatch method is not public");
                }

                Class[] params = method.getParameterTypes();
                if (params.length < 1) {
                    throw new IllegalTestException("@GameTest method does not have a first ServerWorld argument");
                }

                if (params[0] != ServerWorld.class) {
                    throw new IllegalTestException("@GameTest method first argument is not of type ServerWorld");
                }

                BeforeBatch beforeBatchAnnotation = method.getAnnotation(BeforeBatch.class);

                Consumer<ServerWorld> func = world -> {
                    try {
                        method.invoke(null, world);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new TestException(e);
                    }
                };

                String batchId = beforeBatchAnnotation.value();
                if (batchId.isEmpty()) batchId = classId.getPath();

                registerBeforeBatch(classId.getNamespace(), batchId, func);
            }
        }
    }

    public static void dumpDebug() {
        GameTestMod.LOGGER.info("Loaded {} tests:", FUNCTIONS.size());
        for (ResourceLocation res : FUNCTIONS.keySet())
            GameTestMod.LOGGER.info("- {}", res);

        GameTestMod.LOGGER.info("In {} test classes:", CLASSES.size());
        for (ResourceLocation res : CLASSES.keySet())
            GameTestMod.LOGGER.info("- {}", res);

        GameTestMod.LOGGER.info("In {} test batches:", BATCHES.size());
        for (ResourceLocation res : BATCHES.keySet())
            GameTestMod.LOGGER.info("- {}", res);
    }

//    static {
//        registerClass(FallingBlockTests.class);
//
//        dumpDebug();
//    }

    static class BatchEntry {
        Consumer<ServerWorld> beforeBatch = world -> {};
        final Set<GameTestFunction> functions = new HashSet<>();
    }

    static class ClassEntry {
        final Set<GameTestFunction> functions = new HashSet<>();
    }

    static class NamespaceEntry {
        final Set<GameTestFunction> functions = new HashSet<>();
    }
}
