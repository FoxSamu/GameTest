package net.shadew.gametest.net;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class NetChannel {
    private final NetPacketCodec toServer = new NetPacketCodec();
    private final NetPacketCodec toClient = new NetPacketCodec();
    private final INetProtocol protocol;
    private final SimpleChannel channel;

    public NetChannel(String id, INetProtocol protocol) {
        this.protocol = protocol;

        channel = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(id),
            protocol::getVersion,
            protocol::isClientCompatible,
            protocol::isServerCompatible
        );
        protocol.registerToClient(toClient);
        protocol.registerToServer(toServer);

        channel.registerMessage(
            0, MessageToServer.class,
            this::encodeToServer, this::decodeToServer, this::handleToServer,
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        channel.registerMessage(
            1, MessageToClient.class,
            this::encodeToClient, this::decodeToClient, this::handleToClient,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public INetProtocol getProtocol() {
        return protocol;
    }

    public NetPacketCodec getToClientCodec() {
        return toClient;
    }

    public NetPacketCodec getToServerCodec() {
        return toServer;
    }

    public void send(PacketDistributor.PacketTarget target, INetPacket packet) {
        channel.send(target, message(packet, target.getDirection().getReceptionSide()));
    }

    public void send(PacketDistributor<Void> target, INetPacket packet) {
        send(target.noArg(), packet);
    }

    public <T> void send(PacketDistributor<T> target, Supplier<T> arg, INetPacket packet) {
        send(target.with(arg), packet);
    }

    public <T> void send(PacketDistributor<T> target, T arg, INetPacket packet) {
        send(target.with(() -> arg), packet);
    }

    public void sendServer(INetPacket packet) {
        send(PacketDistributor.SERVER, packet);
    }

    public void sendAll(INetPacket packet) {
        send(PacketDistributor.ALL, packet);
    }

    public void sendNear(PacketDistributor.TargetPoint target, INetPacket packet) {
        send(PacketDistributor.NEAR, target, packet);
    }

    public void sendNear(Supplier<PacketDistributor.TargetPoint> target, INetPacket packet) {
        send(PacketDistributor.NEAR, target, packet);
    }

    public void sendNear(ServerPlayerEntity excluded, double x, double y, double z, double radiusSq, RegistryKey<World> dimension, INetPacket packet) {
        sendNear(() -> new PacketDistributor.TargetPoint(excluded, x, y, z, radiusSq, dimension), packet);
    }

    public void sendNear(double x, double y, double z, double radiusSq, RegistryKey<World> dimension, INetPacket packet) {
        sendNear(() -> new PacketDistributor.TargetPoint(x, y, z, radiusSq, dimension), packet);
    }

    public void sendPlayer(ServerPlayerEntity player, INetPacket packet) {
        send(PacketDistributor.PLAYER, player, packet);
    }

    public void sendPlayer(Supplier<ServerPlayerEntity> player, INetPacket packet) {
        send(PacketDistributor.PLAYER, player, packet);
    }

    public void sendTrackingEntity(Entity entity, INetPacket packet) {
        send(PacketDistributor.TRACKING_ENTITY, entity, packet);
    }

    public void sendTrackingEntity(Supplier<Entity> entity, INetPacket packet) {
        send(PacketDistributor.TRACKING_ENTITY, entity, packet);
    }

    public void sendTrackingEntityAndSelf(Entity entity, INetPacket packet) {
        send(PacketDistributor.TRACKING_ENTITY_AND_SELF, entity, packet);
    }

    public void sendTrackingEntityAndSelf(Supplier<Entity> entity, INetPacket packet) {
        send(PacketDistributor.TRACKING_ENTITY_AND_SELF, entity, packet);
    }

    public void sendTrackingChunk(Chunk chunk, INetPacket packet) {
        send(PacketDistributor.TRACKING_CHUNK, chunk, packet);
    }

    public void sendTrackingChunk(Supplier<Chunk> chunk, INetPacket packet) {
        send(PacketDistributor.TRACKING_CHUNK, chunk, packet);
    }

    public void sendTrackingTileEntity(TileEntity te, INetPacket packet) {
        sendTrackingChunk(() -> te.getWorld().getChunkAt(te.getPos()), packet);
    }

    public void sendTrackingTileEntity(Supplier<TileEntity> teSupp, INetPacket packet) {
        sendTrackingChunk(() -> {
            TileEntity te = teSupp.get();
            return te.getWorld().getChunkAt(te.getPos());
        }, packet);
    }

    public void sendNetManagers(List<NetworkManager> managers, INetPacket packet) {
        send(PacketDistributor.NMLIST, managers, packet);
    }

    public void sendNetManagers(Supplier<List<NetworkManager>> managers, INetPacket packet) {
        send(PacketDistributor.NMLIST, managers, packet);
    }

    public void sendDimension(RegistryKey<World> dimension, INetPacket packet) {
        send(PacketDistributor.DIMENSION, dimension, packet);
    }

    public void sendDimension(Supplier<RegistryKey<World>> dimension, INetPacket packet) {
        send(PacketDistributor.DIMENSION, dimension, packet);
    }

    public void sendWorld(World world, INetPacket packet) {
        sendDimension(world::getRegistryKey, packet);
    }

    public void sendWorld(Supplier<World> world, INetPacket packet) {
        sendDimension(() -> world.get().getRegistryKey(), packet);
    }

    public IPacket<?> asVanillaPacket(INetPacket packet, LogicalSide to) {
        return channel.toVanillaPacket(message(packet, to), direction(to));
    }

    private NetworkDirection direction(LogicalSide to) {
        return to == LogicalSide.SERVER ? NetworkDirection.PLAY_TO_SERVER : NetworkDirection.PLAY_TO_CLIENT;
    }

    private Message message(INetPacket packet, LogicalSide to) {
        return to == LogicalSide.SERVER ? new MessageToServer(packet) : new MessageToClient(packet);
    }

    private void encodeToServer(MessageToServer message, PacketBuffer buffer) {
        toServer.write(message.packet, buffer);
    }

    private void encodeToClient(MessageToClient message, PacketBuffer buffer) {
        toClient.write(message.packet, buffer);
    }

    private MessageToServer decodeToServer(PacketBuffer buffer) {
        return new MessageToServer(toServer.read(buffer));
    }

    private MessageToClient decodeToClient(PacketBuffer buffer) {
        return new MessageToClient(toClient.read(buffer));
    }

    private void handleToServer(MessageToServer message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.setPacketHandled(true);
        toServer.handle(message.packet, context, this);
    }

    private void handleToClient(MessageToClient message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.setPacketHandled(true);
        toClient.handle(message.packet, context, this);
    }

    static abstract class Message {
        final INetPacket packet;
        final LogicalSide to;

        Message(INetPacket packet, LogicalSide to) {
            this.packet = packet;
            this.to = to;
        }
    }

    static class MessageToServer extends Message {
        MessageToServer(INetPacket packet) {
            super(packet, LogicalSide.SERVER);
        }
    }

    static class MessageToClient extends Message {
        MessageToClient(INetPacket packet) {
            super(packet, LogicalSide.CLIENT);
        }
    }
}
