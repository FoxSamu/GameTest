package net.shadew.gametest.blockitem.tileentity;

import com.mojang.datafixers.util.Either;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import net.shadew.gametest.blockitem.block.TemplateBlock;
import net.shadew.gametest.blockitem.block.props.DiagonalDirection;
import net.shadew.gametest.blockitem.entity.FrameEntity;
import net.shadew.gametest.framework.GameTestFunction;
import net.shadew.gametest.framework.GameTestRegistry;
import net.shadew.gametest.framework.platforms.IPlatform;
import net.shadew.gametest.net.GameTestNet;
import net.shadew.gametest.net.packet.UpdateTemplateBlockPacket;
import net.shadew.gametest.util.TemplateHelper;
import net.shadew.gametest.util.Utils;

public class TemplateBlockTileEntity extends TileEntity {
    private ResourceLocation name = new ResourceLocation("untitled");
    private boolean rawTemplate;
    private int width;
    private int height;
    private int depth;

    private Template cachedTemplate;

    public TemplateBlockTileEntity() {
        super(GameTestTileEntityTypes.TEMPLATE_BLOCK);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getDepth() {
        return depth;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setRawTemplate(boolean rawTemplate) {
        this.rawTemplate = rawTemplate;
    }

    public ResourceLocation getName() {
        return name;
    }

    public boolean isRawTemplate() {
        return rawTemplate;
    }

    public void setName(ResourceLocation name) {
        if (!this.name.equals(name)) cachedTemplate = null;
        this.name = name;
    }

    public void markUpdate() {
        if (world != null && !world.isRemote) {
            markDirty();
            sendUpdate();
        }
    }

    private Either<ResourceLocation, GameTestFunction> getNameEither() {
        if(rawTemplate) return Either.left(name);
        GameTestFunction func = GameTestRegistry.getFunction(name);
        if(func == null) {
            rawTemplate = false;
            return Either.left(name);
        }
        return Either.right(func);
    }

    private void sendUpdate() {
        GameTestNet.NET.sendTrackingTileEntity(this, new UpdateTemplateBlockPacket(
            pos, getNameEither(), width, height, depth
        ));
    }

    public void changeRotation(DiagonalDirection rotation) {
        world.setBlockState(pos, getBlockState().with(TemplateBlock.DIRECTION, rotation));
        world.playEvent(null, Constants.WorldEvents.IRON_TRAPDOOR_OPEN_SOUND, pos, 0);
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), -1, getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        fromTag(world.getBlockState(pkt.getPos()), pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        nbt.putString("TestName", name.toString());
        nbt.putInt("Width", width);
        nbt.putInt("Height", height);
        nbt.putInt("Depth", depth);
        nbt.putBoolean("RawTemplate", rawTemplate);
        return super.write(nbt);
    }

    @Override
    public void fromTag(BlockState state, CompoundNBT nbt) {
        super.fromTag(state, nbt);
        name = new ResourceLocation(nbt.getString("TestName"));
        width = nbt.getInt("Width");
        height = nbt.getInt("Height");
        depth = nbt.getInt("Depth");
        rawTemplate = nbt.getBoolean("RawTemplate");
    }

    public Rotation getRotation() {
        return getBlockState().get(TemplateBlock.DIRECTION).getRotation();
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return getBox().grow(3);
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 96;
    }

    public boolean save() {
        ResourceLocation name = getTemplateName();

        if (!world.isRemote && name != null) {
            BlockPos pos = getPos().add(0, 1, 0);
            BlockPos size = new BlockPos(width, height, depth);

            TemplateHelper template = new TemplateHelper();

            // We filter air blocks so structure SNBTs stay small (handier for version control)
            // Template block re-adds air on placing the structure
            template.takeBlocksFromWorld(world, pos, size, getRotation(), true, Blocks.AIR);
            CompoundNBT nbt = template.writeToNBT(new CompoundNBT());

            Path testsDir = Paths.get(Utils.TEST_STRUCTURE_DIR);
            File outFile = testsDir.resolve(name.getNamespace() + "/" + name.getPath() + ".snbt").toFile();
            outFile.getParentFile().mkdirs();

            try (FileWriter writer = new FileWriter(outFile)) {
                writer.append(nbt.toFormattedComponent("    ", 0).getString());
                return true;
            } catch (IOException exc) {
                return false;
            }
        } else {
            return false;
        }
    }

    public int loadOrPlace() {
        if (cachedTemplate == null) {
            return load();
        } else {
            return place() ? 4 : 3;
        }
    }

    private ResourceLocation getTemplateName() {
        if(rawTemplate) {
            return name;
        }

        GameTestFunction function = GameTestRegistry.getFunction(name);
        if(function == null) {
            return null;
        }
        return function.getTemplate();
    }

    private Template resolveTemplate(ServerWorld world) throws Exception {
        ResourceLocation name = getTemplateName();
        if(name == null) {
            return null;
        }

        Path testsDir = Paths.get(Utils.TEST_STRUCTURE_DIR);
        File inFile = testsDir.resolve(name.getNamespace() + "/" + name.getPath() + ".snbt").toFile();

        if (!inFile.exists()) {
            TemplateManager manager = world.getStructureTemplateManager();
            return manager.getTemplate(name);
        }

        CompoundNBT nbt;
        try (FileReader reader = new FileReader(inFile)) {
            String str = IOUtils.toString(reader);
            nbt = JsonToNBT.getTagFromJson(str);
        }

        Template template = new Template();
        template.read(nbt);
        return template;
    }

    public boolean loadAndPlace() {
        return load() == 0 && place();
    }

    public int load() {
        if (!world.isRemote && name != null) {
            try {
                Template template = resolveTemplate((ServerWorld) world);
                if (template == null) return 2;

                cachedTemplate = template;
                width = template.getSize().getX();
                height = template.getSize().getY();
                depth = template.getSize().getZ();
                markUpdate();
                return 0;
            } catch (Exception e) {
                return 1;
            }
        } else {
            return 1;
        }
    }

    public boolean place() {
        if (!world.isRemote && name != null && cachedTemplate != null) {
            width = cachedTemplate.getSize().getX();
            height = cachedTemplate.getSize().getY();
            depth = cachedTemplate.getSize().getZ();

            // Clear space, re-add filtered air blocks
            createPlatform(IPlatform.EMPTY);
            AxisAlignedBB box = getBox();
            world.getEntitiesInAABBexcluding(null, box, e -> !(e instanceof PlayerEntity)).forEach(Entity::remove);

            markUpdate();

            PlacementSettings settings = new PlacementSettings().setMirror(Mirror.NONE)
                                                                .setRotation(getRotation())
                                                                .setIgnoreEntities(false)
                                                                .setChunk(null);
            cachedTemplate.place((ServerWorld) world, getPos().up(), settings, new Random());

            if (world instanceof ServerWorld) {
                ServerWorld sworld = (ServerWorld) world;
                MutableBoundingBox mbox = getMBox();
                sworld.getPendingBlockTicks().getPending(mbox, true, false);
                sworld.clearUpdatesInArea(mbox);
            }

            return true;
        } else {
            return false;
        }
    }

    public void createPlatform(CompoundNBT nbt) {
        int type = nbt.getInt("Type");

        IPlatform platform = IPlatform.EMPTY;
        switch (type) {
            case 1:
                platform = IPlatform.platform(
                    NBTUtil.readBlockState(nbt.getCompound("Floor"))
                );
                break;
            case 2:
                platform = IPlatform.ceiled(
                    NBTUtil.readBlockState(nbt.getCompound("Floor")),
                    NBTUtil.readBlockState(nbt.getCompound("Ceil"))
                );
                break;
            case 3:
                platform = IPlatform.box(
                    NBTUtil.readBlockState(nbt.getCompound("Floor")),
                    NBTUtil.readBlockState(nbt.getCompound("Ceil")),
                    NBTUtil.readBlockState(nbt.getCompound("Wall")),
                    nbt.getBoolean("Door")
                );
                break;
            case 4:
                platform = IPlatform.pool(
                    NBTUtil.readBlockState(nbt.getCompound("Floor")),
                    NBTUtil.readBlockState(nbt.getCompound("Wall")),
                    NBTUtil.readBlockState(nbt.getCompound("Inner")),
                    nbt.getInt("Depth"),
                    nbt.getInt("Extra")
                );
                break;
            case 5:
                platform = IPlatform.ceiledPool(
                    NBTUtil.readBlockState(nbt.getCompound("Floor")),
                    NBTUtil.readBlockState(nbt.getCompound("Ceil")),
                    NBTUtil.readBlockState(nbt.getCompound("Wall")),
                    NBTUtil.readBlockState(nbt.getCompound("Inner")),
                    nbt.getInt("Depth"),
                    nbt.getInt("Extra")
                );
                break;
            case 6:
                platform = IPlatform.boxedPool(
                    NBTUtil.readBlockState(nbt.getCompound("Floor")),
                    NBTUtil.readBlockState(nbt.getCompound("Ceil")),
                    NBTUtil.readBlockState(nbt.getCompound("Wall")),
                    NBTUtil.readBlockState(nbt.getCompound("Inner")),
                    nbt.getInt("Depth"),
                    nbt.getBoolean("Door")
                );
                break;
            case 7:
                platform = IPlatform.pool(
                    NBTUtil.readBlockState(nbt.getCompound("Floor")),
                    NBTUtil.readBlockState(nbt.getCompound("Fence")),
                    Blocks.AIR.getDefaultState(),
                    nbt.getInt("Height"),
                    0
                );
                break;
            case 8:
                platform = IPlatform.ceiledPool(
                    NBTUtil.readBlockState(nbt.getCompound("Floor")),
                    NBTUtil.readBlockState(nbt.getCompound("Ceil")),
                    NBTUtil.readBlockState(nbt.getCompound("Fence")),
                    Blocks.AIR.getDefaultState(),
                    nbt.getInt("Height"),
                    0
                );
                break;
        }

        createPlatform(platform);
    }

    public void createPlatform(IPlatform platform) {
        createPlatform0(IPlatform.EMPTY); // Clear everything first
        createPlatform0(platform);

        if (world instanceof ServerWorld) {
            ServerWorld sworld = (ServerWorld) world;
            MutableBoundingBox box = getMBox();
            sworld.getPendingBlockTicks().getPending(box, true, false);
            sworld.clearUpdatesInArea(box);
        }
    }

    private void createPlatform0(IPlatform platform) {
        BlockPos.Mutable mpos = new BlockPos.Mutable();
        BlockPos.Mutable npos = new BlockPos.Mutable();
        BlockPos size = new BlockPos(width, height, depth);
        BlockPos pos = getPos();
        Rotation rotation = getRotation();

        for (int x = 0, xs = size.getX(); x < xs; x++) {
            for (int z = 0, zs = size.getZ(); z < zs; z++) {
                for (int y = 0, ys = size.getY(); y < ys; y++) {
                    mpos.setPos(x, y, z); // Start local, ITestStructurePlatform wants a local coordinate
                    BlockState block = platform.getStateAt(mpos, size);


                    mpos.move(pos).move(Direction.UP);
                    Utils.transformMutablePos(mpos, Mirror.NONE, rotation, pos);

                    block = block.rotate(world, mpos, rotation);
                    if (!(block.getBlock() instanceof DoorBlock)) { // Ignore doors otherwise they will break
                        for (Direction dir : Direction.values()) {
                            npos.setPos(mpos).move(dir);
                            BlockState other = world.getBlockState(npos);
                            block = block.updatePostPlacement(dir, other, world, mpos, npos);
                        }
                    } else if (block.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                        for (int ix = -1; ix <= 1; ix++) {
                            for (int iz = -1; iz <= 1; iz++) {
                                npos.setPos(x + ix, y - 1, z + iz);
                                if (npos.getX() < 0 || npos.getX() >= size.getX() || npos.getZ() < 0 || npos.getZ() >= size.getZ()) {
                                    npos.move(pos).move(Direction.UP);
                                    Utils.transformMutablePos(npos, Mirror.NONE, rotation, pos);
                                    world.setBlockState(npos, Blocks.IRON_BLOCK.getDefaultState(), 2);
                                    world.updateNeighbors(npos, Blocks.IRON_BLOCK);
                                }
                            }
                        }
                    }
                    world.setBlockState(mpos, block, 2);
                    world.updateNeighbors(mpos, block.getBlock());
                }
            }
        }
    }

    public AxisAlignedBB getBox() {
        BlockPos origin = getPos().up();
        BlockPos notRotated = origin.add(width - 1, height - 1, depth - 1);
        BlockPos other = Utils.transformPos(notRotated, Mirror.NONE, getRotation(), origin);

        return new AxisAlignedBB(origin).union(new AxisAlignedBB(other));
    }

    public MutableBoundingBox getMBox() {
        BlockPos origin = getPos().up();
        BlockPos notRotated = origin.add(width - 1, height - 1, depth - 1);
        BlockPos other = Utils.transformPos(notRotated, Mirror.NONE, getRotation(), origin);

        return MutableBoundingBox.createProper(
            origin.getX(), origin.getY(), origin.getZ(),
            other.getX(), other.getY(), other.getZ()
        );
    }

    public List<FrameEntity> findFrames() {
        return world.getEntitiesWithinAABB(FrameEntity.class, getBox());
    }

    public Map<String, List<FrameEntity>> findAndMapFrames() {
        List<FrameEntity> frames = findFrames();
        Map<String, List<FrameEntity>> out = new HashMap<>();

        for (FrameEntity e : frames) {
            if (e.getFrameName().isEmpty()) continue;

            out.computeIfAbsent(e.getFrameName(), k -> new ArrayList<>()).add(e);
        }

        return out;
    }

    public void removeAllFrames() {
        findFrames().forEach(Entity::remove);
    }
}
