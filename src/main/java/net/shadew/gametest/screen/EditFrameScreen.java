package net.shadew.gametest.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;

import net.shadew.gametest.blockitem.entity.FrameEntity;
import net.shadew.gametest.blockitem.item.GameTestItems;
import net.shadew.gametest.net.GameTestNet;
import net.shadew.gametest.net.packet.SetFrameNamePacket;
import net.shadew.gametest.util.FallbackI18nTextComponent;

public class EditFrameScreen extends AlignableScreen {
    private static final ITextComponent NAME = new FallbackI18nTextComponent("gametest.frame_block.rotation", "Name");
    private final FrameEntity entity;

    private final TextFieldWidget tfName;

    public EditFrameScreen(FrameEntity entity) {
        super(GameTestItems.TEST_MARKER_FRAME.getName());
        this.entity = entity;

        this.tfName = new TextFieldWidget(Minecraft.getInstance().fontRenderer, 0, 0, 0, 0, ITextComponent.of(""));
        String name = entity.getFrameName();
        tfName.setText(name);
        if(name.isEmpty()) tfName.setSuggestion(NAME.getString());
        tfName.setResponder(s -> {
            if (s.isEmpty()) {
                tfName.setSuggestion(NAME.getString());
            } else {
                tfName.setSuggestion(null);
            }
        });
    }

    @Override
    public void init(Minecraft mc, int width, int height) {
        super.init(mc, width, height);

        setSize(200, 50);
        Grid g = create(0, 0, 200, 50, Grid::new).cols(1).rows(2, 1, 2);

        g.create(0, 0, (x, y, w, h) -> resize(tfName, x, y, w, h));
        addButton(tfName);

        g.create(0, 2, (x, y, w, h) -> addButton(new Button(x, y, w, h, DialogTexts.DONE, btn -> done())));
    }

    private void done() {
        GameTestNet.NET.sendServer(new SetFrameNamePacket(
            entity.getEntityId(),
            tfName.getText()
        ));
        onClose();
    }

    private static Void resize(TextFieldWidget widget, int x, int y, int w, int h) {
        widget.x = x;
        widget.y = y;
        widget.setWidth(w);
        widget.setHeight(h);
        widget.setText(widget.getText());
        return null;
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseZ, float partialTicks) {
        renderBackground(matrix);
        super.render(matrix, mouseX, mouseZ, partialTicks);
    }
}
