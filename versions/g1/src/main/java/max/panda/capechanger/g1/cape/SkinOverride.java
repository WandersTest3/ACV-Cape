package max.panda.capechanger.g1.cape;

import java.util.UUID;
import max.panda.capechanger.g1.CapeChangerClient;
import max.panda.capechanger.g1.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class SkinOverride {
    private SkinOverride() {
    }

    public static SkinTextures apply(UUID var0, SkinTextures var1, String var2) {
        try {
            return compute(var0, var1, var2);
        } catch (Throwable var4) {
            return null;
        }
    }

    public static SkinTextures applyOrKeep(UUID var0, SkinTextures var1, String var2) {
        SkinTextures var3 = apply(var0, var1, var2);
        return var3 != null ? var3 : var1;
    }

    private static SkinTextures compute(UUID var0, SkinTextures var1, String var2) {
        CapeChangerClient var3 = CapeChangerClient.instance();
        if (var3 != null && var3.getManager() != null) {
            MinecraftClient var4 = MinecraftClient.getInstance();
            UUID var5 = var4 != null && var4.player != null ? var4.player.getUuid() : null;
            boolean var6 = var0 != null && var0.equals(var5);
            CapeSelection var7 = var6 ? var3.getManager().getActiveSelection() : (var0 == null ? null : var3.getRemoteSelection(var0));
            var3.logHook(var2, var6, var7, var1 != null);
            if (var1 == null || var7 == null || var7.isDefault()) {
                return null;
            }

            if (!var7.isNone()) {
                Identifier var8 = var3.getManager().resolveTexture(var7);
                if (var8 == null) {
                    var3.logUnresolved(var7);
                    return null;
                }

                if (var8.equals(P.capeTexture(var1)) && var8.equals(P.elytraTexture(var1))) {
                    return null;
                }

                var3.logApplied(var2, var7);
                return P.withCape(var1, var8, var8);
            } else {
                return P.capeTexture(var1) == null && P.elytraTexture(var1) == null ? null : P.withCape(var1, null, null);
            }
        } else {
            return null;
        }
    }
}
