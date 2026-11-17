package max.panda.capechanger.g3;

import max.panda.capechanger.g3.network.CapeNetworking;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CapeChanger implements ModInitializer {
    public static final String MOD_ID = "acvcape";
    public static final Logger LOGGER = LoggerFactory.getLogger("acvcape");

    public void onInitialize() {
        CapeNetworking.registerCommon();
        CapeChangerServer.register();
    }
}
