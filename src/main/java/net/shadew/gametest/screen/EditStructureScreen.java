package net.shadew.gametest.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.network.play.client.CUpdateStructureBlockPacket;
import net.minecraft.state.properties.StructureMode;
import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class EditStructureScreen extends Screen {
    private static final ITextComponent STRUCTURE_NAME = new TranslationTextComponent("structure_block.structure_name");
    private static final ITextComponent POSITION = new TranslationTextComponent("structure_block.position");
    private static final ITextComponent SIZE = new TranslationTextComponent("structure_block.size");
    private static final ITextComponent INTEGRITY = new TranslationTextComponent("structure_block.integrity");
    private static final ITextComponent CUSTOM_DATA = new TranslationTextComponent("structure_block.custom_data");
    private static final ITextComponent INCLUDE_ENTITIES = new TranslationTextComponent("structure_block.include_entities");
    private static final ITextComponent DETECT_SIZE = new TranslationTextComponent("structure_block.detect_size");
    private static final ITextComponent SHOW_AIR = new TranslationTextComponent("structure_block.show_air");
    private static final ITextComponent SHOW_BOUNDINGBOX = new TranslationTextComponent("structure_block.show_boundingbox");
    private final StructureBlockTileEntity tileStructure;
    private Mirror mirror = Mirror.NONE;
    private Rotation rotation = Rotation.NONE;
    private StructureMode mode = StructureMode.DATA;
    private boolean ignoreEntities;
    private boolean showAir;
    private boolean showBoundingBox;
    private TextFieldWidget nameEdit;
    private TextFieldWidget posXEdit;
    private TextFieldWidget posYEdit;
    private TextFieldWidget posZEdit;
    private TextFieldWidget sizeXEdit;
    private TextFieldWidget sizeYEdit;
    private TextFieldWidget sizeZEdit;
    private TextFieldWidget integrityEdit;
    private TextFieldWidget seedEdit;
    private TextFieldWidget dataEdit;
    private Button doneButton;
    private Button cancelButton;
    private Button saveButton;
    private Button loadButton;
    private Button rotateZeroDegreesButton;
    private Button rotateNinetyDegreesButton;
    private Button rotate180DegreesButton;
    private Button rotate270DegressButton;
    private Button modeButton;
    private Button detectSizeButton;
    private Button showEntitiesButton;
    private Button mirrorButton;
    private Button showAirButton;
    private Button showBoundingBoxButton;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");

    public EditStructureScreen(StructureBlockTileEntity p_i47142_1_) {
        super(new TranslationTextComponent(Blocks.STRUCTURE_BLOCK.getTranslationKey()));
        this.tileStructure = p_i47142_1_;
        this.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
        this.posXEdit.tick();
        this.posYEdit.tick();
        this.posZEdit.tick();
        this.sizeXEdit.tick();
        this.sizeYEdit.tick();
        this.sizeZEdit.tick();
        this.integrityEdit.tick();
        this.seedEdit.tick();
        this.dataEdit.tick();
    }

    private void updateAndClose() {
        if (sendUpdate(StructureBlockTileEntity.UpdateCommand.UPDATE_DATA)) {
            this.client.displayGuiScreen(null);
        }
    }

    private void updateStructureBlock() {
        tileStructure.setMirror(mirror);
        tileStructure.setRotation(rotation);
        tileStructure.setMode(mode);
        tileStructure.setIgnoresEntities(ignoreEntities);
        tileStructure.setShowAir(showAir);
        tileStructure.setShowBoundingBox(showBoundingBox);
        client.displayGuiScreen(null);
    }

    @Override
    protected void init() {
        this.client.keyboardListener.enableRepeatEvents(true);
        this.doneButton = this.addButton(new Button(this.width / 2 - 4 - 150, 210, 150, 20, DialogTexts.DONE, p_214274_1_ -> {
            this.updateAndClose();
        }));
        this.cancelButton = this.addButton(new Button(this.width / 2 + 4, 210, 150, 20, DialogTexts.CANCEL, p_214275_1_ -> {
            this.updateStructureBlock();
        }));
        this.saveButton = this.addButton(new Button(this.width / 2 + 4 + 100, 185, 50, 20, new TranslationTextComponent("structure_block.button.save"), p_214276_1_ -> {
            if (this.tileStructure.getMode() == StructureMode.SAVE) {
                this.sendUpdate(StructureBlockTileEntity.UpdateCommand.SAVE_AREA);
                this.client.displayGuiScreen(null);
            }

        }));
        this.loadButton = this.addButton(new Button(this.width / 2 + 4 + 100, 185, 50, 20, new TranslationTextComponent("structure_block.button.load"), p_214277_1_ -> {
            if (this.tileStructure.getMode() == StructureMode.LOAD) {
                this.sendUpdate(StructureBlockTileEntity.UpdateCommand.LOAD_AREA);
                this.client.displayGuiScreen(null);
            }

        }));
        this.modeButton = this.addButton(new Button(this.width / 2 - 4 - 150, 185, 50, 20, new StringTextComponent("MODE"), p_214280_1_ -> {
            this.tileStructure.nextMode();
            this.updateMode();
        }));
        this.detectSizeButton = this.addButton(new Button(this.width / 2 + 4 + 100, 120, 50, 20, new TranslationTextComponent("structure_block.button.detect_size"), p_214278_1_ -> {
            if (this.tileStructure.getMode() == StructureMode.SAVE) {
                this.sendUpdate(StructureBlockTileEntity.UpdateCommand.SCAN_AREA);
                this.client.displayGuiScreen(null);
            }

        }));
        this.showEntitiesButton = this.addButton(new Button(this.width / 2 + 4 + 100, 160, 50, 20, new StringTextComponent("ENTITIES"), p_214282_1_ -> {
            this.tileStructure.setIgnoresEntities(!this.tileStructure.ignoresEntities());
            this.updateEntitiesButton();
        }));
        this.mirrorButton = this.addButton(new Button(this.width / 2 - 20, 185, 40, 20, new StringTextComponent("MIRROR"), p_214281_1_ -> {
            switch (this.tileStructure.getMirror()) {
                case NONE:
                    this.tileStructure.setMirror(Mirror.LEFT_RIGHT);
                    break;
                case LEFT_RIGHT:
                    this.tileStructure.setMirror(Mirror.FRONT_BACK);
                    break;
                case FRONT_BACK:
                    this.tileStructure.setMirror(Mirror.NONE);
            }

            this.updateMirrorButton();
        }));
        this.showAirButton = this.addButton(new Button(this.width / 2 + 4 + 100, 80, 50, 20, new StringTextComponent("SHOWAIR"), p_214269_1_ -> {
            this.tileStructure.setShowAir(!this.tileStructure.showsAir());
            this.updateToggleAirButton();
        }));
        this.showBoundingBoxButton = this.addButton(new Button(this.width / 2 + 4 + 100, 80, 50, 20, new StringTextComponent("SHOWBB"), p_214270_1_ -> {
            this.tileStructure.setShowBoundingBox(!this.tileStructure.showsBoundingBox());
            this.updateToggleBoundingBox();
        }));
        this.rotateZeroDegreesButton = this.addButton(new Button(this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20, new StringTextComponent("0"), p_214268_1_ -> {
            this.tileStructure.setRotation(Rotation.NONE);
            this.updateDirectionButtons();
        }));
        this.rotateNinetyDegreesButton = this.addButton(new Button(this.width / 2 - 1 - 40 - 20, 185, 40, 20, new StringTextComponent("90"), p_214273_1_ -> {
            this.tileStructure.setRotation(Rotation.CLOCKWISE_90);
            this.updateDirectionButtons();
        }));
        this.rotate180DegreesButton = this.addButton(new Button(this.width / 2 + 1 + 20, 185, 40, 20, new StringTextComponent("180"), p_214272_1_ -> {
            this.tileStructure.setRotation(Rotation.CLOCKWISE_180);
            this.updateDirectionButtons();
        }));
        this.rotate270DegressButton = this.addButton(new Button(this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20, new StringTextComponent("270"), p_214271_1_ -> {
            this.tileStructure.setRotation(Rotation.COUNTERCLOCKWISE_90);
            this.updateDirectionButtons();
        }));
        this.nameEdit = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 40, 300, 20, new TranslationTextComponent("structure_block.structure_name")) {

            @Override
            public boolean charTyped(char p_231042_1_, int p_231042_2_) {
                return EditStructureScreen.this.isValidCharacterForName(this.getText(), p_231042_1_, this.getCursorPosition()) && super.charTyped(p_231042_1_, p_231042_2_);
            }
        };
        this.nameEdit.setMaxStringLength(64);
        this.nameEdit.setText(this.tileStructure.getName());
        this.children.add(this.nameEdit);
        BlockPos blockpos = this.tileStructure.getPosition();
        this.posXEdit = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 80, 80, 20, new TranslationTextComponent("structure_block.position.x"));
        this.posXEdit.setMaxStringLength(15);
        this.posXEdit.setText(Integer.toString(blockpos.getX()));
        this.children.add(this.posXEdit);
        this.posYEdit = new TextFieldWidget(this.textRenderer, this.width / 2 - 72, 80, 80, 20, new TranslationTextComponent("structure_block.position.y"));
        this.posYEdit.setMaxStringLength(15);
        this.posYEdit.setText(Integer.toString(blockpos.getY()));
        this.children.add(this.posYEdit);
        this.posZEdit = new TextFieldWidget(this.textRenderer, this.width / 2 + 8, 80, 80, 20, new TranslationTextComponent("structure_block.position.z"));
        this.posZEdit.setMaxStringLength(15);
        this.posZEdit.setText(Integer.toString(blockpos.getZ()));
        this.children.add(this.posZEdit);
        BlockPos blockpos1 = this.tileStructure.getStructureSize();
        this.sizeXEdit = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 120, 80, 20, new TranslationTextComponent("structure_block.size.x"));
        this.sizeXEdit.setMaxStringLength(15);
        this.sizeXEdit.setText(Integer.toString(blockpos1.getX()));
        this.children.add(this.sizeXEdit);
        this.sizeYEdit = new TextFieldWidget(this.textRenderer, this.width / 2 - 72, 120, 80, 20, new TranslationTextComponent("structure_block.size.y"));
        this.sizeYEdit.setMaxStringLength(15);
        this.sizeYEdit.setText(Integer.toString(blockpos1.getY()));
        this.children.add(this.sizeYEdit);
        this.sizeZEdit = new TextFieldWidget(this.textRenderer, this.width / 2 + 8, 120, 80, 20, new TranslationTextComponent("structure_block.size.z"));
        this.sizeZEdit.setMaxStringLength(15);
        this.sizeZEdit.setText(Integer.toString(blockpos1.getZ()));
        this.children.add(this.sizeZEdit);
        this.integrityEdit = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 120, 80, 20, new TranslationTextComponent("structure_block.integrity.integrity"));
        this.integrityEdit.setMaxStringLength(15);
        this.integrityEdit.setText(this.decimalFormat.format(this.tileStructure.getIntegrity()));
        this.children.add(this.integrityEdit);
        this.seedEdit = new TextFieldWidget(this.textRenderer, this.width / 2 - 72, 120, 80, 20, new TranslationTextComponent("structure_block.integrity.seed"));
        this.seedEdit.setMaxStringLength(31);
        this.seedEdit.setText(Long.toString(this.tileStructure.getSeed()));
        this.children.add(this.seedEdit);
        this.dataEdit = new TextFieldWidget(this.textRenderer, this.width / 2 - 152, 120, 240, 20, new TranslationTextComponent("structure_block.custom_data"));
        this.dataEdit.setMaxStringLength(128);
        this.dataEdit.setText(this.tileStructure.getMetadata());
        this.children.add(this.dataEdit);
        this.mirror = this.tileStructure.getMirror();
        this.updateMirrorButton();
        this.rotation = this.tileStructure.getRotation();
        this.updateDirectionButtons();
        this.mode = this.tileStructure.getMode();
        this.updateMode();
        this.ignoreEntities = this.tileStructure.ignoresEntities();
        this.updateEntitiesButton();
        this.showAir = this.tileStructure.showsAir();
        this.updateToggleAirButton();
        this.showBoundingBox = this.tileStructure.showsBoundingBox();
        this.updateToggleBoundingBox();
        this.setFocusedDefault(this.nameEdit);
    }

    @Override
    public void resize(Minecraft mc, int width, int height) {
        String name = nameEdit.getText();
        String posX = posXEdit.getText();
        String posY = posYEdit.getText();
        String posZ = posZEdit.getText();
        String sizeX = sizeXEdit.getText();
        String sizeY = sizeYEdit.getText();
        String sizeZ = sizeZEdit.getText();
        String integrity = integrityEdit.getText();
        String seed = seedEdit.getText();
        String data = dataEdit.getText();
        init(mc, width, height);
        nameEdit.setText(name);
        posXEdit.setText(posX);
        posYEdit.setText(posY);
        posZEdit.setText(posZ);
        sizeXEdit.setText(sizeX);
        sizeYEdit.setText(sizeY);
        sizeZEdit.setText(sizeZ);
        integrityEdit.setText(integrity);
        seedEdit.setText(seed);
        dataEdit.setText(data);
    }

    @Override
    public void removed() {
        client.keyboardListener.enableRepeatEvents(false);
    }

    private void updateEntitiesButton() {
        showEntitiesButton.setMessage(DialogTexts.getToggleText(!tileStructure.ignoresEntities()));
    }

    private void updateToggleAirButton() {
        showAirButton.setMessage(DialogTexts.getToggleText(tileStructure.showsAir()));
    }

    private void updateToggleBoundingBox() {
        showBoundingBoxButton.setMessage(DialogTexts.getToggleText(tileStructure.showsBoundingBox()));
    }

    private void updateMirrorButton() {
        Mirror mirror = tileStructure.getMirror();
        switch (mirror) {
            case NONE:
                mirrorButton.setMessage(new StringTextComponent("|"));
                break;
            case LEFT_RIGHT:
                mirrorButton.setMessage(new StringTextComponent("< >"));
                break;
            case FRONT_BACK:
                mirrorButton.setMessage(new StringTextComponent("^ v"));
        }

    }

    private void updateDirectionButtons() {
        rotateZeroDegreesButton.active = true;
        rotateNinetyDegreesButton.active = true;
        rotate180DegreesButton.active = true;
        rotate270DegressButton.active = true;
        switch (this.tileStructure.getRotation()) {
            case NONE:
                rotateZeroDegreesButton.active = false;
                break;
            case CLOCKWISE_180:
                rotate180DegreesButton.active = false;
                break;
            case COUNTERCLOCKWISE_90:
                rotate270DegressButton.active = false;
                break;
            case CLOCKWISE_90:
                rotateNinetyDegreesButton.active = false;
        }

    }

    private void updateMode() {
        nameEdit.setVisible(false);
        posXEdit.setVisible(false);
        posYEdit.setVisible(false);
        posZEdit.setVisible(false);
        sizeXEdit.setVisible(false);
        sizeYEdit.setVisible(false);
        sizeZEdit.setVisible(false);
        integrityEdit.setVisible(false);
        seedEdit.setVisible(false);
        dataEdit.setVisible(false);
        saveButton.visible = false;
        loadButton.visible = false;
        detectSizeButton.visible = false;
        showEntitiesButton.visible = false;
        mirrorButton.visible = false;
        rotateZeroDegreesButton.visible = false;
        rotateNinetyDegreesButton.visible = false;
        rotate180DegreesButton.visible = false;
        rotate270DegressButton.visible = false;
        showAirButton.visible = false;
        showBoundingBoxButton.visible = false;
        switch (tileStructure.getMode()) {
            case SAVE:
                nameEdit.setVisible(true);
                posXEdit.setVisible(true);
                posYEdit.setVisible(true);
                posZEdit.setVisible(true);
                sizeXEdit.setVisible(true);
                sizeYEdit.setVisible(true);
                sizeZEdit.setVisible(true);
                saveButton.visible = true;
                detectSizeButton.visible = true;
                showEntitiesButton.visible = true;
                showAirButton.visible = true;
                break;
            case LOAD:
                nameEdit.setVisible(true);
                posXEdit.setVisible(true);
                posYEdit.setVisible(true);
                posZEdit.setVisible(true);
                integrityEdit.setVisible(true);
                seedEdit.setVisible(true);
                loadButton.visible = true;
                showEntitiesButton.visible = true;
                mirrorButton.visible = true;
                rotateZeroDegreesButton.visible = true;
                rotateNinetyDegreesButton.visible = true;
                rotate180DegreesButton.visible = true;
                rotate270DegressButton.visible = true;
                showBoundingBoxButton.visible = true;
                updateDirectionButtons();
                break;
            case CORNER:
                nameEdit.setVisible(true);
                break;
            case DATA:
                dataEdit.setVisible(true);
        }

        modeButton.setMessage(new TranslationTextComponent("structure_block.mode." + tileStructure.getMode().getString()));
    }

    private boolean sendUpdate(StructureBlockTileEntity.UpdateCommand command) {
        BlockPos pos = new BlockPos(
            parseCoordinate(posXEdit.getText()),
            parseCoordinate(posYEdit.getText()),
            parseCoordinate(posZEdit.getText())
        );
        BlockPos size = new BlockPos(
            parseCoordinate(sizeXEdit.getText()),
            parseCoordinate(sizeYEdit.getText()),
            parseCoordinate(sizeZEdit.getText())
        );
        float integrity = parseIntegrity(integrityEdit.getText());
        long seed = parseSeed(seedEdit.getText());
        client.getConnection().sendPacket(new CUpdateStructureBlockPacket(
            tileStructure.getPos(),
            command,
            tileStructure.getMode(),
            nameEdit.getText(),
            pos, size,
            tileStructure.getMirror(),
            tileStructure.getRotation(),
            dataEdit.getText(),
            tileStructure.ignoresEntities(),
            tileStructure.showsAir(),
            tileStructure.showsBoundingBox(),
            integrity,
            seed
        ));
        return true;
    }

    private long parseSeed(String p_189821_1_) {
        try {
            return Long.parseLong(p_189821_1_);
        } catch (NumberFormatException numberformatexception) {
            return 0L;
        }
    }

    private float parseIntegrity(String p_189819_1_) {
        try {
            return Float.parseFloat(p_189819_1_);
        } catch (NumberFormatException numberformatexception) {
            return 1.0F;
        }
    }

    private int parseCoordinate(String p_189817_1_) {
        try {
            return Integer.parseInt(p_189817_1_);
        } catch (NumberFormatException numberformatexception) {
            return 0;
        }
    }

    @Override
    public void onClose() {
        this.updateStructureBlock();
    }

    @Override
    public boolean keyPressed(int key, int scancode, int mods) {
        if (super.keyPressed(key, scancode, mods)) {
            return true;
        } else if (key != GLFW.GLFW_KEY_ENTER && key != GLFW.GLFW_KEY_KP_ENTER) {
            return false;
        } else {
            this.updateAndClose();
            return true;
        }
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseZ, float partialTicks) {
        renderBackground(matrix);
        super.render(matrix, mouseX, mouseZ, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
