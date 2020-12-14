package net.shadew.gametest.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import net.shadew.gametest.framework.command.arguments.BlockStateArgumentType;
import net.shadew.gametest.framework.platforms.*;
import net.shadew.gametest.net.GameTestNet;
import net.shadew.gametest.net.packet.PlatformTemplateBlockPacket;
import net.shadew.gametest.screen.components.AutocompleteTextField;
import net.shadew.gametest.screen.components.Label;
import net.shadew.gametest.screen.components.OnOffButton;
import net.shadew.gametest.util.FallbackI18nTextComponent;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class CreatePlatformScreen extends AlignableScreen {
    private static final ITextComponent FLOOR_BLOCK = new FallbackI18nTextComponent("gametest.create_platform.floor_block", "Floor Block");
    private static final ITextComponent CEILING_BLOCK = new FallbackI18nTextComponent("gametest.create_platform.ceiling_block", "Ceiling Block");
    private static final ITextComponent WALL_BLOCK = new FallbackI18nTextComponent("gametest.create_platform.wall_block", "Wall Block");
    private static final ITextComponent FENCE_BLOCK = new FallbackI18nTextComponent("gametest.create_platform.fence_block", "Fence Block");
    private static final ITextComponent INNER_BLOCK = new FallbackI18nTextComponent("gametest.create_platform.inner_block", "Inner Block");
    private static final ITextComponent DEPTH = new FallbackI18nTextComponent("gametest.create_platform.depth", "Depth");
    private static final ITextComponent HEIGHT = new FallbackI18nTextComponent("gametest.create_platform.depth", "Height");
    private static final ITextComponent EXTRA_HEIGHT = new FallbackI18nTextComponent("gametest.create_platform.extra_height", "Extra Wall Height");
    private static final ITextComponent DOOR = new FallbackI18nTextComponent("gametest.create_platform.door", "Door");
    private static final ITextComponent BUILD = new FallbackI18nTextComponent("gametest.create_platform.build", "Build Platform");
    private static final ITextComponent TYPE = new FallbackI18nTextComponent("gametest.create_platform.type", "Type: ");
    private static final ITextComponent TITLE = new FallbackI18nTextComponent("gametest.create_platform.title", "Create New Platform");

    private final AutocompleteTextField.SuggestionsLayer suggestionsLayer = new AutocompleteTextField.SuggestionsLayer(this);

    private BlockPos pos;
    private Screen parent;

    private AutocompleteTextField<BlockState> tfState1 = new AutocompleteTextField<>(Minecraft.getInstance().fontRenderer, 0, 0, 0, 0, ITextComponent.of(""), suggestionsLayer, BlockStateArgumentType.blockState());
    private AutocompleteTextField<BlockState> tfState2 = new AutocompleteTextField<>(Minecraft.getInstance().fontRenderer, 0, 0, 0, 0, ITextComponent.of(""), suggestionsLayer, BlockStateArgumentType.blockState());
    private AutocompleteTextField<BlockState> tfState3 = new AutocompleteTextField<>(Minecraft.getInstance().fontRenderer, 0, 0, 0, 0, ITextComponent.of(""), suggestionsLayer, BlockStateArgumentType.blockState());
    private AutocompleteTextField<BlockState> tfState4 = new AutocompleteTextField<>(Minecraft.getInstance().fontRenderer, 0, 0, 0, 0, ITextComponent.of(""), suggestionsLayer, BlockStateArgumentType.blockState());
    private AutocompleteTextField<BlockState> tfState5 = new AutocompleteTextField<>(Minecraft.getInstance().fontRenderer, 0, 0, 0, 0, ITextComponent.of(""), suggestionsLayer, BlockStateArgumentType.blockState());
    private AutocompleteTextField<Integer> tfInt1 = new AutocompleteTextField<>(Minecraft.getInstance().fontRenderer, 0, 0, 0, 0, ITextComponent.of(""), suggestionsLayer, IntegerArgumentType.integer(0));
    private AutocompleteTextField<Integer> tfInt2 = new AutocompleteTextField<>(Minecraft.getInstance().fontRenderer, 0, 0, 0, 0, ITextComponent.of(""), suggestionsLayer, IntegerArgumentType.integer(0));
    private OnOffButton btnBoolean1 = new OnOffButton(0, 0, 0, 0, ITextComponent.of(""));

    private Button btnCancel;
    private Button btnCreate;
    private Button btnType;

    private Label lblFloorBlock = new Label(0, 0, 0, 0, FLOOR_BLOCK);
    private Label lblCeilingBlock = new Label(0, 0, 0, 0, CEILING_BLOCK);
    private Label lblWallBlock = new Label(0, 0, 0, 0, WALL_BLOCK);
    private Label lblFenceBlock = new Label(0, 0, 0, 0, FENCE_BLOCK);
    private Label lblInnerBlock = new Label(0, 0, 0, 0, INNER_BLOCK);
    private Label lblDepth = new Label(0, 0, 0, 0, DEPTH);
    private Label lblHeight = new Label(0, 0, 0, 0, HEIGHT);
    private Label lblExtraHeight = new Label(0, 0, 0, 0, EXTRA_HEIGHT);
    private Label lblDoor = new Label(0, 0, 0, 0, DOOR);

    private Grid topGrid;

    private final ILayout[] layouts = {
        new EmptyLayout(), new PlatformLayout(), new CeiledPlatformLayout(), new BoxPlatformLayout(),
        new PoolPlatformLayout(), new PoolCeilingPlatformLayout(), new PoolBoxPlatformLayout(),
        new FenceLayout(), new CeiledFenceLayout()
    };

    private final ITextComponent[] layoutNames = {
        new EmptyPlatformType().getName(),
        new PlatformPlatformType().getName(),
        new CeiledPlatformType().getName(),
        new BoxPlatformType().getName(),
        new PoolPlatformType().getName(),
        new CeiledPoolPlatformType().getName(),
        new BoxPoolPlatformType().getName(),
        new FencePlatformType().getName(),
        new CeiledFencePlatformType().getName()
    };

    private int currentLayout = 0;

    protected CreatePlatformScreen(BlockPos pos, Screen parent) {
        super(TITLE);
        this.pos = pos;
        this.parent = parent;
    }

    @Override
    public void init(Minecraft mc, int width, int height) {
        super.init(mc, width, height);

        Grid g = topGrid = new Grid(0, 42, 0, 0).wrapCols(150, 10, 150).wrapRows(20, 20, 20, 20, 20, 20, 20, 10, 20);
        setSize(g.box.width, g.box.height + 42);
        g.box.x = left();
        g.box.y = top() + 42;

        btnType = addButton(new Button(alignX(200, 0.5), alignY(20, 0) + 20, 200, 20, TYPE.shallowCopy().append(layoutNames[currentLayout]), btn -> {
            currentLayout += 1;
            currentLayout %= layoutNames.length;
            init(this.client, this.width, this.height);
        }));

        addButton(tfState1);
        addButton(tfState2);
        addButton(tfState3);
        addButton(tfState4);
        addButton(tfState5);
        addButton(tfInt1);
        addButton(tfInt2);
        addButton(btnBoolean1);
        addButton(lblFloorBlock);
        addButton(lblCeilingBlock);
        addButton(lblWallBlock);
        addButton(lblFenceBlock);
        addButton(lblInnerBlock);
        addButton(lblDepth);
        addButton(lblHeight);
        addButton(lblExtraHeight);
        addButton(lblDoor);

        btnCancel = g.create(0, 8, (x, y, w, h) -> addButton(new Button(x, y, w, h, DialogTexts.CANCEL, this::onCancel)));
        btnCreate = g.create(2, 8, (x, y, w, h) -> addButton(new Button(x, y, w, h, BUILD, this::onCreate)));

        tfState1.visible = false;
        tfState2.visible = false;
        tfState3.visible = false;
        tfState4.visible = false;
        tfState5.visible = false;
        tfInt1.visible = false;
        tfInt2.visible = false;
        btnBoolean1.visible = false;
        lblFloorBlock.visible = false;
        lblCeilingBlock.visible = false;
        lblWallBlock.visible = false;
        lblFenceBlock.visible = false;
        lblInnerBlock.visible = false;
        lblDepth.visible = false;
        lblHeight.visible = false;
        lblExtraHeight.visible = false;
        lblDoor.visible = false;

        layouts[currentLayout].setup();
    }

    private void field(int gridX, int gridY, int xspan, Widget label, Widget field) {
        label.visible = true;
        field.visible = true;
        topGrid.create(gridX, gridY, xspan, 1, (x, y, w, h) -> resize(x, y, w, h, label));
        topGrid.create(gridX, gridY + 1, xspan, 1, (x, y, w, h) -> resize(x, y, w, h, field));

        if (field instanceof TextFieldWidget) {
            ((TextFieldWidget) field).setText(((TextFieldWidget) field).getText());
        }
    }

    private Void resize(int x, int y, int w, int h, Widget widget) {
        widget.x = x;
        widget.y = y;
        widget.setWidth(w);
        widget.setHeight(h);
        return null;
    }

    private void onCancel(Button button) {
        client.displayGuiScreen(parent);
    }

    private void onCreate(Button button) {
        GameTestNet.NET.sendServer(new PlatformTemplateBlockPacket(
            pos,
            layouts[currentLayout].createPlatformNBT()
        ));
        onClose();
    }

    @Override
    protected void renderDebug(MatrixStack matrix) {
        super.renderDebug(matrix);
        renderDebug(matrix, topGrid);
        renderComponentsDebug(matrix);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return suggestionsLayer.mouseScrolled(mouseX, mouseY, delta) || super.mouseScrolled(mouseX, mouseX, delta);
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        return suggestionsLayer.keyPressed(key, scancode, mods) || super.keyPressed(key, scancode, mods);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return suggestionsLayer.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        suggestionsLayer.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseZ, float partialTicks) {
        renderBackground(matrix);
        super.render(matrix, mouseX, mouseZ, partialTicks);
        suggestionsLayer.render(matrix);
        drawCenteredText(matrix, client.fontRenderer, TITLE, x(0.5), top(), 0xFFFFFFFF);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private <T> T orElse(T t, T u) {
        return t == null ? u : t;
    }

    private interface ILayout {
        void setup();
        CompoundNBT createPlatformNBT();
    }

    private static class EmptyLayout implements ILayout {
        @Override
        public void setup() {
        }

        @Override
        public CompoundNBT createPlatformNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Type", 0);

            return nbt;
        }
    }

    private class PlatformLayout implements ILayout {
        @Override
        public void setup() {
            field(0, 1, 3, lblFloorBlock, tfState1);
            tfState1.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
        }

        @Override
        public CompoundNBT createPlatformNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Type", 1);
            nbt.put("Floor", NBTUtil.writeBlockState(orElse(tfState1.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));

            return nbt;
        }
    }

    private class CeiledPlatformLayout implements ILayout {
        @Override
        public void setup() {
            field(0, 1, 1, lblFloorBlock, tfState1);
            field(2, 1, 1, lblCeilingBlock, tfState2);
            tfState1.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState2.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
        }

        @Override
        public CompoundNBT createPlatformNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Type", 2);
            nbt.put("Floor", NBTUtil.writeBlockState(orElse(tfState1.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Ceil", NBTUtil.writeBlockState(orElse(tfState2.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));

            return nbt;
        }
    }

    private class BoxPlatformLayout implements ILayout {
        @Override
        public void setup() {
            field(0, 1, 1, lblFloorBlock, tfState1);
            field(2, 1, 1, lblCeilingBlock, tfState2);
            field(0, 3, 1, lblWallBlock, tfState3);
            field(2, 3, 1, lblDoor, btnBoolean1);
            tfState1.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState2.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState3.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            btnBoolean1.setOn(true);
        }

        @Override
        public CompoundNBT createPlatformNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Type", 3);
            nbt.put("Floor", NBTUtil.writeBlockState(orElse(tfState1.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Ceil", NBTUtil.writeBlockState(orElse(tfState2.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Wall", NBTUtil.writeBlockState(orElse(tfState3.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.putBoolean("Door", btnBoolean1.isOn());

            return nbt;
        }
    }

    private class PoolPlatformLayout implements ILayout {
        @Override
        public void setup() {
            field(0, 1, 1, lblFloorBlock, tfState1);
            field(2, 1, 1, lblWallBlock, tfState2);
            field(0, 3, 1, lblInnerBlock, tfState3);
            field(0, 5, 1, lblDepth, tfInt1);
            field(2, 5, 1, lblExtraHeight, tfInt2);
            tfState1.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState2.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState3.setValue(Blocks.WATER.getDefaultState(), "water");
            tfInt1.setValue(1, "1");
            tfInt2.setValue(0, "0");
        }

        @Override
        public CompoundNBT createPlatformNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Type", 4);
            nbt.put("Floor", NBTUtil.writeBlockState(orElse(tfState1.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Wall", NBTUtil.writeBlockState(orElse(tfState2.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Inner", NBTUtil.writeBlockState(orElse(tfState3.getValue(), Blocks.WATER.getDefaultState())));
            nbt.putInt("Depth", orElse(tfInt1.getValue(), 1));
            nbt.putInt("Extra", orElse(tfInt2.getValue(), 0));

            return nbt;
        }
    }

    private class PoolCeilingPlatformLayout implements ILayout {
        @Override
        public void setup() {
            field(0, 1, 1, lblFloorBlock, tfState1);
            field(2, 1, 1, lblWallBlock, tfState2);
            field(0, 3, 1, lblCeilingBlock, tfState3);
            field(2, 3, 1, lblInnerBlock, tfState4);
            field(0, 5, 1, lblDepth, tfInt1);
            field(2, 5, 1, lblExtraHeight, tfInt2);
            tfState1.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState2.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState3.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState4.setValue(Blocks.WATER.getDefaultState(), "water");
            tfInt1.setValue(1, "1");
            tfInt2.setValue(0, "0");
        }

        @Override
        public CompoundNBT createPlatformNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Type", 5);
            nbt.put("Floor", NBTUtil.writeBlockState(orElse(tfState1.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Wall", NBTUtil.writeBlockState(orElse(tfState2.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Ceil", NBTUtil.writeBlockState(orElse(tfState3.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Inner", NBTUtil.writeBlockState(orElse(tfState4.getValue(), Blocks.WATER.getDefaultState())));
            nbt.putInt("Depth", orElse(tfInt1.getValue(), 1));
            nbt.putInt("Extra", orElse(tfInt2.getValue(), 0));

            return nbt;
        }
    }

    private class PoolBoxPlatformLayout implements ILayout {
        @Override
        public void setup() {
            field(0, 1, 1, lblFloorBlock, tfState1);
            field(2, 1, 1, lblWallBlock, tfState2);
            field(0, 3, 1, lblCeilingBlock, tfState3);
            field(2, 3, 1, lblInnerBlock, tfState4);
            field(0, 5, 1, lblDepth, tfInt1);
            field(2, 5, 1, lblDoor, btnBoolean1);
            tfState1.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState2.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState3.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState4.setValue(Blocks.WATER.getDefaultState(), "water");
            tfInt1.setValue(1, "1");
            btnBoolean1.setOn(true);
        }

        @Override
        public CompoundNBT createPlatformNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Type", 6);
            nbt.put("Floor", NBTUtil.writeBlockState(orElse(tfState1.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Wall", NBTUtil.writeBlockState(orElse(tfState2.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Ceil", NBTUtil.writeBlockState(orElse(tfState3.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Inner", NBTUtil.writeBlockState(orElse(tfState4.getValue(), Blocks.WATER.getDefaultState())));
            nbt.putInt("Depth", orElse(tfInt1.getValue(), 1));
            nbt.putBoolean("Door", btnBoolean1.isOn());

            return nbt;
        }
    }

    private class FenceLayout implements ILayout {
        @Override
        public void setup() {
            field(0, 1, 1, lblFloorBlock, tfState1);
            field(2, 1, 1, lblFenceBlock, tfState2);
            field(0, 3, 3, lblHeight, tfInt1);
            tfState1.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState2.setValue(Blocks.DARK_OAK_FENCE.getDefaultState(), "dark_oak_fence");
            tfInt1.setValue(1, "1");
        }

        @Override
        public CompoundNBT createPlatformNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Type", 7);
            nbt.put("Floor", NBTUtil.writeBlockState(orElse(tfState1.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Fence", NBTUtil.writeBlockState(orElse(tfState2.getValue(), Blocks.DARK_OAK_FENCE.getDefaultState())));
            nbt.putInt("Height", orElse(tfInt1.getValue(), 1));

            return nbt;
        }
    }

    private class CeiledFenceLayout implements ILayout {
        @Override
        public void setup() {
            field(0, 1, 1, lblFloorBlock, tfState1);
            field(2, 1, 1, lblCeilingBlock, tfState2);
            field(0, 3, 1, lblFenceBlock, tfState3);
            field(2, 3, 1, lblHeight, tfInt1);
            tfState1.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState2.setValue(Blocks.POLISHED_ANDESITE.getDefaultState(), "polished_andesite");
            tfState3.setValue(Blocks.DARK_OAK_FENCE.getDefaultState(), "dark_oak_fence");
            tfInt1.setValue(1, "1");
        }

        @Override
        public CompoundNBT createPlatformNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putInt("Type", 8);
            nbt.put("Floor", NBTUtil.writeBlockState(orElse(tfState1.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Ceil", NBTUtil.writeBlockState(orElse(tfState2.getValue(), Blocks.POLISHED_ANDESITE.getDefaultState())));
            nbt.put("Fence", NBTUtil.writeBlockState(orElse(tfState3.getValue(), Blocks.DARK_OAK_FENCE.getDefaultState())));
            nbt.putInt("Height", orElse(tfInt1.getValue(), 1));

            return nbt;
        }
    }
}
