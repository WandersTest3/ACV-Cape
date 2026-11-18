package max.panda.capechanger.g9.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import max.panda.capechanger.g9.CapeChangerClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public final class EmissiveRenderer {
    public static final int FULL_BRIGHT = 15728880;

    private EmissiveRenderer() {
    }

    public static Identifier emissiveFor(Identifier var0) {
        CapeChangerClient var1 = CapeChangerClient.instance();
        return var1 != null && var1.getManager() != null && var0 != null ? var1.getManager().emissiveFor(var0) : null;
    }

    public static <S> void submit(Model<? super S> var0, S var1, PoseStack var2, SubmitNodeCollector var3, Identifier var4) {
        try {
            Identifier var5 = emissiveFor(var4);
            if (var0 == null || var1 == null || var5 == null) {
                return;
            }

            if (var1 instanceof AvatarRenderState var6 && PreviewPlayerEntity.IDS.contains(var6.id)) {
                var3.submitModel(var0, var1, var2, RenderTypes.entityTranslucent(var4), 15728880, OverlayTexture.NO_OVERLAY, Integer.MIN_VALUE);
            }

            var3.submitModel(var0, var1, var2, RenderTypes.entityTranslucentEmissive(var5), 15728880, OverlayTexture.NO_OVERLAY, 0);
        } catch (Throwable var7) {
        }
    }

    public static <S> void submitElytra(Model<? super S> var0, S var1, PoseStack var2, SubmitNodeCollector var3, Identifier var4) {
        try {
            Identifier var5 = emissiveFor(var4);
            if (var0 == null || var1 == null || var5 == null) {
                return;
            }

            var3.order(8).submitModel(var0, var1, var2, RenderTypes.armorCutoutNoCull(var5), 15728880, OverlayTexture.NO_OVERLAY, 0);
        } catch (Throwable var6) {
        }
    }

    public static Identifier elytraTexture(HumanoidRenderState var0) {
        return var0 instanceof AvatarRenderState ? P.elytraTexture(((AvatarRenderState)var0).skin) : null;
    }
}
