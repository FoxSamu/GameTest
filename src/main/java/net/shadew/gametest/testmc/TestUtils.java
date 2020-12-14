package net.shadew.gametest.testmc;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LecternBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class TestUtils {
    public static ITestReport logger = new LogTestReport();

    public static void startTest(TestInstance instance, BlockPos pos, TestTicker manager) {
        instance.startCountdown();
        manager.start(instance);

        instance.addListener(new ITestCallback() {
            @Override
            public void onStarted(TestInstance instance) {
                TestUtils.createBeacon(instance, Blocks.LIGHT_GRAY_STAINED_GLASS);
            }

            @Override
            public void onFailed(TestInstance instance) {
                TestUtils.createBeacon(instance, instance.isRequired() ? Blocks.RED_STAINED_GLASS : Blocks.ORANGE_STAINED_GLASS);
                TestUtils.createLectern(instance, Util.getInnermostMessage(instance.getError()));
                TestUtils.handleTestFail(instance);
            }

            @Override
            public void onPassed(TestInstance instance) {
                TestUtils.createBeacon(instance, Blocks.LIME_STAINED_GLASS);
            }
        });

        instance.init(pos, 2);
    }

    public static Collection<TestInstance> runTestBatches(Collection<TestBatch> batches, BlockPos pos, Rotation rotation, ServerWorld world, TestTicker manager, int testsPerRow) {
        TestExecutor exec = new TestExecutor(batches, pos, rotation, world, manager, testsPerRow);
        exec.run();
        return exec.getTests();
    }

    public static Collection<TestInstance> runTestFunctions(Collection<TestFunction> funcs, BlockPos pos, Rotation rotation, ServerWorld world, TestTicker manager, int testsPerRow) {
        return runTestBatches(createBatches(funcs), pos, rotation, world, manager, testsPerRow);
    }

    public static Collection<TestBatch> createBatches(Collection<TestFunction> functions) {
        Map<String, Collection<TestFunction>> byName = Maps.newHashMap();
        functions.forEach(func -> {
            String id = func.batch();
            Collection<TestFunction> funcs = byName.computeIfAbsent(id, k -> Lists.newArrayList());
            funcs.add(func);
        });

        return byName.keySet()
                     .stream()
                     .flatMap(name -> {
                         Collection<TestFunction> funcs = byName.get(name);
                         Consumer<ServerWorld> method = Tests.beforeBatch(name);
                         MutableInt part = new MutableInt();
                         return Streams.stream(Iterables.partition(funcs, 100))
                                       .map(funcSet -> new TestBatch(name + ":" + part.incrementAndGet(), funcSet, method));
                     })
                     .collect(Collectors.toList());
    }

    private static void handleTestFail(TestInstance instance) {
        Throwable exc = instance.getError();
        String error = (instance.isRequired() ? "" : "(optional) ")
                           + instance.getName() + " failed! "
                           + Util.getInnermostMessage(exc);

        sendMessage(instance.getWorld(), instance.isRequired() ? TextFormatting.RED : TextFormatting.YELLOW, error);

        if (exc instanceof TestBlockPosException) {
            TestBlockPosException posExc = (TestBlockPosException) exc;
            addTestMarker(instance.getWorld(), posExc.getPos(), posExc.getMarkerMessage());
        }

        logger.onTestFailed(instance);
    }

    @SuppressWarnings("deprecation")
    private static void createBeacon(TestInstance instance, Block glass) {
        ServerWorld world = instance.getWorld();

        BlockPos pos = instance.getPos();
        BlockPos local = new BlockPos(-1, -1, -1);

        BlockPos beaconPos = Template.getTransformedPos(pos.add(local), Mirror.NONE, instance.getRotation(), pos);
        world.setBlockState(beaconPos, Blocks.BEACON.getDefaultState().rotate(instance.getRotation()));

        BlockPos glassPos = beaconPos.add(0, 1, 0);
        world.setBlockState(glassPos, glass.getDefaultState());

        for (int x = -1; x <= 1; ++x) {
            for (int z = -1; z <= 1; ++z) {
                BlockPos ironPos = beaconPos.add(x, -1, z);
                world.setBlockState(ironPos, Blocks.IRON_BLOCK.getDefaultState());
            }
        }

    }

    @SuppressWarnings("deprecation")
    private static void createLectern(TestInstance instance, String text) {
        ServerWorld world = instance.getWorld();
        BlockPos pos = instance.getPos();
        BlockPos local = new BlockPos(-1, 1, -1);
        BlockPos lecternPos = Template.getTransformedPos(pos.add(local), Mirror.NONE, instance.getRotation(), pos);

        world.setBlockState(lecternPos, Blocks.LECTERN.getDefaultState().rotate(instance.getRotation()));

        BlockState lectern = world.getBlockState(lecternPos);
        ItemStack book = createBook(instance.getName(), instance.isRequired(), text);
        LecternBlock.tryPlaceBook(world, lecternPos, lectern, book);
    }

    private static ItemStack createBook(String path, boolean required, String text) {
        ItemStack item = new ItemStack(Items.WRITABLE_BOOK);

        StringBuffer msgBuilder = new StringBuffer();
        Arrays.stream(path.split("\\.")).forEach(p -> msgBuilder.append(p).append('\n'));
        if (!required) {
            msgBuilder.append("(optional)\n");
        }

        msgBuilder.append("-------------------\n");

        ListNBT pages = new ListNBT();
        pages.add(StringNBT.of(msgBuilder.toString() + text));
        item.setTagInfo("pages", pages);
        return item;
    }

    private static void sendMessage(ServerWorld world, TextFormatting format, String text) {
        world.getPlayers(player -> true)
             .forEach(player -> player.sendMessage(new StringTextComponent(text).formatted(format), Util.NIL_UUID));
    }

    public static void clearTestMarkers(ServerWorld world) {
        DebugPacketSender.clearGameTestMarkers(world);
    }

    private static void addTestMarker(ServerWorld world, BlockPos pos, String text) {
        DebugPacketSender.addGameTestMarker(world, pos, text, 0x80ff0000, Integer.MAX_VALUE);
    }

    public static void clearTests(ServerWorld world, BlockPos pos, TestTicker manager, int radius) {
        manager.clear();
        BlockPos lower = pos.add(-radius, 0, -radius);
        BlockPos upper = pos.add(radius, 0, radius);
        BlockPos.getAllInBox(lower, upper)
                .filter(p -> world.getBlockState(p).isIn(Blocks.STRUCTURE_BLOCK))
                .forEach(p -> {
                    StructureBlockTileEntity sb = (StructureBlockTileEntity) world.getTileEntity(p);
                    BlockPos sbPos = sb.getPos();
                    MutableBoundingBox box = TestStructureHelper.getTestBox(sb);
                    TestStructureHelper.clearArea(box, sbPos.getY(), world);
                });
    }
}
