package net.shadew.gametest.blockitem.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

import net.shadew.gametest.blockitem.block.GameTestBlocks;
import net.shadew.gametest.blockitem.block.TestBlock;
import net.shadew.gametest.blockitem.block.props.TestBlockState;
import net.shadew.gametest.framework.*;
import net.shadew.gametest.framework.api.ITestListener;
import net.shadew.gametest.framework.api.Marker;
import net.shadew.gametest.framework.api.output.ITestOutputInstance;
import net.shadew.gametest.framework.output.TestOutputManager;
import net.shadew.gametest.net.GameTestNet;
import net.shadew.gametest.net.packet.TestMarkerClearPacket;
import net.shadew.gametest.net.packet.TestMarkerSetPacket;

public class TestBlockTileEntity extends TileEntity implements ITickableTileEntity {
    private BlockPos templateBlockPos;
    private TemplateBlockTileEntity templateBlock;
    private int lookTimer = 0;
    private GameTestInstance testInstance;

    private Boolean enqueueStartTest;
    private final List<ITestListener> listeners = new ArrayList<>();
    private ITestOutputInstance output;

    private final Map<BlockPos, MarkerHolder> markers = new HashMap<>();

    public TestBlockTileEntity() {
        super(GameTestTileEntityTypes.TEST_BLOCK);
    }

    public BlockPos getTemplateBlockPos() {
        return templateBlockPos;
    }

    public TemplateBlockTileEntity getTemplateBlock() {
        return templateBlock;
    }

    public GameTestInstance getTestInstance() {
        return testInstance;
    }

    public TestBlockState getState() {
        BlockState blockState = getBlockState();
        if (blockState.contains(TestBlock.STATE))
            return getBlockState().get(TestBlock.STATE);
        return TestBlockState.OFF;
    }

    public void setState(TestBlockState state) {
        if (getState() == state) return;
        world.setBlockState(getPos(), GameTestBlocks.TEST_BLOCK.getDefaultState().with(TestBlock.STATE, state));
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), -1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        fromTag(getBlockState(), pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        if (templateBlockPos != null) {
            nbt.putInt("TemplateBlockX", templateBlockPos.getX());
            nbt.putInt("TemplateBlockY", templateBlockPos.getY());
            nbt.putInt("TemplateBlockZ", templateBlockPos.getZ());
        }

        ListNBT markers = new ListNBT();
        for (MarkerHolder marker : getMarkers()) {
            markers.add(marker.write());
        }
        nbt.put("Markers", markers);

        return super.write(nbt);
    }

    @Override
    public void fromTag(BlockState state, CompoundNBT nbt) {
        if (nbt.contains("TemplateBlockX") && nbt.contains("TemplateBlockY") && nbt.contains("TemplateBlockZ")) {
            templateBlockPos = new BlockPos(nbt.getInt("TemplateBlockX"), nbt.getInt("TemplateBlockY"), nbt.getInt("TemplateBlockZ"));
        } else {
            templateBlockPos = null;
        }

        markers.clear();

        ListNBT markersNbt = nbt.getList("Markers", Constants.NBT.TAG_COMPOUND);
        for (int i = 0, l = markersNbt.size(); i < l; i++) {
            MarkerHolder marker = MarkerHolder.read(markersNbt.getCompound(i));
            markers.put(marker.getPos(), marker);
        }

        super.fromTag(state, nbt);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        BlockPos pos = getPos();
        return getState().getColor() != 0 || !markers.isEmpty()
               ? INFINITE_EXTENT_AABB
               : new AxisAlignedBB(pos);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 256;
    }



    private boolean looking = false;

    private static final int SEARCH_RADIUS = 2;

