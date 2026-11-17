package max.panda.capechanger.g8.screen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import max.panda.capechanger.g8.CapeChanger;
import max.panda.capechanger.g8.CapeChangerClient;
import max.panda.capechanger.g8.cape.CapeFormat;
import max.panda.capechanger.g8.cape.CapeSelection;
import max.panda.capechanger.g8.cape.CustomCape;
import max.panda.capechanger.g8.cape.OwnedCapeFetcher;
import max.panda.capechanger.g8.cape.VanillaCape;
import max.panda.capechanger.g8.network.CapeInventoryPayload;
import max.panda.capechanger.g8.platform.P;
import max.panda.capechanger.g8.platform.PreviewPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;

@Environment(EnvType.CLIENT)
public final class PlayerCapesScreen extends CapeBrowserScreen {
    private static final int TAB_ALL = 0;
    private static final int TAB_CUSTOM = 1;
    private static final int TAB_MICROSOFT = 2;
    private static final int TAB_FAVORITES = 3;
    private static final int TIMEOUT_TICKS = 200;
    private final Player target;
    private final UUID targetId;
    private final String targetName;
    private final Consumer<CapeInventoryPayload> listener = this::onInventory;
    private CapeInventoryPayload data;
    private int status = -1;
    private int activeTab;
    private int waitTicks;
    private boolean requested;
    private CapeSelection targetActive;
    private CapeSelection previewed;
    private String pendingImport;
    private final Set<String> previewRequests = new HashSet<>();
    private final List<String> ownedLoading = new ArrayList<>();

    public PlayerCapesScreen(Player var1) {
        super(Component.translatable("screen.acvcape.player_capes", P.profileName(var1.getGameProfile())), null);
        this.target = var1;
        this.targetId = var1.getUUID();
        this.targetName = P.profileName(var1.getGameProfile());
    }

    @Override
    protected void init() {
        this.prepareLayout(1);
        this.ensurePreviewPlayer();
        ArrayList var1 = new ArrayList();
        var1.add(Component.translatable("tab.acvcape.all"));
        var1.add(Component.translatable("tab.acvcape.custom"));
        var1.add(Component.translatable("tab.acvcape.microsoft"));
        var1.add(Component.translatable("tab.acvcape.favorites"));
        this.buildTabs(var1, this.activeTab, this::switchTab);
        this.buildSearch();
        this.addRenderableWidget(
            Button.builder(Component.translatable("button.acvcape.refresh"), var1x -> this.request())
                .bounds(this.leftX, this.bottomY, (this.leftW - 4) / 2, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("button.acvcape.show_current"), var1x -> this.previewCurrent())
                .bounds(this.leftX + (this.leftW - 4) / 2 + 4, this.bottomY, this.leftW - (this.leftW - 4) / 2 - 4, 20)
                .build()
        );
        this.buildPreviewControls(true);
        if (!this.requested) {
            this.request();
        }

        this.rebuildTiles();
    }

    private void ensurePreviewPlayer() {
        Minecraft var1 = Minecraft.getInstance();
        if (this.previewPlayer == null && var1.level != null) {
            this.previewPlayer = new PreviewPlayerEntity(var1.level, this.target.getGameProfile(), this.target);
            this.previewYaw = 25.0F;
            this.applyPreviewState();
        }
    }

    private void request() {
        this.requested = true;
        this.status = -1;
        this.waitTicks = 0;
        this.data = null;
        CapeChangerClient.instance().requestInventory(this.targetId, this.listener);
        this.rebuildTiles();
    }

    private void onInventory(CapeInventoryPayload var1) {
        this.status = var1.status();
        this.data = var1.status() == 0 ? var1 : null;
        this.targetActive = this.data == null ? null : CapeSelection.decode(this.data.active());
        if (this.data != null) {
            for (CapeInventoryPayload.OwnedEntry var3 : this.data.owned()) {
                this.loadOwned(var3);
            }
        }

        this.rebuildTiles();
    }

