package net.shadew.gametest.testmc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TestExecutor {
    private static final Logger LOGGER = LogManager.getLogger();

    private final BlockPos pos;
    private final ServerWorld world;
    private final TestTicker ticker;
    private final int testsPerRow;

    private final List<TestInstance> tests = Lists.newArrayList();
    private final Map<TestInstance, BlockPos> testToPos = Maps.newHashMap();

    private final List<Pair<TestBatch, Collection<TestInstance>>> batches = Lists.newArrayList();

    private TestResults currentBatchTests;
    private int currentBatchIndex = 0;

    private BlockPos.Mutable mpos;

    public TestExecutor(Collection<TestBatch> batches, BlockPos pos, Rotation rotation, ServerWorld world, TestTicker ticker, int testsPerRow) {
        this.mpos = pos.mutableCopy();
        this.pos = pos;
        this.world = world;
        this.ticker = ticker;
        this.testsPerRow = testsPerRow;
        batches.forEach(batch -> {
            Collection<TestInstance> instances = Lists.newArrayList();

            for (TestFunction func : batch.getFunctions()) {
                TestInstance instance = new TestInstance(func, rotation, world);
                instances.add(instance);
                tests.add(instance);
            }

            this.batches.add(Pair.of(batch, instances));
        });
    }

    public List<TestInstance> getTests() {
        return tests;
    }

    public void run() {
        runBatch(0);
    }

    private void runBatch(int index) {
        currentBatchIndex = index;
        currentBatchTests = new TestResults();

        if (index < batches.size()) {
            Pair<TestBatch, Collection<TestInstance>> testEntry = batches.get(currentBatchIndex);
            TestBatch batch = testEntry.getFirst();
            Collection<TestInstance> tests = testEntry.getSecond();

            alignTests(tests);
            batch.setWorld(world);

            String id = batch.getId();
            LOGGER.info("Running test batch '" + id + "' (" + tests.size() + " tests)...");

            tests.forEach(instance -> {
                currentBatchTests.add(instance);
                currentBatchTests.addListener(new ITestCallback() {
                    @Override
                    public void onStarted(TestInstance instance) {
                    }

                    @Override
                    public void onFailed(TestInstance instance) {
                        onTestCompleted(instance);
                    }

                    @Override
                    public void onPassed(TestInstance instance) {
                    }
                });

                BlockPos pos = testToPos.get(instance);
                TestUtils.startTest(instance, pos, ticker);
            });
        }
    }

    private void onTestCompleted(TestInstance instance) { // Why is this unused
        if (currentBatchTests.isDone()) {
            runBatch(currentBatchIndex + 1);
        }
    }

    private void alignTests(Collection<TestInstance> tests) {
        int testCounter = 0;
        AxisAlignedBB aabb = new AxisAlignedBB(mpos);

        for (TestInstance instance : tests) {
            BlockPos p = new BlockPos(mpos);

            StructureBlockTileEntity sb = TestStructureHelper.createTest(
                instance.getStructureName(),
                p,
                instance.getRotation(),
                2, world, true
            );

            AxisAlignedBB testAABB = TestStructureHelper.getTestAABB(sb);
            instance.setPos(sb.getPos());

            testToPos.put(instance, new BlockPos(mpos));

            aabb = aabb.union(testAABB);
            mpos.move((int) testAABB.getXSize() + 5, 0, 0);

            if (testCounter++ % testsPerRow == testsPerRow - 1) {
                mpos.move(0, 0, (int) aabb.getZSize() + 6);
                mpos.setX(pos.getX());

                aabb = new AxisAlignedBB(mpos);
            }
        }

    }
}
