package net.shadew.gametest.framework.api;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Optional;

public interface ITestInstance {
    String getBatch();
    String getTestClass();
    ResourceLocation getBatchId();
    ResourceLocation getTestClassId();
    ResourceLocation getName();

    int getTimeout();
    int getPredelay();

    long getTick();
    long getStartTick();

    Rotation getRotation();

    BlockPos getOriginPos();
    int getWidth();
    int getHeight();
    int getDepth();

    boolean isOptional();
    boolean isRequired();
    boolean isPassed();
    boolean isFailed();
    Throwable getFailure();
    boolean isExecuting();
    boolean isStarted();
    boolean isDone();

    TestStatus getStatus();

    ServerWorld getWorld();

    BlockPos framePos(String name);
    Optional<BlockPos> framePosOptional(String name);
    List<BlockPos> framePosList(String name);

    void addMarker(BlockPos pos, Marker type, String text);
    default void addMarker(BlockPos pos, Marker type) {
        addMarker(pos, type, "");
    }

    void runAtTick(long tick, Runnable task);

    void fail(Throwable error);
    void pass();

    void addListener(ITestListener listener);
    void removeListener(ITestListener listener);

    ITestSequence newSequence();
}
