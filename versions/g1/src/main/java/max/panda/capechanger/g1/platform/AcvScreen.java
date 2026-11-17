package max.panda.capechanger.g1.platform;

import max.panda.capechanger.g1.screen.MenuScale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public abstract class AcvScreen extends Screen {
    private boolean scaled;

    protected AcvScreen(Text var1) {
        super(var1);
    }

    protected void applyMenuScale() {
        Window var1 = this.client.getWindow();
        var1.setScaleFactor(MenuScale.target(var1.getFramebufferWidth(), var1.getFramebufferHeight()));
        this.width = var1.getScaledWidth();
        this.height = var1.getScaledHeight();
        this.scaled = true;
    }

    public static void restoreMenuScale(MinecraftClient var0) {
        Window var1 = var0.getWindow();
        int var2 = var0.options.getGuiScale().getValue();
        var1.setScaleFactor(var1.calculateScaleFactor(var2, var0.forcesUnicodeFont()));
    }

    protected void rescale() {
        this.clearAndInit();
    }

    protected void drawBackdrop(DrawContext var1, int var2, int var3, float var4) {
        super.renderBackground(var1, var2, var3, var4);
    }

    protected void renderScreen(DrawContext var1, int var2, int var3, float var4) {
        this.renderWidgets(var1, var2, var3, var4);
    }

    protected final void renderWidgets(DrawContext var1, int var2, int var3, float var4) {
        super.render(var1, var2, var3, var4);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.drawBackdrop(context, mouseX, mouseY, delta);
        this.renderScreen(context, mouseX, mouseY, delta);
    }

    protected boolean onMouseDown(double var1, double var3, int var5) {
        return this.superMouseDown(var1, var3, var5);
    }

    protected boolean onMouseUp(double var1, double var3, int var5) {
        return this.superMouseUp(var1, var3, var5);
    }

    protected boolean onMouseDrag(double var1, double var3, int var5, double var6, double var8) {
        return this.superMouseDrag(var1, var3, var5, var6, var8);
    }

    protected boolean onScroll(double var1, double var3, double var5, double var7) {
        return this.superScroll(var1, var3, var5, var7);
    }

    protected boolean onKey(int var1, int var2, int var3) {
        return this.superKey(var1, var2, var3);
    }

    protected final boolean superMouseDown(double var1, double var3, int var5) {
        return super.mouseClicked(var1, var3, var5);
    }

    protected final boolean superMouseUp(double var1, double var3, int var5) {
        return super.mouseReleased(var1, var3, var5);
    }

    protected final boolean superMouseDrag(double var1, double var3, int var5, double var6, double var8) {
        return super.mouseDragged(var1, var3, var5, var6, var8);
    }

    protected final boolean superScroll(double var1, double var3, double var5, double var7) {
        return super.mouseScrolled(var1, var3, var5, var7);
    }

    protected final boolean superKey(int var1, int var2, int var3) {
        return super.keyPressed(var1, var2, var3);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return this.onMouseDown(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.onMouseUp(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.onMouseDrag(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.onScroll(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.onKey(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        super.removed();
        if (this.scaled) {
            this.scaled = false;
            restoreMenuScale(this.client);
        }
    }
}
