package net.shadew.gametest.framework;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;

import net.shadew.gametest.blockitem.block.props.TestBlockState;
import net.shadew.gametest.blockitem.entity.FrameEntity;
import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.blockitem.tileentity.TestBlockTileEntity;
import net.shadew.gametest.framework.api.*;
import net.shadew.gametest.framework.api.exception.TestException;
import net.shadew.gametest.framework.api.exception.TimeoutException;
import net.shadew.gametest.framework.api.output.ITestOutputInstance;

public class GameTestInstance implements ITestInstance {
    private final TestBlockTileEntity testBlock;
    private final TemplateBlockTileEntity templateBlock;

    private final GameTestFunction function;
    private final BlockPos originPos;
    private final ServerWorld world;

    private final int timeout;
    private final int predelay;

    private final Set<ITestListener> listeners = new HashSet<>();
    private final Set<DelayedTask> delayedTasks = new HashSet<>();
    private final Set<GameTestSequence> sequences = new HashSet<>();
    private ITestOutputInstance output;

    private long startTick;
    private long tick;

    private Throwable failure;
    private TestStatus status = TestStatus.UNSTARTED;

    private final Map<String, List<BlockPos>> frameData = new HashMap<>();

    public GameTestInstance(ServerWorld world, GameTestFunction function, TestBlockTileEntity testBlock, TemplateBlockTileEntity templateBlock) {
        this.testBlock = testBlock;
        this.templateBlock = templateBlock;
        this.originPos = templateBlock.getPos().up();
        this.function = function;
        this.world = world;
        this.timeout = function.getTimeout();
        this.predelay = function.getPredelay();
    }

    public boolean init() {
        if (!templateBlock.loadAndPlace()) {
            fail(new TestException("Failed to load template"));
            return false;
        }
        listeners.forEach(listener -> listener.onInit(this));
        return true;
    }

    public void useFrameData(Map<String, List<FrameEntity>> frames) {
        frameData.clear();
        frames.forEach((name, frameList) -> frameData.put(
            name,
            frameList.stream()
                     .map(FrameEntity::getFramePos)
                     .distinct()
                     .collect(Collectors.toList())
        ));
    }

    public void tick() {
        if (!isDone()) {
            tick = world.getGameTime() - startTick;
            if (tick >= 0) {
                if (tick == 0) {
                    execute();
                }

                Iterator<DelayedTask> itr = delayedTasks.iterator();
                while (itr.hasNext()) {
                    DelayedTask next = itr.next();
                    if (tick >= next.time) {
                        try {
                            next.task.run();
                        } catch (Exception exc) {
                            fail(exc);
                        }

                        itr.remove();
                    }
                }

                if (tick > timeout) {
                    TimeoutException exc = new TimeoutException("Test did not pass or fail within " + function.getTimeout() + " ticks");
                    sequences.forEach(seq -> seq.tickReported(tick, exc::addSuppressed));
                    fail(exc);
                } else {
                    sequences.forEach(seq -> seq.tickSilently(tick));
                }
            }
        }
    }

    public void start() {
        startTick = world.getGameTime() + 1 + predelay;
    }

    private void execute() {
        if (status != TestStatus.UNSTARTED) {
            throw new IllegalStateException("Already executing");
        } else {
            status = TestStatus.RUNNING;

            try {
                useFrameData(templateBlock.findAndMapFrames());
                templateBlock.removeAllFrames();
                function.runTestMethod(new TestController(this));
            } catch (Exception exc) {
                fail(exc);
            }
        }
    }

    private void setStatus(TestStatus status) {
        this.status = status;
        listeners.forEach(listener -> listener.onStatusChange(this, status));
    }

    @Override
    public void fail(Throwable problem) {
        failure = problem;
        if (problem instanceof TestException) {
            ((TestException) problem).displayErrorInGame(this);
        }
        setStatus(TestStatus.FAILED);
        listeners.forEach(listener -> listener.onFail(this, failure));
        if (output != null) {
            output.logFailed(this, isOptional());
        }
    }

    @Override
    public void pass() {
        setStatus(TestStatus.PASSED);
        listeners.forEach(listener -> listener.onPass(this));
        if (output != null) {
            output.logPassed(this);
        }
    }

    @Override
    public void addListener(ITestListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ITestListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void runAtTick(long tick, Runnable action) {
        delayedTasks.add(new DelayedTask(tick, action));
    }

    @Override
    public boolean isStarted() {
        return status.isStarted();
    }

    @Override
    public boolean isDone() {
        return status.isDone();
    }

    @Override
    public TestStatus getStatus() {
        return status;
    }

    public TestBlockState getTestBlockState() {
        return status.getTestBlockState(isOptional());
    }

    @Override
    public boolean isExecuting() {
        return status == TestStatus.RUNNING;
    }

    @Override
    public boolean isFailed() {
        return status == TestStatus.FAILED;
    }

    @Override
    public Throwable getFailure() {
        return failure;
    }

    @Override
    public boolean isPassed() {
        return status == TestStatus.PASSED;
    }

    @Override
    public boolean isRequired() {
        return function.isRequired();
    }

    @Override
    public boolean isOptional() {
        return !function.isRequired();
    }

    @Override
    public Rotation getRotation() {
        return templateBlock.getRotation();
    }

    @Override
    public long getTick() {
        return tick;
    }

    @Override
    public long getStartTick() {
        return startTick;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public int getPredelay() {
        return predelay;
    }

    @Override
    public ServerWorld getWorld() {
        return world;
    }

    @Override
    public String getBatch() {
        return function.getBatch();
    }

    @Override
    public String getTestClass() {
        return function.getTestClass();
    }

    @Override
    public ResourceLocation getBatchId() {
        return function.getBatchId();
    }

    @Override
    public ResourceLocation getTestClassId() {
        return function.getTestClassId();
    }

    @Override
    public ResourceLocation getName() {
        return function.getName();
    }

    @Override
    public BlockPos framePos(String name) {
        return framePosOptional(name).orElse(null);
    }

    @Override
    public Optional<BlockPos> framePosOptional(String name) {
        return framePosList(name).stream().findFirst();
    }

    @Override
    public List<BlockPos> framePosList(String name) {
        return frameData.getOrDefault(name, Collections.emptyList());
    }

    @Override
    public void addMarker(BlockPos pos, Marker type, String message) {
        testBlock.addMarker(pos, type, message);
    }

    @Override
    public BlockPos getOriginPos() {
        return originPos;
    }

    @Override
    public int getWidth() {
        return templateBlock.getWidth();
    }

    @Override
    public int getHeight() {
        return templateBlock.getHeight();
    }

    @Override
    public int getDepth() {
        return templateBlock.getDepth();
    }

    public void setOutput(ITestOutputInstance output) {
        this.output = output;
    }

    public ITestOutputInstance getOutput() {
        return output;
    }

    @Override
    public GameTestSequence newSequence() {
        GameTestSequence sequence = new GameTestSequence(this);
        sequences.add(sequence);
        return sequence;
    }

    private static class DelayedTask {
        private final long time;
        private final Runnable task;

        private DelayedTask(long time, Runnable task) {
            this.time = time;
            this.task = task;
        }
    }
}
