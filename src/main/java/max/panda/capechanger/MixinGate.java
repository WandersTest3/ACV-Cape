package max.panda.capechanger;

import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class MixinGate implements IMixinConfigPlugin {
    private boolean active;

    public void onLoad(String var1) {
        this.active = var1.startsWith(AcvGroup.prefix() + ".");
    }

    public String getRefMapperConfig() {
        return null;
    }

    public boolean shouldApplyMixin(String var1, String var2) {
        return this.active;
    }

    public void acceptTargets(Set<String> var1, Set<String> var2) {
    }

    public List<String> getMixins() {
        return null;
    }

    public void preApply(String var1, ClassNode var2, String var3, IMixinInfo var4) {
    }

    public void postApply(String var1, ClassNode var2, String var3, IMixinInfo var4) {
    }
}
