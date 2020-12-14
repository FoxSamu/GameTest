package net.shadew.gametest.testmc;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Collection;

public class TestInstance {
    private final TestFunction testFunction;

    @Nullable
    private BlockPos pos;
    private final ServerWorld world;

    private final Collection<ITestCallback> listeners = Lists.newArrayList();
    private final int endTime;

    private final Collection<TestSequence> sequences = Lists.newCopyOnWriteArrayList();
    private Object2LongMap<Runnable> tickTasks = new Object2LongOpenHashMap<>();

    private long startTime;
    private long tick;
    private boolean started = false;
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private boolean completed = false;
    private final Rotation rotation;

    @Nullable
    private Throwable error;

    public TestInstance(TestFunction function, Rotation rot, ServerWorld world) {
        this.testFunction = function;
        this.world = world;
        this.endTime = function.timeout();
        this.rotation = function.rotation().add(rot);
    }

    void setPos(BlockPos pos) {
        this.pos = pos;
    }

    void startCountdown() {
        startTime = world.getGameTime() + 1 + testFunction.predelay();
        stopwatch.start();
    }

    public void tick() {
        if (!isCompleted()) {
            tick = world.getGameTime() - startTime;
            if (tick >= 0) {
                if (tick == 0) {
                    start();
                }

                // Run delayed events
                ObjectIterator<Entry<Runnable>> itr = tickTasks.object2LongEntrySet().iterator();
                while (itr.hasNext()) {
                    Entry<Runnable> entry = itr.next();

                    if (tick >= entry.getLongValue()) {
                        try {
                            entry.getKey().run();
                        } catch (Exception exc) {
                            fail(exc);
                        }

                        itr.remove();
                    }
                }

                if (tick > endTime) {
                    if (sequences.isEmpty()) {
                        fail(new TestTimeoutException("Didn't succeed or fail within " + testFunction.timeout() + " ticks"));
                    } else {
                        sequences.forEach(ticker -> ticker.runReported(tick));
                        if (error == null) {
                            fail(new TestTimeoutException("No sequences finished"));
                        }
                    }
                } else {
                    sequences.forEach(ticker -> ticker.runSilently(tick));
                }
            }
        }
    }

    private void start() {
        if (started) {
            throw new IllegalStateException("Test already started");
        } else {
            started = true;

            try {
                testFunction.start(new TestHelper(this));
            } catch (Exception exception) {
                fail(exception);
            }
        }
    }

    public String getName() {
        return testFunction.id();
    }

    public BlockPos getPos() {
        return pos;
    }

    public ServerWorld getWorld() {
        return world;
    }

    public boolean isPassed() {
        return completed && error == null;
    }

    public boolean isFailed() {
        return error != null;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isCompleted() {
        return completed;
    }

    private void complete() {
        if (!completed) {
            completed = true;
            stopwatch.stop();
        }
    }

    public void fail(Throwable exc) {
        complete();
        error = exc;
        listeners.forEach(callback -> callback.onFailed(this));
    }

    public void pass() {
        complete();
        listeners.forEach(callback -> callback.onPassed(this));
    }

    @Nullable
    public Throwable getError() {
        return error;
    }

    public String toString() {
        return getName();
    }

    public void addListener(ITestCallback callback) {
        listeners.add(callback);
    }

    public void init(BlockPos pos, int i) {
        StructureBlockTileEntity sb = TestStructureHelper.createTest(getStructureName(), pos, getRotation(), i, world, false);
        setPos(sb.getPos());
        sb.setName(getName());
        TestStructureHelper.placeStartButton(pos, new BlockPos(1, 0, -1), getRotation(), world);
        listeners.forEach(callback -> callback.onStarted(this));
    }

    public boolean isRequired() {
        return testFunction.required();
    }

    public boolean isOptional() {
        return !testFunction.required();
    }

    public String getStructureName() {
        return testFunction.structure();
    }

    public Rotation getRotation() {
        return rotation;
    }

    public TestFunction getFunction() {
        return testFunction;
    }

    public long getTick() {
        return tick;
    }
}
