package max.panda.capechanger.g3.platform;

import max.panda.capechanger.g3.CapeChangerClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class EmissiveRenderer {
    public static final int FULL_BRIGHT = 15728880;

    private EmissiveRenderer() {
    }

    public static Identifier emissiveFor(Identifier var0) {
        CapeChangerClient var1 = CapeChangerClient.instance();
        return var1 != null && var1.getManager() != null && var0 != null ? var1.getManager().emissiveFor(var0) : null;
    }

    public static void renderCape(Model var0, MatrixStack var1, VertexConsumerProvider var2, PlayerEntityRenderState var3) {
        try {
            if (var0 == null || var3 == null) {
                return;
            }

            Identifier var4 = P.capeTexture(var3.skinTextures);
            if (var4 != null && PreviewPlayerEntity.IDS.contains(var3.id) && emissiveFor(var4) != null) {
                var0.render(var1, var2.getBuffer(RenderLayer.getEntityTranslucent(var4)), 15728880, OverlayTexture.DEFAULT_UV, Integer.MIN_VALUE);
            }

            render(var0, var1, var2, var4);
        } catch (Throwable var5) {
        }
    }

    public static void render(Model var0, MatrixStack var1, VertexConsumerProvider var2, Identifier var3) {
        try {
            Identifier var4 = emissiveFor(var3);
            if (var0 == null || var4 == null) {
                return;
            }

            var0.render(var1, var2.getBuffer(RenderLayer.getEntityTranslucentEmissive(var4)), 15728880, OverlayTexture.DEFAULT_UV);
        } catch (Throwable var5) {
        }
    }

    public static void renderElytra(Model var0, MatrixStack var1, VertexConsumerProvider var2, Identifier var3) {
        try {
            Identifier var4 = emissiveFor(var3);
            if (var0 == null || var4 == null) {
                return;
            }

            var0.render(var1, var2.getBuffer(RenderLayer.getArmorCutoutNoCull(var4)), 15728880, OverlayTexture.DEFAULT_UV);
        } catch (Throwable var5) {
        }
    }

    public static Identifier elytraTexture(BipedEntityRenderState var0) {
        return var0 instanceof PlayerEntityRenderState ? P.elytraTexture(((PlayerEntityRenderState)var0).skinTextures) : null;
    }
}
