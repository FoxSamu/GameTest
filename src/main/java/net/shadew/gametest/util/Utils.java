package net.shadew.gametest.util;

import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3d;
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
import java.util.stream.Collector;

public final class Utils {
    public static final String TEST_STRUCTURE_DIR = "gametest_structures";

    private static Map<DyeColor, Block> stainedGlassMap;

    private static void initStainedGlassMap() {
        if (stainedGlassMap != null) return;

        stainedGlassMap = new EnumMap<>(DyeColor.class);
        stainedGlassMap.put(DyeColor.BLACK, Blocks.BLACK_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.BLUE, Blocks.BLUE_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.BROWN, Blocks.BROWN_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.CYAN, Blocks.CYAN_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.GRAY, Blocks.GRAY_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.GREEN, Blocks.GREEN_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.LIME, Blocks.LIME_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.MAGENTA, Blocks.MAGENTA_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.ORANGE, Blocks.ORANGE_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.PINK, Blocks.PINK_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.PURPLE, Blocks.PURPLE_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.RED, Blocks.RED_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.WHITE, Blocks.WHITE_STAINED_GLASS);
        stainedGlassMap.put(DyeColor.YELLOW, Blocks.YELLOW_STAINED_GLASS);
    }

    public static Block getStainedGlassBlock(DyeColor color) {
        if (color == null) return Blocks.GLASS;
        initStainedGlassMap();
        return stainedGlassMap.get(color);
    }

    public static int rotateX(Rotation rot, int x, int z, boolean inverse) {
        switch (rot) {
            default:
            case NONE: return x;
            case CLOCKWISE_90: return inverse ? z : -z;
            case CLOCKWISE_180: return -x;
            case COUNTERCLOCKWISE_90: return inverse ? -z : z;
        }
    }

    public static int rotateZ(Rotation rot, int x, int z, boolean inverse) {
        switch (rot) {
            default:
            case NONE: return z;
            case CLOCKWISE_90: return inverse ? -x : x;
            case CLOCKWISE_180: return -z;
            case COUNTERCLOCKWISE_90: return inverse ? x : -x;
        }
    }

    public static int mirrorX(Mirror mirror, int x) {
        return mirror == Mirror.FRONT_BACK ? -x : x;
    }

    public static int mirrorZ(Mirror mirror, int z) {
        return mirror == Mirror.LEFT_RIGHT ? -z : z;
    }

    public static BlockPos transformPos(BlockPos pos, Mirror mirror, Rotation rotation, BlockPos origin) {
        if (rotation == Rotation.NONE && mirror == Mirror.NONE) return pos;

        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        int origX = origin.getX(), origZ = origin.getZ();
        int locX = x - origX, locZ = z - origZ;

        locX = mirrorX(mirror, locX);
        locZ = mirrorZ(mirror, locZ);
        int newX = rotateX(rotation, locX, locZ, false);
        int newZ = rotateZ(rotation, locX, locZ, false);

        x = origX + newX;
        z = origZ + newZ;

        if (pos.getX() == x && pos.getZ() == z) return pos;
        return new BlockPos(x, y, z);
    }

    public static BlockPos untransformPos(BlockPos pos, Mirror mirror, Rotation rotation, BlockPos origin) {
        if (rotation == Rotation.NONE && mirror == Mirror.NONE) return pos;

        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        int origX = origin.getX(), origZ = origin.getZ();
        int locX = x - origX, locZ = z - origZ;

        int newX = rotateX(rotation, locX, locZ, true);
        int newZ = rotateZ(rotation, locX, locZ, true);
        newX = mirrorX(mirror, newX);
        newZ = mirrorZ(mirror, newZ);

        x = origX + newX;
        z = origZ + newZ;

        if (pos.getX() == x && pos.getZ() == z) return pos;
        return new BlockPos(x, y, z);
    }

    public static double rotateX(Rotation rot, double x, double z, boolean inverse) {
        switch (rot) {
            default:
            case NONE: return x;
            case CLOCKWISE_90: return inverse ? z : -z;
            case CLOCKWISE_180: return -x;
            case COUNTERCLOCKWISE_90: return inverse ? -z : z;
        }
    }

