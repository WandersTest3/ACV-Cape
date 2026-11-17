package max.panda.capechanger.g3.cape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum DownloadPolicy {
    ALWAYS,
    NEVER,
    ASK;

    public DownloadPolicy next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
