package net.shadew.gametest.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.StringTextComponent;

import static net.minecraft.command.Commands.*;

public final class NedDebugCommand {
    private static boolean sendMobPaths = false;
    private static boolean sendNeighborUpdates = false;

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            literal(
                "neddebug"
            ).requires(
                src -> src.hasPermissionLevel(3)
            ).then(
                literal(
                    "send_mob_paths"
                ).then(
                    literal(
                        "on"
                    ).executes(cmd -> {
                        sendMobPaths = true;
                        cmd.getSource().sendFeedback(new StringTextComponent("Sending mob paths."), true);
                        return 0;
                    })
                ).then(
                    literal(
                        "off"
                    ).executes(cmd -> {
                        sendMobPaths = false;
                        cmd.getSource().sendFeedback(new StringTextComponent("No longer sending mob paths."), true);
                        return 0;
                    })
                )
            ).then(
                literal(
                    "send_neighbor_updates"
                ).then(
                    literal(
                        "on"
                    ).executes(cmd -> {
                        sendNeighborUpdates = true;
                        cmd.getSource().sendFeedback(new StringTextComponent("Sending neighbor updates."), true);
                        return 0;
                    })
                ).then(
                    literal(
                        "off"
                    ).executes(cmd -> {
                        sendNeighborUpdates = false;
                        cmd.getSource().sendFeedback(new StringTextComponent("No longer sending neighbor updates."), true);
                        return 0;
                    })
                )
            )
        );
    }

    public static boolean sendMobPaths() {
        return sendMobPaths;
    }

    public static boolean sendNeighborUpdates() {
        return sendNeighborUpdates;
    }
}
