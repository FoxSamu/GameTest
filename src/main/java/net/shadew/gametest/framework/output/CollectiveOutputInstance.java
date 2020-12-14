package net.shadew.gametest.framework.output;

import java.util.ArrayList;
import java.util.List;

import net.shadew.gametest.framework.GameTestInstance;
import net.shadew.gametest.framework.api.output.ITestOutputInstance;

public class CollectiveOutputInstance implements ITestOutputInstance {
    private final List<ITestOutputInstance> insts = new ArrayList<>();

    public void add(ITestOutputInstance instance) {
        insts.add(instance);
    }

    @Override
    public void logFailed(GameTestInstance instance, boolean optional) {
        for(ITestOutputInstance i : insts) {
            i.logFailed(instance, optional);
        }
    }

    @Override
    public void logPassed(GameTestInstance instance) {
        for(ITestOutputInstance i : insts) {
            i.logPassed(instance);
        }
    }
}