    public static double rotateZ(Rotation rot, double x, double z, boolean inverse) {
        switch (rot) {
            default:
            case NONE: return z;
            case CLOCKWISE_90: return inverse ? -x : x;
            case CLOCKWISE_180: return -z;
            case COUNTERCLOCKWISE_90: return inverse ? x : -x;
        }
    }

    public static double mirrorX(Mirror mirror, double x) {
        return mirror == Mirror.FRONT_BACK ? -x : x;
    }

    public static double mirrorZ(Mirror mirror, double z) {
        return mirror == Mirror.LEFT_RIGHT ? -z : z;
    }

    public static Vector3d transformPos(Vector3d pos, Mirror mirror, Rotation rotation, BlockPos origin) {
        if (rotation == Rotation.NONE && mirror == Mirror.NONE) return pos;

        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        double origX = origin.getX() + 0.5, origZ = origin.getZ() + 0.5;
        double locX = x - origX, locZ = z - origZ;

        locX = mirrorX(mirror, locX);
        locZ = mirrorZ(mirror, locZ);
        double newX = rotateX(rotation, locX, locZ, false);
        double newZ = rotateZ(rotation, locX, locZ, false);

        x = origX + newX;
        z = origZ + newZ;

        if (pos.getX() == x && pos.getZ() == z) return pos;
        return new Vector3d(x, y, z);
    }

    public static Vector3d untransformPos(Vector3d pos, Mirror mirror, Rotation rotation, BlockPos origin) {
        if (rotation == Rotation.NONE && mirror == Mirror.NONE) return pos;

        double x = pos.getX(), y = pos.getY(), z = pos.getZ();
        double origX = origin.getX() + 0.5, origZ = origin.getZ() + 0.5;
        double locX = x - origX, locZ = z - origZ;

        double newX = rotateX(rotation, locX, locZ, true);
        double newZ = rotateZ(rotation, locX, locZ, true);
        newX = mirrorX(mirror, newX);
        newZ = mirrorZ(mirror, newZ);

        x = origX + newX;
        z = origZ + newZ;

        if (pos.getX() == x && pos.getZ() == z) return pos;
        return new Vector3d(x, y, z);
    }


    public static BlockPos.Mutable transformMutablePos(BlockPos.Mutable pos, Mirror mirror, Rotation rotation, BlockPos origin) {
        if (rotation == Rotation.NONE && mirror == Mirror.NONE) return pos;

        int x = pos.getX(), z = pos.getZ();
        int origX = origin.getX(), origZ = origin.getZ();
        int locX = x - origX, locZ = z - origZ;

        locX = mirrorX(mirror, locX);
        locZ = mirrorZ(mirror, locZ);
        int newX = rotateX(rotation, locX, locZ, false);
        int newZ = rotateZ(rotation, locX, locZ, false);

        pos.setX(origX + newX);
        pos.setZ(origZ + newZ);

        return pos;
    }


    public static BlockPos.Mutable untransformMutablePos(BlockPos.Mutable pos, Mirror mirror, Rotation rotation, BlockPos origin) {
        if (rotation == Rotation.NONE && mirror == Mirror.NONE) return pos;

        int x = pos.getX(), z = pos.getZ();
        int origX = origin.getX(), origZ = origin.getZ();
        int locX = x - origX, locZ = z - origZ;

        int newX = rotateX(rotation, locX, locZ, true);
        int newZ = rotateZ(rotation, locX, locZ, true);
        newX = mirrorX(mirror, newX);
        newZ = mirrorZ(mirror, newZ);

        pos.setX(origX + newX);
        pos.setZ(origZ + newZ);

        return pos;
    }

    public static <N extends INBT> Collector<N, ListNBT, ListNBT> collectListNBT() {
        return Collector.of(
            ListNBT::new,
            List::add,
            (l, r) -> {
                l.addAll(r);
                return l;
            },
            Collector.Characteristics.IDENTITY_FINISH
        );
    }

    public static Rotation rotationFromSteps(int steps) {
        switch (steps & 3) {
            case 0: return Rotation.NONE;
            case 1: return Rotation.CLOCKWISE_90;
            case 2: return Rotation.CLOCKWISE_180;
            case 3: return Rotation.COUNTERCLOCKWISE_90;
            default: throw new Error("Java broke");
        }
    }

