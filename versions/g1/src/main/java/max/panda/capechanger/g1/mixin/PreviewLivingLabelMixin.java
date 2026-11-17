package max.panda.capechanger.g1.mixin;

import max.panda.capechanger.g1.platform.PreviewPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = LivingEntityRenderer.class, remap = false)
public abstract class PreviewLivingLabelMixin {
    @Inject(method = "method_4055(Lnet/minecraft/class_1309;)Z", at = @At(value = "HEAD", remap = false), cancellable = true, remap = false, require = 0)
    private void acvcape$hidePreviewLabel(LivingEntity var1, CallbackInfoReturnable<Boolean> var2) {
        if (var1 instanceof PreviewPlayerEntity) {
            var2.setReturnValue(false);
        }
    }
}
