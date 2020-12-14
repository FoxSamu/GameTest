package net.shadew.gametest.framework.api;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.shadew.gametest.framework.api.exception.AssertException;
import net.shadew.gametest.framework.api.exception.LocalAssertException;
import net.shadew.gametest.framework.api.exception.TestException;
import net.shadew.gametest.util.Utils;

/**
 * A test controller provides the main interaction with a test instance at runtime. It's a required argument in each
 * test method. A test controller provides interaction with the test world, it can get and change blocks, and summon
 * entities, while managing transformation of relative coordinates to absolute coordinates. A test controller also
 * provides common assertions, such as checking for blocks or entities at certain positions.
 *
 * @author Shadew
 * @since 1.0
 */
public final class TestController {
    private final ITestInstance instance;
    private final ServerWorld world;
    private final Rotation rot;
    private final Rotation invRot;
    private final BlockPos origin;

    /**
     * Creates a test controller for a test instance. Eventually, all operations performed with a controller are all
     * performed via the given test instance. This constructor is not needed in test methods, as a test controller is
     * already given as a parameter. In some cases though, a test controller for some instance might be needed
     * elsewhere, then this constructor is an easy way to obtain a controller for that instance.
     *
     * @param instance The instance to create this controller for. This instance must not be null.
     * @throws NullPointerException When the given instance is null.
     * @since 1.0
     */
    public TestController(ITestInstance instance) {
        if (instance == null) {
            throw new NullPointerException("instance = null");
        }

        this.instance = instance;
        this.world = instance.getWorld();
        this.rot = instance.getRotation();
        this.origin = instance.getOriginPos();

        if (rot == Rotation.COUNTERCLOCKWISE_90) invRot = Rotation.CLOCKWISE_90;
        else if (rot == Rotation.CLOCKWISE_90) invRot = Rotation.COUNTERCLOCKWISE_90;
        else invRot = rot; // 0/180 degrees, they are their own inverses
    }

    /**
     * Returns the {@link ITestInstance} that is controlled by this controller. This is always the instance given as
     * parameter in the constructor of this class, and is never null.
     *
     * @return The instance controlled by this controller.
     *
     * @since 1.0
     */
    public ITestInstance getInstance() {
        return instance;
    }

    /**
     * Creates a new {@link ITestSequence} that is managed by the wrapped {@link ITestInstance}. The sequence is created
     * via {@link ITestInstance#newSequence()} directly, and is never null.
     *
     * @return The new sequence instance.
     *
     * @since 1.0
     */
    public ITestSequence newSequence() {
        return instance.newSequence();
    }

    /**
     * Runs the given task at the given tick number. The tick number is relative to the starting time of the test
     * instance. The performed task can throw any unchecked exception. When an unchecked exception is thrown in this
     * task, the test will stop running and fail with that exception.
     *
     * @param tick The tick to run at, relative to the starting time of the test. This must be a non-negative number.
     * @param task The task to run. This must not be null.
     * @since 1.0
     */
    public void runAtTick(long tick, Runnable task) {
        if (tick < 0) throw new IllegalArgumentException("tick < 0");
        if (task == null) throw new NullPointerException("task = null");
        instance.runAtTick(tick, task);
    }

    /**
     * Returns the world of the underlying {@link ITestInstance}, which is the world the test runs in. The returned
     * world is never null. A call to this method is equivalent to calling {@link #getInstance()}{@code .}{@link
     * ITestInstance#getWorld() getWorld()}.
     *
     * @return The world this test runs in.
     * @since 1.0
     */
    public ServerWorld getWorld() {
        return world;
    }

    public <T extends Comparable<T>> void assertBlockHas(BlockPos pos, Property<T> prop, T value) {
        assertBlockMatches(pos, (p, state) -> state.contains(prop) && state.get(prop) == value, "State has incorrect property " + prop.getName());
    }

    public <T extends Comparable<T>> void assertBlockHas(int x, int y, int z, Property<T> prop, T value) {
        assertBlockMatches(x, y, z, (p, state) -> state.contains(prop) && state.get(prop) == value, "State has incorrect property " + prop.getName());
    }

    public <T extends Comparable<T>> void assertBlocksHave(Stream<BlockPos> pos, Property<T> prop, T value) {
        assertBlocksMatch(pos, (p, state) -> state.contains(prop) && state.get(prop) == value, "State has incorrect property " + prop.getName());
    }

    public <T extends Comparable<T>> void assertBlockHas(BlockPos pos, Property<T> prop, Predicate<T> allowedValues) {
        assertBlockMatches(pos, (p, state) -> state.contains(prop) && allowedValues.test(state.get(prop)), "State has incorrect property " + prop.getName());
    }

    public <T extends Comparable<T>> void assertBlockHas(int x, int y, int z, Property<T> prop, Predicate<T> allowedValues) {
        assertBlockMatches(x, y, z, (p, state) -> state.contains(prop) && allowedValues.test(state.get(prop)), "State has incorrect property " + prop.getName());
    }

