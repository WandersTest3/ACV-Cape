package max.panda.capechanger.g1.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import max.panda.capechanger.g1.cape.SkinOverride;
import max.panda.capechanger.g1.platform.EmissiveRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.model.ElytraEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = ElytraFeatureRenderer.class, priority = 1500, remap = false)
public abstract class ElytraFeatureMixin {
    @ModifyExpressionValue(
        method = "method_17161(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_1309;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_742;method_52814()Lnet/minecraft/class_8685;", remap = false),
        remap = false,
        require = 0
    )
    private SkinTextures acvcape$elytraSkin(SkinTextures var1, @Local(argsOnly = true) LivingEntity var2) {
        return SkinOverride.applyOrKeep(var2.getUuid(), var1, "elytra");
    }

    @WrapOperation(
        method = "method_17161(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_1309;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_563;method_60879(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;II)V", remap = false),
        remap = false,
        require = 0
    )
    private void acvcape$emissiveElytra(
        ElytraEntityModel<?> var1,
        MatrixStack var2,
        VertexConsumer var3,
        int var4,
        int var5,
        Operation<Void> var6,
        @Local(argsOnly = true) VertexConsumerProvider var7,
        @Local(argsOnly = true) LivingEntity var8
    ) {
        var6.call(new Object[]{var1, var2, var3, var4, var5});
        EmissiveRenderer.renderElytra(var1, var2, var7, var8, var5);
    }

    @WrapOperation(
        method = "method_17161(Lnet/minecraft/class_4587;Lnet/minecraft/class_4597;ILnet/minecraft/class_1309;FFFFFF)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/class_563;method_2828(Lnet/minecraft/class_4587;Lnet/minecraft/class_4588;III)V", remap = false),
        remap = false,
        require = 0
    )
    private void acvcape$emissiveElytraColor(
        ElytraEntityModel<?> var1,
        MatrixStack var2,
        VertexConsumer var3,
        int var4,
        int var5,
        int var6,
        Operation<Void> var7,
        @Local(argsOnly = true) VertexConsumerProvider var8,
        @Local(argsOnly = true) LivingEntity var9
    ) {
        var7.call(new Object[]{var1, var2, var3, var4, var5, var6});
        EmissiveRenderer.renderElytra(var1, var2, var8, var9, var5);
    }
}
