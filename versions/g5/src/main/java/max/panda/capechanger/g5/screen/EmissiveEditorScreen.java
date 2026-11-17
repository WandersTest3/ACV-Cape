package max.panda.capechanger.g5.screen;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import max.panda.capechanger.g5.CapeChanger;
import max.panda.capechanger.g5.CapeChangerClient;
import max.panda.capechanger.g5.cape.CapeConfig;
import max.panda.capechanger.g5.cape.CapeEmissive;
import max.panda.capechanger.g5.cape.CapeFormat;
import max.panda.capechanger.g5.cape.CapeManager;
import max.panda.capechanger.g5.cape.CapeSelection;
import max.panda.capechanger.g5.platform.AcvScreen;
import max.panda.capechanger.g5.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class EmissiveEditorScreen extends AcvScreen {
    private static final double[] ZOOMS = new double[]{0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 3.0, 4.0, 6.0, 8.0, 10.0, 12.0, 16.0, 20.0, 24.0, 32.0};
    private static final int TOOLBAR_W = 124;
    private static final int MAX_HISTORY = 60;
    private static final Identifier DISPLAY_ID = Identifier.of("acvcape", "textures/editor/emissive_preview");
    private static final Identifier CHECKER_ID = Identifier.of("acvcape", "textures/editor/emissive_checker");
    private NativeImageBackedTexture checker;
    private int checkerW;
    private int checkerH;
    private final Screen parent;
    private final CapeSelection source;
    private final CapeManager manager;
    private final String capeName;
    private byte[] sourceBytes;
    private int[] pixels;
    private int imgW;
    private int imgH;
    private int frameH;
    private CapeEmissive.Mask mask;
    private final Deque<BitSet> undo = new ArrayDeque<>();
    private final Deque<BitSet> redo = new ArrayDeque<>();
    private NativeImageBackedTexture display;
    private boolean fullDirty = true;
    private int dirtyMinX = Integer.MAX_VALUE;
    private int dirtyMinY = Integer.MAX_VALUE;
    private int dirtyMaxX = -1;
    private int dirtyMaxY = -1;
    private EmissiveEditorScreen.Tool tool = EmissiveEditorScreen.Tool.PENCIL;
    private int brush = 1;
    private int zoomIndex = 9;
    private double panX;
    private double panY;
    private boolean fitted;
    private boolean painting;
    private boolean paintValue;
    private boolean panning;
    private int lastPaintX = -1;
    private int lastPaintY = -1;
    private int hoverX = -1;
    private int hoverY = -1;
    private Text statusText;
    private int statusColor = -6250326;
    private boolean changed;
    private int canvasX;
    private int canvasY;
    private int canvasW;
    private int canvasH;
    private final List<ButtonWidget> toolButtons = new ArrayList<>();
    private ButtonWidget brushButton;
    private IntSlider bucketSlider;
    private IntSlider colorSlider;
    private ButtonWidget gridButton;
    private ButtonWidget undoButton;
    private ButtonWidget redoButton;
    private ButtonWidget saveButton;

    public EmissiveEditorScreen(Screen var1, CapeSelection var2) {
        super(Text.translatable("screen.acvcape.emissive"));
        this.parent = var1;
        this.source = var2;
        this.manager = CapeChangerClient.instance().getManager();
        this.capeName = this.manager.displayName(var2);
        this.load();
    }

    private void load() {
        this.sourceBytes = this.manager.sourceBytes(this.source);
        if (this.sourceBytes == null) {
            this.setStatus(Text.translatable("label.acvcape.editor_no_source"), -30652);
        } else {
            try {
                BufferedImage var1 = ImageIO.read(new ByteArrayInputStream(this.sourceBytes));
                if (var1 == null) {
                    throw new IllegalStateException("imagem invalida");
                }

                this.imgW = var1.getWidth();
                this.imgH = var1.getHeight();
                this.pixels = var1.getRGB(0, 0, this.imgW, this.imgH, null, 0, this.imgW);
                CapeFormat.Layout var2 = CapeFormat.analyze(this.imgW, this.imgH);
                this.frameH = var2 == null ? this.imgH : var2.frameH();
                CapeEmissive.Mask var3 = CapeEmissive.read(this.sourceBytes);
                this.mask = var3 != null && var3.width() == this.imgW && var3.height() == this.imgH ? var3 : CapeEmissive.Mask.empty(this.imgW, this.imgH);
            } catch (Exception var4) {
                CapeChanger.LOGGER.error("Falha ao abrir capa no editor", var4);
                this.sourceBytes = null;
                this.pixels = null;
                this.setStatus(Text.translatable("label.acvcape.editor_no_source"), -30652);
            }
        }
    }

    private boolean ready() {
        return this.pixels != null && this.mask != null;
    }

    private void setStatus(Text var1, int var2) {
        this.statusText = var1;
        this.statusColor = var2;
    }

    @Override
    protected void init() {
        this.applyMenuScale();
        this.canvasX = 140;
        this.canvasY = 40;
        this.canvasW = this.width - this.canvasX - 8;
        this.canvasH = this.height - this.canvasY - 22;
        this.toolButtons.clear();
        byte var1 = 8;
        byte var2 = 60;
        int var3 = 28;
        EmissiveEditorScreen.Tool[] var4 = EmissiveEditorScreen.Tool.values();

        for (int var5 = 0; var5 < var4.length; var5++) {
            EmissiveEditorScreen.Tool var6 = var4[var5];
            int var7 = var1 + var5 % 2 * (var2 + 4);
            int var8 = var3 + var5 / 2 * 24;
            ButtonWidget var9 = this.addDrawableChild(
                ButtonWidget.builder(Text.translatable(var6.key), var2x -> this.selectTool(var6)).dimensions(var7, var8, var2, 20).build()
            );
            P.tooltip(var9, Text.translatable(var6.key + ".desc"));
            this.toolButtons.add(var9);
        }

        var3 += 48;
        this.brushButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), var1x -> this.cycleBrush()).dimensions(var1, var3, 124, 20).build());
        P.tooltip(this.brushButton, Text.translatable("tool.acvcape.brush.desc"));
        var3 += 24;
        this.bucketSlider = this.addDrawableChild(
            new IntSlider(
                    var1,
                    var3,
                    124,
                    20,
                    0,
                    10,
                    1,
                    this.manager.getConfig().getBucketSensitivity(),
                    var0 -> Text.translatable("slider.acvcape.bucket_sensitivity", var0),
                    var1x -> this.manager.getConfig().setBucketSensitivity(var1x)
                )
                .withTooltip(Text.translatable("tooltip.acvcape.bucket_sensitivity"))
        );
        var3 += 24;
        this.colorSlider = this.addDrawableChild(
            new IntSlider(
                    var1,
                    var3,
                    124,
                    20,
                    0,
                    10,
                    1,
                    this.manager.getConfig().getColorSensitivity(),
                    var0 -> Text.translatable("slider.acvcape.color_sensitivity", var0),
                    var1x -> this.manager.getConfig().setColorSensitivity(var1x)
                )
                .withTooltip(Text.translatable("tooltip.acvcape.color_sensitivity"))
        );
        var3 += 24;
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Zoom +"), var1x -> this.zoomAt(1, this.canvasX + this.canvasW / 2.0, this.canvasY + this.canvasH / 2.0))
                .dimensions(var1, var3, var2, 20)
                .build()
        );
        this.addDrawableChild(
            ButtonWidget.builder(Text.literal("Zoom -"), var1x -> this.zoomAt(-1, this.canvasX + this.canvasW / 2.0, this.canvasY + this.canvasH / 2.0))
                .dimensions(var1 + var2 + 4, var3, var2, 20)
                .build()
        );
        var3 += 24;
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("tool.acvcape.fit"), var1x -> this.fit()).dimensions(var1, var3, var2, 20).build());
        this.gridButton = this.addDrawableChild(
            ButtonWidget.builder(Text.empty(), var1x -> this.toggleGrid()).dimensions(var1 + var2 + 4, var3, var2, 20).build()
        );
        P.tooltip(this.gridButton, Text.translatable("tool.acvcape.grid.desc"));
        var3 += 24;
        this.undoButton = this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("tool.acvcape.undo"), var1x -> this.undo()).dimensions(var1, var3, var2, 20).build()
        );
        this.redoButton = this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("tool.acvcape.redo"), var1x -> this.redo()).dimensions(var1 + var2 + 4, var3, var2, 20).build()
        );
        var3 += 24;
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("tool.acvcape.select_all"), var1x -> this.fillAll(true)).dimensions(var1, var3, var2, 20).build()
        );
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("tool.acvcape.clear"), var1x -> this.fillAll(false)).dimensions(var1 + var2 + 4, var3, var2, 20).build()
        );
        int var17 = this.height - 8 - 20;
        this.saveButton = this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("button.acvcape.save"), var1x -> this.save()).dimensions(var1, var17, var2, 20).build()
        );
        this.addDrawableChild(ButtonWidget.builder(Text.translatable("gui.cancel"), var1x -> this.close()).dimensions(var1 + var2 + 4, var17, var2, 20).build());
        this.refreshButtons();
        if (!this.fitted) {
            this.fit();
            this.fitted = true;
        }

        this.fullDirty = true;
    }

    private void refreshButtons() {
        EmissiveEditorScreen.Tool[] var1 = EmissiveEditorScreen.Tool.values();

        for (int var2 = 0; var2 < this.toolButtons.size(); var2++) {
            this.toolButtons.get(var2).active = this.ready() && var1[var2] != this.tool;
        }

        this.brushButton.setMessage(Text.translatable("tool.acvcape.brush", this.brush));
        this.brushButton.active = this.ready();
        this.bucketSlider.active = this.ready();
        this.colorSlider.active = this.ready();
        this.gridButton.setMessage(Text.translatable("tool.acvcape.grid").append(": ").append(ScreenTexts.onOrOff(this.manager.getConfig().isEditorGrid())));
        this.undoButton.active = !this.undo.isEmpty();
        this.redoButton.active = !this.redo.isEmpty();
        this.saveButton.active = this.ready();
    }

    private Set<ClickableWidget> selectedWidgets() {
        HashSet var1 = new HashSet();
        int var2 = this.tool.ordinal();
        if (var2 < this.toolButtons.size()) {
            var1.add(this.toolButtons.get(var2));
        }

        if (this.gridButton != null && this.manager.getConfig().isEditorGrid()) {
            var1.add(this.gridButton);
        }

        return var1;
    }

    private void selectTool(EmissiveEditorScreen.Tool var1) {
        this.tool = var1;
        this.refreshButtons();
    }

    private void toggleGrid() {
        CapeConfig var1 = this.manager.getConfig();
        var1.setEditorGrid(!var1.isEditorGrid());
        this.refreshButtons();
    }

    private static boolean similar(int var0, int var1, int var2) {
        if (var0 == var1) {
            return true;
        } else if (var2 > 0 && var0 >>> 24 != 0 && var1 >>> 24 != 0) {
            int var3 = (var0 >> 16 & 0xFF) - (var1 >> 16 & 0xFF);
            int var4 = (var0 >> 8 & 0xFF) - (var1 >> 8 & 0xFF);
            int var5 = (var0 & 0xFF) - (var1 & 0xFF);
            int var6 = var2 * 18;
            return var3 * var3 + var4 * var4 + var5 * var5 <= var6 * var6;
        } else {
            return false;
        }
    }

    private void cycleBrush() {
        this.brush = this.brush >= 4 ? 1 : this.brush + 1;
        this.refreshButtons();
    }

    private double zoom() {
        return ZOOMS[this.zoomIndex];
    }

    private void fit() {
        if (this.ready()) {
            int var1 = 0;

            for (int var2 = 0; var2 < ZOOMS.length; var2++) {
                if (this.imgW * ZOOMS[var2] <= this.canvasW - 16 && this.imgH * ZOOMS[var2] <= this.canvasH - 16) {
                    var1 = var2;
                }
            }

            this.zoomIndex = var1;
            this.panX = 0.0;
            this.panY = 0.0;
        }
    }

    private void zoomAt(int var1, double var2, double var4) {
        if (this.ready()) {
            int var6 = Math.max(0, Math.min(ZOOMS.length - 1, this.zoomIndex + var1));
            if (var6 != this.zoomIndex) {
                double var7 = this.zoom();
                double var9 = (var2 - this.imageLeft()) / var7;
                double var11 = (var4 - this.imageTop()) / var7;
                this.zoomIndex = var6;
                double var13 = this.zoom();
                double var15 = this.canvasX + this.canvasW / 2.0;
                double var17 = this.canvasY + this.canvasH / 2.0;
                this.panX = var2 - var9 * var13 - var15 + this.imgW * var13 / 2.0;
                this.panY = var4 - var11 * var13 - var17 + this.imgH * var13 / 2.0;
            }
        }
    }

    private double imageLeft() {
        return Math.floor(this.canvasX + this.canvasW / 2.0 + this.panX - this.imgW * this.zoom() / 2.0);
    }

    private double imageTop() {
        return Math.floor(this.canvasY + this.canvasH / 2.0 + this.panY - this.imgH * this.zoom() / 2.0);
    }

    private int pixelX(double var1) {
        return (int)Math.floor((var1 - this.imageLeft()) / this.zoom());
    }

    private int pixelY(double var1) {
        return (int)Math.floor((var1 - this.imageTop()) / this.zoom());
    }

    private boolean inImage(int var1, int var2) {
        return var1 >= 0 && var2 >= 0 && var1 < this.imgW && var2 < this.imgH;
    }

    private boolean inCanvas(double var1, double var3) {
        return var1 >= this.canvasX && var1 < this.canvasX + this.canvasW && var3 >= this.canvasY && var3 < this.canvasY + this.canvasH;
    }

    private void pushUndo() {
        this.undo.push((BitSet)this.mask.bits().clone());

        while (this.undo.size() > 60) {
            this.undo.removeLast();
        }

        this.redo.clear();
        this.changed = true;
    }

    private void undo() {
        if (!this.undo.isEmpty()) {
            this.redo.push((BitSet)this.mask.bits().clone());
            this.restore(this.undo.pop());
        }
    }

    private void redo() {
        if (!this.redo.isEmpty()) {
            this.undo.push((BitSet)this.mask.bits().clone());
            this.restore(this.redo.pop());
        }
    }

    private void restore(BitSet var1) {
        this.mask.bits().clear();
        this.mask.bits().or(var1);
        this.fullDirty = true;
        this.changed = true;
        this.refreshButtons();
    }

    private void markDirty(int var1, int var2) {
        this.dirtyMinX = Math.min(this.dirtyMinX, var1);
        this.dirtyMinY = Math.min(this.dirtyMinY, var2);
        this.dirtyMaxX = Math.max(this.dirtyMaxX, var1);
        this.dirtyMaxY = Math.max(this.dirtyMaxY, var2);
    }

    private boolean paintable(int var1, int var2) {
        return this.pixels[var2 * this.imgW + var1] >>> 24 != 0;
    }

    private void setPixel(int var1, int var2, boolean var3) {
        if (this.inImage(var1, var2) && this.paintable(var1, var2) && this.mask.get(var1, var2) != var3) {
            this.mask.set(var1, var2, var3);
            this.markDirty(var1, var2);
        }
    }

    private void stamp(int var1, int var2, boolean var3) {
        int var4 = -(this.brush - 1) / 2;

        for (int var5 = 0; var5 < this.brush; var5++) {
            for (int var6 = 0; var6 < this.brush; var6++) {
                this.setPixel(var1 + var4 + var6, var2 + var4 + var5, var3);
            }
        }
    }

    private void strokeTo(int var1, int var2) {
        if (this.lastPaintX < 0) {
            this.stamp(var1, var2, this.paintValue);
        } else {
            int var3 = this.lastPaintX;
            int var4 = this.lastPaintY;
            int var5 = Math.abs(var1 - var3);
            int var6 = -Math.abs(var2 - var4);
            int var7 = var3 < var1 ? 1 : -1;
            int var8 = var4 < var2 ? 1 : -1;
            int var9 = var5 + var6;

            while (true) {
                this.stamp(var3, var4, this.paintValue);
                if (var3 == var1 && var4 == var2) {
                    break;
                }

                int var10 = 2 * var9;
                if (var10 >= var6) {
                    var9 += var6;
                    var3 += var7;
                }

                if (var10 <= var5) {
                    var9 += var5;
                    var4 += var8;
                }
            }
        }

        this.lastPaintX = var1;
        this.lastPaintY = var2;
    }

    private void bucket(int var1, int var2) {
        int var3 = this.pixels[var2 * this.imgW + var1];
        int var4 = this.manager.getConfig().getBucketSensitivity();
        boolean var5 = !this.mask.get(var1, var2);
        BitSet var6 = new BitSet(this.imgW * this.imgH);
        ArrayDeque var7 = new ArrayDeque();
        var7.add(var2 * this.imgW + var1);
        var6.set(var2 * this.imgW + var1);

        while (!var7.isEmpty()) {
            int var8 = (Integer)var7.poll();
            int var9 = var8 % this.imgW;
            int var10 = var8 / this.imgW;
            this.setPixel(var9, var10, var5);
            int[][] var11 = new int[][]{{var9 + 1, var10}, {var9 - 1, var10}, {var9, var10 + 1}, {var9, var10 - 1}};

            for (int[] var15 : var11) {
                if (this.inImage(var15[0], var15[1])) {
                    int var16 = var15[1] * this.imgW + var15[0];
                    if (!var6.get(var16) && similar(this.pixels[var16], var3, var4)) {
                        var6.set(var16);
                        var7.add(var16);
                    }
                }
            }
        }
    }

    private void colorSelect(int var1, int var2) {
        int var3 = this.pixels[var2 * this.imgW + var1];
        int var4 = this.manager.getConfig().getColorSensitivity();
        boolean var5 = !this.mask.get(var1, var2);

        for (int var6 = 0; var6 < this.imgH; var6++) {
            for (int var7 = 0; var7 < this.imgW; var7++) {
                if (similar(this.pixels[var6 * this.imgW + var7], var3, var4)) {
                    this.setPixel(var7, var6, var5);
                }
            }
        }
    }

    private void fillAll(boolean var1) {
        if (this.ready()) {
            this.pushUndo();

            for (int var2 = 0; var2 < this.imgH; var2++) {
                for (int var3 = 0; var3 < this.imgW; var3++) {
                    this.setPixel(var3, var2, var1);
                }
            }

            this.refreshButtons();
        }
    }

    private void save() {
        if (this.ready()) {
            try {
                byte[] var1 = CapeEmissive.write(this.sourceBytes, this.mask);
                this.manager.saveEmissive(this.source, var1);
                CapeChangerClient.instance().markCommitPending();
                this.changed = false;
                this.client.setScreen(this.parent);
            } catch (Exception var2) {
                CapeChanger.LOGGER.error("Falha ao salvar emissividade", var2);
                this.setStatus(Text.translatable("label.acvcape.editor_save_failed"), -43691);
            }
        }
    }

    private static int shade(int var0, boolean var1) {
        int var2 = var0 >>> 24 & 0xFF;
        int var3 = var0 >> 16 & 0xFF;
        int var4 = var0 >> 8 & 0xFF;
        int var5 = var0 & 0xFF;
        if (var1) {
            var3 += (255 - var3) * 10 / 100;
            var4 += (255 - var4) * 10 / 100;
            var5 += (255 - var5) * 10 / 100;
        } else {
            var3 /= 2;
            var4 /= 2;
            var5 /= 2;
        }

        return var2 << 24 | var5 << 16 | var4 << 8 | var3;
    }

    private void ensureChecker() {
        int var1 = Math.max(1, this.canvasW);
        int var2 = Math.max(1, this.canvasH);
        if (this.checker == null || this.checkerW != var1 || this.checkerH != var2) {
            if (this.checker != null) {
                P.destroy(CHECKER_ID);
                this.checker = null;
            }

            NativeImage var3 = new NativeImage(var1, var2, false);

            for (int var4 = 0; var4 < var2; var4++) {
                for (int var5 = 0; var5 < var1; var5++) {
                    P.setColor(var3, var5, var4, (var5 / 8 + var4 / 8) % 2 == 0 ? -12965330 : -13885662);
                }
            }

            this.checker = P.newTexture(var3);
            P.register(CHECKER_ID, this.checker);
            this.checker.upload();
            this.checkerW = var1;
            this.checkerH = var2;
        }
    }

    private void updateDisplay() {
        if (this.ready()) {
            if (this.display == null) {
                this.display = P.newTexture(new NativeImage(this.imgW, this.imgH, false));
                P.register(DISPLAY_ID, this.display);
                this.fullDirty = true;
            }

            NativeImage var1 = this.display.getImage();
            if (var1 != null) {
                int var2;
                int var3;
                int var4;
                int var5;
                if (this.fullDirty) {
                    var2 = 0;
                    var3 = 0;
                    var4 = this.imgW - 1;
                    var5 = this.imgH - 1;
                } else {
                    if (this.dirtyMaxX < 0) {
                        return;
                    }

                    var2 = this.dirtyMinX;
                    var3 = this.dirtyMinY;
                    var4 = this.dirtyMaxX;
                    var5 = this.dirtyMaxY;
                }

                for (int var6 = var3; var6 <= var5; var6++) {
                    for (int var7 = var2; var7 <= var4; var7++) {
                        P.setColor(var1, var7, var6, shade(this.pixels[var6 * this.imgW + var7], this.mask.get(var7, var6)));
                    }
                }

                this.display.upload();
                this.fullDirty = false;
                this.dirtyMinX = Integer.MAX_VALUE;
                this.dirtyMinY = Integer.MAX_VALUE;
                this.dirtyMaxX = -1;
                this.dirtyMaxY = -1;
            }
        }
    }

    @Override
    protected void drawBackdrop(DrawContext var1, int var2, int var3, float var4) {
        var1.fill(0, 0, this.width, this.height, -938800878);
        var1.fill(0, 0, this.width, 22, -15462113);
        var1.fill(0, 22, this.width, 23, -7643914);
        var1.fill(this.canvasX, this.canvasY, this.canvasX + this.canvasW, this.canvasY + this.canvasH, -535292635);
        P.border(var1, this.canvasX - 1, this.canvasY - 1, this.canvasW + 2, this.canvasH + 2, -13358769);
    }

    @Override
    protected void renderScreen(DrawContext var1, int var2, int var3, float var4) {
        this.renderWidgets(var1, var2, var3, var4);
        Ui.buttons(var1, this.textRenderer, this, this.selectedWidgets());
        P.centered(var1, this.textRenderer, Text.translatable("screen.acvcape.emissive_title", this.capeName), this.width / 2, 7, -1);
        P.centered(var1, this.textRenderer, Text.translatable("label.acvcape.editor_hint"), this.canvasX + this.canvasW / 2, 28, -6250326);
        if (!this.ready()) {
            if (this.statusText != null) {
                P.centered(var1, this.textRenderer, this.statusText, this.canvasX + this.canvasW / 2, this.canvasY + this.canvasH / 2, this.statusColor);
            }
        } else {
            this.updateDisplay();
            double var5 = this.zoom();
            int var7 = (int)this.imageLeft();
            int var8 = (int)this.imageTop();
            int var9 = Math.max(1, (int)Math.round(this.imgW * var5));
            int var10 = Math.max(1, (int)Math.round(this.imgH * var5));
            var1.enableScissor(this.canvasX, this.canvasY, this.canvasX + this.canvasW, this.canvasY + this.canvasH);
            int var11 = Math.max(var7, this.canvasX);
            int var12 = Math.max(var8, this.canvasY);
            int var13 = Math.min(var7 + var9, this.canvasX + this.canvasW);
            int var14 = Math.min(var8 + var10, this.canvasY + this.canvasH);
            this.ensureChecker();
            if (this.checker != null && var13 > var11 && var14 > var12) {
                P.texture(
                    var1,
                    CHECKER_ID,
                    var11,
                    var12,
                    var13 - var11,
                    var14 - var12,
                    var11 - this.canvasX,
                    var12 - this.canvasY,
                    var13 - var11,
                    var14 - var12,
                    this.checkerW,
                    this.checkerH
                );
            }

            P.texture(var1, DISPLAY_ID, var7, var8, var9, var10, 0.0F, 0.0F, this.imgW, this.imgH, this.imgW, this.imgH);
            if (var5 >= 12.0 && this.manager.getConfig().isEditorGrid()) {
                int var15 = Math.max(0, (int)((var11 - var7) / var5));
                int var16 = Math.min(this.imgW, (int)((var13 - var7) / var5) + 1);

                for (int var17 = var15; var17 <= var16; var17++) {
                    int var18 = var7 + (int)Math.round(var17 * var5);
                    var1.fill(var18, var12, var18 + 1, var14, 822083583);
                }

                int var28 = Math.max(0, (int)((var12 - var8) / var5));
                int var30 = Math.min(this.imgH, (int)((var14 - var8) / var5) + 1);

                for (int var19 = var28; var19 <= var30; var19++) {
                    int var20 = var8 + (int)Math.round(var19 * var5);
                    var1.fill(var11, var20, var13, var20 + 1, 822083583);
                }
            }

            if (this.frameH > 0 && this.frameH < this.imgH) {
                for (int var22 = 1; var22 * this.frameH < this.imgH; var22++) {
                    int var25 = var8 + (int)Math.round(var22 * this.frameH * var5);
                    var1.fill(var11, var25, var13, var25 + 1, -7643914);
                }
            }

            P.border(var1, var7 - 1, var8 - 1, var9 + 2, var10 + 2, -7643914);
            this.hoverX = -1;
            this.hoverY = -1;
            if (this.inCanvas(var2, var3)) {
                int var23 = this.pixelX(var2);
                int var26 = this.pixelY(var3);
                if (this.inImage(var23, var26)) {
                    this.hoverX = var23;
                    this.hoverY = var26;
                    int var29 = this.tool != EmissiveEditorScreen.Tool.PENCIL && this.tool != EmissiveEditorScreen.Tool.ERASER ? 1 : this.brush;
                    int var31 = -(var29 - 1) / 2;
                    int var32 = var7 + (int)Math.round((var23 + var31) * var5);
                    int var33 = var8 + (int)Math.round((var26 + var31) * var5);
                    int var21 = Math.max(1, (int)Math.round(var29 * var5));
                    P.border(var1, var32 - 1, var33 - 1, var21 + 2, var21 + 2, -1003472);
                }
            }

            var1.disableScissor();
            String var24 = String.format(
                Locale.ROOT,
                "%dx%d  ·  %s %.2fx  ·  %s",
                this.imgW,
                this.imgH,
                "Zoom",
                var5,
                Text.translatable("label.acvcape.emissive_count", this.mask.count()).getString()
            );
            if (this.hoverX >= 0) {
                var24 = var24 + String.format(Locale.ROOT, "  ·  %d, %d", this.hoverX, this.hoverY);
            }

            P.text(var1, this.textRenderer, var24, this.canvasX, this.height - 16, -6250326);
            if (this.statusText != null) {
                int var27 = this.textRenderer.getWidth(this.statusText);
                P.text(var1, this.textRenderer, this.statusText, this.canvasX + this.canvasW - var27, this.height - 16, this.statusColor);
            }
        }
    }

    @Override
    protected boolean onMouseDown(double var1, double var3, int var5) {
        if (this.superMouseDown(var1, var3, var5)) {
            return true;
        }

        if (!this.ready() || !this.inCanvas(var1, var3)) {
            return false;
        }

        if (var5 == 1 || var5 == 2) {
            this.panning = true;
            return true;
        }

        if (var5 != 0) {
            return false;
        }

        int var6 = this.pixelX(var1);
        int var7 = this.pixelY(var3);
        if (!this.inImage(var6, var7)) {
            return true;
        }

        this.pushUndo();
        switch (this.tool) {
            case PENCIL:
            case ERASER:
                this.painting = true;
                this.paintValue = this.tool == EmissiveEditorScreen.Tool.PENCIL;
                this.lastPaintX = -1;
                this.lastPaintY = -1;
                this.strokeTo(var6, var7);
                break;
            case BUCKET:
                this.bucket(var6, var7);
                break;
            case COLOR:
                this.colorSelect(var6, var7);
        }

        this.refreshButtons();
        return true;
    }

    @Override
    protected boolean onMouseDrag(double var1, double var3, int var5, double var6, double var8) {
        if (this.panning) {
            this.panX += var6;
            this.panY += var8;
            return true;
        } else if (this.painting) {
            int var10 = Math.max(0, Math.min(this.imgW - 1, this.pixelX(var1)));
            int var11 = Math.max(0, Math.min(this.imgH - 1, this.pixelY(var3)));
            this.strokeTo(var10, var11);
            return true;
        } else {
            return this.superMouseDrag(var1, var3, var5, var6, var8);
        }
    }

    @Override
    protected boolean onMouseUp(double var1, double var3, int var5) {
        this.painting = false;
        this.panning = false;
        this.lastPaintX = -1;
        this.lastPaintY = -1;
        return this.superMouseUp(var1, var3, var5);
    }

    @Override
    protected boolean onScroll(double var1, double var3, double var5, double var7) {
        if (this.inCanvas(var1, var3) && var7 != 0.0) {
            this.zoomAt(var7 > 0.0 ? 1 : -1, var1, var3);
            return true;
        } else {
            return this.superScroll(var1, var3, var5, var7);
        }
    }

    @Override
    protected boolean onKey(int var1, int var2, int var3) {
        boolean var4 = P.ctrlDown();
        if (var4 && var1 == 90) {
            if (P.shiftDown()) {
                this.redo();
            } else {
                this.undo();
            }

            this.refreshButtons();
            return true;
        } else {
            if (var4 && var1 == 89) {
                this.redo();
                this.refreshButtons();
                return true;
            }

            if (!var4) {
                switch (var1) {
                    case 45:
                    case 333:
                        this.zoomAt(-1, this.canvasX + this.canvasW / 2.0, this.canvasY + this.canvasH / 2.0);
                        return true;
                    case 61:
                    case 334:
                        this.zoomAt(1, this.canvasX + this.canvasW / 2.0, this.canvasY + this.canvasH / 2.0);
                        return true;
                    case 66:
                        this.selectTool(EmissiveEditorScreen.Tool.BUCKET);
                        return true;
                    case 67:
                        this.selectTool(EmissiveEditorScreen.Tool.COLOR);
                        return true;
                    case 69:
                        this.selectTool(EmissiveEditorScreen.Tool.ERASER);
                        return true;
                    case 70:
                        this.fit();
                        return true;
                    case 80:
                        this.selectTool(EmissiveEditorScreen.Tool.PENCIL);
                        return true;
                }
            }

            return this.superKey(var1, var2, var3);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void removed() {
        this.manager.getConfig().save();
        super.removed();
        if (this.display != null) {
            P.destroy(DISPLAY_ID);
            this.display = null;
        }

        if (this.checker != null) {
            P.destroy(CHECKER_ID);
            this.checker = null;
        }
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    private enum Tool {
        PENCIL("tool.acvcape.pencil"),
        ERASER("tool.acvcape.eraser"),
        BUCKET("tool.acvcape.bucket"),
        COLOR("tool.acvcape.color");

        final String key;

        Tool(String nullxx) {
            this.key = nullxx;
        }
    }
}
