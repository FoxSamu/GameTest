package net.shadew.gametest.testmc;

import com.google.common.collect.Lists;

import java.util.Collection;

public class TestTicker {
    public static final TestTicker INSTANCE = new TestTicker();
    private final Collection<TestInstance> tests = Lists.newCopyOnWriteArrayList();

    public void start(TestInstance tracker) {
        tests.add(tracker);
    }

    public void clear() {
        tests.clear();
    }

    public void tick() {
        tests.forEach(TestInstance::tick);
        tests.removeIf(TestInstance::isCompleted);
    }

    public static void tickStatic() {
        INSTANCE.tick();
    }
}