    private Identifier ownedTexture(String var1) {
        return Identifier.fromNamespaceAndPath("acvcape", "textures/remote_owned/" + var1.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_") + ".png");
    }

    private void loadOwned(CapeInventoryPayload.OwnedEntry var1) {
        Identifier var2 = this.ownedTexture(var1.id());
        Minecraft var3 = Minecraft.getInstance();
        if (!P.has(var2) && !this.ownedLoading.contains(var1.id())) {
            String var4 = var1.url();
            if (var4 != null && (var4.startsWith("http://textures.minecraft.net/") || var4.startsWith("https://textures.minecraft.net/"))) {
                this.ownedLoading.add(var1.id());
                OwnedCapeFetcher.downloadTexture(var4, var3x -> {
                    if (CapeFormat.inspect(var3x) != null) {
                        CapeMenuScreen.registerOwnedTexture(var2, var3x);
                    }

                    this.ownedLoading.remove(var1.id());
                    this.rebuildTiles();
                }, var2x -> this.ownedLoading.remove(var1.id()));
            }
        }
    }

    private void switchTab(int var1) {
        this.activeTab = Math.max(0, Math.min(3, var1));
        this.contextTile = null;
        this.scrollOffset = 0.0;
        this.setActiveTab(this.activeTab);
        this.rebuildTiles();
    }

    private CapeBrowserScreen.Tile customTile(CapeInventoryPayload.CustomEntry var1) {
        CustomCape var2 = this.manager.getCape(var1.hash());
        Identifier var3 = var2 == null ? null : var2.texture();
        if (var3 == null && this.previewRequests.add(var1.hash())) {
            CapeChangerClient.instance().requestPreview(var1.hash(), this.targetId, this.targetName, var1.name());
        }

        List var4 = CapeMenuScreen.capeInfo(var1.size(), var1.frames());
        if (var2 != null && var2.hasEmissive()) {
            var4 = new ArrayList(var4);
            var4.add(Component.translatable("label.acvcape.emissive"));
        }

        return new CapeBrowserScreen.Tile(var3, var1.name(), null, CapeSelection.customSelection(var1.hash()), false, 0, this.lockedByOwner(), var4);
    }

    private CapeBrowserScreen.Tile ownedTile(CapeInventoryPayload.OwnedEntry var1) {
        Identifier var2 = this.ownedTexture(var1.id());
        boolean var3 = P.has(var2);
        ArrayList var4 = new ArrayList();
        var4.add(Component.translatable("label.acvcape.microsoft_cape"));
        return new CapeBrowserScreen.Tile(var3 ? var2 : null, var1.alias(), null, CapeSelection.ownedSelection(var1.id()), false, 0, false, var4);
    }

    private void rebuildTiles() {
        this.tiles.clear();
        if (this.data != null) {
            switch (this.activeTab) {
                case 1:
                    for (CapeInventoryPayload.CustomEntry var13 : this.data.customs()) {
                        this.tiles.add(this.customTile(var13));
                    }
                    break;
                case 2:
                    for (CapeInventoryPayload.OwnedEntry var12 : this.data.owned()) {
                        this.tiles.add(this.ownedTile(var12));
                    }
                    break;
                case 3:
                    this.addFavorites();
                    break;
                default:
                    for (CapeInventoryPayload.CustomEntry var2 : this.data.customs()) {
                        this.tiles.add(this.customTile(var2));
                    }

                    for (CapeInventoryPayload.OwnedEntry var10 : this.data.owned()) {
                        this.tiles.add(this.ownedTile(var10));
                    }

                    for (String var11 : this.data.favorites()) {
                        CapeSelection var3 = CapeSelection.decode(var11);
                        if (var3.type() == CapeSelection.Type.VANILLA) {
                            VanillaCape var4 = VanillaCape.byId(var3.hash());
                            if (var4 != null) {
                                this.tiles.add(new CapeBrowserScreen.Tile(var4.getTexture(), var4.getDisplayName(), var3, var4.isMissingTexture(), 0));
                            }
                        }
                    }

                    if (this.targetActive != null && this.targetActive.type() == CapeSelection.Type.VANILLA && !this.containsSelection(this.targetActive)) {
                        VanillaCape var7 = VanillaCape.byId(this.targetActive.hash());
                        if (var7 != null) {
                            this.tiles.add(0, new CapeBrowserScreen.Tile(var7.getTexture(), var7.getDisplayName(), this.targetActive, var7.isMissingTexture(), 0));
                        }
                    }
            }
        }

        this.applySearch();
    }

    private boolean containsSelection(CapeSelection var1) {
        for (CapeBrowserScreen.Tile var3 : this.tiles) {
            if (var3.selection().equals(var1)) {
                return true;
            }
        }

        return false;
    }

    private void addFavorites() {
        for (String var2 : this.data.favorites()) {
            CapeSelection var3 = CapeSelection.decode(var2);
            if (var3.type() == CapeSelection.Type.VANILLA) {
                VanillaCape var6 = VanillaCape.byId(var3.hash());
                if (var6 != null) {
                    this.tiles.add(new CapeBrowserScreen.Tile(var6.getTexture(), var6.getDisplayName(), var3, var6.isMissingTexture(), 0));
                }
            } else if (var3.type() == CapeSelection.Type.CUSTOM) {
                for (CapeInventoryPayload.CustomEntry var5 : this.data.customs()) {
                    if (var5.hash().equals(var3.hash())) {
                        this.tiles.add(this.customTile(var5));
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.pendingImport != null && this.manager.getCape(this.pendingImport) != null) {
            this.finishImport(this.pendingImport);
        }

        if (this.status < 0 && this.requested && ++this.waitTicks > 200) {
            this.status = 2;
        }

        if (this.data != null && this.animTick % 10 == 0) {
            boolean var1 = false;

            for (CapeBrowserScreen.Tile var3 : this.tiles) {
                if (var3.texture() == null) {
                    var1 = true;
                    break;
                }
            }

            if (var1) {
                this.rebuildTiles();
            }
        }
    }

    @Override
    protected CapeSelection activeSelection() {
        return this.targetActive;
    }

    @Override
    protected boolean isHighlighted(CapeBrowserScreen.Tile var1) {
        return this.previewed != null && var1.selection().equals(this.previewed);
    }

    @Override
    protected Component gridMessage() {
        if (this.status < 0) {
            return Component.translatable("label.acvcape.inventory_loading");
        }

        switch (this.status) {
            case 0:
                return null;
            case 1:
                return Component.translatable("label.acvcape.inventory_private", this.targetName);
            case 2:
            default:
                return Component.translatable("label.acvcape.inventory_unavailable", this.targetName);
            case 3:
                return Component.translatable("label.acvcape.inventory_offline", this.targetName);
            case 4:
                return Component.translatable("label.acvcape.inventory_server", this.targetName);
        }
    }

    @Override
    protected Component emptyMessage() {
        return !this.searchQuery.isBlank() ? Component.translatable("label.acvcape.search_empty") : Component.translatable("label.acvcape.inventory_empty");
    }

    private boolean lockedByOwner() {
        return this.data != null && (this.data.flags() & 1) != 0;
    }

    @Override
    protected List<CapeBrowserScreen.ContextAction> contextActionsFor(CapeBrowserScreen.Tile var1) {
        return var1.selection().type() == CapeSelection.Type.CUSTOM && !var1.locked()
            ? List.of(new CapeBrowserScreen.ContextAction(Component.translatable("menu.acvcape.import_custom"), () -> this.importCustom(var1)))
            : List.of();
    }

    private void importCustom(CapeBrowserScreen.Tile var1) {
        String var2 = var1.selection().hash();
        CustomCape var3 = this.manager.findDiskCape(var2);
        if (var3 != null) {
            this.showStatus(Component.translatable("label.acvcape.already_imported", var3.displayName()));
        } else {
            CustomCape var4 = this.manager.getCape(var2);
            if (var4 == null) {
                this.pendingImport = var2;
                this.previewRequests.add(var2);
                CapeChangerClient.instance().requestPreview(var2, this.targetId, this.targetName, var1.label());
                this.showStatus(Component.translatable("label.acvcape.import_waiting"));
            } else {
                this.finishImport(var2);
            }
        }
    }

    private void finishImport(String var1) {
        this.pendingImport = null;

        try {
            CustomCape var2 = this.manager.importDownloaded(var1);
            this.showStatus(Component.translatable("label.acvcape.imported", var2.displayName()));
        } catch (Exception var3) {
            CapeChanger.LOGGER.error("Falha ao importar capa do inventario de outro jogador", var3);
            this.showStatus(Component.translatable("acvcape.error.import_failed"));
        }
    }

    @Override
    protected void onTileClicked(CapeBrowserScreen.Tile var1, int var2) {
        if (var1.texture() != null && this.previewPlayer != null) {
            this.previewed = var1.selection();
            this.previewPlayer.setForcedCape(var1.texture());
        }
    }

    private void previewCurrent() {
        this.previewed = null;
        if (this.previewPlayer != null) {
            this.previewPlayer.setForcedCape(null);
        }
    }

    @Override
    protected void renderExtras(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
        String var5;
        if (this.previewed != null) {
            var5 = this.previewLabel(this.previewed);
        } else {
            var5 = Component.translatable("label.acvcape.current_cape").getString();
        }

        P.centered(var1, this.font, this.font.plainSubstrByWidth(var5, this.previewW - 8), this.previewX + this.previewW / 2, this.previewY + 5, -14006);
        if (this.target instanceof AbstractClientPlayer var6 && this.previewed == null) {
            PlayerSkin var8 = P.skinOf(var6);
            if (var8 == null || P.capeTexture(var8) == null) {
                P.centered(
                    var1, this.font, Component.translatable("label.acvcape.no_visible_cape"), this.previewX + this.previewW / 2, this.previewY + 17, -6250326
                );
            }
        }
    }

    private String previewLabel(CapeSelection var1) {
        for (CapeBrowserScreen.Tile var3 : this.tiles) {
            if (var3.selection().equals(var1)) {
                return var3.label();
            }
        }

        return this.manager.displayName(var1);
    }

    @Override
    public void removed() {
        CapeChangerClient.instance().cancelInventory(this.listener);
        super.removed();
    }
}
