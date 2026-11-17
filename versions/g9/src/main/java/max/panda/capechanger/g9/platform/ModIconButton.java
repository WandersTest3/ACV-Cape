package max.panda.capechanger.g9.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button.OnPress;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class ModIconButton extends Button {
    private static final Identifier ICON = Identifier.fromNamespaceAndPath("acvcape", "icon.png");

    public ModIconButton(int var1, int var2, OnPress var3) {
        super(var1, var2, 20, 20, Component.translatable("screen.acvcape.title"), var3, DEFAULT_NARRATION);
        this.setTooltip(Tooltip.create(Component.translatable("tooltip.acvcape.pause_button")));
    }

    @Override
    public void extractContents(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
        P.texture(var1, ICON, this.getX() + 2, this.getY() + 2, 16, 16, 0.0F, 0.0F, 256, 256, 256, 256);
    }
}
