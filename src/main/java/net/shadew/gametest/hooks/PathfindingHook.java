package net.shadew.gametest.hooks;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SCustomPayloadPlayPacket;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import net.shadew.gametest.command.NedDebugCommand;

import javax.annotation.Nullable;
import java.util.List;

public final class PathfindingHook {
    public static void onSendPath(World world, MobEntity ent, @Nullable Path path, float maxDist) {
        if (NedDebugCommand.sendMobPaths() && world instanceof ServerWorld && path != null) {
            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            buf.writeInt(ent.getEntityId());
            buf.writeFloat(maxDist);
            buf.writeBoolean(path.reachesTarget());
            buf.writeInt(path.getCurrentPathIndex());

            // Unused flaggedPathPoints (field_224772_d) field, send an empty set because it's always null...
            buf.writeInt(0); // 0 length, no extra points to send after it

            // Write target
            BlockPos target = path.getTarget();
            buf.writeInt(target.getX());
            buf.writeInt(target.getY());
            buf.writeInt(target.getZ());

            // Write path itself
            List<PathPoint> points = ObfuscationReflectionHelper.getPrivateValue(Path.class, path, "field_75884_a");
            buf.writeInt(points.size());
            for (PathPoint pt : points) {
                writePathPoint(pt, buf);
            }

            // Write open and closed set
            writePathPointSet(path.getOpenSet(), buf);
            writePathPointSet(path.getClosedSet(), buf);

            // Send
            send((ServerWorld) world, buf, SCustomPayloadPlayPacket.DEBUG_PATH);
        }
    }

    private static void writePathPoint(PathPoint pt, PacketBuffer buf) {
        buf.writeInt(pt.x);
        buf.writeInt(pt.y);
        buf.writeInt(pt.z);
        buf.writeFloat(pt.field_222861_j);
        buf.writeFloat(pt.costMalus);
        buf.writeBoolean(pt.visited);
        buf.writeInt(pt.nodeType.ordinal());
        buf.writeFloat(pt.distanceToTarget);
    }

    private static void writePathPointSet(PathPoint[] pts, PacketBuffer buf) {
        buf.writeInt(pts.length);
        for (PathPoint pt : pts) {
            writePathPoint(pt, buf);
        }
    }

    private static void send(ServerWorld world, PacketBuffer buf, ResourceLocation id) {
        IPacket<?> ipacket = new SCustomPayloadPlayPacket(id, buf);

        for (PlayerEntity playerentity : world.getPlayers()) {
            ((ServerPlayerEntity) playerentity).connection.sendPacket(ipacket);
        }
    }
}
