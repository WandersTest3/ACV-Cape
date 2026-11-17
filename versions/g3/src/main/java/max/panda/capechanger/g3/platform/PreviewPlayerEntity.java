package max.panda.capechanger.g3.platform;

import com.mojang.authlib.GameProfile;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import max.panda.capechanger.g3.cape.SkinOverride;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class PreviewPlayerEntity extends OtherClientPlayerEntity {
    public static final Set<Integer> IDS = ConcurrentHashMap.newKeySet();
    private static final Identifier TRANSPARENT_SKIN = Identifier.of("acvcape", "textures/skin_transparent.png");
    private boolean capeOnlyMode;
    private boolean elytraMode;
    private Identifier forcedCape;

    public PreviewPlayerEntity(ClientWorld var1, GameProfile var2, PlayerEntity var3) {
        this(var1, var2, var3.getDataTracker().get(PLAYER_MODEL_PARTS));
    }

    public PreviewPlayerEntity(ClientWorld var1, GameProfile var2, byte var3) {
        super(var1, var2);
        IDS.add(this.getId());
        this.getDataTracker().set(PLAYER_MODEL_PARTS, var3);
    }

    public static PreviewPlayerEntity create() {
        MinecraftClient var0 = MinecraftClient.getInstance();
        if (var0.world != null && var0.player != null) {
            return new PreviewPlayerEntity(var0.world, var0.player.getGameProfile(), var0.player);
        }

        EntityRenderDispatcher var1 = var0.getEntityRenderDispatcher();
        if (var1.camera == null) {
            var1.camera = new Camera();
        }

        int var2 = 0;

        for (PlayerModelPart var6 : PlayerModelPart.values()) {
            if (var0.options.isPlayerModelPartEnabled(var6)) {
                var2 |= var6.getBitFlag();
            }
        }

        PreviewPlayerEntity var7 = new PreviewPlayerEntity(PreviewWorld.get(), var0.getGameProfile(), (byte)var2);
        var7.refreshPositionAndAngles(16000.0, 0.0, 0.0, 0.0F, 0.0F);
        var7.animate(false, false, 0.0, 0);
        var7.animate(false, false, 0.0, 0);
        return var7;
    }

    public void applyState(boolean var1, boolean var2, boolean var3) {
        this.setPose(var1 ? EntityPose.CROUCHING : EntityPose.STANDING);
        this.setSprinting(false);
        this.equipStack(EquipmentSlot.CHEST, var2 ? new ItemStack(Items.ELYTRA) : ItemStack.EMPTY);
        if (var2 && !this.elytraMode) {
            for (int var4 = 0; var4 < 24; var4++) {
                this.elytraFlightController.update();
            }
        }

        this.capeOnlyMode = var3;
        this.elytraMode = var2;
    }

    public void animate(boolean var1, boolean var2, double var3, int var5) {
        this.age++;
        this.elytraFlightController.update();
        this.limbAnimator.updateLimbs(var1 ? 0.4F : 0.0F, 0.3F, 1.0F);
        this.lastStrideDistance = this.strideDistance;
        this.strideDistance = this.strideDistance + ((var1 ? 0.04F : 0.0F) - this.strideDistance) * 0.3F;
        double var6;
        double var8;
        if (var2) {
            var6 = var1 ? 0.5 + Math.sin(var5 * 0.15) * 0.05 : 0.0;
            var8 = Math.toRadians(var3);
        } else {
            var6 = var1 ? 0.1 + Math.sin(var5 * 0.15) * 0.04 : 0.05 + Math.sin(var5 * 0.1) * 0.03;
            var8 = Math.toRadians(var3 + 180.0);
        }

        this.lastCapeX = this.capeX;
        this.lastCapeY = this.capeY;
        this.lastCapeZ = this.capeZ;
        this.capeX = this.getX() + Math.sin(var8) * var6;
        this.capeY = this.getY();
        this.capeZ = this.getZ() - Math.cos(var8) * var6;
    }

    public void syncRotation(float var1) {
        this.bodyYaw = var1;
        this.lastBodyYaw = var1;
        this.headYaw = var1;
        this.lastHeadYaw = var1;
        this.setYaw(var1);
        this.lastYaw = var1;
        this.setPitch(0.0F);
        this.lastPitch = 0.0F;
    }

    public boolean isCapeOnlyMode() {
        return this.capeOnlyMode;
    }

    public void setForcedCape(Identifier var1) {
        this.forcedCape = var1;
    }

    public Identifier getForcedCape() {
        return this.forcedCape;
    }

    @Override
    public SkinTextures getSkinTextures() {
        SkinTextures var1 = MinecraftClient.getInstance().getNetworkHandler() == null ? P.titleSkin(this.getGameProfile()) : super.getSkinTextures();
        SkinTextures var2 = SkinOverride.apply(this.getUuid(), var1, "preview");
        if (var2 != null) {
            var1 = var2;
        }

        if (this.forcedCape != null) {
            var1 = P.withCape(var1, this.forcedCape, this.forcedCape);
        }

        if (this.elytraMode) {
            var1 = P.withCape(var1, null, P.elytraTexture(var1));
        }

        return !this.capeOnlyMode ? var1 : P.withBody(var1, TRANSPARENT_SKIN);
    }

    @Override
    public boolean isGliding() {
        return false;
    }

    @Override
    public int getGlidingTicks() {
        return 0;
    }

    @Override
    public boolean shouldRenderName() {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public Text getCustomName() {
        return null;
    }

    @Override
    public boolean isInvisibleTo(PlayerEntity player) {
        return false;
    }

    @Override
    public boolean isInvisible() {
        return false;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }
}
