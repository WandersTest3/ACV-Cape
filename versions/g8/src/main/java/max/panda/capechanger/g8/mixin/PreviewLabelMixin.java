package max.panda.capechanger.g8.mixin;

import max.panda.capechanger.g8.platform.PreviewPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(value = EntityRenderer.class, remap = false)
public abstract class PreviewLabelMixin {
    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z", at = @At(value = "HEAD", remap = false), cancellable = true, remap = false, require = 0)
    private void acvcape$hidePreviewLabel(Entity var1, double var2, CallbackInfoReturnable<Boolean> var4) {
        if (var1 instanceof PreviewPlayerEntity) {
            var4.setReturnValue(false);
        }
    }
}
