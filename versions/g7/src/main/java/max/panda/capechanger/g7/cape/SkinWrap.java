package max.panda.capechanger.g7.cape;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;
import max.panda.capechanger.g7.CapeChanger;
import max.panda.capechanger.g7.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.entity.player.PlayerSkin;

@Environment(EnvType.CLIENT)
public final class SkinWrap {
    private static Field field;
    private static boolean failed;
    private static boolean reported;
    private static String lastCheck = "";
    private static int counter;

    private SkinWrap() {
    }

    public static void tick(Minecraft var0) {
        if (!failed && var0.player != null) {
            ClientPacketListener var1 = var0.getConnection();
            if (var1 != null) {
                try {
                    if (field == null) {
                        for (Field var5 : PlayerInfo.class.getDeclaredFields()) {
                            if (Supplier.class.equals(var5.getType()) && !Modifier.isStatic(var5.getModifiers())) {
                                field = var5;
                                break;
                            }
                        }

                        if (field == null) {
                            throw new NoSuchFieldException("Supplier");
                        }

                        field.setAccessible(true);
                    }

                    for (PlayerInfo var9 : new ArrayList<>(var1.getOnlinePlayers())) {
                        Object var11 = field.get(var9);
                        if (var11 != null && !(var11 instanceof SkinWrap.Wrapper)) {
                            UUID var12 = P.profileId(var9.getProfile());
                            field.set(var9, new SkinWrap.Wrapper((Supplier<Object>)var11, var12));
                        }
                    }

                    if (++counter % 100 == 0) {
                        PlayerSkin var8 = P.skinOf(var0.player);
                        String var10 = var8 == null ? "null" : String.valueOf(P.capeTexture(var8));
                        if (!var10.equals(lastCheck)) {
                            lastCheck = var10;
                            CapeChanger.LOGGER.info("Autoteste capa do jogador: " + var10);
                        }
                    }

                    if (!reported) {
                        reported = true;
                        CapeChanger.LOGGER.info("Wrapper de skin ativo");
                    }
                } catch (Throwable var6) {
                    failed = true;
                    CapeChanger.LOGGER.warn("Wrapper de skin falhou: " + var6);
                }
            }
        }
    }

    private static final class Wrapper implements Supplier<Object> {
        private final Supplier<Object> delegate;
        private final UUID uuid;

        private Wrapper(Supplier<Object> var1, UUID var2) {
            this.delegate = var1;
            this.uuid = var2;
        }

        @Override
        public Object get() {
            Object var1 = this.delegate.get();
            if (!(var1 instanceof PlayerSkin var2)) {
                return var1;
            } else {
                PlayerSkin var3 = SkinOverride.apply(this.uuid, var2, "supplier");
                return var3 == null ? var2 : var3;
            }
        }
    }
}
