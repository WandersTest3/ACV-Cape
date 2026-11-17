package max.panda.capechanger.g1.mixin;

import max.panda.capechanger.g1.platform.PreviewPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = EntityRenderer.class, remap = false)
public abstract class PreviewLabelMixin {
    @Inject(method = "method_3921(Lnet/minecraft/class_1297;)Z", at = @At(value = "HEAD", remap = false), cancellable = true, remap = false, require = 0)
    private void acvcape$hidePreviewLabel(Entity var1, CallbackInfoReturnable<Boolean> var2) {
        if (var1 instanceof PreviewPlayerEntity) {
            var2.setReturnValue(false);
        }
    }
}
