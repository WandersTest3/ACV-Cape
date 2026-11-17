package max.panda.capechanger.g9.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import max.panda.capechanger.g9.cape.SkinOverride;
import max.panda.capechanger.g9.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerInfo.class, priority = 1500, remap = false)
public abstract class PlayerListEntryMixin {
    @ModifyReturnValue(method = "getSkin()Lnet/minecraft/world/entity/player/PlayerSkin;", at = @At(value = "RETURN", remap = false), remap = false, require = 0)
    private PlayerSkin acvcape$overrideCape(PlayerSkin var1) {
        GameProfile var2 = ((PlayerInfo)(Object)this).getProfile();
        return var2 == null ? var1 : SkinOverride.applyOrKeep(P.profileId(var2), var1, "entry");
    }
}
