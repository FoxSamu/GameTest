package net.shadew.gametest.screen.components;

import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

public class OnOffButton extends Button {
    private boolean on;

    public OnOffButton(int x, int y, int w, int h, ITextComponent text) {
        super(x, y, w, h, text, btn -> {});
    }

    public OnOffButton(int x, int y, int w, int h, ITextComponent text, IPressable action) {
        super(x, y, w, h, text, action);
    }

    public OnOffButton(int x, int y, int w, int h, ITextComponent text, IPressable action, ITooltip tooltip) {
        super(x, y, w, h, text, action, tooltip);
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    @Override
    public void onPress() {
        super.onPress();
        on = !on;
    }

    @Override
    public ITextComponent getMessage() {
        return DialogTexts.getToggleText(on);
    }
}
