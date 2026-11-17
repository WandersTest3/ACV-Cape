package max.panda.capechanger.g6.platform;

import max.panda.capechanger.g6.screen.MenuScale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import org.joml.Matrix3x2fStack;

@Environment(EnvType.CLIENT)
public abstract class AcvScreen extends Screen {
    private static float activeFactor = 1.0F;
    private float factor = 1.0F;
    private boolean lastDoubled;
    private int lastModifiers;

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

    private Click click(double var1, double var3, int var5) {
        return new Click(var1, var3, new MouseInput(var5, this.lastModifiers));
    }

    protected final boolean superMouseDown(double var1, double var3, int var5) {
        return super.mouseClicked(this.click(var1, var3, var5), this.lastDoubled);
    }

    protected final boolean superMouseUp(double var1, double var3, int var5) {
        return super.mouseReleased(this.click(var1, var3, var5));
    }

    protected final boolean superMouseDrag(double var1, double var3, int var5, double var6, double var8) {
        return super.mouseDragged(this.click(var1, var3, var5), var6, var8);
    }

    protected final boolean superScroll(double var1, double var3, double var5, double var7) {
        return super.mouseScrolled(var1, var3, var5, var7);
    }

    protected final boolean superKey(int var1, int var2, int var3) {
        return super.keyPressed(new KeyInput(var1, var2, var3));
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        this.lastDoubled = doubled;
        this.lastModifiers = click.buttonInfo().modifiers();
        return this.onMouseDown(this.virtual(click.x()), this.virtual(click.y()), click.button());
    }

    @Override
    public boolean mouseReleased(Click click) {
        this.lastModifiers = click.buttonInfo().modifiers();
        return this.onMouseUp(this.virtual(click.x()), this.virtual(click.y()), click.button());
    }

    @Override
    public boolean mouseDragged(Click click, double offsetX, double offsetY) {
        this.lastModifiers = click.buttonInfo().modifiers();
        return this.onMouseDrag(this.virtual(click.x()), this.virtual(click.y()), click.button(), this.virtual(offsetX), this.virtual(offsetY));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return this.onScroll(this.virtual(mouseX), this.virtual(mouseY), horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        return this.onKey(input.key(), input.scancode(), input.modifiers());
    }

    @Override
    public void removed() {
        super.removed();
        activeFactor = 1.0F;
    }
}
