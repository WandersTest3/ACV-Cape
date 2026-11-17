package max.panda.capechanger;

import java.util.Optional;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class AcvGroup {
    private static String current;

    private AcvGroup() {
    }

    public static String current() {
        if (current == null) {
            current = resolve(minecraftVersion());
        }

        return current;
    }

    public static String prefix() {
        return "max.panda.capechanger." + current();
    }

    private static String minecraftVersion() {
        Optional var0 = FabricLoader.getInstance().getModContainer("minecraft");
        if (var0.isEmpty()) {
            throw new IllegalStateException("ACV Cape: Minecraft version not found");
        } else {
            return ((ModContainer)var0.get()).getMetadata().getVersion().getFriendlyString();
        }
    }

    static String resolve(String var0) {
        String[] var1 = var0.split("[^0-9]+");
        int var2 = 0;
        int var3 = 0;
        int var4 = 0;
        int var5 = 0;

        for (String var9 : var1) {
            if (!var9.isEmpty()) {
                int var10 = Integer.parseInt(var9);
                if (var5 == 0) {
                    var2 = var10;
                } else if (var5 == 1) {
                    var3 = var10;
                } else {
                    if (var5 != 2) {
                        break;
                    }

                    var4 = var10;
                }

                var5++;
            }
        }

        if (var2 == 26) {
            if (var3 <= 1) {
                return "g7";
            }

            return var3 == 2 ? "g8" : "g9";
        } else if (var2 != 1 || var3 != 21) {
            throw new IllegalStateException("ACV Cape: unsupported Minecraft version " + var0);
        } else if (var4 <= 1) {
            return "g1";
        } else if (var4 <= 4) {
            return "g2";
        } else if (var4 == 5) {
            return "g3";
        } else if (var4 <= 8) {
            return "g4";
        } else {
            return var4 <= 10 ? "g5" : "g6";
        }
    }
}
