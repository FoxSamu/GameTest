package net.shadew.gametest.framework.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;

import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.blockitem.block.props.DiagonalDirection;
import net.shadew.gametest.blockitem.block.props.TestBlockState;
import net.shadew.gametest.blockitem.tileentity.TestBlockTileEntity;
import net.shadew.gametest.framework.*;
import net.shadew.gametest.framework.api.output.ITestOutputInstance;
import net.shadew.gametest.framework.command.arguments.*;
import net.shadew.gametest.framework.config.GameTestConfig;
import net.shadew.gametest.framework.output.TestOutputManager;
import net.shadew.gametest.framework.run.TestRunManager;

public final class GameTestCommand {
    private static final SimpleCommandExceptionType NO_NEARBY_TEST = new SimpleCommandExceptionType(
        new LiteralMessage("No tests found here")
    );
    private static final SimpleCommandExceptionType NO_INSIDE_TEST = new SimpleCommandExceptionType(
        new LiteralMessage("No test found at this position")
    );
    private static final SimpleCommandExceptionType FAILED_BUILDING = new SimpleCommandExceptionType(
        new LiteralMessage("Failed to load and build test")
    );
    private static final SimpleCommandExceptionType FAILED_RELOADING = new SimpleCommandExceptionType(
        new LiteralMessage("Failed to reload test")
    );

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("test")
                    .then(makeCreate())
                    .then(makeLoad())
                    .then(makeRun())
                    .then(makeRunThis())
                    .then(makeRunBatch())
                    .then(makeRunClass())
                    .then(makeRunNamespace())
                    .then(makeRunAll())
                    .then(makeRunConfigured())
                    .then(makeRunThese())
        );
    }

    private static LiteralCommandNode<CommandSource> makeCreate() {
        LiteralArgumentBuilder<CommandSource> create = Commands.literal("create");
        create.then(
            Commands.argument("id", ResourceLocationArgument.resourceLocation())
                    .executes(printErrors(GameTestCommand::runCreate))
                    .then(
                        Commands.argument("size", SizeArgumentType.size())
                                .executes(printErrors(GameTestCommand::runCreate))
                    )
        );
        return create.build();
    }

    private static int runCreate(CommandContext<CommandSource> ctx) {
        ResourceLocation id = getProperty(ctx, "id", ResourceLocation.class, new ResourceLocation("null"));
        BlockPos size = getProperty(ctx, "size", BlockPos.class, new BlockPos(5, 5, 5));
        CommandSource src = ctx.getSource();

        return create(src, id, size.getX(), size.getY(), size.getZ());
    }

    private static int create(CommandSource src, ResourceLocation id, int w, int h, int d) {
        DiagonalDirection direction = DiagonalDirection.NW;
        Entity e = src.getEntity();
        if (e != null) {
            direction = DiagonalDirection.fromEntityFacing(e);
        }

        BlockPos pos = projectPos(src);
        ServerWorld world = src.getWorld();

        GameTestBuilder.buildNewTest(world, pos, direction, id, w, h, d);
        return 1;
    }

    private static LiteralCommandNode<CommandSource> makeLoad() {
        LiteralArgumentBuilder<CommandSource> create = Commands.literal("load");
        create.then(
            Commands.argument("function", FunctionArgumentType.function())
                    .executes(printErrors(GameTestCommand::runLoad))
        );
        return create.build();
    }

    private static int runLoad(CommandContext<CommandSource> ctx) {
        GameTestFunction fn = FunctionArgumentType.getFunction(ctx, "function");
        CommandSource src = ctx.getSource();

        return load(src, fn.getName());
    }

    private static int load(CommandSource src, ResourceLocation id) {
        DiagonalDirection direction = DiagonalDirection.NW;
        Entity e = src.getEntity();
        if (e != null) {
            direction = DiagonalDirection.fromEntityFacing(e);
        }

        BlockPos pos = projectPos(src);
        ServerWorld world = src.getWorld();

        GameTestBuilder.buildTest(world, pos, direction, id);
        return 1;
    }

    private static LiteralCommandNode<CommandSource> makeRun() {
        LiteralArgumentBuilder<CommandSource> create = Commands.literal("run");
        create.then(
            Commands.argument("function", FunctionArgumentType.function())
                    .executes(printErrors(GameTestCommand::runRun))
        );
        return create.build();
    }

    private static int runRun(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        GameTestFunction fn = FunctionArgumentType.getFunction(ctx, "function");
        CommandSource src = ctx.getSource();

        return run(src, fn.getName());
    }

    private static int run(CommandSource src, ResourceLocation id) throws CommandSyntaxException {
        DiagonalDirection direction = DiagonalDirection.NW;
        Entity e = src.getEntity();
        if (e != null) {
            direction = DiagonalDirection.fromEntityFacing(e);
        }

        BlockPos pos = projectPos(src);
        ServerWorld world = src.getWorld();

        GameTestBuilder.TestBuildResult result = GameTestBuilder.buildTest(world, pos, direction, id);
        if (!result.loaded) {
            throw FAILED_BUILDING.create();
        }

        result.testBlock.setOutput(TestOutputManager.openInstance());
        result.testBlock.enqueueStartTest(true);
        return 1;
    }

    private static LiteralCommandNode<CommandSource> makeRunThis() {
        LiteralArgumentBuilder<CommandSource> create = Commands.literal("runthis")
                                                               .executes(printErrors(GameTestCommand::runRunThis));
        return create.build();
    }

    private static int runRunThis(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        CommandSource src = ctx.getSource();
        return runThis(src);
    }

    private static int runThis(CommandSource src) throws CommandSyntaxException {
        BlockPos pos = new BlockPos(src.getPos());
        ServerWorld world = src.getWorld();

        Optional<TestBlockTileEntity> test = GameTestFinder.findTestBlockContaining(world, pos);

        if (!test.isPresent()) {
            throw NO_INSIDE_TEST.create();
        }

        TestBlockTileEntity testBlock = test.get();
        if (testBlock.getState() != TestBlockState.OFF) {
            if (!testBlock.getTemplateBlock().loadAndPlace()) {
                throw FAILED_RELOADING.create();
            }
        }
        testBlock.setOutput(TestOutputManager.openInstance());
        testBlock.enqueueStartTest(true);
        return 1;
    }

    private static LiteralCommandNode<CommandSource> makeRunBatch() {
        LiteralArgumentBuilder<CommandSource> create = Commands.literal("runbatch");
        create.then(
            Commands.argument("batch", BatchArgumentType.batch())
                    .executes(printErrors(GameTestCommand::runRunBatch))
                    .then(Commands.argument("testsPerRow", IntegerArgumentType.integer(1))
                                  .executes(printErrors(GameTestCommand::runRunBatch))
                                  .then(Commands.argument("simultaneousTests", IntegerArgumentType.integer(1))
                                                .executes(printErrors(GameTestCommand::runRunBatch))
                                  )
                    )
        );
        return create.build();
    }

    private static int runRunBatch(CommandContext<CommandSource> ctx) {
        ResourceLocation fn = BatchArgumentType.getBatch(ctx, "batch");
        int testsPerRow = getProperty(ctx, "testsPerRow", Integer.class, GameTestConfig.getDefaultTestsPerRow());
        int simultaneousTests = getProperty(ctx, "simultaneousTests", Integer.class, GameTestConfig.getDefaultSimultaneousTests());
        CommandSource src = ctx.getSource();

        return runBatch(src, fn, testsPerRow, simultaneousTests);
    }

    private static int runBatch(CommandSource src, ResourceLocation id, int testsPerRow, int simultaneousTests) {
        DiagonalDirection direction = DiagonalDirection.NW;
        Entity e = src.getEntity();
        if (e != null) {
            direction = DiagonalDirection.fromEntityFacing(e);
        }

        BlockPos pos = projectPos(src);
        ServerWorld world = src.getWorld();

        GameTestExecutor executor = new GameTestExecutor(
            world,
            GameTestRegistry.getAllOfBatch(id),
            testsPerRow,
            simultaneousTests,
            pos,
            direction
        );
        executor.setOutput(TestOutputManager.openInstance());
        GameTestManager.addExecutor(executor);
        return 1;
    }

    private static LiteralCommandNode<CommandSource> makeRunClass() {
        LiteralArgumentBuilder<CommandSource> create = Commands.literal("runclass");
        create.then(
            Commands.argument("class", ClassArgumentType.testClass())
                    .executes(printErrors(GameTestCommand::runRunClass))
                    .then(Commands.argument("testsPerRow", IntegerArgumentType.integer(1))
                                  .executes(printErrors(GameTestCommand::runRunClass))
                                  .then(Commands.argument("simultaneousTests", IntegerArgumentType.integer(1))
                                                .executes(printErrors(GameTestCommand::runRunClass))
                                  )
                    )
        );
        return create.build();
    }

    private static int runRunClass(CommandContext<CommandSource> ctx) {
        ResourceLocation fn = ClassArgumentType.getClass(ctx, "class");
        int testsPerRow = getProperty(ctx, "testsPerRow", Integer.class, GameTestConfig.getDefaultTestsPerRow());
        int simultaneousTests = getProperty(ctx, "simultaneousTests", Integer.class, GameTestConfig.getDefaultSimultaneousTests());
        CommandSource src = ctx.getSource();

        return runClass(src, fn, testsPerRow, simultaneousTests);
    }

    private static int runClass(CommandSource src, ResourceLocation id, int testsPerRow, int simultaneousTests) {
        DiagonalDirection direction = DiagonalDirection.NW;
        Entity e = src.getEntity();
        if (e != null) {
            direction = DiagonalDirection.fromEntityFacing(e);
        }

        BlockPos pos = projectPos(src);
        ServerWorld world = src.getWorld();

        GameTestExecutor executor = new GameTestExecutor(
            world,
            GameTestRegistry.getAllOfClass(id),
            testsPerRow,
            simultaneousTests,
            pos,
            direction
        );
        executor.setOutput(TestOutputManager.openInstance());
        GameTestManager.addExecutor(executor);
        return 1;
    }

    private static LiteralCommandNode<CommandSource> makeRunNamespace() {
        LiteralArgumentBuilder<CommandSource> create = Commands.literal("runnamespace");
        create.then(
            Commands.argument("namespace", NamespaceArgumentType.namespace())
                    .executes(printErrors(GameTestCommand::runRunNamespace))
                    .then(Commands.argument("testsPerRow", IntegerArgumentType.integer(1))
                                  .executes(printErrors(GameTestCommand::runRunNamespace))
                                  .then(Commands.argument("simultaneousTests", IntegerArgumentType.integer(1))
                                                .executes(printErrors(GameTestCommand::runRunNamespace))
                                  )
                    )
        );
        return create.build();
    }

    private static int runRunNamespace(CommandContext<CommandSource> ctx) {
        String fn = NamespaceArgumentType.getNamespace(ctx, "namespace");
        int testsPerRow = getProperty(ctx, "testsPerRow", Integer.class, GameTestConfig.getDefaultTestsPerRow());
        int simultaneousTests = getProperty(ctx, "simultaneousTests", Integer.class, GameTestConfig.getDefaultSimultaneousTests());
        CommandSource src = ctx.getSource();

        return runNamespace(src, fn, testsPerRow, simultaneousTests);
    }

    private static int runNamespace(CommandSource src, String id, int testsPerRow, int simultaneousTests) {
        DiagonalDirection direction = DiagonalDirection.NW;
        Entity e = src.getEntity();
        if (e != null) {
            direction = DiagonalDirection.fromEntityFacing(e);
        }

        BlockPos pos = projectPos(src);
        ServerWorld world = src.getWorld();

        GameTestExecutor executor = new GameTestExecutor(
            world,
            GameTestRegistry.getAllOfNamespace(id),
            testsPerRow,
            simultaneousTests,
            pos,
            direction
        );
        executor.setOutput(TestOutputManager.openInstance());
        GameTestManager.addExecutor(executor);
        return 1;
    }

    private static LiteralCommandNode<CommandSource> makeRunAll() {
        LiteralArgumentBuilder<CommandSource> create = Commands.literal("runall");
        create.executes(printErrors(GameTestCommand::runRunAll));
        create.then(Commands.argument("testsPerRow", IntegerArgumentType.integer(1))
                            .executes(printErrors(GameTestCommand::runRunAll))
                            .then(Commands.argument("simultaneousTests", IntegerArgumentType.integer(1))
                                          .executes(printErrors(GameTestCommand::runRunAll))
                            )
        );
        return create.build();
    }

    private static int runRunAll(CommandContext<CommandSource> ctx) {
        int testsPerRow = getProperty(ctx, "testsPerRow", Integer.class, GameTestConfig.getDefaultTestsPerRow());
        int simultaneousTests = getProperty(ctx, "simultaneousTests", Integer.class, GameTestConfig.getDefaultSimultaneousTests());
        CommandSource src = ctx.getSource();

        return runAll(src, testsPerRow, simultaneousTests);
    }

    private static int runAll(CommandSource src, int testsPerRow, int simultaneousTests) {
        DiagonalDirection direction = DiagonalDirection.NW;
        Entity e = src.getEntity();
        if (e != null) {
            direction = DiagonalDirection.fromEntityFacing(e);
        }

        BlockPos pos = projectPos(src);
        ServerWorld world = src.getWorld();

        GameTestExecutor executor = new GameTestExecutor(
            world,
            GameTestRegistry.getAll(),
            testsPerRow,
            simultaneousTests,
            pos,
            direction
        );
        executor.setOutput(TestOutputManager.openInstance());
        GameTestManager.addExecutor(executor);
        return 1;
    }

    private static LiteralCommandNode<CommandSource> makeRunConfigured() {
        LiteralArgumentBuilder<CommandSource> create = Commands.literal("runconfigured");
        create.executes(printErrors(GameTestCommand::runRunConfigured));
        create.then(Commands.argument("testsPerRow", IntegerArgumentType.integer(1))
                            .executes(printErrors(GameTestCommand::runRunConfigured))
                            .then(Commands.argument("simultaneousTests", IntegerArgumentType.integer(1))
                                          .executes(printErrors(GameTestCommand::runRunConfigured))
                            )
        );
        return create.build();
    }

    private static int runRunConfigured(CommandContext<CommandSource> ctx) {
        int testsPerRow = getProperty(ctx, "testsPerRow", Integer.class, GameTestConfig.getDefaultTestsPerRow());
        int simultaneousTests = getProperty(ctx, "simultaneousTests", Integer.class, GameTestConfig.getDefaultSimultaneousTests());
        CommandSource src = ctx.getSource();

        return runConfigured(src, testsPerRow, simultaneousTests);
    }

    private static int runConfigured(CommandSource src, int testsPerRow, int simultaneousTests) {
        DiagonalDirection direction = DiagonalDirection.NW;
        Entity e = src.getEntity();
        if (e != null) {
            direction = DiagonalDirection.fromEntityFacing(e);
        }

        BlockPos pos = projectPos(src);
        ServerWorld world = src.getWorld();

        GameTestExecutor executor = new GameTestExecutor(
            world,
            TestRunManager.collection(),
            testsPerRow,
            simultaneousTests,
            pos,
            direction
        );
        executor.setOutput(TestOutputManager.openInstance());
        GameTestManager.addExecutor(executor);
        return 1;
    }

    private static LiteralCommandNode<CommandSource> makeRunThese() {
        LiteralArgumentBuilder<CommandSource> create = Commands.literal("runthese");
        create.executes(printErrors(GameTestCommand::runRunThese))
              .then(Commands.argument("radius", IntegerArgumentType.integer(1, 400))
                            .executes(printErrors(GameTestCommand::runRunThese)));
        return create.build();
    }

    private static int runRunThese(CommandContext<CommandSource> ctx) throws CommandSyntaxException {
        int radius = getProperty(ctx, "radius", Integer.class, GameTestConfig.getDefaultRunThisRadius());
        CommandSource src = ctx.getSource();

        return runThese(src, radius);
    }

    private static int runThese(CommandSource src, int radius) throws CommandSyntaxException {
        BlockPos pos = new BlockPos(src.getPos());
        ServerWorld world = src.getWorld();

        Collection<TestBlockTileEntity> near = GameTestFinder.findTestBlocks(world, pos, radius);
        if (near.isEmpty()) {
            throw NO_NEARBY_TEST.create();
        }

        ITestOutputInstance out = TestOutputManager.openInstance();
        for (TestBlockTileEntity testBlock : near) {
            if (testBlock.getState() != TestBlockState.OFF) {
                if (!testBlock.getTemplateBlock().loadAndPlace()) {
                    throw FAILED_RELOADING.create();
                }
            }
            testBlock.setOutput(out);
            testBlock.enqueueStartTest(true);
        }
        return 1;
    }

    private static BlockPos projectPos(CommandSource src) {
        BlockPos srcPos = new BlockPos(src.getPos());
        ServerWorld world = src.getWorld();
        int h = world.getHeight(Heightmap.Type.WORLD_SURFACE, srcPos.getX(), srcPos.getZ());
        return new BlockPos(srcPos.getX(), h, srcPos.getZ());
    }

    private static <T> T getProperty(CommandContext<?> ctx, String arg, Class<T> cls, T orElse) {
        try {
            return ctx.getArgument(arg, cls);
        } catch (IllegalArgumentException exc) {
            return orElse;
        }
    }

    private static Command<CommandSource> printErrors(Command<CommandSource> cmd) {
        return ctx -> {
            try {
                return cmd.run(ctx);
            } catch (CommandSyntaxException exc) {
                throw exc;
            } catch (Throwable exc) {
                GameTestMod.LOGGER.error("Failed executing command", exc);
                throw exc;
            }
        };
    }
}
