package net.shadew.gametest.framework;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.shadew.gametest.framework.api.ITestSequence;
import net.shadew.gametest.framework.api.exception.AssertException;
import net.shadew.gametest.framework.api.exception.TestException;

public class GameTestSequence implements ITestSequence {
    private final GameTestInstance instance;
    private final Deque<Event> events = new ArrayDeque<>();
    private long lastTime;

    public GameTestSequence(GameTestInstance instance) {
        this.instance = instance;
    }

    @Override
    public GameTestSequence wait(Runnable assertion) {
        events.add(new Event(null, assertion));
        return this;
    }

    @Override
    public GameTestSequence wait(long time, Runnable assertion) {
        events.add(new Event(time, assertion));
        return this;
    }

    @Override
    public GameTestSequence waitAtMost(long time, Runnable assertion) {
        long[] ticks = {0};
        events.add(new Event(time, () -> {
            if (ticks[0]++ < time) {
                assertion.run();
            }
        }));
        return this;
    }

    @Override
    public GameTestSequence idle(long time) {
        long[] ticks = {0};
        events.add(new Event(time, () -> {
            if (ticks[0]++ < time) {
                throw new AssertException("Still idle");
            }
        }));
        return this;
    }

    @Override
    public GameTestSequence after(long time, Runnable action) {
        long[] ticks = {0};
        events.add(new Event(time, () -> {
            if (ticks[0]++ < time) {
                throw new AssertException("Still idle");
            } else {
                try {
                    action.run();
                } catch (Throwable e) {
                    instance.fail(e);
                    throw e;
                }
            }
        }));
        return this;
    }

    @Override
    public GameTestSequence run(Runnable action) {
        events.add(new Event(0L, () -> {
            try {
                action.run();
            } catch (Throwable e) {
                instance.fail(e);
                throw e;
            }
        }));
        return this;
    }

    @Override
    public void fail(Supplier<Throwable> failure) {
        events.add(new Event(0L, () -> instance.fail(failure.get())));
    }

    @Override
    public void pass() {
        events.add(new Event(0L, instance::pass));
    }

    public void tickSilently(long gameTime) {
        try {
            tick(gameTime);
        } catch (Throwable ignored) {
        }
    }

    public void tickReported(long gameTime, Consumer<Throwable> errorConsumer) {
        try {
            tick(gameTime);
        } catch (Throwable e) {
            errorConsumer.accept(e);
        }
    }

    private void tick(long gameTime) {
        Iterator<Event> itr = events.iterator();

        while (itr.hasNext()) {
            Event task = itr.next();
            task.assertion.run();
            itr.remove();

            long delta = gameTime - lastTime;
            long last = lastTime;
            lastTime = gameTime;
            if (task.expectedTime != null && task.expectedTime != delta) {
                instance.fail(new TestException("Succeeded in invalid tick: expected " + (last + task.expectedTime) + ", but current tick is " + gameTime));
                break;
            }
        }
    }

    private static class Event {
        final Long expectedTime;
        final Runnable assertion;

        Event(Long expectedTime, Runnable assertion) {
            this.expectedTime = expectedTime;
            this.assertion = assertion;
        }
    }
}
