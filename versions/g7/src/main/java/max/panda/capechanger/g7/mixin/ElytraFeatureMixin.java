package max.panda.capechanger.g7.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import max.panda.capechanger.g7.platform.EmissiveRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.object.equipment.ElytraModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = WingsLayer.class, priority = 1500, remap = false)
public abstract class ElytraFeatureMixin {
    @Shadow(remap = false)
    private ElytraModel elytraModel;

    @Inject(
        method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V",
        at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", remap = false),
        remap = false,
        require = 0
    )
    private void acvcape$emissiveElytra(PoseStack var1, SubmitNodeCollector var2, int var3, HumanoidRenderState var4, float var5, float var6, CallbackInfo var7) {
        EmissiveRenderer.submitElytra(this.elytraModel, var4, var1, var2, EmissiveRenderer.elytraTexture(var4));
    }
}
