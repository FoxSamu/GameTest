package net.shadew.gametest.framework;

import com.google.common.collect.Iterables;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.shadew.gametest.blockitem.block.props.DiagonalDirection;
import net.shadew.gametest.blockitem.block.props.TestBlockState;
import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.blockitem.tileentity.TestBlockTileEntity;
import net.shadew.gametest.framework.api.ITestListener;
import net.shadew.gametest.framework.api.TestStatus;
import net.shadew.gametest.framework.api.output.ITestOutputInstance;

public class GameTestExecutor {
    private final ServerWorld world;
    private final Collection<GameTestFunction> testsToRun;
    private final int testsPerRow;
    private final int simultaneousTests;
    private final BlockPos pos;
    private final DiagonalDirection direction;

    private int currentRowSize = 0;
    private int currentRowDepth = 0;
    private final BlockPos.Mutable nextPos = new BlockPos.Mutable();

    private boolean enqueued;
    private final Set<ResourceLocation> running = new HashSet<>();
    private final Map<ResourceLocation, TestBlockTileEntity> testBlocks = new HashMap<>();
    private final Deque<EnqueuedBatch> enqueuedBatches = new ArrayDeque<>();

    private final Map<ResourceLocation, Throwable> failed = new HashMap<>();

    private final List<ITestListener> listeners = new ArrayList<>();

    private ITestOutputInstance output;

    public GameTestExecutor(ServerWorld world, Collection<GameTestFunction> testsToRun, int testsPerRow, int simultaneousTests, BlockPos pos, DiagonalDirection direction) {
        this.world = world;
        this.testsToRun = testsToRun;
        this.testsPerRow = testsPerRow;
        this.simultaneousTests = simultaneousTests;
        this.pos = pos;
        this.direction = direction;

        nextPos.setPos(pos);
    }

    public void setOutput(ITestOutputInstance output) {
        this.output = output;
    }

    public ITestOutputInstance getOutput() {
        return output;
    }

    public void addListener(ITestListener listener) {
        listeners.add(listener);
    }

    public Map<ResourceLocation, Throwable> getFailed() {
        return failed;
    }

    public void addTestBlock(ResourceLocation id, TestBlockTileEntity testBlock) {
        testBlocks.put(id, testBlock);
    }

    public void tick() {
        if (!enqueued) {
            enqueueBatches();
            enqueued = true;
        }
        if (!isDone()) {
            if (running.isEmpty()) {
                nextEnqueuedBatch();
            }
        }
    }

    public boolean isDone() {
        return enqueuedBatches.isEmpty();
    }

    private void enqueueBatches() {
        Map<ResourceLocation, List<GameTestFunction>> sortedByBatch = new HashMap<>();
        for (GameTestFunction testFn : testsToRun) {
            sortedByBatch.computeIfAbsent(testFn.getBatchId(), k -> new ArrayList<>()).add(testFn);
        }

        for (Map.Entry<ResourceLocation, List<GameTestFunction>> batchEntry : sortedByBatch.entrySet()) {
            enqueueBatch(batchEntry.getKey(), batchEntry.getValue());
        }
    }

    private void enqueueBatch(ResourceLocation id, List<GameTestFunction> fns) {
        Consumer<ServerWorld> before = GameTestRegistry.getBeforeBatchFunction(id);
        for (List<GameTestFunction> fnsPart : Iterables.partition(fns, simultaneousTests)) {
            enqueuedBatches.addLast(new EnqueuedBatch(before, fnsPart));
        }
    }

    private void nextEnqueuedBatch() {
        if (enqueuedBatches.isEmpty()) return;

        EnqueuedBatch batch = enqueuedBatches.removeFirst();

        List<GameTestBuilder.TestBuildResult> results = addTests(batch.tests);
        if (batch.beforeBatch != null) {
            try {
                batch.beforeBatch.accept(world);
            } catch (Throwable throwable) {
                Throwable wrapped = new TemplateLoadingException("BeforeBatch failed", throwable);

                for (GameTestFunction test : batch.tests) {
                    failed.put(test.getName(), wrapped);
                }
                for (GameTestBuilder.TestBuildResult result : results) {
                    result.testBlock.setOutput(output);
                    result.testBlock.addListener(new ITestListener() {
                        @Override
                        public void onInit(GameTestInstance instance) {
                            instance.fail(wrapped);
                        }
                    });
                }
                return;
            }
        }

        for (GameTestBuilder.TestBuildResult result : results) {
            forceLoadChunks(result.templateBlock);
            result.testBlock.setOutput(output);
            result.testBlock.enqueueStartTest(false);
            result.testBlock.addListener(new ITestListener() {
                @Override
                public void onStatusChange(GameTestInstance instance, TestStatus status) {
                    if (status.isDone()) {
                        running.remove(instance.getName());
                    }
                }
            });
            listeners.forEach(result.testBlock::addListener);
            running.add(result.id);
        }
    }

