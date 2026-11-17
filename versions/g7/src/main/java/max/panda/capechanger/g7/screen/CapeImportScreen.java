package max.panda.capechanger.g7.screen;

import java.nio.file.Path;
import java.util.Set;
import max.panda.capechanger.g7.CapeChanger;
import max.panda.capechanger.g7.CapeChangerClient;
import max.panda.capechanger.g7.cape.CapeSelection;
import max.panda.capechanger.g7.cape.CapeStorage;
import max.panda.capechanger.g7.cape.CustomCape;
import max.panda.capechanger.g7.platform.AcvScreen;
import max.panda.capechanger.g7.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class CapeImportScreen extends AcvScreen {
    private static final int PANEL_PAD = 8;
    private final Screen parent;
    private EditBox pathField;
    private EditBox pathNameField;
    private EditBox browseNameField;
    private Button browseButton;
    private final Component waitingText = Component.translatable("acvcape.import.waiting");
    private Component statusText = Component.empty();
    private boolean dialogOpen;
    private int panelW;
    private int leftX;
    private int rightX;
    private int panelY;
    private int panelH;

    public CapeImportScreen(Screen var1) {
        super(Component.translatable("screen.acvcape.import"));
        this.parent = var1;
    }

    private Component hint(String var1, int var2) {
        String var3 = Component.translatable(var1).getString();
        int var4 = Math.max(8, var2 - 12);
        if (this.font.width(var3) > var4) {
            var3 = this.font.plainSubstrByWidth(var3, var4 - this.font.width("...")) + "...";
        }

        return Component.literal(var3);
    }

    @Override
    protected void init() {
        this.applyMenuScale();
        int var1 = this.width / 2;
        this.panelW = Math.max(130, Math.min(200, (this.width - 40) / 2));
        this.leftX = var1 - this.panelW - 6;
        this.rightX = var1 + 6;
        this.panelY = this.height / 2 - 62;
        this.panelH = 132;
        int var2 = this.panelW - 16;
        int var3 = this.panelY + 34;
        this.pathField = new EditBox(this.font, this.leftX + 8, var3, var2, 20, Component.translatable("acvcape.field.path"));
        this.pathField.setMaxLength(512);
        this.pathField.setHint(this.hint("acvcape.field.path.hint", var2));
        this.addRenderableWidget(this.pathField);
        this.pathNameField = new EditBox(this.font, this.leftX + 8, var3 + 26, var2, 20, Component.translatable("acvcape.field.name"));
        this.pathNameField.setMaxLength(64);
        this.pathNameField.setHint(this.hint("acvcape.field.name.hint", var2));
        this.addRenderableWidget(this.pathNameField);
        this.addRenderableWidget(
            Button.builder(Component.translatable("acvcape.button.import"), var1x -> this.importFromPath()).bounds(this.leftX + 8, var3 + 52, var2, 20).build()
        );
        this.browseButton = this.addRenderableWidget(
            Button.builder(Component.translatable("acvcape.button.browse"), var1x -> this.browse()).bounds(this.rightX + 8, var3, var2, 20).build()
        );
        this.browseNameField = new EditBox(this.font, this.rightX + 8, var3 + 26, var2, 20, Component.translatable("acvcape.field.name"));
        this.browseNameField.setMaxLength(64);
        this.browseNameField.setHint(this.hint("acvcape.field.name.hint", var2));
        this.addRenderableWidget(this.browseNameField);
        this.addRenderableWidget(
            Button.builder(Component.translatable("gui.cancel"), var1x -> this.minecraft.setScreen(this.parent))
                .bounds(var1 - 75, this.panelY + this.panelH + 8, 150, 20)
                .build()
        );
    }

    @Override
    protected void drawBackdrop(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
        super.drawBackdrop(var1, var2, var3, var4);

        for (int var8 : new int[]{this.leftX, this.rightX}) {
            var1.fill(var8, this.panelY, var8 + this.panelW, this.panelY + this.panelH, -535292635);
            P.border(var1, var8, this.panelY, this.panelW, this.panelH, -13358769);
        }
    }

    @Override
    protected void renderScreen(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
        this.renderWidgets(var1, var2, var3, var4);
        Ui.buttons(var1, this.font, this, Set.of());
        P.centered(var1, this.font, this.getTitle(), this.width / 2, 20, 16777215);
        this.drawPanel(var1, this.leftX, "acvcape.import.method.path", "acvcape.import.path.desc");
        this.drawPanel(var1, this.rightX, "acvcape.import.method.browse", "acvcape.import.browse.desc");
        if (!this.statusText.getString().isEmpty()) {
            P.centered(var1, this.font, this.statusText, this.width / 2, this.panelY + this.panelH + 34, 16755268);
        }
    }

    private void drawPanel(GuiGraphicsExtractor var1, int var2, String var3, String var4) {
        P.centered(var1, this.font, Component.translatable(var3), var2 + this.panelW / 2, this.panelY + 7, 16777215);
        String var5 = this.font.plainSubstrByWidth(Component.translatable(var4).getString(), this.panelW - 16);
        P.centered(var1, this.font, var5, var2 + this.panelW / 2, this.panelY + 20, 11184810);
    }

    private void browse() {
        if (!this.dialogOpen) {
            this.dialogOpen = true;
            this.browseButton.active = false;
            this.statusText = this.waitingText;
            NativeFileDialog.pickPng(var1 -> this.doImport(var1, this.browseNameField.getValue().trim()), var1 -> {
                this.dialogOpen = false;
                this.browseButton.active = true;
                if (var1) {
                    this.statusText = Component.translatable("acvcape.error.dialog_failed");
                } else if (this.statusText == this.waitingText) {
                    this.statusText = Component.empty();
                }
            });
        }
    }

    private void importFromPath() {
        String var1 = this.pathField.getValue().trim();
        if (var1.length() >= 2 && var1.startsWith("\"") && var1.endsWith("\"")) {
            var1 = var1.substring(1, var1.length() - 1).trim();
        }

        if (var1.isEmpty()) {
            this.statusText = Component.translatable("acvcape.error.no_path");
        } else {
            Path var2;
            try {
                var2 = Path.of(var1);
            } catch (Exception var4) {
                this.statusText = Component.translatable("acvcape.error.import_failed");
                return;
            }

            this.doImport(var2, this.pathNameField.getValue().trim());
        }
    }

    private void doImport(Path var1, String var2) {
        if (this.minecraft != null && this.minecraft.screen == this) {
            try {
                CapeChangerClient var3 = CapeChangerClient.instance();
                CustomCape var4 = var3.getManager().importFromFile(var1, var2);
                CapeSelection var5 = CapeSelection.customSelection(var4.hash());
                var3.getManager().setActiveSelection(var5);
                var3.markCommitPending();
                this.minecraft.setScreen(this.parent);
            } catch (CapeStorage.InvalidCapeException var6) {
                this.statusText = Component.literal(var6.getMessage());
            } catch (Exception var7) {
                CapeChanger.LOGGER.error("Falha ao importar capa", var7);
                this.statusText = Component.translatable("acvcape.error.import_failed");
            }
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void removed() {
        super.removed();
    }
}
