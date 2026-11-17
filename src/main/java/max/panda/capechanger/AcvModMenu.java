package max.panda.capechanger;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

public final class AcvModMenu implements ModMenuApi {
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        InvocationHandler handler = (proxy, method, args) -> {
            if (method.getDeclaringClass() == Object.class) {
                return switch (method.getName()) {
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> "AcvModMenu";
                };
            }

            try {
                Constructor<?> constructor = Class.forName(AcvGroup.prefix() + ".screen.CapeMenuScreen").getConstructors()[0];
                return constructor.newInstance(args[0]);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("ACV Cape: failed to open the menu", e);
            }
        };
        return (ConfigScreenFactory<?>)Proxy.newProxyInstance(AcvModMenu.class.getClassLoader(), new Class<?>[]{ConfigScreenFactory.class}, handler);
    }
}
