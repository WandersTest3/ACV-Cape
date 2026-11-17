package max.panda.capechanger.g9.screen;

import java.util.Locale;
import max.panda.capechanger.g9.cape.CapeManager;
import max.panda.capechanger.g9.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public final class AnimationSpeedSlider extends AbstractSliderButton {
    private static final float STEP = 0.05F;
    private final CapeManager manager;

    public AnimationSpeedSlider(int var1, int var2, int var3, int var4, CapeManager var5) {
        super(var1, var2, var3, var4, Component.empty(), toSlider(var5.getAnimationSpeed()));
        this.manager = var5;
        P.tooltip(this, Component.translatable("tooltip.acvcape.animation_speed"));
        this.updateMessage();
    }

    private static double toSlider(float var0) {
        return Math.max(0.0, Math.min(1.0, (var0 - 0.25) / 1.75));
    }

    public double sliderValue() {
        return this.value;
    }

    private float speed() {
        float var1 = 0.25F + (float)this.value * 1.75F;
        return Math.round(var1 / 0.05F) * 0.05F;
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.translatable("button.acvcape.animation_speed", String.format(Locale.ROOT, "%.2fx", this.speed())));
    }

    @Override
    protected void applyValue() {
        this.manager.setAnimationSpeed(this.speed());
    }
}
