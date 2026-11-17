package max.panda.capechanger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.LoggerFactory;

public final class AcvLauncher implements ModInitializer, ClientModInitializer {
    public void onInitialize() {
        ((ModInitializer)create("CapeChanger")).onInitialize();
    }

    public void onInitializeClient() {
        LoggerFactory.getLogger("acvcape").info("ACV Cape: perfil " + AcvGroup.current() + " selecionado");
        ((ClientModInitializer)create("CapeChangerClient")).onInitializeClient();
    }

    private static Object create(String var0) {
        try {
            return Class.forName(AcvGroup.prefix() + "." + var0).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException var2) {
            throw new IllegalStateException("ACV Cape: failed to start " + var0, var2);
        }
    }
}
