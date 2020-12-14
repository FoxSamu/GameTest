package net.shadew.gametest.net;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.function.Supplier;

public class NetPacketCodec {
    private final HashMap<Integer, Supplier<? extends INetPacket>> idToConstructor = new HashMap<>();
    private final HashMap<Integer, Class<? extends INetPacket>> idToClass = new HashMap<>();
    private final HashMap<Class<? extends INetPacket>, Integer> classToId = new HashMap<>();
    private int nextSafeId;

    public <P extends INetPacket> void register(int id, Class<? extends P> cls, Supplier<? extends P> ctor) {
        if(idToConstructor.containsKey(id)) {
            throw new IllegalArgumentException("ID " + id + " already in use");
        }

        idToConstructor.put(id, ctor);
        idToClass.put(id, cls);
        classToId.put(cls, id);
    }

    public <P extends INetPacket> void register(Class<? extends P> cls, Supplier<? extends P> ctor) {
        while(idToConstructor.containsKey(nextSafeId)) {
            nextSafeId ++;
        }
        register(nextSafeId, cls, ctor);
    }

    private static Supplier<? extends INetPacket> ctorFromCls(Class<? extends INetPacket> cls) {
        if(Modifier.isAbstract(cls.getModifiers())) {
            throw new IllegalArgumentException("Can't get constructor of abstract class");
        }
        if(Modifier.isInterface(cls.getModifiers())) {
            throw new IllegalArgumentException("Can't get constructor of interface");
        }
        if(cls.isEnum()) {
            INetPacket[] consts = cls.getEnumConstants();
            if(consts.length == 0) {
                throw new IllegalArgumentException("Can't get base enum constant to return in constructor");
            }
            return () -> consts[0];
        }
        try {
            Constructor<? extends INetPacket> pkt = cls.getConstructor();
            return () -> {
                try {
                    return pkt.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to instantiate packet", e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Packet class has no public nullary constructor");
        }
    }

    public void register(int id, Class<? extends INetPacket> cls) {
        register(id, cls, ctorFromCls(cls));
    }

    public void register(Class<? extends INetPacket> cls) {
        register(cls, ctorFromCls(cls));
    }

    public INetPacket read(PacketBuffer buf) {
        int i = buf.readInt();

        Class<? extends INetPacket> cls = idToClass.get(i);
        if(cls == null) {
            throw new RuntimeException("Got invalid packet index: " + i);
        }
        Supplier<? extends INetPacket> ctor = idToConstructor.get(i);
        if(ctor == null) {
            throw new RuntimeException("Got invalid packet index: " + i);
        }

        INetPacket pkt = ctor.get();
        if(pkt == null) {
            throw new RuntimeException("Got null packet from constructor");
        }

        INetPacket read = pkt.read(buf);
        if(read != null) {
            pkt = read;
        }

        return pkt;
    }

    public void write(INetPacket pkt, PacketBuffer buf) {
        Class<? extends INetPacket> cls = pkt.getClass();

        if(!classToId.containsKey(cls)) {
            throw new RuntimeException("Class " + cls.getName() + " is not a registered packet");
        }

        int id = classToId.get(cls);

        buf.writeInt(id);
        pkt.write(buf);
    }

    public void handle(INetPacket pkt, NetworkEvent.Context ctx, NetChannel channel) {
        pkt.handle(NetContext.get(ctx, pkt, channel));
    }
}
