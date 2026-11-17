package max.panda.capechanger.wavey.g5;

import dev.tr7zw.transition.mc.entitywrapper.PlayerWrapper;
import dev.tr7zw.waveycapes.render.CapeRenderer;
import dev.tr7zw.waveycapes.render.CustomCapeRenderer;
import max.panda.capechanger.g5.platform.DarkConsumer;
import max.panda.capechanger.g5.platform.EmissiveRenderer;
import max.panda.capechanger.g5.platform.PreviewPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
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
    private void acvcape$emissiveWavey(OrderedRenderCommandQueue var1, PlayerWrapper var2, MatrixStack var3, int var4, float var5, CallbackInfo var6) {
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
                var1.getBatchingQueue(8).submitCustom(var3, RenderLayer.getEntityTranslucent(var7), (var5x, var6x) -> {
                    MatrixStack var7x = new MatrixStack();
                    var7x.peek().copy(var5x);
                    var10.render(var2, var9, new DarkConsumer(var6x), var7x, var4, var5);
                });
            }

            var1.getBatchingQueue(9).submitCustom(var3, RenderLayer.getEntityTranslucentEmissive(var8), (var4x, var5x) -> {
                MatrixStack var6x = new MatrixStack();
                var6x.peek().copy(var4x);
                var10.render(var2, var9, var5x, var6x, 15728880, var5);
            });
        } catch (Throwable var11) {
        }
    }
}
