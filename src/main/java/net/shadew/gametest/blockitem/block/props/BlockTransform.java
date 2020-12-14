package net.shadew.gametest.blockitem.block.props;

import net.minecraft.block.BlockState;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public enum BlockTransform implements IStringSerializable {
    IDENTITY("identity", Rotation.NONE, false, false),
    ROT90("rot90", Rotation.CLOCKWISE_90, false, false),
    ROT180("rot180", Rotation.CLOCKWISE_180, false, false),
    ROT270("rot270", Rotation.COUNTERCLOCKWISE_90, false, false),
    FLIPX("flipx", Rotation.NONE, true, false),
    ROT90_FLIPX("rot90_flipx", Rotation.CLOCKWISE_90, true, false),
    ROT180_FLIPX("rot180_flipx", Rotation.CLOCKWISE_180, true, false),
    ROT270_FLIPX("rot270_flipx", Rotation.COUNTERCLOCKWISE_90, true, false),
    FLIPZ("flipz", Rotation.NONE, false, true),
    ROT90_FLIPZ("rot90_flipz", Rotation.CLOCKWISE_90, false, true),
    ROT180_FLIPZ("rot180_flipz", Rotation.CLOCKWISE_180, false, true),
    ROT270_FLIPZ("rot270_flipz", Rotation.COUNTERCLOCKWISE_90, false, true),
    FLIPXZ("flipxz", Rotation.NONE, true, true),
    ROT90_FLIPXZ("rot90_flipxz", Rotation.CLOCKWISE_90, true, true),
    ROT180_FLIPXZ("rot180_flipxz", Rotation.CLOCKWISE_180, true, true),
    ROT270_FLIPXZ("rot270_flipxz", Rotation.COUNTERCLOCKWISE_90, true, true);

    private final String str;
    private final Rotation rotation;
    private final boolean flipX;
    private final boolean flipZ;

    BlockTransform(String str, Rotation rotation, boolean flipX, boolean flipZ) {
        this.str = str;
        this.rotation = rotation;
        this.flipX = flipX;
        this.flipZ = flipZ;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public boolean flipX() {
        return flipX;
    }

    public boolean flipZ() {
        return flipZ;
    }

    public BlockTransform withRotation(Rotation rotation) {
        return of(rotation, flipX, flipZ);
    }

    public BlockTransform withFlipX(boolean flipX) {
        return of(rotation, flipX, flipZ);
    }

    public BlockTransform withFlipZ(boolean flipZ) {
        return of(rotation, flipX, flipZ);
    }

    public BlockTransform rotate(Rotation r) {
        if (r == Rotation.NONE) return this;

        boolean swapFlips = r == Rotation.COUNTERCLOCKWISE_90 || r == Rotation.CLOCKWISE_90;
        return of(rotation.add(r), swapFlips ? flipZ : flipX, swapFlips ? flipX : flipZ);
    }

    public BlockTransform mirror(Mirror m) {
        return of(rotation, (m == Mirror.FRONT_BACK) != flipX, (m == Mirror.LEFT_RIGHT) != flipZ);
    }

    public BlockState transform(BlockState state, IWorld world, BlockPos pos) {
        state = state.rotate(world, pos, rotation);
        if(flipX) state = state.mirror(Mirror.FRONT_BACK);
        if(flipZ) state = state.mirror(Mirror.LEFT_RIGHT);
        return state;
    }

    public static BlockTransform of(Rotation rot, boolean flipX, boolean flipZ) {
        for (BlockTransform t : values()) {
            if (t.rotation == rot && t.flipX == flipX && t.flipZ == flipZ) return t;
        }
        assert false;
        return IDENTITY;
    }

    @Override
    public String getString() {
        return str;
    }
}