    private void forceLoadChunks(TemplateBlockTileEntity templateBlock) {
        MutableBoundingBox box = templateBlock.getMBox();

        int nx = box.minX >> 4;
        int nz = box.minZ >> 4;
        int px = (box.maxX + 1 >> 4) + 1;
        int pz = (box.maxZ + 1 >> 4) + 1;

        for (int cx = nx; cx <= px; cx++) {
            for (int cz = nz; cz <= pz; cz++) {
                world.forceChunk(cx, cz, true);
            }
        }
    }

    private List<GameTestBuilder.TestBuildResult> addTests(Collection<GameTestFunction> tests) {
        return tests.stream().map(this::addTest).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private GameTestBuilder.TestBuildResult addTest(GameTestFunction fn) {
        ResourceLocation id = fn.getName();

        if (testBlocks.containsKey(id)) {
            TestBlockTileEntity testBlock = testBlocks.get(id);
            TemplateBlockTileEntity templateBlock = testBlock.getTemplateBlock();

            if (templateBlock != null && templateBlock.getName().equals(fn.getName()) && !templateBlock.isRawTemplate()) {
                MutableBoundingBox box = templateBlock.getMBox();
                box.minX -= 3; // grow 3
                box.minY -= 3;
                box.minZ -= 3;
                box.maxX += 3;
                box.maxY += 3;
                box.maxZ += 3;
                return new GameTestBuilder.TestBuildResult(
                    fn.getName(), box,
                    testBlock.getPos(),
                    templateBlock.getPos(),
                    testBlock,
                    templateBlock,
                    testBlock.getState() == TestBlockState.OFF || templateBlock.loadAndPlace()
                );
            }
        }

        GameTestBuilder.TestBuildResult result = GameTestBuilder.buildTest(world, nextPos, direction, id);
        testBlocks.put(id, result.testBlock);
        if (!result.loaded) {
            failed.put(id, new TemplateLoadingException("Failed to load test " + id));
            return null;
        }

        int zLength = getLength(result.box, Direction.SOUTH, direction);
        currentRowDepth = Math.max(currentRowDepth, zLength);

        currentRowSize++;
        if (currentRowSize >= testsPerRow) {
            Direction.Axis resetAxis = getAxis(Direction.EAST, direction);
            if (resetAxis == Direction.Axis.X) {
                nextPos.setX(pos.getX());
            } else if (resetAxis == Direction.Axis.Z) {
                nextPos.setZ(pos.getZ());
            }

            Direction moveDir = getDir(Direction.SOUTH, direction);
            nextPos.move(moveDir, currentRowDepth + 2);

            currentRowSize = 0;
            currentRowDepth = 0;
        } else {
            int xLength = getLength(result.box, Direction.EAST, direction);

            Direction moveDir = getDir(Direction.EAST, direction);
            nextPos.move(moveDir, xLength + 2);
        }

        return result;
    }

    private static Direction getDir(Direction face, DiagonalDirection dir) {
        return dir.getRotation().rotate(face);
    }

    private static Direction.Axis getAxis(Direction face, DiagonalDirection dir) {
        return getDir(face, dir).getAxis();
    }

    private static int getLength(MutableBoundingBox box, Direction face, DiagonalDirection dir) {
        Direction.Axis axis = getAxis(face, dir);
        switch (axis) {
            default:
            case Y: return box.getYSize();

            case X: return box.getXSize();
            case Z: return box.getZSize();
        }
    }

    static class EnqueuedBatch {
        final Consumer<ServerWorld> beforeBatch;
        final Collection<GameTestFunction> tests;

        EnqueuedBatch(Consumer<ServerWorld> beforeBatch, Collection<GameTestFunction> tests) {
            this.beforeBatch = beforeBatch;
            this.tests = tests;
        }
    }
}
