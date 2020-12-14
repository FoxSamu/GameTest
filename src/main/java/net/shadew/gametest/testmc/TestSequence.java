package net.shadew.gametest.testmc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class TestSequence {
    private final TestInstance test;
    private final List<TestEvent> tasks = new ArrayList<>();
    private long tick;

    public TestSequence(TestInstance test) {
        this.test = test;
    }

    public TestSequence wait(Runnable event) {
        tasks.add(TestEvent.create(event));
        return this;
    }

    public TestSequence wait(long time, Runnable event) {
        tasks.add(TestEvent.create(time, event));
        return this;
    }

    public TestSequence idle(int time) {
        int[] t = {time};
        tasks.add(TestEvent.create(time, () -> {
            if(t[0]-->0) {
                throw new TestRuntimeException("Idle");
            }
        }));
        return this;
    }

    public TestSequence execute(Runnable task) {
        return wait(() -> noFail(task));
    }

    public TestSequence executeAfter(int time, Runnable task) {
        return idle(time).wait(() -> noFail(task));
    }

    public TestSequence fail(Supplier<Throwable> errorSupplier) {
        tasks.add(TestEvent.create(() -> test.fail(errorSupplier.get())));
        return this;
    }

    public TestSequence pass() {
        tasks.add(TestEvent.create(test::pass));
        return this;
    }

    private static void noFail(Runnable task) {
        try {
            task.run();
        } catch (Throwable ignored) {
        }
    }

    public void runSilently(long time) {
        try {
            runTasks(time);
        } catch (Exception ignored) {
        }
    }

    public void runReported(long time) {
        try {
            runTasks(time);
        } catch (Exception exception) {
            test.fail(exception);
        }
    }

    private void runTasks(long time) {
        Iterator<TestEvent> itr = tasks.iterator();

        while (itr.hasNext()) {
            TestEvent task = itr.next();
            task.assertion.run();
            itr.remove();

            long delta = time - tick;
            long last = tick;
            tick = time;
            if (task.delay != null && task.delay != delta) {
                test.fail(new TestRuntimeException("Succeeded in invalid tick: expected " + (last + task.delay) + ", but current tick is " + time));
                break;
            }
        }
    }
}
