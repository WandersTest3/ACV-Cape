package max.panda.capechanger.g2.screen;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import max.panda.capechanger.g2.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
public final class IntSlider extends SliderWidget {
    private final int min;
    private final int max;
    private final int step;
    private final IntFunction<Text> label;
    private final IntConsumer onChange;

    public IntSlider(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, IntFunction<Text> var9, IntConsumer var10) {
        super(var1, var2, var3, var4, Text.empty(), toSlider(var8, var5, var6));
        this.min = var5;
        this.max = var6;
        this.step = Math.max(1, var7);
        this.label = var9;
        this.onChange = var10;
        this.updateMessage();
    }

    private static double toSlider(int var0, int var1, int var2) {
        return Math.max(0.0, Math.min(1.0, (double)(var0 - var1) / Math.max(1, var2 - var1)));
    }

    public IntSlider withTooltip(Text var1) {
        P.tooltip(this, var1);
        return this;
    }

    public double sliderValue() {
        return this.value;
    }

    public int current() {
        double var1 = this.min + this.value * (this.max - this.min);
        int var3 = (int)Math.round(var1 / this.step) * this.step;
        return Math.max(this.min, Math.min(this.max, var3));
    }

    public void setCurrent(int var1) {
        this.value = toSlider(var1, this.min, this.max);
        this.updateMessage();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(this.label.apply(this.current()));
    }

    @Override
    protected void applyValue() {
        this.onChange.accept(this.current());
    }
}
