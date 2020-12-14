package net.shadew.gametest.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.NewChatGui;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.event.DebugKeyEvent;
import org.lwjgl.glfw.GLFW;

public final class DebugKeyHook {
    public static boolean onProcessDebugKey(boolean current, int key) {
        if(key != GLFW.GLFW_KEY_F3) {
            DebugKeyEvent event = new DebugKeyEvent(key);
            return GameTestMod.DEBUG_EVENT_BUS.post(event) || current;
        }
        return current;
    }

    @SubscribeEvent
    public static void onDebugKey(DebugKeyEvent event) {
        int key = event.getKey();
        Minecraft mc = Minecraft.getInstance();

        if(key == GLFW.GLFW_KEY_Q) {
            NewChatGui chat = mc.ingameGUI.getChatGUI();
            chat.printChatMessage(new StringTextComponent("F3 + W = Toggle fluid debug info"));
            chat.printChatMessage(new StringTextComponent("F3 + E = Toggle pathfinding debug info"));
            chat.printChatMessage(new StringTextComponent("F3 + U = Toggle neigbor updates debug info"));
            event.setCanceled(true);
            return;
        }

        if(key == GLFW.GLFW_KEY_W) {
            DebugRenderHook.showFluids = !DebugRenderHook.showFluids;
            if(DebugRenderHook.showFluids) {
                printDebugMessage("Fluid debug shown");
            } else {
                printDebugMessage("Fluid debug hidden");
            }
            event.setCanceled(true);
            return;
        }

        if(key == GLFW.GLFW_KEY_U) {
            DebugRenderHook.showNeighborsUpdate = !DebugRenderHook.showNeighborsUpdate;
            if(DebugRenderHook.showNeighborsUpdate) {
                printDebugMessage("Neighbor updates debug shown");
                printDebugMessage("Make sure the server sends updates using '/neddebug send_neighbor_updates on'");
            } else {
                printDebugMessage("Neighbor updates debug hidden");
            }
            event.setCanceled(true);
            return;
        }

        if(key == GLFW.GLFW_KEY_E) {
            DebugRenderHook.showPathfinding = !DebugRenderHook.showPathfinding;
            if(DebugRenderHook.showPathfinding) {
                printDebugMessage("Pathfinding debug shown");
                printDebugMessage("Make sure the server sends mob paths using '/neddebug send_mob_paths on'");
            } else {
                printDebugMessage("Pathfinding debug hidden");
            }
            event.setCanceled(true);
            return;
        }
    }
    private static void printDebugMessage(String message, Object... args) {
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new StringTextComponent("").append(new TranslationTextComponent("debug.prefix").formatted(TextFormatting.YELLOW, TextFormatting.BOLD)).append(" ").append(new StringTextComponent(String.format(message, args))));
    }

    private static void printDebugWarning(String message, Object... args) {
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(new StringTextComponent("").append(new TranslationTextComponent("debug.prefix").formatted(TextFormatting.RED, TextFormatting.BOLD)).append(" ").append(new StringTextComponent(String.format(message, args))));
    }

    static {
        GameTestMod.DEBUG_EVENT_BUS.register(DebugKeyHook.class);
    }
}
