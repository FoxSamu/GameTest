package net.shadew.gametest.blockitem.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.Pose;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Collections;

import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.blockitem.item.GameTestItems;

public class FrameEntity extends Entity {
    private BlockPos framePos = new BlockPos(0, 0, 0);

    private static final DataParameter<String> NAME = EntityDataManager.createKey(FrameEntity.class, DataSerializers.STRING);

    public FrameEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public FrameEntity(World world, BlockPos pos) {
        super(GameTestEntityTypes.FRAME, world);
        setFramePos(pos);
    }

    public boolean canSpawn() {
        return world.getEntitiesInAABBexcluding(this, getBoundingBox().grow(-0.2), entity -> entity instanceof FrameEntity).isEmpty();
    }

    public void setFramePos(BlockPos framePos) {
        this.framePos = framePos;
        updateBoundingBox();
    }

    public BlockPos getFramePos() {
        return framePos;
    }

    protected void updateBoundingBox() {
        double cx = framePos.getX() + 0.5, cy = framePos.getY() + 0.5, cz = framePos.getZ() + 0.5;
        setPos(cx, cy, cz);

        double o = 0.501d;
        setBoundingBox(new AxisAlignedBB(cx - o, cy - o, cz - o, cx + o, cy + o, cz + o));
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Pose pose) {
        return getBoundingBox();
    }

    public void playPlaceSound() {
        playSound(SoundEvents.BLOCK_NETHERITE_BLOCK_PLACE, 1, 1);
    }

    @Override
    public void move(MoverType type, Vector3d velo) {
    }

    @Override
    public void addVelocity(double accX, double accY, double accZ) {
    }

    @Override
    public float getCollisionBorderSize() {
        return 0;
    }

    public void destroy() {
        remove();
        playSound(SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK, 1, 1);
        markVelocityChanged();
    }

    @Override
    public boolean attackEntityFrom(DamageSource src, float damage) {
        if (isInvulnerableTo(src)) {
            return false;
        } else {
            if (isAlive() && !world.isRemote) {
                destroy();
            }

            return true;
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distSq) {
        double max = 16;
        max = max * 64 * getRenderDistanceWeight();
        return distSq < max * max;
    }

    @Override
    public boolean hitByEntity(Entity entity) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) entity;
            if (player.canUseCommandBlock()) {
                return attackEntityFrom(DamageSource.causePlayerDamage(player), 0);
            } else {
                playSound(SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK, 1, 1);
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    protected boolean shouldSetPosAfterLoading() {
        return false;
    }

    @Override
    protected void registerData() {
        dataManager.register(NAME, "");
    }

    public void setFrameName(String name) {
        dataManager.set(NAME, name);
    }

    public String getFrameName() {
        return dataManager.get(NAME);
    }

    @Override
    protected void readAdditional(CompoundNBT nbt) {
        framePos = new BlockPos(nbt.getInt("FrameX"), nbt.getInt("FrameY"), nbt.getInt("FrameZ"));
        setFrameName(nbt.getString("FrameName"));
    }

    @Override
    protected void writeAdditional(CompoundNBT nbt) {
        nbt.putInt("FrameX", framePos.getX());
        nbt.putInt("FrameY", framePos.getY());
        nbt.putInt("FrameZ", framePos.getZ());
        nbt.putString("FrameName", getFrameName());
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningBoltEntity lightning) {
    }

    @Override
    public void recalculateSize() {
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        framePos = new BlockPos(x, y, z);
        updateBoundingBox();
        isAirBorne = true;
    }

    @Override
    public RayTraceResult pick(double dist, float partialTicks, boolean fluids) {
        Vector3d v1 = getEyePosition(partialTicks);
        Vector3d d = getLook(partialTicks);
        Vector3d v2 = v1.add(d.x * dist, d.y * dist, d.z * dist);
        return this.world.rayTraceBlocks(new RayTraceContext(v1, v2, RayTraceContext.BlockMode.OUTLINE, fluids ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE, this));
    }

    private static float getBlockReachDistance(PlayerEntity player) {
        float d = (float) player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        return player.isCreative() ? d : d - 0.5F;
    }

    public BlockRayTraceResult target(Entity entity) {
        AxisAlignedBB box = getBoundingBox();

        Vector3d v1 = entity.getEyePosition(1);
        Vector3d d = entity.getLook(1);
        double dist = entity instanceof PlayerEntity ? getBlockReachDistance((PlayerEntity) entity) : 10;
        Vector3d v2 = v1.add(d.x * dist, d.y * dist, d.z * dist);

        return AxisAlignedBB.rayTrace(Collections.singletonList(box), v1, v2, BlockPos.ZERO);
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource src) {
        return isInvulnerable() && src != DamageSource.OUT_OF_WORLD && !src.isCreativePlayer();
    }


    @Override
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    public ActionResultType processInitialInteract(PlayerEntity player, Hand hand) {
        if (!player.canUseCommandBlock())
            return super.processInitialInteract(player, hand);

        if (player.getHeldItem(hand).getItem() == GameTestItems.TEST_MARKER_FRAME) {
            BlockRayTraceResult rtr = target(player);
            if (rtr != null) {
                Direction face = rtr.getFace();
                if(player.isSneaking()) face = face.getOpposite();

                if (!world.isRemote) {
                    setFramePos(getFramePos().offset(face));
                    playSound(SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK, 1, 1);
                }
                return ActionResultType.SUCCESS;
            }
        } else if (!player.isSneaking()) {
            if (world.isRemote) {
                GameTestMod.getScreenProxy().openFrame(player, this);
            }
            return ActionResultType.SUCCESS;
        }
        return super.processInitialInteract(player, hand);
    }
}
