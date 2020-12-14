package net.shadew.gametest.testmc;

import javax.annotation.Nullable;

public class TestEvent {
    @Nullable
    public final Long delay;
    public final Runnable assertion;

    private TestEvent(@Nullable Long delay, Runnable assertion) {
        this.delay = delay;
        this.assertion = assertion;
    }

    public static TestEvent create(Runnable assertion) {
        return new TestEvent(null, assertion);
    }

   public static TestEvent create(long delay, Runnable assertion) {
      return new TestEvent(delay, assertion);
   }
}
