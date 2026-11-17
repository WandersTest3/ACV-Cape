package max.panda.capechanger.g7.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import max.panda.capechanger.g7.cape.SkinOverride;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = AbstractClientPlayer.class, priority = 1500, remap = false)
public abstract class AbstractClientPlayerEntityMixin {
    @ModifyReturnValue(method = "getSkin()Lnet/minecraft/world/entity/player/PlayerSkin;", at = @At(value = "RETURN", remap = false), remap = false, require = 0)
    private PlayerSkin acvcape$overrideCape(PlayerSkin var1) {
        return SkinOverride.applyOrKeep(((AbstractClientPlayer)(Object)this).getUUID(), var1, "player");
    }
}
