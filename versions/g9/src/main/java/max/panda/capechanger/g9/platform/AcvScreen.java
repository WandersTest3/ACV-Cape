package max.panda.capechanger.g9.platform;

import com.mojang.blaze3d.platform.Window;
import max.panda.capechanger.g9.screen.MenuScale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

@Environment(EnvType.CLIENT)
public abstract class AcvScreen extends Screen {
    private static float activeFactor = 1.0F;
    private float factor = 1.0F;
    private boolean lastDoubled;
    private int lastModifiers;

    protected AcvScreen(Component var1) {
        super(var1);
    }

    public static float activeFactor() {
        Minecraft var0 = Minecraft.getInstance();
        return var0 != null && var0.gui.screen() instanceof AcvScreen ? activeFactor : 1.0F;
    }

    protected void applyMenuScale() {
        Window var1 = this.minecraft.getWindow();
        int var2 = Math.max(1, var1.getGuiScale());
        double var3 = MenuScale.target(var1.getWidth(), var1.getHeight());
        this.factor = (float)(var3 / var2);
        this.width = (int)Math.floor((double)var1.getGuiScaledWidth() / this.factor);
        this.height = (int)Math.floor((double)var1.getGuiScaledHeight() / this.factor);
        activeFactor = this.factor;
    }

    public static void restoreMenuScale(Minecraft var0) {
        activeFactor = 1.0F;
    }

    protected void rescale() {
        this.rebuildWidgets();
    }

    protected void drawBackdrop(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
        super.extractBackground(var1, var2, var3, var4);
    }

    protected void renderScreen(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
        this.renderWidgets(var1, var2, var3, var4);
    }

    protected final void renderWidgets(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
        super.extractRenderState(var1, var2, var3, var4);
    }

    private double virtual(double var1) {
        return var1 / this.factor;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
        activeFactor = this.factor;
        int var5 = (int)this.virtual(var2);
        int var6 = (int)this.virtual(var3);
        Matrix3x2fStack var7 = var1.pose();
        var7.pushMatrix();
        var7.scale(this.factor, this.factor);
        this.drawBackdrop(var1, var5, var6, var4);
        this.renderScreen(var1, var5, var6, var4);
        P.widgetTips(var1, this.font, this, var5, var6);
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

    private MouseButtonEvent click(double var1, double var3, int var5) {
        return new MouseButtonEvent(var1, var3, new MouseButtonInfo(var5, this.lastModifiers));
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
        return super.keyPressed(new KeyEvent(var1, var2, var3));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
        this.lastDoubled = var2;
        this.lastModifiers = var1.buttonInfo().modifiers();
        return this.onMouseDown(this.virtual(var1.x()), this.virtual(var1.y()), var1.button());
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent var1) {
        this.lastModifiers = var1.buttonInfo().modifiers();
        return this.onMouseUp(this.virtual(var1.x()), this.virtual(var1.y()), var1.button());
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
        this.lastModifiers = var1.buttonInfo().modifiers();
        return this.onMouseDrag(this.virtual(var1.x()), this.virtual(var1.y()), var1.button(), this.virtual(var2), this.virtual(var4));
    }

    @Override
    public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
        return this.onScroll(this.virtual(var1), this.virtual(var3), var5, var7);
    }

    @Override
    public boolean keyPressed(KeyEvent var1) {
        return this.onKey(var1.key(), var1.keycode(), var1.modifiers());
    }

    @Override
    public void removed() {
        super.removed();
        activeFactor = 1.0F;
    }
}
