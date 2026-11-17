package max.panda.capechanger.g5.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import max.panda.capechanger.g5.CapeChanger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.network.AbstractClientPlayerEntity;

@Environment(EnvType.CLIENT)
public final class WaveyCompat {
    private static final String HOLDER = "dev.tr7zw.waveycapes.versionless.CapeHolder";
    private static final String DELEGATE = "dev.tr7zw.waveycapes.delegate.PlayerDelegate";
    private static final String MC_PLAYER = "dev.tr7zw.waveycapes.versionless.nms.MinecraftPlayer";
    private static final String SIMULATION = "dev.tr7zw.waveycapes.versionless.sim.BasicSimulation";
    private static final String VECTOR = "dev.tr7zw.waveycapes.versionless.util.Vector3";
    private static final String BASE = "dev.tr7zw.waveycapes.versionless.ModBase";
    private static final int PART_COUNT = 16;
    private static boolean resolved;
    private static boolean available;
    private static Class<?> holderClass;
    private static Method updateSimulation;
    private static Method setGravityVectorRequest;
    private static Method getSimulation;
    private static Method simulate;
    private static Method applyMovement;
    private static Constructor<?> delegateConstructor;
    private static Constructor<?> vectorConstructor;
    private static Field configField;
    private static Field gravityVectorField;

    private WaveyCompat() {
    }

    public static boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded("waveycapes");
    }

    private static boolean resolve() {
        if (resolved) {
            return available;
        }

        resolved = true;
        if (!isLoaded()) {
            return false;
        }

        try {
            ClassLoader var0 = WaveyCompat.class.getClassLoader();
            holderClass = Class.forName("dev.tr7zw.waveycapes.versionless.CapeHolder", true, var0);
            Class var1 = Class.forName("dev.tr7zw.waveycapes.delegate.PlayerDelegate", true, var0);
            Class var2 = Class.forName("dev.tr7zw.waveycapes.versionless.nms.MinecraftPlayer", true, var0);
            Class var3 = Class.forName("dev.tr7zw.waveycapes.versionless.sim.BasicSimulation", true, var0);
            Class var4 = Class.forName("dev.tr7zw.waveycapes.versionless.util.Vector3", true, var0);
            updateSimulation = holderClass.getMethod("updateSimulation", int.class);
            setGravityVectorRequest = holderClass.getMethod("setGravityVectorRequest", boolean.class);
            getSimulation = holderClass.getMethod("getSimulation");
            simulate = holderClass.getMethod("simulate", var2);
            applyMovement = var3.getMethod("applyMovement", var4);
            delegateConstructor = var1.getConstructor(AbstractClientPlayerEntity.class);
            vectorConstructor = var4.getConstructor(float.class, float.class, float.class);

            try {
                Class var5 = Class.forName("dev.tr7zw.waveycapes.versionless.ModBase", true, var0);
                configField = var5.getField("config");
                gravityVectorField = configField.getType().getField("computeGravityVector");
            } catch (Throwable var6) {
                configField = null;
                gravityVectorField = null;
            }

            available = true;
        } catch (Throwable var7) {
            CapeChanger.LOGGER.warn("Compatibilidade com WaveyCapes indisponivel: " + var7);
            available = false;
        }

        return available;
    }

    public static boolean isActive(AbstractClientPlayerEntity var0) {
        if (var0 != null && resolve()) {
            try {
                return holderClass.isInstance(var0) && getSimulation.invoke(var0) != null;
            } catch (Throwable var2) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static boolean beginFixedGravity() {
        if (resolve() && configField != null && gravityVectorField != null) {
            try {
                Object var0 = configField.get(null);
                if (var0 != null && gravityVectorField.getBoolean(var0)) {
                    gravityVectorField.setBoolean(var0, false);
                    return true;
                } else {
                    return false;
                }
            } catch (Throwable var1) {
                return false;
            }
        } else {
            return false;
        }
    }

    public static void endFixedGravity(boolean var0) {
        if (var0) {
            try {
                gravityVectorField.setBoolean(configField.get(null), true);
            } catch (Throwable var2) {
            }
        }
    }

    public static void tick(AbstractClientPlayerEntity var0) {
        if (var0 != null && resolve() && holderClass.isInstance(var0)) {
            try {
                Object var1 = getSimulation.invoke(var0);
                updateSimulation.invoke(var0, 16);
                setGravityVectorRequest.invoke(var0, Boolean.FALSE);
                Object var2 = getSimulation.invoke(var0);
                if (var2 == null) {
                    return;
                }

                Object var3 = delegateConstructor.newInstance(var0);
                boolean var4 = beginFixedGravity();

                try {
                    if (var1 != var2) {
                        for (int var5 = 0; var5 < 40; var5++) {
                            simulate.invoke(var0, var3);
                        }
                    }

                    simulate.invoke(var0, var3);
                } finally {
                    endFixedGravity(var4);
                }
            } catch (Throwable var10) {
                available = false;
                CapeChanger.LOGGER.warn("Falha na simulacao da capa do WaveyCapes no menu: " + var10);
            }
        }
    }
}
