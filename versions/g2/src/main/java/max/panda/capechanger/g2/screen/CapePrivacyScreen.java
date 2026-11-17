package max.panda.capechanger.g2.screen;

import java.util.Set;
import max.panda.capechanger.g2.CapeChangerClient;
import max.panda.capechanger.g2.cape.CapeConfig;
import max.panda.capechanger.g2.platform.AcvScreen;
import max.panda.capechanger.g2.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class CapePrivacyScreen extends AcvScreen {
    private static final int ROW_H = 56;
    private final Screen parent;
    private ButtonWidget inventoryButton;
    private ButtonWidget useButton;
    private ButtonWidget invisibleButton;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public CapePrivacyScreen(Screen var1) {
        super(Text.translatable("screen.acvcape.privacy"));
        this.parent = var1;
    }

    @Override
    protected void init() {
        this.applyMenuScale();
        this.panelW = Math.min(320, this.width - 32);
        this.panelH = 184;
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = Math.max(30, (this.height - this.panelH) / 2 - 12);
        byte var1 = 70;
        int var2 = this.panelX + this.panelW - var1 - 8;
        CapeConfig var3 = CapeChangerClient.instance().getConfig();
        this.inventoryButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), var2x -> {
            CapeChangerClient.instance().setAllowInventory(!var3.isAllowInventory());
            this.refreshLabels();
        }).dimensions(var2, this.rowY(0) + 16, var1, 20).build());
        this.useButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), var2x -> {
            CapeChangerClient.instance().setAllowUse(!var3.isAllowUse());
            this.refreshLabels();
        }).dimensions(var2, this.rowY(1) + 16, var1, 20).build());
        this.invisibleButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), var2x -> {
            CapeChangerClient.instance().setInvisible(!var3.isInvisible());
            this.refreshLabels();
        }).dimensions(var2, this.rowY(2) + 16, var1, 20).build());
        this.addDrawableChild(
            ButtonWidget.builder(ScreenTexts.DONE, var1x -> this.close()).dimensions(this.width / 2 - 75, this.panelY + this.panelH + 8, 150, 20).build()
        );
        this.refreshLabels();
    }

    private int rowY(int var1) {
        return this.panelY + 8 + var1 * 56;
    }

    private void refreshLabels() {
        CapeConfig var1 = CapeChangerClient.instance().getConfig();
        this.inventoryButton.setMessage(ScreenTexts.onOrOff(!var1.isAllowInventory()));
        this.useButton.setMessage(ScreenTexts.onOrOff(!var1.isAllowUse()));
        this.invisibleButton.setMessage(ScreenTexts.onOrOff(var1.isInvisible()));
    }

    @Override
    protected void drawBackdrop(DrawContext var1, int var2, int var3, float var4) {
        var1.fill(0, 0, this.width, this.height, -938800878);
        var1.fill(0, 0, this.width, 22, -15462113);
        var1.fill(0, 22, this.width, 23, -7643914);
        var1.fill(this.panelX, this.panelY, this.panelX + this.panelW, this.panelY + this.panelH, -535292635);
        P.border(var1, this.panelX, this.panelY, this.panelW, this.panelH, -13358769);
    }

    @Override
    protected void renderScreen(DrawContext var1, int var2, int var3, float var4) {
        this.renderWidgets(var1, var2, var3, var4);
        Ui.buttons(var1, this.textRenderer, this, Set.of());
        P.centered(var1, this.textRenderer, this.title, this.width / 2, 7, -1);
        String[] var5 = new String[]{"privacy.acvcape.inventory", "privacy.acvcape.use", "privacy.acvcape.invisible"};
        int var6 = this.panelW - 70 - 24;

        for (int var7 = 0; var7 < var5.length; var7++) {
            int var8 = this.rowY(var7);
            if (var7 > 0) {
                var1.fill(this.panelX + 8, var8 - 4, this.panelX + this.panelW - 8, var8 - 3, -13358769);
            }

            P.text(var1, this.textRenderer, Text.translatable(var5[var7]), this.panelX + 10, var8 + 4, -1);
            String var9 = Text.translatable(var5[var7] + ".desc").getString();
            int var10 = 0;
            StringBuilder var11 = new StringBuilder();

            for (String var15 : var9.split(" ")) {
                String var16 = var11.length() == 0 ? var15 : var11 + " " + var15;
                if (this.textRenderer.getWidth(var16) > var6 && var11.length() > 0) {
                    P.text(var1, this.textRenderer, var11.toString(), this.panelX + 10, var8 + 16 + var10 * 10, -6250326);
                    var10++;
                    var11 = new StringBuilder(var15);
                } else {
                    var11 = new StringBuilder(var16);
                }

                if (var10 >= 3) {
                    break;
                }
            }

            if (var10 < 3 && var11.length() > 0) {
                P.text(var1, this.textRenderer, var11.toString(), this.panelX + 10, var8 + 16 + var10 * 10, -6250326);
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
