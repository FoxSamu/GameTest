package net.shadew.gametest.net;

import net.shadew.gametest.net.protocol.V1Protocol;

public final class GameTestNet {
    public static final NetChannel NET = new NetChannel("gametest:net", new V1Protocol());
}
