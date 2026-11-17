package max.panda.capechanger.wavey.g9;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.tr7zw.transition.mc.entitywrapper.PlayerWrapper;
import dev.tr7zw.waveycapes.render.CapeRenderer;
import dev.tr7zw.waveycapes.render.CustomCapeRenderer;
import max.panda.capechanger.g9.platform.DarkConsumer;
import max.panda.capechanger.g9.platform.EmissiveRenderer;
import max.panda.capechanger.g9.platform.PreviewPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
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
    private void acvcape$emissiveWavey(SubmitNodeCollector var1, PlayerWrapper var2, PoseStack var3, int var4, float var5, CallbackInfo var6) {
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

            CustomCapeRenderer var10 = this.customCapeRenderer;
            if (var2.getRenderState() != null && PreviewPlayerEntity.IDS.contains(var2.getRenderState().id)) {
                var1.order(8).submitCustomGeometry(var3, RenderTypes.entityTranslucent(var7), (var5x, var6x) -> {
                    PoseStack var7x = new PoseStack();
                    var7x.last().set(var5x);
                    var10.render(var2, var9, new DarkConsumer(var6x), var7x, var4, var5);
                });
            }

            var1.order(9).submitCustomGeometry(var3, RenderTypes.entityTranslucentEmissive(var8), (var4x, var5x) -> {
                PoseStack var6x = new PoseStack();
                var6x.last().set(var4x);
                var10.render(var2, var9, var5x, var6x, 15728880, var5);
            });
        } catch (Throwable var11) {
        }
    }
}
