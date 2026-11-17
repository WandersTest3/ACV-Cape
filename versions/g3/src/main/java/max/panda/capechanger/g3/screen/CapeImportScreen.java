package max.panda.capechanger.g3.screen;

import java.nio.file.Path;
import java.util.Set;
import max.panda.capechanger.g3.CapeChanger;
import max.panda.capechanger.g3.CapeChangerClient;
import max.panda.capechanger.g3.cape.CapeSelection;
import max.panda.capechanger.g3.cape.CapeStorage;
import max.panda.capechanger.g3.cape.CustomCape;
import max.panda.capechanger.g3.platform.AcvScreen;
import max.panda.capechanger.g3.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class CapeImportScreen extends AcvScreen {
    private static final int PANEL_PAD = 8;
    private final Screen parent;
    private TextFieldWidget pathField;
    private TextFieldWidget pathNameField;
    private TextFieldWidget browseNameField;
    private ButtonWidget browseButton;
    private final Text waitingText = Text.translatable("acvcape.import.waiting");
    private Text statusText = Text.empty();
    private boolean dialogOpen;
    private int panelW;
    private int leftX;
    private int rightX;
    private int panelY;
    private int panelH;

    public CapeImportScreen(Screen var1) {
        super(Text.translatable("screen.acvcape.import"));
        this.parent = var1;
    }

    private Text hint(String var1, int var2) {
        String var3 = Text.translatable(var1).getString();
        int var4 = Math.max(8, var2 - 12);
        if (this.textRenderer.getWidth(var3) > var4) {
            var3 = this.textRenderer.trimToWidth(var3, var4 - this.textRenderer.getWidth("...")) + "...";
        }

        return Text.literal(var3);
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
        this.pathField = new TextFieldWidget(this.textRenderer, this.leftX + 8, var3, var2, 20, Text.translatable("acvcape.field.path"));
        this.pathField.setMaxLength(512);
        this.pathField.setPlaceholder(this.hint("acvcape.field.path.hint", var2));
        this.addDrawableChild(this.pathField);
        this.pathNameField = new TextFieldWidget(this.textRenderer, this.leftX + 8, var3 + 26, var2, 20, Text.translatable("acvcape.field.name"));
        this.pathNameField.setMaxLength(64);
        this.pathNameField.setPlaceholder(this.hint("acvcape.field.name.hint", var2));
        this.addDrawableChild(this.pathNameField);
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("acvcape.button.import"), var1x -> this.importFromPath())
                .dimensions(this.leftX + 8, var3 + 52, var2, 20)
                .build()
        );
        this.browseButton = this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("acvcape.button.browse"), var1x -> this.browse()).dimensions(this.rightX + 8, var3, var2, 20).build()
        );
        this.browseNameField = new TextFieldWidget(this.textRenderer, this.rightX + 8, var3 + 26, var2, 20, Text.translatable("acvcape.field.name"));
        this.browseNameField.setMaxLength(64);
        this.browseNameField.setPlaceholder(this.hint("acvcape.field.name.hint", var2));
        this.addDrawableChild(this.browseNameField);
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("gui.cancel"), var1x -> this.client.setScreen(this.parent))
                .dimensions(var1 - 75, this.panelY + this.panelH + 8, 150, 20)
                .build()
        );
    }

    @Override
    protected void drawBackdrop(DrawContext var1, int var2, int var3, float var4) {
        super.drawBackdrop(var1, var2, var3, var4);

        for (int var8 : new int[]{this.leftX, this.rightX}) {
            var1.fill(var8, this.panelY, var8 + this.panelW, this.panelY + this.panelH, -535292635);
            P.border(var1, var8, this.panelY, this.panelW, this.panelH, -13358769);
        }
    }

    @Override
    protected void renderScreen(DrawContext var1, int var2, int var3, float var4) {
        this.renderWidgets(var1, var2, var3, var4);
        Ui.buttons(var1, this.textRenderer, this, Set.of());
        P.centered(var1, this.textRenderer, this.getTitle(), this.width / 2, 20, 16777215);
        this.drawPanel(var1, this.leftX, "acvcape.import.method.path", "acvcape.import.path.desc");
        this.drawPanel(var1, this.rightX, "acvcape.import.method.browse", "acvcape.import.browse.desc");
        if (!this.statusText.getString().isEmpty()) {
            P.centered(var1, this.textRenderer, this.statusText, this.width / 2, this.panelY + this.panelH + 34, 16755268);
        }
    }

    private void drawPanel(DrawContext var1, int var2, String var3, String var4) {
        P.centered(var1, this.textRenderer, Text.translatable(var3), var2 + this.panelW / 2, this.panelY + 7, 16777215);
        String var5 = this.textRenderer.trimToWidth(Text.translatable(var4).getString(), this.panelW - 16);
        P.centered(var1, this.textRenderer, var5, var2 + this.panelW / 2, this.panelY + 20, 11184810);
    }

    private void browse() {
        if (!this.dialogOpen) {
            this.dialogOpen = true;
            this.browseButton.active = false;
            this.statusText = this.waitingText;
            NativeFileDialog.pickPng(var1 -> this.doImport(var1, this.browseNameField.getText().trim()), var1 -> {
                this.dialogOpen = false;
                this.browseButton.active = true;
                if (var1) {
                    this.statusText = Text.translatable("acvcape.error.dialog_failed");
                } else if (this.statusText == this.waitingText) {
                    this.statusText = Text.empty();
                }
            });
        }
    }

    private void importFromPath() {
        String var1 = this.pathField.getText().trim();
        if (var1.length() >= 2 && var1.startsWith("\"") && var1.endsWith("\"")) {
            var1 = var1.substring(1, var1.length() - 1).trim();
        }

        if (var1.isEmpty()) {
            this.statusText = Text.translatable("acvcape.error.no_path");
        } else {
            Path var2;
            try {
                var2 = Path.of(var1);
            } catch (Exception var4) {
                this.statusText = Text.translatable("acvcape.error.import_failed");
                return;
            }

            this.doImport(var2, this.pathNameField.getText().trim());
        }
    }

    private void doImport(Path var1, String var2) {
        if (this.client != null && this.client.currentScreen == this) {
            try {
                CapeChangerClient var3 = CapeChangerClient.instance();
                CustomCape var4 = var3.getManager().importFromFile(var1, var2);
                CapeSelection var5 = CapeSelection.customSelection(var4.hash());
                var3.getManager().setActiveSelection(var5);
                var3.markCommitPending();
                this.client.setScreen(this.parent);
            } catch (CapeStorage.InvalidCapeException var6) {
                this.statusText = Text.literal(var6.getMessage());
            } catch (Exception var7) {
                CapeChanger.LOGGER.error("Falha ao importar capa", var7);
                this.statusText = Text.translatable("acvcape.error.import_failed");
            }
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void removed() {
        super.removed();
    }
}
