package net.shadew.gametest.testmc;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.CommandBlockTileEntity;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@SuppressWarnings("deprecation")
public final class TestStructureHelper {
    public static String testStructuresDirectoryName = "gameteststructures";

    public static Rotation getRotation(int rotationSteps) {
        switch (rotationSteps) {
            case 0:
                return Rotation.NONE;
            case 1:
                return Rotation.CLOCKWISE_90;
            case 2:
                return Rotation.CLOCKWISE_180;
            case 3:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                throw new IllegalArgumentException("rotationSteps must be a value from 0-3. Got value " + rotationSteps);
        }
    }

    public static AxisAlignedBB getTestAABB(StructureBlockTileEntity sb) {
        BlockPos pos = sb.getPos();
        BlockPos localUpper = pos.add(sb.getStructureSize().add(-1, -1, -1));
        BlockPos upper = Template.getTransformedPos(localUpper, Mirror.NONE, sb.getRotation(), pos);
        return new AxisAlignedBB(pos, upper);
    }

    public static MutableBoundingBox getTestBox(StructureBlockTileEntity entity) {
        BlockPos pos = entity.getPos();
        BlockPos localUpper = pos.add(entity.getStructureSize().add(-1, -1, -1));
        BlockPos upper = Template.getTransformedPos(localUpper, Mirror.NONE, entity.getRotation(), pos);
        return new MutableBoundingBox(pos, upper);
    }

    public static void placeStartButton(BlockPos pos, BlockPos off, Rotation rotation, ServerWorld world) {
        BlockPos cmdBlock = Template.getTransformedPos(pos.add(off), Mirror.NONE, rotation, pos);
        world.setBlockState(cmdBlock, Blocks.COMMAND_BLOCK.getDefaultState());

        CommandBlockTileEntity cmdBlockTE = (CommandBlockTileEntity) world.getTileEntity(cmdBlock);
        assert cmdBlockTE != null;
        cmdBlockTE.getCommandBlockLogic().setCommand("test runthis");

        BlockPos btnPos = Template.getTransformedPos(cmdBlock.add(0, 0, -1), Mirror.NONE, rotation, cmdBlock);
        world.setBlockState(btnPos, Blocks.STONE_BUTTON.getDefaultState().rotate(rotation));
    }

    public static void createTestArea(String name, BlockPos pos, BlockPos size, Rotation rotation, ServerWorld world) {
        MutableBoundingBox box = createTestBox(pos, size, rotation);
        clearArea(box, pos.getY(), world);

        world.setBlockState(pos, Blocks.STRUCTURE_BLOCK.getDefaultState());
        StructureBlockTileEntity sb = (StructureBlockTileEntity) world.getTileEntity(pos);
        assert sb != null;

        sb.setIgnoresEntities(false);
        sb.setName(new ResourceLocation(name));
        sb.setSize(size);
        sb.setMode(StructureMode.SAVE);
        sb.setShowBoundingBox(true);
    }

    public static StructureBlockTileEntity createTest(String name, BlockPos pos, Rotation rotation, int i, ServerWorld world, boolean lazy) {
        BlockPos size = createStructure(name, world).getSize();
        MutableBoundingBox box = createTestBox(pos, size, rotation);

        // Get the rotated corner position
        BlockPos corner;
        if (rotation == Rotation.NONE) {
            corner = pos;
        } else if (rotation == Rotation.CLOCKWISE_90) {
            corner = pos.add(size.getZ() - 1, 0, 0);
        } else if (rotation == Rotation.CLOCKWISE_180) {
            corner = pos.add(size.getX() - 1, 0, size.getZ() - 1);
        } else if (rotation == Rotation.COUNTERCLOCKWISE_90) {
            corner = pos.add(0, 0, size.getX() - 1);
        } else {
            throw new IllegalArgumentException("Invalid rotation: " + rotation);
        }

        // Create structure
        forceLoadNearbyChunks(pos, world);
        clearArea(box, pos.getY(), world);
        StructureBlockTileEntity sb = placeStructure(name, corner, rotation, world, lazy);

        // Remove block ticks once again (placing the structure might have created block ticks)
        world.getPendingBlockTicks().getPending(box, true, false);
        world.clearUpdatesInArea(box);

        return sb;
    }

    private static void forceLoadNearbyChunks(BlockPos pos, ServerWorld world) {
        ChunkPos cpos = new ChunkPos(pos);

        for (int x = -1; x < 4; x++) {
            for (int z = -1; z < 4; z++) {
                int cx = cpos.x + x;
                int cz = cpos.z + z;
                world.forceChunk(cx, cz, true);
            }
        }
    }

