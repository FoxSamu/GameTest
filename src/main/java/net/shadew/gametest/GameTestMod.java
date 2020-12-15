package net.shadew.gametest;

import com.google.common.reflect.Reflection;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.BusBuilder;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.shadew.gametest.blockitem.block.GameTestBlocks;
import net.shadew.gametest.blockitem.block.GameTestBlocksClient;
import net.shadew.gametest.blockitem.entity.GameTestEntityTypes;
import net.shadew.gametest.blockitem.entity.renderer.EntityRenderRegistry;
import net.shadew.gametest.blockitem.item.GameTestItems;
import net.shadew.gametest.blockitem.tileentity.GameTestTileEntityTypes;
import net.shadew.gametest.blockitem.tileentity.renderer.TileEntityRenderRegistry;
import net.shadew.gametest.command.NedDebugCommand;
import net.shadew.gametest.framework.GameTestManager;
import net.shadew.gametest.framework.GameTestRegistry;
import net.shadew.gametest.framework.command.GameTestCommand;
import net.shadew.gametest.framework.command.arguments.GameTestArguments;
import net.shadew.gametest.framework.config.GameTestConfig;
import net.shadew.gametest.framework.output.TestOutputManager;
import net.shadew.gametest.framework.run.TestRunManager;
import net.shadew.gametest.net.GameTestNet;
import net.shadew.gametest.screen.proxy.ClientScreenProxy;
import net.shadew.gametest.screen.proxy.ScreenProxy;
import net.shadew.gametest.util.RenderLayerUtil;

@Mod("gametest")
public class GameTestMod {
    public static final Logger LOGGER = LogManager.getLogger("GameTest");

    public static final IEventBus DEBUG_EVENT_BUS
        = BusBuilder.builder()
                    .setExceptionHandler((bus, event, listeners, index, throwable) -> LOGGER.error("Exception in debug event bus, supressing...", throwable))
                    .build();

    private static ScreenProxy screenProxy;

    public GameTestMod() {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public static ScreenProxy getScreenProxy() {
        return screenProxy;
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent event) {
        screenProxy = DistExecutor.safeRunForDist(
            () -> ClientScreenProxy::new,
            () -> ScreenProxy::new
        );
        GameTestArguments.setup();
        Reflection.initialize(GameTestNet.class);
        GameTestConfig.loadAll();
        GameTestRegistry.dumpDebug();
        TestOutputManager.dumpDebug();
        TestRunManager.dumpDebug();
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void doClientStuff(FMLClientSetupEvent event) {
        GameTestBlocksClient.setupClient();
        TileEntityRenderRegistry.setup();
        EntityRenderRegistry.setup();
        RenderLayerUtil.init();
        // Fix bug in NeighborsUpdateDebugRenderer by using our own
//        Minecraft.getInstance().debugRenderer.neighborsUpdate = new FixedNeighborsUpdateDebugRenderer(Minecraft.getInstance());
    }

    public static class EventHandler {
        @SubscribeEvent
        public void registerCommands(RegisterCommandsEvent event) {
            CommandDispatcher<CommandSource> dispatcher = event.getDispatcher();
            NedDebugCommand.register(dispatcher);
            GameTestCommand.register(dispatcher);
//            TestCommand.register(dispatcher);
        }

        @SubscribeEvent
        public void onTick(TickEvent.ServerTickEvent event) {
            GameTestManager.tick();
        }

//        @SubscribeEvent
//        @OnlyIn(Dist.CLIENT)
//        @SuppressWarnings("deprecation")
//        public void onRenderLast(RenderWorldLastEvent event) {
//            RenderSystem.matrixMode(0x1701);
//            RenderSystem.loadIdentity();
//            RenderSystem.multMatrix(event.getProjectionMatrix());
//            RenderSystem.matrixMode(0x1700);
//            RenderLayerUtil.renderPost();
//        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class RegistryEvents {
        @SubscribeEvent
        public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
            GameTestBlocks.register(event.getRegistry());
        }
        @SubscribeEvent
        public static void onRegisterItems(RegistryEvent.Register<Item> event) {
            GameTestBlocks.registerItems(event.getRegistry());
            GameTestItems.register(event.getRegistry());
        }
        @SubscribeEvent
        public static void onRegisterTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
            GameTestTileEntityTypes.register(event.getRegistry());
        }
        @SubscribeEvent
        public static void onRegisterEntities(RegistryEvent.Register<EntityType<?>> event) {
            GameTestEntityTypes.register(event.getRegistry());
        }
    }
}
