package net.shadew.gametest.blockitem.tileentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RemoteTileEntity extends TileEntity implements ITickableTileEntity {
    private final Map<ResourceLocation, BlockPos> testLocations = new HashMap<>();
    private int nextCheck = 40;

    private int currentRowLength = 0;
    private BlockPos.Mutable nextPos;

    public RemoteTileEntity() {
        super(GameTestTileEntityTypes.REMOTE);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        CompoundNBT locs = new CompoundNBT();
        for (Map.Entry<ResourceLocation, BlockPos> location : testLocations.entrySet()) {
            BlockPos v = location.getValue();
            locs.putIntArray(location.getKey().toString(), new int[] {v.getX(), v.getY(), v.getZ()});
        }
        nbt.put("TestLocations", locs);
        nbt.putInt("NextCheck", nextCheck);

        if(nextPos != null) {
            nbt.putInt("NextX", nextPos.getX());
            nbt.putInt("NextY", nextPos.getY());
            nbt.putInt("NextZ", nextPos.getZ());
            nbt.putInt("CurrentRowLength", currentRowLength);
        }
        return super.write(nbt);
    }

    @Override
    public void fromTag(BlockState state, CompoundNBT nbt) {
        testLocations.clear();
        CompoundNBT locs = nbt.getCompound("TestLocations");
        for(String key : locs.keySet()) {
            ResourceLocation name = new ResourceLocation(key);
            int[] coords = locs.getIntArray(key);
            testLocations.put(name, new BlockPos(coords[0], coords[1], coords[2]));
        }
        nextCheck = nbt.getInt("NextCheck");
        super.fromTag(state, nbt);
    }

    private void checkTests() {
        HashSet<ResourceLocation> toRemove = new HashSet<>();
        for(Map.Entry<ResourceLocation, BlockPos> entry : testLocations.entrySet()) {
            if(!isValid(entry.getKey(), entry.getValue())) {
                toRemove.add(entry.getKey());
            }
        }

        for(ResourceLocation removeKey : toRemove) {
            testLocations.remove(removeKey);
        }
    }

    private boolean isValid(ResourceLocation name, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if(!(te instanceof TestBlockTileEntity)) return false;

        TestBlockTileEntity testBlock = (TestBlockTileEntity) te;
        TemplateBlockTileEntity templateBlock = testBlock.getTemplateBlock();
        if(templateBlock == null || testBlock.getTemplateBlockPos() == null) return false;

        ResourceLocation actualName = templateBlock.getName();
        return actualName.equals(name);
    }

    @Override
    public void tick() {
        if(nextCheck -- <= 0) {
            nextCheck += 40;
            checkTests();
        }

        if(nextPos == null) {
            nextPos = new BlockPos.Mutable();
            nextPos.setPos(getPos());
            nextPos.move(Direction.NORTH, 5);
            currentRowLength = 0;
        }
    }
}
