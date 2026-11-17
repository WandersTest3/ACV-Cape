package max.panda.capechanger.g5.mixin;

import max.panda.capechanger.g5.platform.EmissiveRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = ElytraFeatureRenderer.class, priority = 1500, remap = false)
public abstract class ElytraFeatureMixin {
    @Shadow(remap = false)
    private ElytraEntityModel field_4852;

    @Inject(
        method = "method_17161(Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;ILnet/minecraft/class_10034;FF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22909()V", remap = false),
        remap = false,
        require = 0
    )
    private void acvcape$emissiveElytra(
        MatrixStack var1, OrderedRenderCommandQueue var2, int var3, BipedEntityRenderState var4, float var5, float var6, CallbackInfo var7
    ) {
        EmissiveRenderer.submitElytra(this.field_4852, var4, var1, var2, EmissiveRenderer.elytraTexture(var4));
    }
}