    public <T extends Comparable<T>> void assertBlocksHave(Stream<BlockPos> pos, Property<T> prop, Predicate<T> allowedValues) {
        assertBlocksMatch(pos, (p, state) -> state.contains(prop) && allowedValues.test(state.get(prop)), "State has incorrect property " + prop.getName());
    }

    public void assertBlockIs(BlockPos pos, BlockState blockState) {
        assertBlockMatches(pos, (p, state) -> state == blockState, "Expected " + blockState.getBlock().getRegistryName() + " state");
    }

    public void assertBlockIs(int x, int y, int z, BlockState blockState) {
        assertBlockMatches(x, y, z, (p, state) -> state == blockState, "Expected " + blockState.getBlock().getRegistryName() + " state");
    }

    public void assertBlocksAre(Stream<BlockPos> pos, BlockState blockState) {
        assertBlocksMatch(pos, (p, state) -> state == blockState, "Expected " + blockState.getBlock().getRegistryName() + " state");
    }

    public void assertBlockIs(BlockPos pos, Block block) {
        assertBlockMatches(pos, (p, state) -> state.isIn(block), "Expected " + block.getRegistryName());
    }

    public void assertBlockIs(int x, int y, int z, Block block) {
        assertBlockMatches(x, y, z, (p, state) -> state.isIn(block), "Expected " + block.getRegistryName());
    }

    public void assertBlocksAre(Stream<BlockPos> pos, Block block) {
        assertBlocksMatch(pos, (p, state) -> state.isIn(block), "Expected " + block.getRegistryName());
    }

    public void assertBlockMatches(BlockPos pos, BlockPredicate assertion) {
        assertBlockMatches(pos, assertion, "Block does not match");
    }

    public void assertBlockMatches(int x, int y, int z, BlockPredicate assertion) {
        assertBlockMatches(x, y, z, assertion, "Block does not match");
    }

    public void assertBlocksMatch(Stream<BlockPos> pos, BlockPredicate assertion) {
        assertBlocksMatch(pos, assertion, "Block does not match");
    }

    private void assertBlockMatches(BlockPos pos, BlockPredicate assertion, String problem) {
        assertBlock(pos, toAssertion(assertion, problem));
    }

    private void assertBlockMatches(int x, int y, int z, BlockPredicate assertion, String problem) {
        assertBlock(x, y, z, toAssertion(assertion, problem));
    }

    private void assertBlocksMatch(Stream<BlockPos> pos, BlockPredicate assertion, String problem) {
        assertBlocks(pos, toAssertion(assertion, problem));
    }

    public void assertBlock(BlockPos pos, BlockConsumer assertion) {
        assertion.accept(absolute(pos), getBlock(pos));
    }

    public void assertBlock(int x, int y, int z, BlockConsumer assertion) {
        assertBlock(new BlockPos(x, y, z), assertion);
    }

    public void assertBlocks(Stream<BlockPos> pos, BlockConsumer assertion) {
        pos.forEach(p -> assertBlock(p, assertion));
    }

    @FunctionalInterface
    public interface BlockConsumer {
        void accept(BlockPos pos, BlockState state);
    }

    @FunctionalInterface
    public interface BlockPredicate {
        boolean test(BlockPos pos, BlockState state);
    }



    public void setBlocks(Stream<BlockPos> stream, BlockState state) {
        stream.map(this::absolute).forEach(p -> setBlockState(p, state));
    }

    public void setBlocks(Stream<BlockPos> stream, BlockState state, int flags) {
        stream.map(this::absolute).forEach(p -> setBlockState(p, state, flags));
    }

    public Stream<BlockState> getBlocks(Stream<BlockPos> stream) {
        return stream.map(this::absolute).map(this::getBlockState);
    }

    public void setBlock(BlockPos pos, BlockState state) {
        setBlockState(absolute(pos), state);
    }

    public void setBlock(BlockPos pos, BlockState state, int flags) {
        setBlockState(absolute(pos), state, flags);
    }

    public BlockState getBlock(BlockPos pos) {
        return getBlockState(absolute(pos));
    }

    public void setBlock(int x, int y, int z, BlockState state) {
        setBlockState(absolute(x, y, z), state);
    }

    public void setBlock(int x, int y, int z, BlockState state, int flags) {
        setBlockState(absolute(x, y, z), state, flags);
    }

    public BlockState getBlock(int x, int y, int z) {
        return getBlockState(absolute(x, y, z));
    }

    private void setBlockState(BlockPos pos, BlockState state, int flags) {
        world.setBlockState(pos, state.rotate(world, pos, rot), flags);
    }

    private void setBlockState(BlockPos pos, BlockState state) {
        world.setBlockState(pos, state.rotate(world, pos, rot));
    }

