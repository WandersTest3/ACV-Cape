package max.panda.capechanger.g5.platform;

import max.panda.capechanger.g5.CapeChangerClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
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

    public static <S> void submit(Model<? super S> var0, S var1, MatrixStack var2, OrderedRenderCommandQueue var3, Identifier var4) {
        try {
            Identifier var5 = emissiveFor(var4);
            if (var0 == null || var1 == null || var5 == null) {
                return;
            }

            if (var1 instanceof PlayerEntityRenderState var6 && PreviewPlayerEntity.IDS.contains(var6.id)) {
                var3.submitModel(var0, var1, var2, RenderLayer.getEntityTranslucent(var4), 15728880, OverlayTexture.DEFAULT_UV, Integer.MIN_VALUE, null, 0, null);
            }

            var3.submitModel(var0, var1, var2, RenderLayer.getEntityTranslucentEmissive(var5), 15728880, OverlayTexture.DEFAULT_UV, 0, null);
        } catch (Throwable var7) {
        }
    }

    public static <S> void submitElytra(Model<? super S> var0, S var1, MatrixStack var2, OrderedRenderCommandQueue var3, Identifier var4) {
        try {
            Identifier var5 = emissiveFor(var4);
            if (var0 == null || var1 == null || var5 == null) {
                return;
            }

            var3.getBatchingQueue(8).submitModel(var0, var1, var2, RenderLayer.getArmorCutoutNoCull(var5), 15728880, OverlayTexture.DEFAULT_UV, 0, null);
        } catch (Throwable var6) {
        }
    }

    public static Identifier elytraTexture(BipedEntityRenderState var0) {
        return var0 instanceof PlayerEntityRenderState ? P.elytraTexture(((PlayerEntityRenderState)var0).skinTextures) : null;
    }
}
