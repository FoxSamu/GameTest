package net.shadew.gametest.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import net.shadew.gametest.GameTestMod;
import net.shadew.gametest.blockitem.block.GameTestBlocks;
import net.shadew.gametest.blockitem.block.TemplateBlock;
import net.shadew.gametest.blockitem.block.props.DiagonalDirection;
import net.shadew.gametest.blockitem.tileentity.TemplateBlockTileEntity;
import net.shadew.gametest.framework.GameTestFunction;
import net.shadew.gametest.framework.GameTestRegistry;
import net.shadew.gametest.framework.command.arguments.FunctionOrTemplateArgumentType;
import net.shadew.gametest.net.GameTestNet;
import net.shadew.gametest.net.packet.LoadTemplateBlockPacket;
import net.shadew.gametest.net.packet.RotateTemplateBlockPacket;
import net.shadew.gametest.net.packet.SaveTemplateBlockPacket;
import net.shadew.gametest.net.packet.UpdateTemplateBlockPacket;
import net.shadew.gametest.screen.components.AutocompleteTextField;
import net.shadew.gametest.screen.components.Label;
import net.shadew.gametest.util.FallbackI18nTextComponent;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class EditTemplateScreen extends AlignableScreen {
    private static final ITextComponent ROTATION = new FallbackI18nTextComponent("gametest.template_block.rotation", "Rotation");
    private static final ITextComponent TEST_NAME = new FallbackI18nTextComponent("gametest.template_block.test_name", "Test Name");
    private static final ITextComponent SIZE = new FallbackI18nTextComponent("gametest.template_block.size", "Size");
    private static final ITextComponent CREATE = new FallbackI18nTextComponent("gametest.template_block.create", "Create Template");
    private static final ITextComponent LOAD = new FallbackI18nTextComponent("gametest.template_block.load", "Load Template");
    private static final ITextComponent SAVE = new FallbackI18nTextComponent("gametest.template_block.save", "Save Template");
    private static final ITextComponent WIDTH = new FallbackI18nTextComponent("gametest.template_block.width", "Width");
    private static final ITextComponent HEIGHT = new FallbackI18nTextComponent("gametest.template_block.height", "Height");
    private static final ITextComponent DEPTH = new FallbackI18nTextComponent("gametest.template_block.depth", "Depth");
    private static final ITextComponent ROTATE_SE = new FallbackI18nTextComponent("gametest.template_block.rotate_se", "SE");
    private static final ITextComponent ROTATE_SW = new FallbackI18nTextComponent("gametest.template_block.rotate_sw", "SW");
    private static final ITextComponent ROTATE_NW = new FallbackI18nTextComponent("gametest.template_block.rotate_nw", "NW");
    private static final ITextComponent ROTATE_NE = new FallbackI18nTextComponent("gametest.template_block.rotate_ne", "NE");

    private final AutocompleteTextField.SuggestionsLayer suggestionsLayer = new AutocompleteTextField.SuggestionsLayer(this);

    private BlockPos pos;

    private DiagonalDirection selectedRotation;

    private Button btnDone;
    private Label lblRotations;
    private Label lblPlatform;
    private Label lblName;
    private Label lblSize;
    private Button btnCreate;
    private Button btnLoad;
    private Button btnSave;
    private Button btnRotationSe;
    private Button btnRotationSw;
    private Button btnRotationNw;
    private Button btnRotationNe;

    private AutocompleteTextField<Either<ResourceLocation, GameTestFunction>> tfName;
    private AutocompleteTextField<Integer> tfWidth;
    private AutocompleteTextField<Integer> tfHeight;
    private AutocompleteTextField<Integer> tfDepth;

    private Grid rotationsGrid;
    private Grid sizeGrid;
    private Grid topGrid;

    public EditTemplateScreen() {
        super(new TranslationTextComponent(GameTestBlocks.TEST_TEMPLATE_BLOCK.getTranslationKey()));
        setSize(360, 150);

        FontRenderer fontRenderer = Minecraft.getInstance().fontRenderer;

        tfName = new AutocompleteTextField<>(fontRenderer, 0, 0, 0, 0, TEST_NAME, suggestionsLayer, FunctionOrTemplateArgumentType.functionOrTemplate());
        tfName.setMaxStringLength(200);

        tfWidth = new AutocompleteTextField<>(fontRenderer, 0, 0, 0, 0, WIDTH, suggestionsLayer, IntegerArgumentType.integer(0, 48));
        tfHeight = new AutocompleteTextField<>(fontRenderer, 0, 0, 0, 0, HEIGHT, suggestionsLayer, IntegerArgumentType.integer(0, 48));
        tfDepth = new AutocompleteTextField<>(fontRenderer, 0, 0, 0, 0, DEPTH, suggestionsLayer, IntegerArgumentType.integer(0, 48));
    }

    public EditTemplateScreen loadTileEntity(BlockPos pos) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.world == null) {
            GameTestMod.LOGGER.error("Tried to open template block screen while no game running");
        } else {
            TileEntity e = mc.world.getTileEntity(pos);
            if(e instanceof TemplateBlockTileEntity) {
                TemplateBlockTileEntity templateBlock = (TemplateBlockTileEntity) e;
                if(templateBlock.isRawTemplate()) {
                    tfName.setValue(Either.left(templateBlock.getName()), templateBlock.getName().toString());
                } else {
                    GameTestFunction fn = GameTestRegistry.getFunction(templateBlock.getName());
                    if(fn != null)
                        tfName.setValue(Either.right(fn), "#" + templateBlock.getName().toString());
                }
                tfWidth.setValue(templateBlock.getWidth(), templateBlock.getWidth() + "");
                tfHeight.setValue(templateBlock.getHeight(), templateBlock.getHeight() + "");
                tfDepth.setValue(templateBlock.getDepth(), templateBlock.getDepth() + "");

                selectedRotation = mc.world.getBlockState(pos).get(TemplateBlock.DIRECTION);
            }
        }
        this.pos = pos;
        return this;
    }

    @Override
    public void init(Minecraft mc, int width, int height) {
        super.init(mc, width, height);

        addChild(suggestionsLayer);

        Grid g = topGrid = new Grid(left(), top(), screenWidth, 0).cols(1, 1, 1).wrapRows(20, 2, 20, 2, 20, 2, 20);

        lblName = g.create(0, 0, (x, y, w, h) -> addButton(new Label(x, y, w, h, TEST_NAME)));
        g.create(1, 0, 2, 1, (x, y, w, h) -> resize(tfName, x, y, w, h));
        addButton(tfName);

        lblSize = g.create(0, 2, (x, y, w, h) -> addButton(new Label(x, y, w, h, SIZE)));
        Grid sg = sizeGrid = g.create(1, 2, 2, 1, Grid::new).init(3, 1);
        sg.create(0, 0, (x, y, w, h) -> resize(tfWidth, x, y, w, h));
        sg.create(1, 0, (x, y, w, h) -> resize(tfHeight, x, y, w, h));
        sg.create(2, 0, (x, y, w, h) -> resize(tfDepth, x, y, w, h));
        addButton(tfWidth);
        addButton(tfHeight);
        addButton(tfDepth);

        lblRotations = g.create(0, 4, (x, y, w, h) -> addButton(new Label(x, y, w, h, ROTATION)));
        Grid rg = rotationsGrid = g.create(1, 4, 2, 1, Grid::new).init(4, 1);
        btnRotationSe = rg.create(0, 0, (x, y, w, h) -> addButton(new Button(x, y, w, h, ROTATE_SE, btn -> onSetDir(DiagonalDirection.SE))));
        btnRotationSw = rg.create(1, 0, (x, y, w, h) -> addButton(new Button(x, y, w, h, ROTATE_SW, btn -> onSetDir(DiagonalDirection.SW))));
        btnRotationNw = rg.create(2, 0, (x, y, w, h) -> addButton(new Button(x, y, w, h, ROTATE_NW, btn -> onSetDir(DiagonalDirection.NW))));
        btnRotationNe = rg.create(3, 0, (x, y, w, h) -> addButton(new Button(x, y, w, h, ROTATE_NE, btn -> onSetDir(DiagonalDirection.NE))));

        btnLoad = g.create(0, 6, (x, y, w, h) -> addButton(new Button(x, y, w, h, LOAD, this::onLoadTemplate)));
        btnSave = g.create(1, 6, (x, y, w, h) -> addButton(new Button(x, y, w, h, SAVE, this::onSaveTemplate)));
        btnCreate = g.create(2, 6, (x, y, w, h) -> addButton(new Button(x, y, w, h, CREATE, this::onCreateTemplate)));

        btnDone = addButton(new Button(alignX(200, 0.5), alignY(20, 1), 200, 20, DialogTexts.DONE, this::onDone));

        updateRotations(selectedRotation);
    }

    private static Void resize(TextFieldWidget widget, int x, int y, int w, int h) {
        widget.x = x;
        widget.y = y;
        widget.setWidth(w);
        widget.setHeight(h);
        widget.setText(widget.getText());
        return null;
    }

    private void updateRotations(DiagonalDirection dir) {
        selectedRotation = dir;
        btnRotationNe.active = dir != DiagonalDirection.NE;
        btnRotationSe.active = dir != DiagonalDirection.SE;
        btnRotationSw.active = dir != DiagonalDirection.SW;
        btnRotationNw.active = dir != DiagonalDirection.NW;
    }

    private static <T> T orElse(T t, T u) {
        return t == null ? u : t;
    }

    private void onDone(Button button) {
        GameTestNet.NET.sendServer(new UpdateTemplateBlockPacket(
            pos,
            orElse(tfName.getValue(), Either.left(new ResourceLocation("untitled"))),
            orElse(tfWidth.getValue(), 0),
            orElse(tfHeight.getValue(), 0),
            orElse(tfDepth.getValue(), 0)
        ));
        onClose();
    }

    private void onSetDir(DiagonalDirection dir) {
        GameTestNet.NET.sendServer(new RotateTemplateBlockPacket(pos, dir));
        updateRotations(dir);
    }

    private void onSaveTemplate(Button button) {
        GameTestNet.NET.sendServer(new SaveTemplateBlockPacket(
            pos,
            orElse(tfName.getValue(), Either.left(new ResourceLocation("untitled"))),
            orElse(tfWidth.getValue(), 0),
            orElse(tfHeight.getValue(), 0),
            orElse(tfDepth.getValue(), 0)
        ));
        onClose();
    }

    private void onLoadTemplate(Button button) {
        GameTestNet.NET.sendServer(new LoadTemplateBlockPacket(
            pos,
            orElse(tfName.getValue(), Either.left(new ResourceLocation("untitled"))),
            orElse(tfWidth.getValue(), 0),
            orElse(tfHeight.getValue(), 0),
            orElse(tfDepth.getValue(), 0)
        ));
        onClose();
    }

    private void onCreateTemplate(Button button) {
        GameTestNet.NET.sendServer(new UpdateTemplateBlockPacket(
            pos,
            orElse(tfName.getValue(), Either.left(new ResourceLocation("untitled"))),
            orElse(tfWidth.getValue(), 0),
            orElse(tfHeight.getValue(), 0),
            orElse(tfDepth.getValue(), 0)
        ));
        client.displayGuiScreen(new CreatePlatformScreen(pos, this));
    }

    @Override
    protected void renderDebug(MatrixStack matrix) {
        super.renderDebug(matrix);
        renderDebug(matrix, rotationsGrid);
        renderDebug(matrix, sizeGrid);
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
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
