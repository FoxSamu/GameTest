package net.shadew.gametest.hooks;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.shadew.gametest.command.NedDebugCommand;

public final class NeighborUpdateHook {
    public static void onSendNeighborUpdate(World world, BlockPos pos) {
        if(world instanceof ServerWorld && NedDebugCommand.sendNeighborUpdates()) {
            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            buf.writeVarLong(world.getGameTime());
            buf.writeBlockPos(pos);
            send((ServerWorld) world, buf, SCustomPayloadPlayPacket.DEBUG_NEIGHBORS_UPDATE);
        }
    }

    private static void send(ServerWorld world, PacketBuffer buf, ResourceLocation id) {
        IPacket<?> ipacket = new SCustomPayloadPlayPacket(id, buf);

        for (PlayerEntity playerentity : world.getPlayers()) {
            ((ServerPlayerEntity) playerentity).connection.sendPacket(ipacket);
        }
    }
}
