package max.panda.capechanger.g1.platform;

import java.lang.reflect.Field;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import sun.misc.Unsafe;

@Environment(EnvType.CLIENT)
public final class PreviewWorld extends ClientWorld {
    private static PreviewWorld instance;
    private static final Scoreboard SCOREBOARD = new Scoreboard();

    private PreviewWorld() {
        super(null, null, null, null, 0, 0, null, null, false, 0L);
    }

    public static synchronized PreviewWorld get() {
        if (instance == null) {
            try {
                Field var0 = Unsafe.class.getDeclaredField("theUnsafe");
                var0.setAccessible(true);
                Unsafe var1 = (Unsafe)var0.get(null);
                PreviewWorld var2 = (PreviewWorld)var1.allocateInstance(PreviewWorld.class);
                String var3 = FabricLoader.getInstance().getMappingResolver().mapFieldName("intermediary", "net.minecraft.class_1937", "field_9236", "Z");
                var1.putBoolean(var2, var1.objectFieldOffset(World.class.getDeclaredField(var3)), true);
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

    @Override
    public BlockPos getSpawnPos() {
        return BlockPos.ORIGIN;
    }

    @Override
    public float getSpawnAngle() {
        return 0.0F;
    }

    @Override
    public int getLightLevel(LightType type, BlockPos pos) {
        return 15;
    }
}
