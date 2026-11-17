package max.panda.capechanger.g7.platform;

import java.lang.reflect.Field;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.scores.Scoreboard;
import sun.misc.Unsafe;

@Environment(EnvType.CLIENT)
public final class PreviewWorld extends ClientLevel {
    private static PreviewWorld instance;
    private static final Scoreboard SCOREBOARD = new Scoreboard();

    private PreviewWorld() {
        super(null, null, null, null, 0, 0, null, false, 0L, 0);
    }

    public static synchronized PreviewWorld get() {
        if (instance == null) {
            try {
                Field var0 = Unsafe.class.getDeclaredField("theUnsafe");
                var0.setAccessible(true);
                Unsafe var1 = (Unsafe)var0.get(null);
                PreviewWorld var2 = (PreviewWorld)var1.allocateInstance(PreviewWorld.class);
                String var3 = "isClientSide";
                var1.putBoolean(var2, var1.objectFieldOffset(Level.class.getDeclaredField(var3)), true);
                instance = var2;
            } catch (Throwable var4) {
                throw new IllegalStateException(var4);
            }
        }

        return instance;
    }

    @Override
    public Scoreboard getScoreboard() {
        return SCOREBOARD;
    }

    public BlockPos getSpawnPos() {
        return BlockPos.ZERO;
    }

    public float getSpawnAngle() {
        return 0.0F;
    }

    @Override
    public int getBrightness(LightLayer var1, BlockPos var2) {
        return 15;
    }
}
