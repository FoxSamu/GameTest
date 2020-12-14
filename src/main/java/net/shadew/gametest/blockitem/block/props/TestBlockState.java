package net.shadew.gametest.blockitem.block.props;

import net.minecraft.util.IStringSerializable;

public enum TestBlockState implements IStringSerializable {
    OFF("off", 0),
    LOOKING("looking", 0),
    E404("e404", 0),
    WORKING("working", 0xFF3459EB),
    OK("ok", 0xFF34EB68),
    WARNING("warning", 0xFFF5B433),
    ERROR("error", 0xFFFC2E05),
    ITEM("item", 0); // State with item texture, only placeable via debug stick :)

    private final String string;
    private final int color;

    TestBlockState(String string, int color) {
        this.string = string;
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public String getString() {
        return string;
    }
}
