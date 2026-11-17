package max.panda.capechanger.g1.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import max.panda.capechanger.g1.cape.SkinOverride;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = AbstractClientPlayerEntity.class, priority = 1500, remap = false)
public abstract class AbstractClientPlayerEntityMixin {
    @ModifyReturnValue(method = "method_52814()Lnet/minecraft/class_8685;", at = @At(value = "RETURN", remap = false), remap = false, require = 0)
    private SkinTextures acvcape$overrideCape(SkinTextures var1) {
        return SkinOverride.applyOrKeep(((AbstractClientPlayerEntity)(Object)this).getUuid(), var1, "player");
    }
}
