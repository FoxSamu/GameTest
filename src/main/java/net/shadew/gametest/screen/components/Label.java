package net.shadew.gametest.screen.components;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Label extends AbstractButton {

    public Label(int x, int y, int width, int height, ITextComponent text) {
        super(x, y, width, height, text);
    }

    @Override
    public void onPress() {
    }

    @Override
    public void renderButton(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        FontRenderer font = mc.fontRenderer;
        drawTextWithShadow(
            matrix, font, getMessage(),
            x,
            y + (height - 8) / 2,
            0xFFFFFF | MathHelper.ceil(alpha * 255) << 24
        );
    }

    @Override
    public boolean changeFocus(boolean forward) {
        return false;
    }

    @Override
    public void playDownSound(SoundHandler soundHandler) {
    }
}
