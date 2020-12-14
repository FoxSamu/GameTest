package net.shadew.gametest.framework.api.output;

import net.shadew.gametest.framework.GameTestInstance;

public interface ITestOutputInstance {
    void logFailed(GameTestInstance instance, boolean optional);
    void logPassed(GameTestInstance instance);
}
