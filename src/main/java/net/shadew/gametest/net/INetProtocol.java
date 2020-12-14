package net.shadew.gametest.net;

public interface INetProtocol {
    String getVersion();
    void registerToServer(NetPacketCodec codec);
    void registerToClient(NetPacketCodec codec);

    default boolean isCompatible(String version) {
        return version.equals(getVersion());
    }

    default boolean isClientCompatible(String version) {
        return isCompatible(version);
    }

    default boolean isServerCompatible(String version) {
        return isCompatible(version);
    }
}
