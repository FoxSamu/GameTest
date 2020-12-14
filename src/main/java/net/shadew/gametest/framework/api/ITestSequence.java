package net.shadew.gametest.framework.api;

import java.util.function.Supplier;

public interface ITestSequence {
    ITestSequence wait(Runnable assertion);
    ITestSequence wait(long expectedTime, Runnable assertion);
    ITestSequence waitAtMost(long time, Runnable assertion);
    ITestSequence idle(long time);
    ITestSequence after(long time, Runnable action);
    ITestSequence run(Runnable action);
    void pass();
    void fail(Supplier<Throwable> error);
}
