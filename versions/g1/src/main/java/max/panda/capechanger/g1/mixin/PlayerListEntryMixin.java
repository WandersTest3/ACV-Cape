package max.panda.capechanger.g1.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import max.panda.capechanger.g1.cape.SkinOverride;
import max.panda.capechanger.g1.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Environment(EnvType.CLIENT)
@Mixin(value = PlayerListEntry.class, priority = 1500, remap = false)
public abstract class PlayerListEntryMixin {
    @ModifyReturnValue(method = "method_52810()Lnet/minecraft/class_8685;", at = @At(value = "RETURN", remap = false), remap = false, require = 0)
    private SkinTextures acvcape$overrideCape(SkinTextures var1) {
        GameProfile var2 = ((PlayerListEntry)(Object)this).getProfile();
        return var2 == null ? var1 : SkinOverride.applyOrKeep(P.profileId(var2), var1, "entry");
    }
}