    private void lookForTemplate() {
        TemplateBlockTileEntity found = null;
        int leastDistance = Integer.MAX_VALUE;
        BlockPos.Mutable mpos = new BlockPos.Mutable();
        BlockPos.Mutable foundPos = new BlockPos.Mutable();

        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS; y++) {
                for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                    mpos.setPos(pos).move(x, y, z);

                    BlockState state = world.getBlockState(mpos);
                    if (state.isIn(GameTestBlocks.TEST_TEMPLATE_BLOCK)) {
                        TileEntity tileEntity = world.getTileEntity(mpos);
                        if (tileEntity instanceof TemplateBlockTileEntity) {
                            int dist = mpos.manhattanDistance(pos);
                            if (dist < leastDistance || found == null) {
                                foundPos.setPos(mpos);
                                leastDistance = dist;
                                found = (TemplateBlockTileEntity) tileEntity;
                            }
                        }
                    }
                }
            }
        }

        templateBlockPos = foundPos.toImmutable();
        templateBlock = found;
    }

    private void startLooking() {
        setState(TestBlockState.LOOKING);
        lookTimer = 0;
        lookForTemplate();
    }

    private void tickLooking() {
        validateTemplateBlock();
        if (mustBeLooking()) {
            templateBlock = null;
            templateBlockPos = null;

            if (!looking) {
                startLooking();
                looking = true;
            }

            lookTimer++;
            if (lookTimer >= 20) {
                lookForTemplate();
                lookTimer -= 20;
            }
        } else {
            looking = false;
        }
    }

    private boolean mustBeLooking() {
        return templateBlock == null || templateBlockPos == null;
    }

    private void validateTemplateBlock() {
        if (templateBlockPos != null) {
            TileEntity te = world.getTileEntity(templateBlockPos);
            if (te instanceof TemplateBlockTileEntity) {
                templateBlock = (TemplateBlockTileEntity) te;
            } else {
                templateBlockPos = null;
            }
        }
    }



    private void tickTest() {
        TestBlockState state = getState();
        if (!looking) {
            if (getState() == TestBlockState.LOOKING) {
                state = TestBlockState.OFF;
            }

            ResourceLocation name = templateBlock.getName();
            if (!GameTestRegistry.hasFunction(name)) {
                state = TestBlockState.E404;
            } else {
                if (enqueueStartTest != null) {
                    testInstance = null;
                    templateBlock.save();
                    startTest(enqueueStartTest);
                    enqueueStartTest = null;
                }
                if (testInstance != null) {
                    if (!listeners.isEmpty()) {
                        listeners.forEach(testInstance::addListener);
                        listeners.clear();
                    }
                    if(output != null) {
                        testInstance.setOutput(output);
                        output = null;
                    }
                    testInstance.tick();
                    state = testInstance.getTestBlockState();
                }
            }
        }

        if (state != getState()) {
            setState(state);
        }
    }

    private boolean initTestInstance() {
        ResourceLocation name = templateBlock.getName();
        ServerWorld sworld = (ServerWorld) world;
        GameTestFunction function = GameTestRegistry.getFunction(name);

        if (function == null) {
            setState(TestBlockState.E404);
            return false;
        }

        testInstance = new GameTestInstance(sworld, function, this, templateBlock);
        return testInstance.init();
    }

    private void startTest(boolean withBeforeBatch) {
        if (!world.isRemote) {
            clearMarkers();
            boolean init = initTestInstance();

            if (testInstance != null && withBeforeBatch) {
                Consumer<ServerWorld> beforeBatch = GameTestRegistry.getBeforeBatchFunction(testInstance.getBatchId());
                ServerWorld sworld = (ServerWorld) world;
                if (beforeBatch != null) {
                    try {
                        beforeBatch.accept(sworld);
                    } catch (Throwable throwable) {
                        testInstance.fail(throwable);
                    }
                }
            }

            if (!init) {
                return;
            }

            testInstance.start();
        }
    }

    public void enqueueStartTest(boolean withBeforeBatch) {
        enqueueStartTest = withBeforeBatch;
        testInstance = null;
    }

    public void addListener(ITestListener listener) {
        listeners.add(listener);
    }

    public void setOutput(ITestOutputInstance output) {
        this.output = output;
    }

    @Override
    public void tick() {
        if (!world.isRemote) {
            tickLooking();
            tickTest();
        }
    }

    private ITextComponent getStatus() {
        if (templateBlockPos == null || templateBlock == null) {
            return new StringTextComponent("Test Block: No template block found");
        }

        return new StringTextComponent("Test Block: Found template block at " + templateBlockPos);
    }

    public void sendTestInfo(PlayerEntity entity) {
        if (getState() != TestBlockState.LOOKING && getState() != TestBlockState.E404) {
            if (getState() == TestBlockState.OFF) {
                templateBlock.save();
                setOutput(TestOutputManager.openInstance());
                startTest(true);
                entity.sendStatusMessage(new StringTextComponent("Running test " + templateBlock.getName()), true);
            } else {
                setState(TestBlockState.OFF);
                clearMarkers();
                templateBlock.loadAndPlace();
                testInstance = null;
            }
        } else {
            entity.sendStatusMessage(getStatus(), true);
        }
    }

    public void addMarker(BlockPos pos, Marker type, String message) {
        if (type == Marker.REMOVE) markers.remove(pos);
        else markers.put(pos, new MarkerHolder(pos, type, message));

        if (!world.isRemote) {
            GameTestNet.NET.sendTrackingTileEntity(this, new TestMarkerSetPacket(getPos(), pos, type, message));
        }
    }

    public void clearMarkers() {
        markers.clear();
        if (!world.isRemote) {
            GameTestNet.NET.sendTrackingTileEntity(this, new TestMarkerClearPacket(getPos()));
        }
    }

    public Collection<MarkerHolder> getMarkers() {
        return markers.values();
    }

    public static class MarkerHolder {
        private final BlockPos pos;
        private final Marker type;
        private final String message;

        private MarkerHolder(BlockPos pos, Marker type, String message) {
            this.pos = pos;
            this.type = type;
            this.message = message;
        }

        public BlockPos getPos() {
            return pos;
        }

        public Marker getType() {
            return type;
        }

        public String getMessage() {
            return message;
        }

        CompoundNBT write() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("X", pos.getX());
            nbt.putInt("Y", pos.getY());
            nbt.putInt("Z", pos.getZ());
            nbt.putInt("Type", type.ordinal());
            nbt.putString("Message", message);
            return nbt;
        }

        static MarkerHolder read(CompoundNBT nbt) {
            return new MarkerHolder(
                new BlockPos(nbt.getInt("X"), nbt.getInt("Y"), nbt.getInt("Z")),
                Marker.values()[nbt.getInt("Type")],
                nbt.getString("Message")
            );
        }
    }
}
