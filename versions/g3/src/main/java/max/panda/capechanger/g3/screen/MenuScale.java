package max.panda.capechanger.g3.screen;

import max.panda.capechanger.g3.CapeChangerClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class MenuScale {
    public static final double TARGET_SCALE = 2.2;
    public static final double MENU_FACTOR = 0.85;
    public static final double MIN_WIDTH = 480.0;
    public static final double MIN_HEIGHT = 270.0;
    public static final double MIN_SCALE = 0.25;

    private MenuScale() {
    }

    public static int percent() {
        CapeChangerClient var0 = CapeChangerClient.instance();
        return var0 == null ? 100 : var0.getConfig().getMenuScale();
    }

    public static double target(int var0, int var1) {
        double var2 = Math.min(var0 / 480.0, var1 / 270.0);
        double var4 = Math.min(2.2, var2) * 0.85;
        double var6 = var4 * percent() / 100.0;
        return Math.max(0.25, Math.min(Math.max(var4, var2), var6));
    }
}
