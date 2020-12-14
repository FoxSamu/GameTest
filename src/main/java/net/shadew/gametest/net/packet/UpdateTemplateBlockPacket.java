package net.shadew.gametest.net.packet;

import com.mojang.datafixers.util.Either;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.framework.GameTestFunction;

public class UpdateTemplateBlockPacket extends TemplateBlockPacket {
    private ResourceLocation name;
    private boolean rawTemplate;
    private int width;
    private int height;
    private int depth;

    public UpdateTemplateBlockPacket(BlockPos pos, Either<ResourceLocation, GameTestFunction> name, int width, int height, int depth) {
        super(pos);
        this.name = name.map(l -> l, GameTestFunction::getName);
        this.rawTemplate = name.left().isPresent();
        this.width = width;
        this.height = height;
        this.depth = depth;
    }

    public UpdateTemplateBlockPacket() {
    }

    @Override
    public void write(PacketBuffer buf) {
        super.write(buf);
        buf.writeResourceLocation(name);
        buf.writeBoolean(rawTemplate);
        buf.writeByte(width);
        buf.writeByte(height);
        buf.writeByte(depth);
    }

    @Override
    public TemplateBlockPacket read(PacketBuffer buf) {
        super.read(buf);
        name = buf.readResourceLocation();
        rawTemplate = buf.readBoolean();
        width = buf.readByte();
        height = buf.readByte();
        depth = buf.readByte();
        return this;
    }

    @Override
    protected void updateTemplateBlock(TemplateBlockTileEntity templateBlock) {
        templateBlock.setName(name);
        templateBlock.setRawTemplate(rawTemplate);
        templateBlock.setWidth(width);
        templateBlock.setHeight(height);
        templateBlock.setDepth(depth);
        templateBlock.markUpdate();
    }
}
