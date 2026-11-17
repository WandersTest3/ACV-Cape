package max.panda.capechanger.g9.platform;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.NativeImage;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset.ResourceTexture;
import net.minecraft.core.ClientAsset.Texture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerSkin;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public final class P {
    public static final String VERSION_GROUP = "1.21.11";
    private static final Set<Identifier> REGISTERED = new HashSet<>();
    private static Category keyCategory;
    private static Method drawEntityMethod;
    private static final Map<AbstractWidget, Component> TIPS = new WeakHashMap<>();

    private P() {
    }

    public static InteractionResult pass() {
        return InteractionResult.PASS;
    }

    public static InteractionResult fail() {
        return InteractionResult.FAIL;
    }

    public static String profileName(GameProfile var0) {
        return var0 == null ? null : var0.name();
    }

    public static UUID profileId(GameProfile var0) {
        return var0 == null ? null : var0.id();
    }

    public static boolean shiftDown() {
        return Minecraft.getInstance().hasShiftDown();
    }

    public static boolean ctrlDown() {
        return Minecraft.getInstance().hasControlDown();
    }

    public static KeyMapping createKey(String var0, int var1, String var2) {
        if (keyCategory == null) {
            keyCategory = Category.register(Identifier.fromNamespaceAndPath("acvcape", "main"));
        }

        return new KeyMapping(var0, var1, keyCategory);
    }

    public static boolean keyMatches(KeyMapping var0, int var1, int var2) {
        return var0 != null && var0.matches(new KeyEvent(var1, var2, 0));
    }

    public static PlayerSkin skinOf(AbstractClientPlayer var0) {
        return var0 == null ? null : var0.getSkin();
    }

    private static Identifier path(Texture var0) {
        return var0 == null ? null : var0.texturePath();
    }

    private static Texture asset(Identifier var0) {
        return var0 == null ? null : new ResourceTexture(var0, var0);
    }

    public static Identifier capeTexture(PlayerSkin var0) {
        return var0 == null ? null : path(var0.cape());
    }

    public static Identifier elytraTexture(PlayerSkin var0) {
        return var0 == null ? null : path(var0.elytra());
    }

    public static PlayerSkin withCape(PlayerSkin var0, Identifier var1, Identifier var2) {
        return new PlayerSkin(var0.body(), asset(var1), asset(var2), var0.model(), var0.secure());
    }

    public static PlayerSkin withBody(PlayerSkin var0, Identifier var1) {
        return new PlayerSkin(asset(var1), var0.cape(), var0.elytra(), var0.model(), var0.secure());
    }

    public static DynamicTexture newTexture(NativeImage var0) {
        return new DynamicTexture(() -> "acvcape", var0);
    }

    public static void register(Identifier var0, AbstractTexture var1) {
        Minecraft.getInstance().getTextureManager().register(var0, var1);
        REGISTERED.add(var0);
    }

    public static boolean has(Identifier var0) {
        return var0 != null && REGISTERED.contains(var0);
    }

    public static void destroy(Identifier var0) {
        if (var0 != null && REGISTERED.remove(var0)) {
            Minecraft.getInstance().getTextureManager().release(var0);
        }
    }

    private static int alpha(int var0) {
        return (var0 & -67108864) == 0 ? var0 | 0xFF000000 : var0;
    }

    public static void text(GuiGraphicsExtractor var0, Font var1, String var2, int var3, int var4, int var5) {
        var0.text(var1, var2, var3, var4, alpha(var5));
    }

    public static void text(GuiGraphicsExtractor var0, Font var1, Component var2, int var3, int var4, int var5) {
        var0.text(var1, var2, var3, var4, alpha(var5));
    }

    public static void centered(GuiGraphicsExtractor var0, Font var1, String var2, int var3, int var4, int var5) {
        var0.centeredText(var1, var2, var3, var4, alpha(var5));
    }

    public static void centered(GuiGraphicsExtractor var0, Font var1, Component var2, int var3, int var4, int var5) {
        var0.centeredText(var1, var2, var3, var4, alpha(var5));
    }

    public static void border(GuiGraphicsExtractor var0, int var1, int var2, int var3, int var4, int var5) {
        var0.fill(var1, var2, var1 + var3, var2 + 1, var5);
        var0.fill(var1, var2 + var4 - 1, var1 + var3, var2 + var4, var5);
        var0.fill(var1, var2 + 1, var1 + 1, var2 + var4 - 1, var5);
        var0.fill(var1 + var3 - 1, var2 + 1, var1 + var3, var2 + var4 - 1, var5);
    }

    public static void texture(
        GuiGraphicsExtractor var0, Identifier var1, int var2, int var3, int var4, int var5, float var6, float var7, int var8, int var9, int var10, int var11
    ) {
        var0.blit(RenderPipelines.GUI_TEXTURED, var1, var2, var3, var6, var7, var4, var5, var8, var9, var10, var11);
    }

    public static void pushOverlay(GuiGraphicsExtractor var0) {
        var0.nextStratum();
    }

    public static PlayerSkin titleSkin(GameProfile var0) {
        return Minecraft.getInstance().getSkinManager().createLookup(var0, false).get();
    }

    public static void pushButtons(GuiGraphicsExtractor var0) {
        pushOverlay(var0);
    }

    public static void popButtons(GuiGraphicsExtractor var0) {
        popOverlay(var0);
    }

    public static void popOverlay(GuiGraphicsExtractor var0) {
    }

    public static void capeIcon(GuiGraphicsExtractor var0, Identifier var1, Identifier var2, int var3, int var4, int var5, int var6, float var7, LivingEntity var8) {
        if (var1 != null) {
            if (var8 != null) {
                float var9 = AcvScreen.activeFactor();
                int var10 = Math.round(var3 * var9);
                int var11 = Math.round(var4 * var9);
                int var12 = Math.round((var3 + var5) * var9);
                int var13 = Math.round((var4 + var6) * var9);
                int var14 = Math.max(1, var6 * 92 / 100);
                if (var14 * 10 / 16 > var5) {
                    var14 = Math.max(1, var5 * 16 / 10);
                }

                float var15 = var14 / 0.9375F * var9;
                var0.entity(
                    (EntityRenderState)drawEntityState(var8),
                    var15,
                    new Vector3f(0.0F, 0.95F, 0.0F),
                    new Quaternionf().rotateZ((float) Math.PI),
                    null,
                    var10,
                    var11,
                    var12,
                    var13
                );
            } else {
                int var17 = Math.max(1, var6 * 92 / 100);
                int var18 = Math.max(1, var17 * 10 / 16);
                if (var18 > var5) {
                    var18 = var5;
                    var17 = Math.max(1, var5 * 16 / 10);
                }

                int var19 = var3 + (var5 - var18) / 2;
                int var20 = var4 + (var6 - var17) / 2;
                float var21 = (float)Math.cos(Math.toRadians(var7));
                float var22 = var21 < 0.0F ? 12.0F : 1.0F;
                int var23 = Math.max(1, Math.round(var18 * Math.max(0.06F, Math.abs(var21))));
                int var16 = var19 + (var18 - var23) / 2;
                var0.blit(RenderPipelines.GUI_TEXTURED, var1, var16, var20, var22, 1.0F, var23, var17, 10, 16, 64, 32, var2 != null ? -8355712 : -1);
                if (var2 != null) {
                    var0.blit(RenderPipelines.GUI_TEXTURED, var2, var16, var20, var22, 1.0F, var23, var17, 10, 16, 64, 32);
                }
            }
        }
    }

    private static Object drawEntityState(LivingEntity var0) {
        try {
            if (drawEntityMethod == null) {
                Method var1 = null;

                for (Method var5 : InventoryScreen.class.getDeclaredMethods()) {
                    if (var5.getParameterCount() == 1 && LivingEntity.class.isAssignableFrom(var5.getParameterTypes()[0])) {
                        var1 = var5;
                        break;
                    }
                }

                var1.setAccessible(true);
                drawEntityMethod = var1;
            }

            return drawEntityMethod.invoke(null, var0);
        } catch (ReflectiveOperationException var6) {
            throw new RuntimeException(var6);
        }
    }

    public static void entity(GuiGraphicsExtractor var0, LivingEntity var1, int var2, int var3, int var4, int var5, float var6, float var7) {
        float var8 = AcvScreen.activeFactor();
        int var9 = Math.round(var2 * var8);
        int var10 = Math.round(var3 * var8);
        int var11 = Math.round(var4 * var8);
        int var12 = Math.round(var5 * var8);
        float var13 = var6 * var8;
        var0.entity(
            (EntityRenderState)drawEntityState(var1),
            var13,
            new Vector3f(0.0F, 0.9625F + var7 / Math.max(1.0F, var6), 0.0F),
            new Quaternionf().rotateZ((float) Math.PI),
            null,
            var9,
            var10,
            var11,
            var12
        );
    }

    public static void tooltip(AbstractWidget var0, Component var1) {
        if (var0 != null) {
            TIPS.put(var0, var1);
        }
    }

    public static void widgetTips(GuiGraphicsExtractor var0, Font var1, Screen var2, int var3, int var4) {
        for (GuiEventListener var6 : var2.children()) {
            if (var6 instanceof AbstractWidget var7) {
                Component var8 = TIPS.get(var7);
                if (var8 != null && var7.visible && var7.isHovered()) {
                    hoverTip(var0, var1, List.of(var8), var3, var4);
                    return;
                }
            }
        }
    }

    public static void hoverTip(GuiGraphicsExtractor var0, Font var1, List<Component> var2, int var3, int var4) {
        Screen var5 = Minecraft.getInstance().gui.screen();
        int var6 = var5 == null ? 320 : var5.width;
        int var7 = var5 == null ? 240 : var5.height;
        int var8 = Math.max(100, Math.min(220, var6 / 2));
        ArrayList<Component> var9 = new ArrayList<>();

        for (Component var11 : var2) {
            String var12 = var11.getString();
            if (var1.width(var12) <= var8) {
                var9.add(var11);
            } else {
                StringBuilder var13 = new StringBuilder();

                for (String var17 : var12.split(" ")) {
                    String var18 = var13.length() == 0 ? var17 : var13 + " " + var17;
                    if (var1.width(var18) > var8 && var13.length() > 0) {
                        var9.add(Component.literal(var13.toString()));
                        var13 = new StringBuilder(var17);
                    } else {
                        var13 = new StringBuilder(var18);
                    }
                }

                if (var13.length() > 0) {
                    var9.add(Component.literal(var13.toString()));
                }
            }
        }

        if (!var9.isEmpty()) {
            int var19 = 0;

            for (Component var22 : var9) {
                var19 = Math.max(var19, var1.width(var22.getString()));
            }

            int var21 = var9.size() * 10 - 1;
            int var23 = var3 + 12;
            int var24 = var4 - 12;
            if (var23 + var19 + 6 > var6) {
                var23 = Math.max(6, var3 - 12 - var19);
            }

            if (var24 + var21 + 6 > var7) {
                var24 = var7 - var21 - 6;
            }

            if (var24 < 6) {
                var24 = 6;
            }

            pushOverlay(var0);
            var0.fill(var23 - 3, var24 - 3, var23 + var19 + 3, var24 + var21 + 3, -267386864);
            border(var0, var23 - 3, var24 - 3, var19 + 6, var21 + 6, -12903553);

            for (int var25 = 0; var25 < var9.size(); var25++) {
                text(var0, var1, (Component)var9.get(var25), var23, var24 + var25 * 10, 16777215);
            }

            popOverlay(var0);
        }
    }

    public static void setColor(NativeImage var0, int var1, int var2, int var3) {
        int var4 = var3 & -16711936 | var3 >> 16 & 0xFF | (var3 & 0xFF) << 16;
        var0.setPixel(var1, var2, var4);
    }

    public static boolean entityIcons() {
        return true;
    }
}
