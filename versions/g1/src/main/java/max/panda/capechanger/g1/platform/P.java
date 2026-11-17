package max.panda.capechanger.g1.platform;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.InputUtil.Type;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public final class P {
    public static final String VERSION_GROUP = "1.21-1.21.1";
    private static final Set<Identifier> REGISTERED = new HashSet<>();
    private static ModelPart capeModel;

    private P() {
    }

    public static ActionResult pass() {
        return ActionResult.PASS;
    }

    public static ActionResult fail() {
        return ActionResult.FAIL;
    }

    public static String profileName(GameProfile var0) {
        return var0 == null ? null : var0.getName();
    }

    public static UUID profileId(GameProfile var0) {
        return var0 == null ? null : var0.getId();
    }

    public static boolean shiftDown() {
        return Screen.hasShiftDown();
    }

    public static boolean ctrlDown() {
        return Screen.hasControlDown();
    }

    public static KeyBinding createKey(String var0, int var1, String var2) {
        return new KeyBinding(var0, Type.KEYSYM, var1, var2);
    }

    public static boolean keyMatches(KeyBinding var0, int var1, int var2) {
        return var0 != null && var0.matchesKey(var1, var2);
    }

    public static SkinTextures skinOf(AbstractClientPlayerEntity var0) {
        return var0 == null ? null : var0.getSkinTextures();
    }

    public static Identifier capeTexture(SkinTextures var0) {
        return var0 == null ? null : var0.capeTexture();
    }

    public static Identifier elytraTexture(SkinTextures var0) {
        return var0 == null ? null : var0.elytraTexture();
    }

    public static SkinTextures withCape(SkinTextures var0, Identifier var1, Identifier var2) {
        return new SkinTextures(var0.texture(), var0.textureUrl(), var1, var2, var0.model(), var0.secure());
    }

    public static SkinTextures withBody(SkinTextures var0, Identifier var1) {
        return new SkinTextures(var1, null, var0.capeTexture(), var0.elytraTexture(), var0.model(), var0.secure());
    }

    public static NativeImageBackedTexture newTexture(NativeImage var0) {
        return new NativeImageBackedTexture(var0);
    }

    public static void register(Identifier var0, AbstractTexture var1) {
        MinecraftClient.getInstance().getTextureManager().registerTexture(var0, var1);
        REGISTERED.add(var0);
    }

    public static boolean has(Identifier var0) {
        return var0 != null && REGISTERED.contains(var0);
    }

    public static void destroy(Identifier var0) {
        if (var0 != null && REGISTERED.remove(var0)) {
            MinecraftClient.getInstance().getTextureManager().destroyTexture(var0);
        }
    }

    private static int alpha(int var0) {
        return (var0 & -67108864) == 0 ? var0 | 0xFF000000 : var0;
    }

    public static void text(DrawContext var0, TextRenderer var1, String var2, int var3, int var4, int var5) {
        var0.drawTextWithShadow(var1, var2, var3, var4, alpha(var5));
    }

    public static void text(DrawContext var0, TextRenderer var1, Text var2, int var3, int var4, int var5) {
        var0.drawTextWithShadow(var1, var2, var3, var4, alpha(var5));
    }

    public static void centered(DrawContext var0, TextRenderer var1, String var2, int var3, int var4, int var5) {
        var0.drawCenteredTextWithShadow(var1, var2, var3, var4, alpha(var5));
    }

    public static void centered(DrawContext var0, TextRenderer var1, Text var2, int var3, int var4, int var5) {
        var0.drawCenteredTextWithShadow(var1, var2, var3, var4, alpha(var5));
    }

    public static void border(DrawContext var0, int var1, int var2, int var3, int var4, int var5) {
        var0.drawBorder(var1, var2, var3, var4, var5);
    }

    public static void texture(
        DrawContext var0, Identifier var1, int var2, int var3, int var4, int var5, float var6, float var7, int var8, int var9, int var10, int var11
    ) {
        var0.drawTexture(var1, var2, var3, var4, var5, var6, var7, var8, var9, var10, var11);
    }

    public static void pushOverlay(DrawContext var0) {
        var0.getMatrices().push();
        var0.getMatrices().translate(0.0F, 0.0F, 400.0F);
    }

    public static SkinTextures titleSkin(GameProfile var0) {
        return MinecraftClient.getInstance().getSkinProvider().getSkinTextures(var0);
    }

    public static void pushButtons(DrawContext var0) {
        var0.getMatrices().push();
        var0.getMatrices().translate(0.0F, 0.0F, 300.0F);
    }

    public static void popButtons(DrawContext var0) {
        var0.getMatrices().pop();
    }

    public static void popOverlay(DrawContext var0) {
        var0.getMatrices().pop();
    }

    private static ModelPart capeModel() {
        if (capeModel == null) {
            ModelData var0 = new ModelData();
            ModelPartData var1 = var0.getRoot();
            var1.addChild("cape", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F), ModelTransform.pivot(0.0F, 0.0F, 0.0F));
            capeModel = TexturedModelData.of(var0, 64, 32).createModel().getChild("cape");
        }

        return capeModel;
    }

    public static void capeIcon(DrawContext var0, Identifier var1, Identifier var2, int var3, int var4, int var5, int var6, float var7, LivingEntity var8) {
        if (var1 != null) {
            float var9 = Math.min(var6 * 0.92F, var5 / 0.7F);
            DiffuseLighting.disableGuiDepthLighting();
            MatrixStack var10 = var0.getMatrices();
            var10.push();
            var10.translate(var3 + var5 / 2.0F, var4 + var6 / 2.0F, 120.0F);
            var10.scale(var9, var9, var9);
            var10.multiply(new Quaternionf().rotateX((float)Math.toRadians(-10.0)).rotateY((float)Math.toRadians(206.0 + var7)));
            var10.translate(0.0F, -0.5F, 0.03F);
            VertexConsumer var11 = var0.getVertexConsumers().getBuffer(RenderLayer.getEntityTranslucent(var1));
            capeModel().render(var10, var11, 15728880, OverlayTexture.DEFAULT_UV, var2 != null ? -8355712 : -1);
            var0.draw();
            if (var2 != null) {
                VertexConsumer var12 = var0.getVertexConsumers().getBuffer(RenderLayer.getEntityTranslucentEmissive(var2));
                capeModel().render(var10, var12, 15728880, OverlayTexture.DEFAULT_UV);
                var0.draw();
            }

            var10.pop();
            DiffuseLighting.enableGuiDepthLighting();
        }
    }

    public static void entity(DrawContext var0, LivingEntity var1, int var2, int var3, int var4, int var5, float var6, float var7) {
        float var8 = (var2 + var4) / 2.0F;
        float var9 = (var3 + var5) / 2.0F + var7;
        InventoryScreen.drawEntity(var0, var8, var9, var6, new Vector3f(0.0F, 0.9625F, 0.0F), new Quaternionf().rotateZ((float) Math.PI), null, var1);
    }

    public static void tooltip(ClickableWidget var0, Text var1) {
        var0.setTooltip(Tooltip.of(var1));
    }

    public static void hoverTip(DrawContext var0, TextRenderer var1, List<Text> var2, int var3, int var4) {
        Screen var5 = MinecraftClient.getInstance().currentScreen;
        int var6 = var5 == null ? 320 : var5.width;
        int var7 = Math.max(100, Math.min(220, var6 / 2));
        ArrayList var8 = new ArrayList();

        for (Text var10 : var2) {
            String var11 = var10.getString();
            if (var1.getWidth(var11) <= var7) {
                var8.add(var10);
            } else {
                StringBuilder var12 = new StringBuilder();

                for (String var16 : var11.split(" ")) {
                    String var17 = var12.length() == 0 ? var16 : var12 + " " + var16;
                    if (var1.getWidth(var17) > var7 && var12.length() > 0) {
                        var8.add(Text.literal(var12.toString()).setStyle(var10.getStyle()));
                        var12 = new StringBuilder(var16);
                    } else {
                        var12 = new StringBuilder(var17);
                    }
                }

                if (var12.length() > 0) {
                    var8.add(Text.literal(var12.toString()).setStyle(var10.getStyle()));
                }
            }
        }

        var0.drawTooltip(var1, var8, var3, var4);
    }

    public static void setColor(NativeImage var0, int var1, int var2, int var3) {
        var0.setColor(var1, var2, var3);
    }

    public static boolean entityIcons() {
        return false;
    }
}
