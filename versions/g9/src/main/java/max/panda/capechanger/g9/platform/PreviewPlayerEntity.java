package max.panda.capechanger.g9.platform;

import com.mojang.authlib.GameProfile;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import max.panda.capechanger.g9.cape.SkinOverride;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public final class PreviewPlayerEntity extends RemotePlayer {
    public static final Set<Integer> IDS = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger NEXT_ID = new AtomicInteger(-1000000);
    private static final Identifier TRANSPARENT_SKIN = Identifier.fromNamespaceAndPath("acvcape", "textures/skin_transparent.png");
    private boolean capeOnlyMode;
    private boolean elytraMode;
    private Identifier forcedCape;

    public PreviewPlayerEntity(ClientLevel var1, GameProfile var2, Player var3) {
        this(var1, var2, var3.getEntityData().get(DATA_PLAYER_MODE_CUSTOMISATION));
    }

    public PreviewPlayerEntity(ClientLevel var1, GameProfile var2, byte var3) {
        super(var1, var2);
        this.setId(NEXT_ID.getAndDecrement());
        IDS.add(this.getId());
        this.getEntityData().set(DATA_PLAYER_MODE_CUSTOMISATION, var3);
    }

    public static PreviewPlayerEntity create() {
        Minecraft var0 = Minecraft.getInstance();
        if (var0.level != null && var0.player != null) {
            return new PreviewPlayerEntity(var0.level, var0.player.getGameProfile(), var0.player);
        }

        EntityRenderDispatcher var1 = var0.getEntityRenderDispatcher();
        if (var1.camera == null) {
            var1.camera = new Camera();
        }

        int var2 = 0;

        for (PlayerModelPart var6 : PlayerModelPart.values()) {
            if (var0.options.isModelPartEnabled(var6)) {
                var2 |= var6.getMask();
            }
        }

        PreviewPlayerEntity var7 = new PreviewPlayerEntity(PreviewWorld.get(), var0.getGameProfile(), (byte)var2);
        var7.snapTo(16000.0, 0.0, 0.0, 0.0F, 0.0F);
        var7.animate(false, false, 0.0, 0);
        var7.animate(false, false, 0.0, 0);
        return var7;
    }

    public void applyState(boolean var1, boolean var2, boolean var3) {
        this.setPose(var1 ? Pose.CROUCHING : Pose.STANDING);
        this.setSprinting(false);
        this.setItemSlot(EquipmentSlot.CHEST, var2 ? new ItemStack(Items.ELYTRA) : ItemStack.EMPTY);
        if (var2 && !this.elytraMode) {
            for (int var4 = 0; var4 < 24; var4++) {
                this.elytraAnimationState.tick();
            }
        }

        this.capeOnlyMode = var3;
        this.elytraMode = var2;
    }

    public void animate(boolean var1, boolean var2, double var3, int var5) {
        this.tickCount++;
        this.elytraAnimationState.tick();
        this.walkAnimation.update(var1 ? 0.4F : 0.0F, 0.3F, 1.0F);
        double var6;
        double var8;
        if (var2) {
            var6 = var1 ? 0.5 + Math.sin(var5 * 0.15) * 0.05 : 0.0;
            var8 = Math.toRadians(var3);
        } else {
            var6 = var1 ? 0.1 + Math.sin(var5 * 0.15) * 0.04 : 0.05 + Math.sin(var5 * 0.1) * 0.03;
            var8 = Math.toRadians(var3 + 180.0);
        }

        this.avatarState().tick(new Vec3(this.getX() + Math.sin(var8) * var6, this.getY(), this.getZ() - Math.cos(var8) * var6), Vec3.ZERO);
    }

    public void syncRotation(float var1) {
        this.yBodyRot = var1;
        this.yBodyRotO = var1;
        this.yHeadRot = var1;
        this.yHeadRotO = var1;
        this.setYRot(var1);
        this.yRotO = var1;
        this.setXRot(0.0F);
        this.xRotO = 0.0F;
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
    public PlayerSkin getSkin() {
        PlayerSkin var1 = Minecraft.getInstance().getConnection() == null ? P.titleSkin(this.getGameProfile()) : super.getSkin();
        PlayerSkin var2 = SkinOverride.apply(this.getUUID(), var1, "preview");
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
    public boolean isFallFlying() {
        return false;
    }

    @Override
    public int getFallFlyingTicks() {
        return 0;
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public Component getCustomName() {
        return null;
    }

    @Override
    public boolean isInvisibleTo(Player var1) {
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
