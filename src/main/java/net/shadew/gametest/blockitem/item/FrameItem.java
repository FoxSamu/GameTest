package net.shadew.gametest.blockitem.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.shadew.gametest.blockitem.entity.FrameEntity;

public class FrameItem extends Item  {
    public FrameItem(Properties props) {
        super(props);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        BlockPos pos = context.getPos();
        World world = context.getWorld();
        if(world.isRemote) return ActionResultType.SUCCESS;

        FrameEntity frameEntity = new FrameEntity(world, pos);
        if(frameEntity.canSpawn()) {
            world.addEntity(frameEntity);
            frameEntity.playPlaceSound();
            return ActionResultType.SUCCESS;
        }
        return super.onItemUse(context);
    }
}
