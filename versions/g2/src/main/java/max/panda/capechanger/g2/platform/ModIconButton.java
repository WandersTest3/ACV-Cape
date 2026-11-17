package max.panda.capechanger.g2.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class ModIconButton extends ButtonWidget {
    private static final Identifier ICON = Identifier.of("acvcape", "icon.png");

    public ModIconButton(int var1, int var2, PressAction var3) {
        super(var1, var2, 20, 20, Text.translatable("screen.acvcape.title"), var3, DEFAULT_NARRATION_SUPPLIER);
        this.setTooltip(Tooltip.of(Text.translatable("tooltip.acvcape.pause_button")));
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        Text var5 = this.getMessage();
        this.setMessage(Text.empty());
        super.renderWidget(context, mouseX, mouseY, delta);
        this.setMessage(var5);
        P.texture(context, ICON, this.getX() + 2, this.getY() + 2, 16, 16, 0.0F, 0.0F, 256, 256, 256, 256);
    }
}
