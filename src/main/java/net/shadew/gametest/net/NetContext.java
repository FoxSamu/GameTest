package net.shadew.gametest.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.concurrent.ThreadTaskExecutor;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDispatcher;

import java.util.Optional;

public abstract class NetContext {
    final NetworkEvent.Context ctx;
    final INetPacket packet;
    final NetChannel channel;

    NetContext(NetworkEvent.Context ctx, INetPacket packet, NetChannel channel) {
        this.ctx = ctx;
        this.packet = packet;
        this.channel = channel;
    }

    public NetworkEvent.Context getForgeContext() {
        return ctx;
    }

    public NetworkManager getNetManager() {
        return ctx.getNetworkManager();
    }

    public NetworkDirection getDirection() {
        return ctx.getDirection();
    }

    public PacketDispatcher getDispatcher() {
        return ctx.getPacketDispatcher();
    }

    public abstract PlayerEntity getPlayer();

    public World getWorld() {
        return getPlayer().getEntityWorld();
    }

    public Optional<MinecraftServer> getServer() {
        return Optional.ofNullable(getWorld().getServer());
    }

    public LogicalSide getOriginSide() {
        return getDirection().getOriginationSide();
    }

    public LogicalSide getArrivalSide() {
        return getDirection().getReceptionSide();
    }

    public ThreadTaskExecutor<?> getExecutor() {
        return LogicalSidedProvider.WORKQUEUE.get(getArrivalSide());
    }

    public void runOnMainThread(Runnable task) {
        if(isMainThread()) {
            task.run();
        } else {
            getExecutor().deferTask(task);
        }
    }

    public boolean isMainThread() {
        return getExecutor().isOnExecutionThread();
    }

    public void ensureMainThread() {
        if(isMainThread()) return;
        runOnMainThread(() -> packet.handle(this));
        throw ThreadQuickExitException.INSTANCE;
    }

    public abstract void reply(INetPacket packet);

    public Server assertServer() {
        if(this instanceof Server) return (Server) this;
        throw new RuntimeException("Not on server");
    }

    public Client assertClient() {
        if(this instanceof Client) return (Client) this;
        throw new RuntimeException("Not on client");
    }

    public static NetContext get(NetworkEvent.Context ctx, INetPacket packet, NetChannel channel) {
        LogicalSide side = ctx.getDirection().getReceptionSide();
        if(side == LogicalSide.SERVER) return new Server(ctx, packet, channel);
        return new Client(ctx, packet, channel);
    }

    public static class Server extends NetContext {
        Server(NetworkEvent.Context ctx, INetPacket packet, NetChannel channel) {
            super(ctx, packet, channel);
        }

        @Override
        public ServerPlayerEntity getPlayer() {
            return ctx.getSender();
        }

        @Override
        public ServerWorld getWorld() {
            return getPlayer().getServerWorld();
        }

        public MinecraftServer getMinecraftServer() {
            return getWorld().getServer();
        }

        @Override
        public void reply(INetPacket packet) {
            channel.sendPlayer(getPlayer(), packet);
        }
    }

    public static class Client extends NetContext {
        Client(NetworkEvent.Context ctx, INetPacket packet, NetChannel channel) {
            super(ctx, packet, channel);
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ClientPlayerEntity getPlayer() {
            return Minecraft.getInstance().player;
        }

        @Override
        @OnlyIn(Dist.CLIENT)
        public ClientWorld getWorld() {
            return Minecraft.getInstance().world;
        }

        @OnlyIn(Dist.CLIENT)
        public Minecraft getMinecraft() {
            return Minecraft.getInstance();
        }

        @Override
        public void reply(INetPacket packet) {
            channel.sendServer(packet);
        }
    }
}
