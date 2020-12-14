package net.shadew.gametest.blockitem.entity;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.registries.IForgeRegistry;

public abstract class GameTestEntityTypes {
    public static final EntityType<FrameEntity> FRAME = EntityType.Builder.<FrameEntity>create(FrameEntity::new, EntityClassification.MISC)
                                                                          .setShouldReceiveVelocityUpdates(false)
                                                                          .size(1.002f, 1.002f)
                                                                          .build("gametest:frame");

    public static void register(IForgeRegistry<EntityType<?>> registry) {
        registry.registerAll(
            FRAME.setRegistryName("gametest:frame")
        );
    }
}
