package net.shadew.gametest.framework;

import java.util.*;

public final class GameTestManager {
    private static final Set<GameTestExecutor> EXECUTORS = new HashSet<>();
    private static final Queue<GameTestExecutor> QUEUED_EXECUTORS = new ArrayDeque<>();

    public static void addExecutor(GameTestExecutor executor) {
        QUEUED_EXECUTORS.add(executor);
    }

    public static void tick() {
        EXECUTORS.addAll(QUEUED_EXECUTORS);
        QUEUED_EXECUTORS.clear();
        EXECUTORS.forEach(GameTestExecutor::tick);
        EXECUTORS.removeIf(GameTestExecutor::isDone);
    }
}
