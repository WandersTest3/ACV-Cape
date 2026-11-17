package max.panda.capechanger.g2.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import max.panda.capechanger.g2.platform.EmissiveRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(value = CapeFeatureRenderer.class, priority = 1500, remap = false)
public abstract class CapeFeatureMixin {
    @Shadow(remap = false)
    private BipedEntityModel<PlayerEntityRenderState> field_53206;

    @WrapOperation(
        method = "method_4177(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_10055;FF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1921;method_23572(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", remap = false),
        remap = false,
        require = 0
    )
    private RenderLayer acvcape$translucentCape(Identifier var1, Operation<RenderLayer> var2) {
        return RenderLayer.getEntityTranslucent(var1);
    }

    @Inject(
        method = "method_4177(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_10055;FF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22909()V", remap = false),
        remap = false,
        require = 0
    )
    private void acvcape$emissiveCape(
        MatrixStack var1, VertexConsumerProvider var2, int var3, PlayerEntityRenderState var4, float var5, float var6, CallbackInfo var7
    ) {
        EmissiveRenderer.renderCape(this.field_53206, var1, var2, var4);
    }
}