    public static void clearArea(MutableBoundingBox box, int i, ServerWorld world) {
        MutableBoundingBox mbox = new MutableBoundingBox(
            box.minX - 2, box.minY - 3, box.minZ - 3,
            box.maxX + 3, box.maxY + 20, box.maxZ + 3
        );

        // Remove blocks
        BlockPos.stream(mbox).forEach(pos -> prepareTestBlock(i, pos, world));

        // Remove block ticks
        world.getPendingBlockTicks().getPending(mbox, true, false);
        world.clearUpdatesInArea(mbox);

        // Remove entities
        AxisAlignedBB aabb = new AxisAlignedBB(mbox.minX, mbox.minY, mbox.minZ, mbox.maxX, mbox.maxY, mbox.maxZ);
        List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, aabb, e -> !(e instanceof PlayerEntity));
        entities.forEach(Entity::remove);
    }

    public static MutableBoundingBox createTestBox(BlockPos pos, BlockPos size, Rotation rotation) {
        BlockPos offLocal = pos.add(size).add(-1, -1, -1);
        BlockPos off = Template.getTransformedPos(offLocal, Mirror.NONE, rotation, pos);

        MutableBoundingBox mbox = MutableBoundingBox.createProper(
            pos.getX(), pos.getY(), pos.getZ(),
            off.getX(), off.getY(), off.getZ()
        );

        int x = Math.min(mbox.minX, mbox.maxX);
        int z = Math.min(mbox.minZ, mbox.maxZ);
        BlockPos offset = new BlockPos(pos.getX() - x, 0, pos.getZ() - z);
        mbox.move(offset);
        return mbox;
    }

    public static Optional<BlockPos> findOwnerStructureBlock(BlockPos pos, int r, ServerWorld world) {
        return findStructureBlocks(pos, r, world)
                   .stream()
                   .filter(sbPos -> isInStructureBounds(sbPos, pos, world))
                   .findFirst();
    }

    @Nullable
    public static BlockPos findNearestStructureBlock(BlockPos pos, int r, ServerWorld world) {
        Comparator<BlockPos> distToPos = Comparator.comparingInt(p -> p.manhattanDistance(pos));
        Collection<BlockPos> sbs = findStructureBlocks(pos, r, world);
        Optional<BlockPos> result = sbs.stream().min(distToPos);
        return result.orElse(null);
    }

    public static Collection<BlockPos> findStructureBlocks(BlockPos pos, int radius, ServerWorld world) {
        Collection<BlockPos> out = Lists.newArrayList();
        AxisAlignedBB aabb = new AxisAlignedBB(pos);
        aabb = aabb.grow(radius);

        for (int x = (int) aabb.minX; x <= (int) aabb.maxX; ++x) {
            for (int y = (int) aabb.minY; y <= (int) aabb.maxY; ++y) {
                for (int z = (int) aabb.minZ; z <= (int) aabb.maxZ; ++z) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState state = world.getBlockState(p);
                    if (state.isIn(Blocks.STRUCTURE_BLOCK)) {
                        out.add(p);
                    }
                }
            }
        }

        return out;
    }

    private static Template createStructure(String name, ServerWorld world) {
        TemplateManager templates = world.getStructureTemplateManager();
        Template template = templates.getTemplate(new ResourceLocation(name));

        if (template != null) {
            return template;
        } else {
            String fileName = name + ".snbt";
            Path path = Paths.get(testStructuresDirectoryName, fileName);

            CompoundNBT snbt = loadSnbt(path);
            if (snbt == null) {
                throw new RuntimeException("Could not find structure file " + path + ", and the structure is not available in the world structures either.");
            } else {
                return templates.createStructure(snbt);
            }
        }
    }

    private static StructureBlockTileEntity placeStructure(String name, BlockPos pos, Rotation rotation, ServerWorld world, boolean lazy) {
        world.setBlockState(pos, Blocks.STRUCTURE_BLOCK.getDefaultState());

        StructureBlockTileEntity sb = (StructureBlockTileEntity) world.getTileEntity(pos);
        assert sb != null;

        sb.setMode(StructureMode.LOAD);
        sb.setRotation(rotation);
        sb.setIgnoresEntities(false);
        sb.setName(new ResourceLocation(name));
        sb.loadStructure(world, lazy);
        if (sb.getStructureSize() != BlockPos.ZERO) {
            return sb;
        } else {
            Template template = createStructure(name, world);
            sb.place(world, lazy, template);
            if (sb.getStructureSize() == BlockPos.ZERO) {
                throw new RuntimeException("Failed to load structure " + name);
            } else {
                return sb;
            }
        }
    }

    @Nullable
    private static CompoundNBT loadSnbt(Path path) {
        try {
            BufferedReader reader = Files.newBufferedReader(path);
            String str = IOUtils.toString(reader);
            return JsonToNBT.getTagFromJson(str);
        } catch (IOException exc) {
            return null;
        } catch (CommandSyntaxException exc) {
            throw new RuntimeException("Error while trying to load structure " + path, exc);
        }
    }

    private static void prepareTestBlock(int testHeight, BlockPos pos, ServerWorld world) {
        BlockState state = null;

        FlatGenerationSettings settings = FlatGenerationSettings.getDefaultConfig(world.getRegistryManager().get(Registry.BIOME_KEY));
        BlockState[] layers = settings.getStates();

        if (pos.getY() < testHeight && pos.getY() <= layers.length) {
            state = layers[pos.getY() - 1];
        }

        if (state == null) {
            state = Blocks.AIR.getDefaultState();
        }

        BlockStateInput input = new BlockStateInput(state, Collections.emptySet(), null);
        input.place(world, pos, 2);
        world.updateNeighbors(pos, state.getBlock());
    }

    private static boolean isInStructureBounds(BlockPos sbPos, BlockPos pos, ServerWorld world) {
        StructureBlockTileEntity sb = (StructureBlockTileEntity) world.getTileEntity(sbPos);
        assert sb != null;

        AxisAlignedBB aabb = getTestAABB(sb).grow(1.0D);
        return aabb.contains(Vector3d.ofCenter(pos));
    }
}
