package max.panda.capechanger.g4.cape;

import java.util.Collection;
import max.panda.capechanger.g4.CapeChangerClient;

public final class HeavyCape {
    public static final int THRESHOLD = 3145728;

    private HeavyCape() {
    }

    public static boolean isHeavy(int var0) {
        return var0 >= 3145728;
    }

    public static DownloadPolicy policy(CapeManager var0, RemoteCape var1) {
        DownloadPolicy var2 = var0.getDownloadPolicy();
        return var2 == DownloadPolicy.ALWAYS && var1 != null && isHeavy(var1.size()) ? DownloadPolicy.ASK : var2;
    }

    public static DownloadPolicy policy(CapeManager var0, Collection<CapeChangerClient.PendingDownload> var1) {
        DownloadPolicy var2 = var0.getDownloadPolicy();
        if (var2 != DownloadPolicy.ALWAYS) {
            return var2;
        }

        for (CapeChangerClient.PendingDownload var4 : var1) {
            if (isHeavy(var4.size())) {
                return DownloadPolicy.ASK;
            }
        }

        return var2;
    }
}
