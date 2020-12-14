package net.shadew.gametest.event;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

@Cancelable
public class DebugKeyEvent extends Event {
    private final int key;

    public DebugKeyEvent(int key) {
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
