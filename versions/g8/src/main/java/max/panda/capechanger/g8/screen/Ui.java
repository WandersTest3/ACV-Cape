package max.panda.capechanger.g8.screen;

import java.util.Set;
import max.panda.capechanger.g8.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public final class Ui {
    public static final int BACKGROUND = -938800878;
    public static final int HEADER = -15462113;
    public static final int ACCENT = -7643914;
    public static final int GOLD = -1003472;
    public static final int PANEL = -535292635;
    public static final int PANEL_EDGE = -13358769;
    public static final int PREVIEW_TOP = -14016192;
    public static final int PREVIEW_BOTTOM = -15462110;
    public static final int TILE = -14804436;
    public static final int TILE_HOVER = -14015680;
    public static final int TILE_EDGE = -12963502;
    public static final int TEXT_DIM = -6251340;
    private static final int BUTTON = -14738642;
    private static final int BUTTON_EDGE = -12963502;
    private static final int BUTTON_HOVER = -13884347;
    private static final int BUTTON_SELECTED = -14016699;
    private static final int BUTTON_DISABLED = -15264481;
    private static final int BUTTON_DISABLED_EDGE = -14014920;
    private static final int TEXT_DISABLED = -10593680;
    private static final int SLIDER_TRACK = -14016699;
    private static final int SLIDER_FILL = -9623335;
    private static final int SLIDER_FILL_HOVER = -8635667;
    private static final int SLIDER_KNOB = -3885571;

    private Ui() {
    }

    public static void buttons(GuiGraphicsExtractor var0, Font var1, Screen var2, Set<AbstractWidget> var3) {
        P.pushButtons(var0);

        for (GuiEventListener var5 : var2.children()) {
            if (var5 instanceof AbstractWidget var6 && var6.visible && !(var6 instanceof EditBox)) {
                if (var6 instanceof IntSlider var7) {
                    slider(var0, var1, var6, var7.sliderValue());
                } else if (var6 instanceof AnimationSpeedSlider var8) {
                    slider(var0, var1, var6, var8.sliderValue());
                } else {
                    button(var0, var1, var6, var3.contains(var6));
                }
            }
        }

        P.popButtons(var0);
    }

    private static void button(GuiGraphicsExtractor var0, Font var1, AbstractWidget var2, boolean var3) {
        int var4 = var2.getX();
        int var5 = var2.getY();
        int var6 = var2.getWidth();
        int var7 = var2.getHeight();
        boolean var8 = var2.active && var2.isHovered();
        int var9;
        int var10;
        int var11;
        if (var3) {
            var9 = var8 ? -13884347 : -14016699;
            var10 = -1003472;
            var11 = -1003472;
        } else if (!var2.active) {
            var9 = -15264481;
            var10 = -14014920;
            var11 = -10593680;
        } else if (var8) {
            var9 = -13884347;
            var10 = -7643914;
            var11 = -1;
        } else {
            var9 = -14738642;
            var10 = -12963502;
            var11 = -1;
        }

        var0.fill(var4, var5, var4 + var6, var5 + var7, var9);
        P.border(var0, var4, var5, var6, var7, var10);
        label(var0, var1, var2, var11);
    }

    private static void slider(GuiGraphicsExtractor var0, Font var1, AbstractWidget var2, double var3) {
        int var5 = var2.getX();
        int var6 = var2.getY();
        int var7 = var2.getWidth();
        int var8 = var2.getHeight();
        boolean var9 = var2.active && var2.isHovered();
        int var10 = var5 + 1 + (int)Math.round((var7 - 2) * Math.max(0.0, Math.min(1.0, var3)));
        var0.fill(var5, var6, var5 + var7, var6 + var8, var2.active ? -14016699 : -15264481);
        if (var2.active) {
            var0.fill(var5 + 1, var6 + 1, var10, var6 + var8 - 1, var9 ? -8635667 : -9623335);
            var0.fill(Math.max(var5 + 1, var10 - 2), var6 + 1, Math.min(var5 + var7 - 1, var10), var6 + var8 - 1, -3885571);
        }

        P.border(var0, var5, var6, var7, var8, var9 ? -7643914 : (var2.active ? -12963502 : -14014920));
        label(var0, var1, var2, var2.active ? -1 : -10593680);
    }

    private static void label(GuiGraphicsExtractor var0, Font var1, AbstractWidget var2, int var3) {
        String var4 = var2.getMessage().getString();
        int var5 = Math.max(4, var2.getWidth() - 6);
        if (var1.width(var4) > var5) {
            var4 = var1.plainSubstrByWidth(var4, var5);
        }

        P.centered(var0, var1, var4, var2.getX() + var2.getWidth() / 2, var2.getY() + (var2.getHeight() - 8) / 2, var3);
    }
}
