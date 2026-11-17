package max.panda.capechanger.g1.platform;

import max.panda.capechanger.g1.CapeChangerClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
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

    public static void renderCape(PlayerEntityModel<?> var0, MatrixStack var1, VertexConsumerProvider var2, AbstractClientPlayerEntity var3, int var4) {
        try {
            SkinTextures var5 = var3.getSkinTextures();
            Identifier var6 = emissiveFor(var5 == null ? null : var5.capeTexture());
            if (var6 == null) {
                return;
            }

            var0.renderCape(var1, var2.getBuffer(RenderLayer.getEntityTranslucentEmissive(var6)), 15728880, var4);
        } catch (Throwable var7) {
        }
    }

    public static VertexConsumer dim(VertexConsumer var0, AbstractClientPlayerEntity var1) {
        try {
            if (var1 instanceof PreviewPlayerEntity) {
                SkinTextures var2 = var1.getSkinTextures();
                if (var2 != null && emissiveFor(var2.capeTexture()) != null) {
                    return new HalfConsumer(var0);
                }
            }
        } catch (Throwable var3) {
        }

        return var0;
    }

    public static void renderElytra(ElytraEntityModel<?> var0, MatrixStack var1, VertexConsumerProvider var2, LivingEntity var3, int var4) {
        if (var3 instanceof AbstractClientPlayerEntity) {
            try {
                SkinTextures var5 = ((AbstractClientPlayerEntity)var3).getSkinTextures();
                if (var5 == null) {
                    return;
                }

                Identifier var6 = var5.elytraTexture() != null ? var5.elytraTexture() : var5.capeTexture();
                Identifier var7 = emissiveFor(var6);
                if (var7 == null) {
                    return;
                }

                var0.render(var1, var2.getBuffer(RenderLayer.getArmorCutoutNoCull(var7)), 15728880, var4);
            } catch (Throwable var8) {
            }
        }
    }
}