    private BlockState getBlockState(BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return state.rotate(world, pos, invRot);
    }




    public Direction relative(Direction abs) {
        return invRot.rotate(abs);
    }

    public BlockPos relative(BlockPos abs) {
        return Utils.untransformPos(abs, Mirror.NONE, rot, origin)
                    .subtract(origin);
    }

    public BlockPos relative(int x, int y, int z) {
        return Utils.untransformPos(new BlockPos(x, y, z), Mirror.NONE, rot, origin)
                    .subtract(origin);
    }

    public BlockPos.Mutable moveRelative(BlockPos.Mutable abs) {
        return Utils.untransformMutablePos(abs, Mirror.NONE, rot, origin)
                    .move(-origin.getX(), -origin.getY(), -origin.getZ());
    }

    public Vector3d relative(Vector3d abs) {
        return Utils.untransformPos(abs, Mirror.NONE, rot, origin)
                    .subtract(origin.getX(), origin.getY(), origin.getZ());
    }

    public Direction absolute(Direction rel) {
        return rot.rotate(rel);
    }

    public BlockPos absolute(int x, int y, int z) {
        return Utils.transformPos(new BlockPos(x + origin.getX(), y + origin.getY(), z + origin.getZ()), Mirror.NONE, rot, origin);
    }

    public BlockPos absolute(BlockPos rel) {
        return Utils.transformPos(rel.add(origin), Mirror.NONE, rot, origin);
    }

    public BlockPos.Mutable moveAbsolute(BlockPos.Mutable rel) {
        return Utils.transformMutablePos(rel.move(origin), Mirror.NONE, rot, origin);
    }

    public Vector3d absolute(Vector3d rel) {
        return Utils.transformPos(rel.add(origin.getX(), origin.getY(), origin.getZ()), Mirror.NONE, rot, origin);
    }

    public BlockPos frameSoft(String name) {
        return frameOptional(name).orElse(null);
    }

    public BlockPos frame(String name) {
        return frameOptional(name)
                   .orElseThrow(() -> new AssertException("Frame '" + name + "' not present"));
    }

    public Optional<BlockPos> frameOptional(String name) {
        return instance.framePosOptional(name).map(this::relative);
    }

    public List<BlockPos> frameList(String name) {
        return frames(name).collect(Collectors.toList());
    }

    public Stream<BlockPos> frames(String name) {
        return instance.framePosList(name).stream().map(this::relative);
    }

    public BlockPos absoluteFrameSoft(String name) {
        return instance.framePos(name);
    }

    public BlockPos absoluteFrame(String name) {
        return instance.framePosOptional(name)
                       .orElseThrow(() -> new AssertException("Frame '" + name + "' not present"));
    }

    public Optional<BlockPos> absoluteFrameOptional(String name) {
        return instance.framePosOptional(name);
    }

    public List<BlockPos> absoluteFrameList(String name) {
        return instance.framePosList(name);
    }

    public Stream<BlockPos> absoluteFrames(String name) {
        return instance.framePosList(name).stream();
    }

    public Stream<BlockPos> inBox(int nx, int ny, int nz, int px, int py, int pz) {
        return BlockPos.getAllInBox(nx, ny, nz, px - 1, py - 1, pz - 1);
    }

    public Stream<BlockPos> inBox(MutableBoundingBox box) {
        return BlockPos.stream(box);
    }

    public Stream<BlockPos> inBox(AxisAlignedBB box) {
        return BlockPos.stream(box);
    }


    public <T> Consumer<T> toAssertion(Predicate<? super T> check, Function<T, AssertException> error) {
        return t -> {
            if (!check.test(t)) {
                throw error.apply(t);
            }
        };
    }

    public <T> Consumer<T> toAssertion(Predicate<? super T> check, String orError) {
        return toAssertion(check, t -> new AssertException(orError));
    }

    public <T> Consumer<T> toAssertion(Predicate<? super T> check) {
        return toAssertion(check, t -> new AssertException("Tested negative"));
    }


    public BlockConsumer toAssertion(BlockPredicate check, BiFunction<BlockPos, BlockState, AssertException> error) {
        return (p, s) -> {
            if (!check.test(p, s)) {
                throw error.apply(p, s);
            }
        };
    }

    public BlockConsumer toAssertion(BlockPredicate check, String orError) {
        return toAssertion(check, (p, s) -> new LocalAssertException(orError, p, relative(p)));
    }

    public BlockConsumer toAssertion(BlockPredicate check) {
        return toAssertion(check, (p, s) -> new LocalAssertException("Tested negative", p, relative(p)));
    }

    public <T> Predicate<T> testNoFail(Consumer<? super T> consumer) {
        return t -> {
            try {
                consumer.accept(t);
                return true;
            } catch (TestException e) {
                return false;
            }
        };
    }
}