    public static MutableBoundingBox areaAround(BlockPos pos, int r) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ(), d = r + 1;
        return new MutableBoundingBox(
            x - r, y - r, z - r,
            x + d, y + d, z + d
        );
    }

    public static int manhattanDistanceToBox(BlockPos pos, MutableBoundingBox box) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        int nx = box.minX, ny = box.minY, nz = box.minZ;
        int px = box.maxX, py = box.maxY, pz = box.maxZ;

        int xd = x < nx ? nx - x : x > px ? x - px : 0;
        int yd = y < ny ? ny - y : y > py ? y - py : 0;
        int zd = z < nz ? nz - z : z > pz ? z - pz : 0;

        return xd + yd + zd;
    }

    public static Template loadTemplate(ResourceLocation name, ServerWorld world) {
        TemplateManager templates = world.getStructureTemplateManager();
        Template template = templates.getTemplate(name);

        if (template != null) {
            return template;
        } else {
            String fileName = name.getNamespace() + "/" + name.getPath() + ".snbt";
            Path path = Paths.get(TEST_STRUCTURE_DIR, fileName);

            CompoundNBT snbt = loadSnbt(path);
            if (snbt == null) {
                throw new RuntimeException("Could not find structure file " + path + ", and the structure is not available in the world structures either.");
            } else {
                return templates.createStructure(snbt);
            }
        }
    }

    public static StructureBlockTileEntity placeStructure(ResourceLocation name, BlockPos pos, Rotation rotation, ServerWorld world, boolean lazy) {
        world.setBlockState(pos, Blocks.STRUCTURE_BLOCK.getDefaultState());

        StructureBlockTileEntity sb = (StructureBlockTileEntity) world.getTileEntity(pos);
        assert sb != null;

        sb.setMode(StructureMode.LOAD);
        sb.setRotation(rotation);
        sb.setIgnoresEntities(false);
        sb.setName(name);
        sb.loadStructure(world, lazy);
        if (sb.getStructureSize() != BlockPos.ZERO) {
            return sb;
        } else {
            Template template = loadTemplate(name, world);
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

    public static Optional<BlockPos> findOwnerStructureBlock(BlockPos pos, int r, ServerWorld world) {
        return findStructureBlocks(pos, r, world)
                   .stream()
                   .filter(sbPos -> isInStructureBounds(sbPos, pos, world))
                   .findFirst();
    }

    public static Optional<BlockPos> findNearestStructureBlock(BlockPos pos, int r, ServerWorld world) {
        Comparator<BlockPos> distToPos = Comparator.comparingInt(p -> p.manhattanDistance(pos));
        Collection<BlockPos> sbs = findStructureBlocks(pos, r, world);
        return sbs.stream().min(distToPos);
    }

    public static Collection<BlockPos> findStructureBlocks(BlockPos pos, int r, ServerWorld world) {
        Collection<BlockPos> out = Lists.newArrayList();
        MutableBoundingBox aabb = areaAround(pos, r);
        aabb.minY = Math.max(aabb.minY, 0);
        aabb.maxY = Math.min(aabb.maxY, 255);

        for (int x = aabb.minX; x <= aabb.maxX; x++) {
            for (int y = aabb.minY; y <= aabb.maxY; y++) {
                for (int z = aabb.minZ; z <= aabb.maxZ; z++) {
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

    private static boolean isInStructureBounds(BlockPos sbPos, BlockPos pos, ServerWorld world) {
        StructureBlockTileEntity sb = (StructureBlockTileEntity) world.getTileEntity(sbPos);
        assert sb != null;

        MutableBoundingBox box = getStructureAABB(sb, 3);
        return box.isVecInside(pos);
    }

    public static MutableBoundingBox getStructureAABB(StructureBlockTileEntity sb, int extend) {
        BlockPos pos = sb.getPos();
        BlockPos localUpper = pos.add(sb.getStructureSize().add(-1, -1, -1));
        BlockPos upper = Template.getTransformedPos(localUpper, Mirror.NONE, sb.getRotation(), pos);
        return MutableBoundingBox.createProper(
            pos.getX() - extend, pos.getY() - extend, pos.getZ() - extend,
            upper.getX() + extend, pos.getY() + extend, pos.getZ() + extend
        );
    }
}
