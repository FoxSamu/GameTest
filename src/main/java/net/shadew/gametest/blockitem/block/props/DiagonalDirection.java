package net.shadew.gametest.blockitem.block.props;

import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public enum DiagonalDirection implements IStringSerializable {
    NW("nw", Rotation.CLOCKWISE_180, Direction.NORTH, Direction.WEST),
    NE("ne", Rotation.COUNTERCLOCKWISE_90, Direction.NORTH, Direction.EAST),
    SE("se", Rotation.NONE, Direction.SOUTH, Direction.EAST),
    SW("sw", Rotation.CLOCKWISE_90, Direction.SOUTH, Direction.WEST);

    private final String name;
    private final Rotation rotation;
    private final Direction z;
    private final Direction x;

    DiagonalDirection(String name, Rotation rot, Direction z, Direction x) {
        this.name = name;
        this.rotation = rot;
        this.z = z;
        this.x = x;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public Direction getXAxisDir() {
        return x;
    }

    public Direction getZAxisDir() {
        return z;
    }

    public DiagonalDirection rotate(Rotation rot) {
        return fromRotation(getRotation().add(rot));
    }

    public DiagonalDirection mirror(Mirror mirr) {
        return from2Dir(mirr.mirror(z), mirr.mirror(x));
    }

    @Override
    public String getString() {
        return name;
    }

    public static DiagonalDirection fromRotation(Rotation rot) {
        if (rot == null) throw new NullPointerException();
        if (rot == Rotation.NONE) return SE;
        if (rot == Rotation.CLOCKWISE_90) return SW;
        if (rot == Rotation.CLOCKWISE_180) return NW;
        if (rot == Rotation.COUNTERCLOCKWISE_90) return NE;
        throw new Error("Who broke the universe");
    }

    public static DiagonalDirection from2Dir(Direction dir1, Direction dir2) {
        if (dir1 == null) throw new NullPointerException("dir1");
        if (dir2 == null) throw new NullPointerException("dir2");
        if (dir1.getAxis() == Direction.Axis.Y) throw new NullPointerException("dir1 is vertical");
        if (dir2.getAxis() == Direction.Axis.Y) throw new NullPointerException("dir2 is vertical");
        if (dir1.getAxis() == dir2.getAxis()) throw new NullPointerException("dir1 and dir2 are parallel");

        if (dir1.getAxis() == Direction.Axis.X) {
            Direction tmp = dir2;
            dir2 = dir1;
            dir1 = tmp;
        }

        if(dir1 == Direction.NORTH && dir2 == Direction.WEST) return NW;
        if(dir1 == Direction.NORTH && dir2 == Direction.EAST) return NE;
        if(dir1 == Direction.SOUTH && dir2 == Direction.EAST) return SE;
        if(dir1 == Direction.SOUTH && dir2 == Direction.WEST) return SW;
        throw new Error("Who broke the universe");
    }

    public static DiagonalDirection fromEntityFacing(Entity entity) {
        Direction dir1 = null;
        Direction dir2 = null;

        Direction[] facings = Direction.getFacingDirections(entity);
        for(Direction facing : facings) {
            if(facing.getAxis().isHorizontal()) {
                if(dir1 == null) dir1 = facing;
                else if(dir2 == null && facing.getAxis() != dir1.getAxis()) dir2 = facing;
            }
        }

        assert dir1 != null;
        assert dir2 != null;
        assert dir1.getAxis() != dir2.getAxis();

        return from2Dir(dir1, dir2);
    }
}
