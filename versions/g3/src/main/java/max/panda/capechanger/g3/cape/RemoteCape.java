package max.panda.capechanger.g3.cape;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record RemoteCape(CapeSelection selection, String name, int size, int frames, int flags) {
    public boolean isLocked() {
        return (this.flags & 1) != 0;
    }
}
