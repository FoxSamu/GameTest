package net.shadew.gametest.framework.api;

import net.shadew.gametest.blockitem.block.props.TestBlockState;

public enum TestStatus {
    UNSTARTED(TestBlockState.OFF),
    RUNNING(TestBlockState.WORKING),
    PASSED(TestBlockState.OK),
    FAILED(TestBlockState.ITEM);

    private final TestBlockState state;

    TestStatus(TestBlockState state) {
        this.state = state;
    }

    public TestBlockState getTestBlockState(boolean optional) {
        if(this == FAILED) {
            return optional ? TestBlockState.WARNING : TestBlockState.ERROR;
        }
        return state;
    }

    public boolean isStarted() {
        return this != UNSTARTED;
    }

    public boolean isDone() {
        return this != UNSTARTED && this != RUNNING;
    }
}
