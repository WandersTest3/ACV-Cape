package max.panda.capechanger;

import java.util.List;
import java.util.Set;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class WaveyGate implements IMixinConfigPlugin {
    private boolean active;

    public void onLoad(String var1) {
        try {
            this.active = FabricLoader.getInstance().isModLoaded("waveycapes")
                && WaveyGate.class.getClassLoader().getResource("dev/tr7zw/waveycapes/render/CapeNodeCollector.class") != null;
        } catch (Throwable var3) {
            this.active = false;
        }
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
        if (!this.active) {
            return null;
        }

        String var1 = AcvGroup.prefix();
        return List.of(var1.substring(var1.lastIndexOf(46) + 1) + ".WaveyCapeMixin");
    }

    public void preApply(String var1, ClassNode var2, String var3, IMixinInfo var4) {
    }

    public void postApply(String var1, ClassNode var2, String var3, IMixinInfo var4) {
    }
}
