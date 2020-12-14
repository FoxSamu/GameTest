package net.shadew.gametest.framework.api.exception;

import net.minecraft.util.math.BlockPos;

import net.shadew.gametest.framework.api.ITestInstance;
import net.shadew.gametest.framework.api.Marker;

public class LocalAssertException extends AssertException {
    private final BlockPos pos;
    private final BlockPos relativePos;

    public LocalAssertException(String message, BlockPos pos, BlockPos relativePos) {
        super(message);
        this.pos = pos;
        this.relativePos = relativePos;
    }

    @Override
    public String getMessage() {
        return String.format(
            "%s at [%d, %d, %d], relative [%d, %d, %d]",
            super.getMessage(),
            pos.getX(), pos.getY(), pos.getZ(),
            relativePos.getX(), relativePos.getY(), relativePos.getZ()
        );
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockPos getRelativePos() {
        return relativePos;
    }

    @Override
    public void displayErrorInGame(ITestInstance instance) {
        instance.addMarker(pos, Marker.ERROR, super.getMessage() + " here");
    }
}
