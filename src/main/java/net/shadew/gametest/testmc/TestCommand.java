package net.shadew.gametest.testmc;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.data.NBTToSNBTConverter;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class TestCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("test")

                    .then(Commands.literal("runthis").executes(ctx -> executeRunThis(ctx.getSource())))

                    .then(Commands.literal("runthese").executes(ctx -> executeRunThese(ctx.getSource())))

                    .then(
                        Commands.literal("runfailed").executes(
                            ctx -> executeRunFailed(ctx.getSource(), false, 0, 8)
                        ).then(
                            Commands.argument("onlyRequiredTests", BoolArgumentType.bool()).executes(
                                ctx -> executeRunFailed(
                                    ctx.getSource(),
                                    BoolArgumentType.getBool(ctx, "onlyRequiredTests"),
                                    0, 8
                                )
                            ).then(
                                Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes(
                                    ctx -> executeRunFailed(
                                        ctx.getSource(),
                                        BoolArgumentType.getBool(ctx, "onlyRequiredTests"),
                                        IntegerArgumentType.getInteger(ctx, "rotationSteps"),
                                        8
                                    )
                                ).then(
                                    Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes(
                                        ctx -> executeRunFailed(
                                            ctx.getSource(),
                                            BoolArgumentType.getBool(ctx, "onlyRequiredTests"),
                                            IntegerArgumentType.getInteger(ctx, "rotationSteps"),
                                            IntegerArgumentType.getInteger(ctx, "testsPerRow")
                                        )
                                    )
                                )
                            )
                        )
                    )

                    .then(
                        Commands.literal("run").then(
                            Commands.argument("testName", TestFunctionArgument.testFunction()).executes(
                                ctx -> executeRun(
                                    ctx.getSource(),
                                    TestFunctionArgument.getFunction(ctx, "testName"),
                                    0
                                )
                            ).then(
                                Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes(
                                    ctx -> executeRun(
                                        ctx.getSource(),
                                        TestFunctionArgument.getFunction(ctx, "testName"),
                                        IntegerArgumentType.getInteger(ctx, "rotationSteps")
                                    )
                                )
                            )
                        )
                    )

                    .then(
                        Commands.literal("runall").executes(
                            ctx -> executeRunAll(ctx.getSource(), 0, 8)
                        ).then(
                            Commands.argument("testClassName", TestClassArgument.testClass()).executes(
                                ctx -> executeRunAll(
                                    ctx.getSource(),
                                    TestClassArgument.getTestClass(ctx, "testClassName"),
                                    0, 8
                                )
                            ).then(
                                Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes(
                                    ctx -> executeRunAll(
                                        ctx.getSource(),
                                        TestClassArgument.getTestClass(ctx, "testClassName"),
                                        IntegerArgumentType.getInteger(ctx, "rotationSteps"),
                                        8
                                    )
                                ).then(
                                    Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes(
                                        ctx -> executeRunAll(
                                            ctx.getSource(),
                                            TestClassArgument.getTestClass(ctx, "testClassName"),
                                            IntegerArgumentType.getInteger(ctx, "rotationSteps"),
                                            IntegerArgumentType.getInteger(ctx, "testsPerRow")
                                        )
                                    )
                                )
                            )
                        ).then(
                            Commands.argument("rotationSteps", IntegerArgumentType.integer()).executes(
                                ctx -> executeRunAll(
                                    ctx.getSource(),
                                    IntegerArgumentType.getInteger(ctx, "rotationSteps"),
                                    8
                                )
                            ).then(
                                Commands.argument("testsPerRow", IntegerArgumentType.integer()).executes(
                                    ctx -> executeRunAll(
                                        ctx.getSource(),
                                        IntegerArgumentType.getInteger(ctx, "rotationSteps"),
                                        IntegerArgumentType.getInteger(ctx, "testsPerRow")
                                    )
                                )
                            )
                        )
                    )

                    .then(
                        Commands.literal("export")
                                .then(Commands.argument("testName", StringArgumentType.word()).executes(
                                    ctx -> executeExport(ctx.getSource(), StringArgumentType.getString(ctx, "testName"))
                                ))
                    )

                    .then(Commands.literal("exportthis").executes(ctx -> executeExportThis(ctx.getSource())))

                    .then(
                        Commands.literal("import")
                                .then(Commands.argument("testName", StringArgumentType.word()).executes(
                                    ctx -> executeImport(ctx.getSource(), StringArgumentType.getString(ctx, "testName"))
                                ))
                    )

                    .then(
                        Commands.literal("pos")
                                .executes(ctx -> executePos(ctx.getSource(), "pos"))
                                .then(
                                    Commands.argument("var", StringArgumentType.word()).executes(
                                        ctx -> executePos(ctx.getSource(), StringArgumentType.getString(ctx, "var"))
                                    )
                                )
                    )

                    .then(
                        Commands.literal("create").then(
                            Commands.argument("testName", StringArgumentType.word()).executes(
                                ctx -> executeCreate(
                                    ctx.getSource(),
                                    StringArgumentType.getString(ctx, "testName"),
                                    5, 5, 5
                                )
                            ).then(
                                Commands.argument("width", IntegerArgumentType.integer()).executes(
                                    ctx -> executeCreate(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "testName"),
                                        IntegerArgumentType.getInteger(ctx, "width"),
                                        IntegerArgumentType.getInteger(ctx, "width"),
                                        IntegerArgumentType.getInteger(ctx, "width")
                                    )
                                ).then(
                                    Commands.argument("height", IntegerArgumentType.integer()).then(
                                        Commands.argument("depth", IntegerArgumentType.integer()).executes(
                                            ctx -> executeCreate(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "testName"),
                                                IntegerArgumentType.getInteger(ctx, "width"),
                                                IntegerArgumentType.getInteger(ctx, "height"),
                                                IntegerArgumentType.getInteger(ctx, "depth")
                                            )
                                        )
                                    ).then(
                                        Commands.argument("baseBlock", BlockStateArgument.blockState()).executes(
                                            ctx -> executeCreate(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "testName"),
                                                IntegerArgumentType.getInteger(ctx, "width"),
                                                IntegerArgumentType.getInteger(ctx, "height"),
                                                IntegerArgumentType.getInteger(ctx, "depth"),
                                                BlockStateArgument.getBlockState(ctx, "baseBlock")
                                            )
                                        )
                                    )
                                ).then(
                                    Commands.argument("baseBlock", BlockStateArgument.blockState()).executes(
                                        ctx -> executeCreate(
                                            ctx.getSource(),
                                            StringArgumentType.getString(ctx, "testName"),
                                            IntegerArgumentType.getInteger(ctx, "width"),
                                            IntegerArgumentType.getInteger(ctx, "width"),
                                            IntegerArgumentType.getInteger(ctx, "width"),
                                            BlockStateArgument.getBlockState(ctx, "baseBlock")
                                        )
                                    )
                                )
                            ).then(
                                Commands.argument("baseBlock", BlockStateArgument.blockState()).executes(
                                    ctx -> executeCreate(
                                        ctx.getSource(),
                                        StringArgumentType.getString(ctx, "testName"),
                                        5, 5, 5,
                                        BlockStateArgument.getBlockState(ctx, "baseBlock")
                                    )
                                )
                            )
                        )
                    )

                    .then(Commands.literal("clearall")
                                  .executes(ctx -> executeClearAll(ctx.getSource(), 200))
                                  .then(Commands.argument("radius", IntegerArgumentType.integer()).executes(
                                      ctx -> executeClearAll(
                                          ctx.getSource(),
                                          IntegerArgumentType.getInteger(ctx, "radius")
                                      )
                                  ))
                    ));
    }

    private static int executeCreate(CommandSource src, String name, int w, int h, int d) {
        return executeCreate(
            src, name, w, h, d,
            new BlockStateInput(Blocks.POLISHED_ANDESITE.getDefaultState(), Collections.emptySet(), null)
        );
    }

    private static int executeCreate(CommandSource src, String name, int w, int h, int d, BlockStateInput input) {
        if (w <= 48 && h <= 48 && d <= 48) {
            ServerWorld world = src.getWorld();
            BlockPos srcPos = new BlockPos(src.getPos());

            BlockPos pos = new BlockPos(srcPos.getX(), src.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, srcPos).getY(), srcPos.getZ() + 3);
            TestStructureHelper.createTestArea(name.toLowerCase(), pos, new BlockPos(w, h, d), Rotation.NONE, world);

            for (int x = 0; x < w; ++x) {
                for (int z = 0; z < d; ++z) {
                    BlockPos off = new BlockPos(pos.getX() + x, pos.getY() + 1, pos.getZ() + z);
                    input.place(world, off, 2);
                }
            }

            TestStructureHelper.placeStartButton(pos, new BlockPos(1, 0, -1), Rotation.NONE, world);
            return 0;
        } else {
            src.sendErrorMessage(new StringTextComponent("Structure must be less than 48 blocks long on each axis"));
            return 1;
        }
    }

    private static int executePos(CommandSource src, String varName) throws CommandSyntaxException {
        BlockRayTraceResult rtr = (BlockRayTraceResult) src.asPlayer().pick(10, 1, false);
        BlockPos pos = rtr.getPos();
        ServerWorld world = src.getWorld();

        Optional<BlockPos> owner = TestStructureHelper.findOwnerStructureBlock(pos, 15, world);
        if (!owner.isPresent()) {
            owner = TestStructureHelper.findOwnerStructureBlock(pos, 200, world);
        }

        if (!owner.isPresent()) {
            src.sendErrorMessage(new StringTextComponent("Can't find a structure block that contains the targeted pos " + pos));
            return 0;
        } else {
            StructureBlockTileEntity sb = (StructureBlockTileEntity) world.getTileEntity(owner.get());
            assert sb != null;

            BlockPos local = pos.subtract(owner.get());
            String coordString = local.getX() + ", " + local.getY() + ", " + local.getZ();
            String path = sb.getStructurePath();

            ITextComponent msg = new StringTextComponent(coordString).setStyle(
                Style.EMPTY.withBold(true)
                           .withColor(TextFormatting.GREEN)
                           .withHoverEvent(new HoverEvent(
                               HoverEvent.Action.SHOW_TEXT,
                               new StringTextComponent("Click to copy to clipboard")
                           ))
                           .withClickEvent(new ClickEvent(
                                               ClickEvent.Action.COPY_TO_CLIPBOARD,
                                               "BlockPos " + varName + " = new BlockPos(" + coordString + ");"
                                           )
                           ));
            src.sendFeedback(new StringTextComponent("Position relative to " + path + ": ").append(msg), false);
            DebugPacketSender.addGameTestMarker(world, new BlockPos(pos), coordString, 0x8000ff00, 10000);
            return 1;
        }
    }

    private static int executeRunThis(CommandSource src) {
        BlockPos pos = new BlockPos(src.getPos());
        ServerWorld world = src.getWorld();

        BlockPos structPos = TestStructureHelper.findNearestStructureBlock(pos, 15, world);
        if (structPos == null) {
            sendMessage(world, "Couldn't find any structure block within 15 radius", TextFormatting.RED);
            return 0;
        } else {
            TestUtils.clearTestMarkers(world);
            run(world, structPos, null);
            return 1;
        }
    }

    private static int executeRunThese(CommandSource src) {
        BlockPos pos = new BlockPos(src.getPos());
        ServerWorld world = src.getWorld();
        Collection<BlockPos> sbs = TestStructureHelper.findStructureBlocks(pos, 200, world);
        if (sbs.isEmpty()) {
            sendMessage(world, "Couldn't find any structure blocks within 200 block radius", TextFormatting.RED);
            return 1;
        } else {
            TestUtils.clearTestMarkers(world);
            sendMessage(src, "Running " + sbs.size() + " tests...");

            TestResults results = new TestResults();
            sbs.forEach(sbPos -> run(world, sbPos, results));
            return 0;
        }
    }

    private static void run(ServerWorld world, BlockPos pos, @Nullable TestResults results) {
        StructureBlockTileEntity sb = (StructureBlockTileEntity) world.getTileEntity(pos);
        assert sb != null;

        String path = sb.getStructurePath();
        TestFunction func = Tests.byName(path);
        TestInstance instance = new TestInstance(func, sb.getRotation(), world);

        if (results != null) {
            results.add(instance);
            instance.addListener(new Callback(world, results));
        }

        setWorld(func, world);
        AxisAlignedBB aabb = TestStructureHelper.getTestAABB(sb);
        BlockPos root = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);
        TestUtils.startTest(instance, root, TestTicker.INSTANCE);
    }

    private static void onCompletion(ServerWorld world, TestResults results) {
        if (results.isDone()) {
            sendMessage(world, "GameTest done! " + results.getTestCount() + " tests were run", TextFormatting.WHITE);

            if (results.failed()) {
                sendMessage(world, results.getFailedRequiredTestCount() + " required tests failed :(", TextFormatting.RED);
            } else {
                sendMessage(world, "All required tests passed :)", TextFormatting.GREEN);
            }

            if (results.hasFailedOptionalTests()) {
                sendMessage(world, results.getFailedOptionalTestCount() + " optional tests failed :|", TextFormatting.YELLOW);
            }
        }
    }

    private static int executeClearAll(CommandSource src, int radius) {
        ServerWorld world = src.getWorld();
        TestUtils.clearTestMarkers(world);
        BlockPos pos = new BlockPos(
            src.getPos().x,
            src.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, new BlockPos(src.getPos())).getY(),
            src.getPos().z
        );
        TestUtils.clearTests(world, pos, TestTicker.INSTANCE, MathHelper.clamp(radius, 0, 1024));
        return 1;
    }

    private static int executeRun(CommandSource src, TestFunction function, int rotSteps) {
        ServerWorld world = src.getWorld();
        BlockPos pos = new BlockPos(src.getPos());

        int height = src.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, pos).getY();
        BlockPos hpos = new BlockPos(pos.getX(), height, pos.getZ() + 3);

        TestUtils.clearTestMarkers(world);
        setWorld(function, world);

        TestInstance instance = new TestInstance(function, TestStructureHelper.getRotation(rotSteps), world);
        TestUtils.startTest(instance, hpos, TestTicker.INSTANCE);
        return 1;
    }

    private static void setWorld(TestFunction function, ServerWorld world) {
        Consumer<ServerWorld> worldSetter = Tests.beforeBatch(function.batch());
        if (worldSetter != null) {
            worldSetter.accept(world);
        }
    }

    private static int executeRunAll(CommandSource src, int rotation, int testsPerRow) {
        TestUtils.clearTestMarkers(src.getWorld());

        Collection<TestFunction> tests = Tests.functions();
        sendMessage(src, "Running all " + tests.size() + " tests...");

        Tests.clearFailed();
        run(src, tests, rotation, testsPerRow);
        return 1;
    }

    private static int executeRunAll(CommandSource src, String batch, int rotation, int testsPerRow) {
        TestUtils.clearTestMarkers(src.getWorld());

        Collection<TestFunction> tests = Tests.functions(batch);
        sendMessage(src, "Running " + tests.size() + " tests from " + batch + "...");

        Tests.clearFailed();
        run(src, tests, rotation, testsPerRow);
        return 1;
    }

    private static int executeRunFailed(CommandSource src, boolean requiredOnly, int rotation, int testsPerRow) {
        Collection<TestFunction> tests;
        if (requiredOnly) {
            tests = Tests.failed()
                         .stream()
                         .filter(TestFunction::required)
                         .collect(Collectors.toList());
        } else {
            tests = Tests.failed();
        }

        if (tests.isEmpty()) {
            sendMessage(src, "No failed tests to rerun");
            return 0;
        } else {
            TestUtils.clearTestMarkers(src.getWorld());

            sendMessage(src, "Rerunning " + tests.size() + " failed tests (" + (requiredOnly ? "only required tests" : "including optional tests") + ")");
            run(src, tests, rotation, testsPerRow);
            return 1;
        }
    }

    private static void run(CommandSource ctx, Collection<TestFunction> tests, int rotSteps, int testsPerRow) {
        BlockPos srcPos = new BlockPos(ctx.getPos());
        BlockPos pos = new BlockPos(
            srcPos.getX(),
            ctx.getWorld().getHeight(Heightmap.Type.WORLD_SURFACE, srcPos).getY(),
            srcPos.getZ() + 3
        );

        ServerWorld world = ctx.getWorld();
        Rotation rot = TestStructureHelper.getRotation(rotSteps);

        Collection<TestInstance> instances = TestUtils.runTestFunctions(tests, pos, rot, world, TestTicker.INSTANCE, testsPerRow);

        TestResults results = new TestResults(instances);
        results.addListener(new Callback(world, results));
        results.onFailed(failed -> Tests.addFailed(failed.getFunction()));
    }

    private static void sendMessage(CommandSource src, String text) {
        src.sendFeedback(new StringTextComponent(text), false);
    }

    private static int executeExportThis(CommandSource src) {
        BlockPos pos = new BlockPos(src.getPos());
        ServerWorld world = src.getWorld();
        BlockPos testPos = TestStructureHelper.findNearestStructureBlock(pos, 15, world);

        if (testPos == null) {
            sendMessage(world, "Couldn't find any structure block within 15 radius", TextFormatting.RED);
            return 0;
        } else {
            StructureBlockTileEntity sb = (StructureBlockTileEntity) world.getTileEntity(testPos);
            assert sb != null;

            String path = sb.getStructurePath();
            return executeExport(src, path);
        }
    }

    private static int executeExport(CommandSource src, String path) {
        Path testsDir = Paths.get(TestStructureHelper.testStructuresDirectoryName);

        ResourceLocation id = new ResourceLocation("minecraft", path);
        Path nbtPath = src.getWorld().getStructureTemplateManager().resolvePathStructures(id, ".nbt");
        Path snbtPath = NBTToSNBTConverter.convertNbtToSnbt(nbtPath, path, testsDir);

        if (snbtPath == null) {
            sendMessage(src, "Failed to export " + nbtPath);
            return 1;
        } else {
            try {
                Files.createDirectories(snbtPath.getParent());
            } catch (IOException exc) {
                sendMessage(src, "Could not create folder " + snbtPath.getParent());
                exc.printStackTrace();
                return 1;
            }

            sendMessage(src, "Exported " + path + " to " + snbtPath.toAbsolutePath());
            return 0;
        }
    }

    private static int executeImport(CommandSource src, String path) {
        ResourceLocation id = new ResourceLocation("minecraft", path);
        Path snbtPath = Paths.get(TestStructureHelper.testStructuresDirectoryName, path + ".snbt");
        Path nbtPath = src.getWorld().getStructureTemplateManager().resolvePathStructures(id, ".nbt");

        try {
            BufferedReader reader = Files.newBufferedReader(snbtPath);
            String snbt = IOUtils.toString(reader);
            Files.createDirectories(nbtPath.getParent());

            try (OutputStream out = Files.newOutputStream(nbtPath)) {
                CompressedStreamTools.writeCompressed(JsonToNBT.getTagFromJson(snbt), out);
            }

            sendMessage(src, "Imported to " + nbtPath.toAbsolutePath());
            return 0;
        } catch (CommandSyntaxException | IOException exc) {
            System.err.println("Failed to load structure " + path);
            exc.printStackTrace();
            return 1;
        }
    }

    private static void sendMessage(ServerWorld world, String message, TextFormatting format) {
        world.getPlayers(player -> true)
             .forEach(player -> player.sendMessage(new StringTextComponent(format + message), Util.NIL_UUID));
    }

    public static class Callback implements ITestCallback {
        private final ServerWorld world;
        private final TestResults tests;

        public Callback(ServerWorld world, TestResults results) {
            this.world = world;
            this.tests = results;
        }

        @Override
        public void onStarted(TestInstance instance) {
        }

        @Override
        public void onFailed(TestInstance instance) {
            TestCommand.onCompletion(world, tests);
        }

        @Override
        public void onPassed(TestInstance instance) {
            TestCommand.onCompletion(world, tests);
        }
    }
}
