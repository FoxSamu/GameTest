package net.shadew.gametest.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.Rotation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.EmptyBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TemplateHelper {
    private final List<Palette> blocks = Lists.newArrayList();
    private final List<EntityInfo> entities = Lists.newArrayList();
    private BlockPos size = BlockPos.ZERO;

    public void takeBlocksFromWorld(World world, BlockPos pos, BlockPos size, Rotation rot, boolean entities, @Nullable Block ignore) {
        if (size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1) {
            BlockPos pos2 = Utils.transformPos(pos.add(size).add(-1, -1, -1), Mirror.NONE, rot, pos);

            List<BlockInfo> solid = Lists.newArrayList();
            List<BlockInfo> withNbt = Lists.newArrayList();
            List<BlockInfo> other = Lists.newArrayList();

            BlockPos lower = new BlockPos(
                Math.min(pos.getX(), pos2.getX()),
                Math.min(pos.getY(), pos2.getY()),
                Math.min(pos.getZ(), pos2.getZ())
            );
            BlockPos upper = new BlockPos(
                Math.max(pos.getX(), pos2.getX()),
                Math.max(pos.getY(), pos2.getY()),
                Math.max(pos.getZ(), pos2.getZ())
            );

            this.size = size;

            for (BlockPos ipos : BlockPos.getAllInBoxMutable(lower, upper)) {
                BlockPos lpos = Utils.untransformPos(ipos, Mirror.NONE, rot, pos).subtract(pos);
                BlockState state = world.getBlockState(ipos);
                Rotation invRot = rot;
                if(rot == Rotation.COUNTERCLOCKWISE_90) invRot = Rotation.CLOCKWISE_90;
                if(rot == Rotation.CLOCKWISE_90) invRot = Rotation.COUNTERCLOCKWISE_90;
                state = state.rotate(world, ipos, invRot);
                if (ignore == null || ignore != state.getBlock()) {
                    TileEntity te = world.getTileEntity(ipos);
                    BlockInfo info;
                    if (te != null) {
                        CompoundNBT nbt = te.write(new CompoundNBT());
                        nbt.remove("x");
                        nbt.remove("y");
                        nbt.remove("z");
                        info = new BlockInfo(lpos, state, nbt.copy());
                    } else {
                        info = new BlockInfo(lpos, state, null);
                    }

                    sortBlock(info, solid, withNbt, other);
                }
            }

            List<BlockInfo> merged = mergeBlocks(solid, withNbt, other);
            this.blocks.clear();
            this.blocks.add(new Palette(merged));
            if (entities) {
                takeEntitiesFromWorld(world, pos, size, rot);
            } else {
                this.entities.clear();
            }

        }
    }

    private static void sortBlock(BlockInfo info, List<BlockInfo> solid, List<BlockInfo> withNbt, List<BlockInfo> other) {
        if (info.nbt != null) {
            withNbt.add(info);
        } else if (!info.state.getBlock().isVariableOpacity() && info.state.isFullCube(EmptyBlockReader.INSTANCE, BlockPos.ZERO)) {
            solid.add(info);
        } else {
            other.add(info);
        }

    }

    private static List<BlockInfo> mergeBlocks(List<BlockInfo> solid, List<BlockInfo> withNbt, List<BlockInfo> other) {
        Comparator<BlockInfo> comparator = Comparator.<BlockInfo>comparingInt(info -> info.pos.getY())
                                               .thenComparingInt(info -> info.pos.getX())
                                               .thenComparingInt(info -> info.pos.getZ());

        solid.sort(comparator);
        other.sort(comparator);
        withNbt.sort(comparator);
        List<BlockInfo> merged = Lists.newArrayList();
        merged.addAll(solid);
        merged.addAll(other);
        merged.addAll(withNbt);
        return merged;
    }

    private void takeEntitiesFromWorld(World world, BlockPos pos, BlockPos size, Rotation rot) {
        BlockPos pos2 = Utils.transformPos(pos.add(size).add(-1, -1, -1), Mirror.NONE, rot, pos);

        BlockPos lower = new BlockPos(
            Math.min(pos.getX(), pos2.getX()),
            Math.min(pos.getY(), pos2.getY()),
            Math.min(pos.getZ(), pos2.getZ())
        );
        BlockPos upper = new BlockPos(
            Math.max(pos.getX(), pos2.getX()),
            Math.max(pos.getY(), pos2.getY()),
            Math.max(pos.getZ(), pos2.getZ())
        );

        Rotation invRot = rot;
        if(rot == Rotation.COUNTERCLOCKWISE_90) invRot = Rotation.CLOCKWISE_90;
        if(rot == Rotation.CLOCKWISE_90) invRot = Rotation.COUNTERCLOCKWISE_90;

        List<Entity> inBox = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(lower, upper), entity -> !(entity instanceof PlayerEntity));
        entities.clear();

        for (Entity entity : inBox) {
            Vector3d global = Utils.untransformPos(entity.getPositionVec(), Mirror.NONE, rot, pos);
            Vector3d local = new Vector3d(global.getX() - pos.getX(), global.getY() - pos.getY(), global.getZ() - pos.getZ());


            CompoundNBT nbt = new CompoundNBT();
            entity.writeUnlessPassenger(nbt);
            BlockPos entityPos;
            if (entity instanceof PaintingEntity) {
                entityPos = Utils.untransformPos(((PaintingEntity) entity).getHangingPosition(), Mirror.NONE, rot, pos).subtract(pos);
            } else {
                entityPos = new BlockPos(local);
            }

            ListNBT rotation = nbt.getList("Rotation", Constants.NBT.TAG_FLOAT);
            rotation.clear();
            rotation.add(FloatNBT.of(entity.getRotatedYaw(invRot)));
            rotation.add(FloatNBT.of(entity.rotationPitch));
            nbt.put("Rotation", rotation);

            entities.add(new EntityInfo(local, entityPos, nbt.copy()));
        }
    }

    public Template toTemplate() {
        Template template = new Template();
        template.read(writeToNBT(new CompoundNBT()));
        return template;
    }

    public CompoundNBT writeToNBT(CompoundNBT nbt) {
        if (blocks.isEmpty()) {
            nbt.put("blocks", new ListNBT());
            nbt.put("palette", new ListNBT());
        } else {
            List<BasicPalette> palettes = Lists.newArrayList();
            BasicPalette palette = new BasicPalette();
            palettes.add(palette);

            for (int i = 1; i < blocks.size(); ++i) {
                palettes.add(new BasicPalette());
            }

            ListNBT blocksList = new ListNBT();
            List<BlockInfo> blockInfo = blocks.get(0).getAll();

            for (int j = 0; j < blockInfo.size(); ++j) {
                BlockInfo block = blockInfo.get(j);

                CompoundNBT blockNbt = new CompoundNBT();
                blockNbt.put("pos", writeInts(block.pos.getX(), block.pos.getY(), block.pos.getZ()));

                int k = palette.idFor(block.state);
                blockNbt.putInt("state", k);

                if (block.nbt != null) {
                    blockNbt.put("nbt", block.nbt);
                }

                blocksList.add(blockNbt);

                for (int b = 1; b < blocks.size(); ++b) {
                    BasicPalette palette1 = palettes.get(b);
                    palette1.addMapping(blocks.get(b).getAll().get(j).state, k);
                }
            }

            nbt.put("blocks", blocksList);
            if (palettes.size() == 1) {
                ListNBT paletteList = new ListNBT();

                for (BlockState blockstate : palette) {
                    paletteList.add(NBTUtil.writeBlockState(blockstate));
                }

                nbt.put("palette", paletteList);
            } else {
                ListNBT palettesList = new ListNBT();

                for (BasicPalette palette1 : palettes) {
                    ListNBT paletteList = new ListNBT();

                    for (BlockState state : palette1) {
                        paletteList.add(NBTUtil.writeBlockState(state));
                    }

                    palettesList.add(paletteList);
                }

                nbt.put("palettes", palettesList);
            }
        }

        ListNBT entitiesList = new ListNBT();

        for (EntityInfo entity : entities) {
            CompoundNBT entityNbt = new CompoundNBT();
            entityNbt.put("pos", writeDoubles(entity.pos.x, entity.pos.y, entity.pos.z));
            entityNbt.put("blockPos", writeInts(entity.blockPos.getX(), entity.blockPos.getY(), entity.blockPos.getZ()));
            if (entity.nbt != null) {
                entityNbt.put("nbt", entity.nbt);
            }

            entitiesList.add(entityNbt);
        }

        nbt.put("entities", entitiesList);
        nbt.put("size", writeInts(size.getX(), size.getY(), size.getZ()));
        nbt.putInt("DataVersion", SharedConstants.getVersion().getWorldVersion());
        return nbt;
    }

    private ListNBT writeInts(int... ints) {
        ListNBT nbtList = new ListNBT();

        for (int i : ints) {
            nbtList.add(IntNBT.of(i));
        }

        return nbtList;
    }

    private ListNBT writeDoubles(double... doubles) {
        ListNBT nbtList = new ListNBT();

        for (double d : doubles) {
            nbtList.add(DoubleNBT.of(d));
        }

        return nbtList;
    }

    static class BasicPalette implements Iterable<BlockState> {
        public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.getDefaultState();
        private final ObjectIntIdentityMap<BlockState> ids = new ObjectIntIdentityMap<>(16);
        private int lastId;

        private BasicPalette() {
        }

        public int idFor(BlockState p_189954_1_) {
            int i = this.ids.getId(p_189954_1_);
            if (i == -1) {
                i = this.lastId++;
                this.ids.put(p_189954_1_, i);
            }

            return i;
        }

        @Nullable
        public BlockState stateFor(int p_189955_1_) {
            BlockState blockstate = this.ids.getByValue(p_189955_1_);
            return blockstate == null ? DEFAULT_BLOCK_STATE : blockstate;
        }

        public Iterator<BlockState> iterator() {
            return this.ids.iterator();
        }

        public void addMapping(BlockState p_189956_1_, int p_189956_2_) {
            this.ids.put(p_189956_1_, p_189956_2_);
        }
    }

    public static class BlockInfo {
        public final BlockPos pos;
        public final BlockState state;
        public final CompoundNBT nbt;

        public BlockInfo(BlockPos p_i47042_1_, BlockState p_i47042_2_, @Nullable CompoundNBT p_i47042_3_) {
            this.pos = p_i47042_1_;
            this.state = p_i47042_2_;
            this.nbt = p_i47042_3_;
        }

        public String toString() {
            return String.format("<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
        }
    }

    public static class EntityInfo {
        public final Vector3d pos;
        public final BlockPos blockPos;
        public final CompoundNBT nbt;

        public EntityInfo(Vector3d p_i47101_1_, BlockPos p_i47101_2_, CompoundNBT p_i47101_3_) {
            this.pos = p_i47101_1_;
            this.blockPos = p_i47101_2_;
            this.nbt = p_i47101_3_;
        }
    }

    public static final class Palette {
        private final List<BlockInfo> infos;
        private final Map<Block, List<BlockInfo>> blockToInfos = Maps.newHashMap();

        private Palette(List<BlockInfo> p_i232120_1_) {
            this.infos = p_i232120_1_;
        }

        public List<BlockInfo> getAll() {
            return this.infos;
        }

        public List<BlockInfo> getAllOf(Block p_237158_1_) {
            return this.blockToInfos.computeIfAbsent(p_237158_1_, (p_237160_1_) -> {
                return this.infos.stream().filter((p_237159_1_) -> {
                    return p_237159_1_.state.isIn(p_237160_1_);
                }).collect(Collectors.toList());
            });
        }
    }
}
