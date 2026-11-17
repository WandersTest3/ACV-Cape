package max.panda.capechanger.g4.platform;

import max.panda.capechanger.g4.screen.MenuScale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

@Environment(EnvType.CLIENT)
public abstract class AcvScreen extends Screen {
    private static float activeFactor = 1.0F;
    private float factor = 1.0F;

    protected AcvScreen(Text var1) {
        super(var1);
    }

    public static float activeFactor() {
        MinecraftClient var0 = MinecraftClient.getInstance();
        return var0 != null && var0.currentScreen instanceof AcvScreen ? activeFactor : 1.0F;
    }

    protected void applyMenuScale() {
        Window var1 = this.client.getWindow();
        int var2 = Math.max(1, var1.getScaleFactor());
        double var3 = MenuScale.target(var1.getFramebufferWidth(), var1.getFramebufferHeight());
        this.factor = (float)(var3 / var2);
        this.width = (int)Math.floor((double)var1.getScaledWidth() / this.factor);
        this.height = (int)Math.floor((double)var1.getScaledHeight() / this.factor);
        activeFactor = this.factor;
    }

    public static void restoreMenuScale(MinecraftClient var0) {
        activeFactor = 1.0F;
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

    private double virtual(double var1) {
        return var1 / this.factor;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        activeFactor = this.factor;
        int var5 = (int)this.virtual(mouseX);
        int var6 = (int)this.virtual(mouseY);
        Matrix3x2fStack var7 = context.getMatrices();
        var7.pushMatrix();
        var7.scale(this.factor, this.factor);
        this.drawBackdrop(context, var5, var6, deltaTicks);
        this.renderScreen(context, var5, var6, deltaTicks);
        P.widgetTips(context, this.textRenderer, this, var5, var6);
        var7.popMatrix();
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
        return this.onMouseDown(this.virtual(mouseX), this.virtual(mouseY), button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return this.onMouseUp(this.virtual(mouseX), this.virtual(mouseY), button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return this.onMouseDrag(this.virtual(mouseX), this.virtual(mouseY), button, this.virtual(deltaX), this.virtual(deltaY));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.onScroll(this.virtual(mouseX), this.virtual(mouseY), horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.onKey(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        super.removed();
        activeFactor = 1.0F;
    }
}
