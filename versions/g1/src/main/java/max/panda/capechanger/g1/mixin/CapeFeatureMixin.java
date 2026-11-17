package max.panda.capechanger.g1.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import max.panda.capechanger.g1.cape.SkinOverride;
import max.panda.capechanger.g1.platform.EmissiveRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = CapeFeatureRenderer.class, priority = 1500, remap = false)
public abstract class CapeFeatureMixin {
    @WrapOperation(
        method = "method_4177(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_1921;method_23572(Lnet/minecraft/class_2960;)Lnet/minecraft/class_1921;", remap = false),
        remap = false,
        require = 0
    )
    private RenderLayer acvcape$translucentCape(Identifier var1, Operation<RenderLayer> var2) {
        return RenderLayer.getEntityTranslucent(var1);
    }

    @ModifyExpressionValue(
        method = "method_4177(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_52814()Lnet/minecraft/class_8685;", remap = false),
        remap = false,
        require = 0
    )
    private SkinTextures acvcape$capeSkin(SkinTextures var1, @Local(argsOnly = true) AbstractClientPlayerEntity var2) {
        return SkinOverride.applyOrKeep(var2.getUuid(), var1, "layer");
    }

    @WrapOperation(
        method = "method_4177(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_742;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_591;method_2823(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;II)V", remap = false),
        remap = false,
        require = 0
    )
    private void acvcape$emissiveCape(
        PlayerEntityModel<?> var1,
        MatrixStack var2,
        VertexConsumer var3,
        int var4,
        int var5,
        Operation<Void> var6,
        @Local(argsOnly = true) VertexConsumerProvider var7,
        @Local(argsOnly = true) AbstractClientPlayerEntity var8
    ) {
        var6.call(new Object[]{var1, var2, EmissiveRenderer.dim(var3, var8), var4, var5});
        EmissiveRenderer.renderCape(var1, var2, var7, var8, var5);
    }
}
