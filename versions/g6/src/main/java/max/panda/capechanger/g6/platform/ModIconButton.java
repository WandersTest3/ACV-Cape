package max.panda.capechanger.g6.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class ModIconButton extends ButtonWidget {
    private static final Identifier ICON = Identifier.of("acvcape", "icon.png");

    public ModIconButton(int var1, int var2, PressAction var3) {
        super(var1, var2, 20, 20, net.minecraft.text.Text.translatable("screen.acvcape.title"), var3, DEFAULT_NARRATION_SUPPLIER);
        this.setTooltip(Tooltip.of(net.minecraft.text.Text.translatable("tooltip.acvcape.pause_button")));
    }

    @Override
    public void drawIcon(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        P.texture(context, ICON, this.getX() + 2, this.getY() + 2, 16, 16, 0.0F, 0.0F, 256, 256, 256, 256);
    }
}
