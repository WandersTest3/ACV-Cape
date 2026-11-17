package max.panda.capechanger.wavey.g3;

import dev.tr7zw.transition.mc.entitywrapper.PlayerWrapper;
import dev.tr7zw.waveycapes.render.CapeRenderer;
import dev.tr7zw.waveycapes.render.CustomCapeRenderer;
import max.panda.capechanger.g3.platform.DarkConsumer;
import max.panda.capechanger.g3.platform.EmissiveRenderer;
import max.panda.capechanger.g3.platform.PreviewPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(targets = "dev.tr7zw.waveycapes.render.CapeNodeCollector", remap = false)
public abstract class WaveyCapeMixin {
    @Shadow(remap = false)
    @Final
    private CustomCapeRenderer customCapeRenderer;

    @Shadow(remap = false)
    private CapeRenderer getCapeRenderer(PlayerWrapper var1) {
        return null;
    }

    @Inject(method = "submitCape", at = @At("TAIL"), remap = false, require = 0)
    private void acvcape$emissiveWavey(VertexConsumerProvider var1, PlayerWrapper var2, MatrixStack var3, int var4, float var5, CallbackInfo var6) {
        try {
            Identifier var7 = var2.getCapeTexture();
            Identifier var8 = EmissiveRenderer.emissiveFor(var7);
            if (var8 == null) {
                return;
            }

            CapeRenderer var9 = this.getCapeRenderer(var2);
            if (var9 == null) {
                return;
            }

            if (var2.getRenderState() != null && PreviewPlayerEntity.IDS.contains(var2.getRenderState().id)) {
                this.customCapeRenderer.render(var2, var9, new DarkConsumer(var1.getBuffer(RenderLayer.getEntityTranslucent(var7))), var3, var4, var5);
            }

            this.customCapeRenderer.render(var2, var9, var1.getBuffer(RenderLayer.getEntityTranslucentEmissive(var8)), var3, 15728880, var5);
        } catch (Throwable var10) {
        }
    }
}
