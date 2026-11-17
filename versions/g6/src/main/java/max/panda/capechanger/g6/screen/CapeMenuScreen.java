package max.panda.capechanger.g6.screen;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import max.panda.capechanger.g6.CapeChanger;
import max.panda.capechanger.g6.CapeChangerClient;
import max.panda.capechanger.g6.cape.CapeSelection;
import max.panda.capechanger.g6.cape.CapeStorage;
import max.panda.capechanger.g6.cape.CustomCape;
import max.panda.capechanger.g6.cape.DownloadPolicy;
import max.panda.capechanger.g6.cape.OwnedCapeFetcher;
import max.panda.capechanger.g6.cape.VanillaCape;
import max.panda.capechanger.g6.platform.P;
import max.panda.capechanger.g6.platform.PreviewPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public final class CapeMenuScreen extends CapeBrowserScreen {
    private static final int TAB_ALL = 0;
    private static final int TAB_JAVA = 1;
    private static final int TAB_BEDROCK = 2;
    private static final int TAB_DUNGEONS = 3;
    private static final int TAB_OWNED = 4;
    private static final int TAB_CUSTOM = 5;
    private static final int TAB_FAVORITES = 6;
    private static final int TAB_OTHERS = 7;
    private static final int TAB_EMISSIVE = 8;
    private static final int TAB_COUNT = 9;
    private static final String[] TAB_KEYS = new String[]{
        "tab.acvcape.all",
        "tab.acvcape.java",
        "tab.acvcape.bedrock",
        "tab.acvcape.dungeons",
        "tab.acvcape.owned",
        "tab.acvcape.custom",
        "tab.acvcape.favorites",
        "tab.acvcape.others",
        "tab.acvcape.emissive"
    };
    private static final int[] TAB_ORDER = new int[]{0, 1, 2, 3, 4, 6, 8, 5, 7};
    private static int lastTab;
    private final List<CapeBrowserScreen.Tile> allTiles = new ArrayList<>();
    private int activeTab;
    private ButtonWidget downloadPolicyButton;
    private ButtonWidget emissiveButton;
    private CapeMenuScreen.OwnedState ownedState = CapeMenuScreen.OwnedState.IDLE;
    private String ownedError = "";
    private final List<CapeMenuScreen.OwnedTile> ownedTiles = new ArrayList<>();
    private boolean pendingRescale;
    private boolean mouseHeld;

    public CapeMenuScreen(Screen var1) {
        super(Text.translatable("screen.acvcape.title"), var1);
        this.activeTab = lastTab;
        this.tabRowSplit = 6;
    }

    @Override
    protected void init() {
        this.prepareLayout(3);
        this.ensurePreviewPlayer();
        ArrayList var1 = new ArrayList();

        for (int var5 : TAB_ORDER) {
            var1.add(Text.translatable(TAB_KEYS[var5]));
        }

        this.buildTabs(var1, displayOf(this.activeTab), var1x -> this.switchTab(TAB_ORDER[var1x]));
        this.buildSearch();
        int var10 = (this.leftW - 8) / 3;
        int var11 = this.leftW - (var10 + 4) * 2;
        int var12 = this.bottomY - 48;
        int var13 = this.bottomY - 24;
        int var6 = this.bottomY;
        int var7 = this.leftX + var10 + 4;
        int var8 = this.leftX + (var10 + 4) * 2;
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("button.acvcape.open_folder"), var1x -> this.openFolder()).dimensions(this.leftX, var12, var10, 20).build()
        );
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("button.acvcape.import"), var1x -> this.openImport()).dimensions(var7, var12, var10, 20).build()
        );
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("button.acvcape.refresh"), var1x -> this.refresh()).dimensions(var8, var12, var11, 20).build()
        );
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("button.acvcape.no_cape"), var1x -> this.select(CapeSelection.noneSelection()))
                .dimensions(this.leftX, var13, var10, 20)
                .build()
        );
        this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("button.acvcape.reset"), var1x -> this.select(this.manager.defaultTarget()))
                .dimensions(var7, var13, var10, 20)
                .build()
        );
        this.downloadPolicyButton = this.addDrawableChild(
            ButtonWidget.builder(this.downloadPolicyLabel(), var1x -> this.cycleDownloadPolicy()).dimensions(var8, var13, var11, 20).build()
        );
        P.tooltip(this.downloadPolicyButton, Text.translatable("tooltip.acvcape.download_policy"));
        this.addDrawableChild(new AnimationSpeedSlider(this.leftX, var6, var10, 20, this.manager));
        this.addDrawableChild(
            new IntSlider(
                    var7,
                    var6,
                    var10,
                    20,
                    75,
                    150,
                    5,
                    this.manager.getConfig().getMenuScale(),
                    var0 -> Text.translatable("slider.acvcape.menu_scale", var0 + "%"),
                    var1x -> {
                        if (var1x != this.manager.getConfig().getMenuScale()) {
                            this.manager.getConfig().setMenuScale(var1x);
                            this.pendingRescale = true;
                        }
                    }
                )
                .withTooltip(Text.translatable("tooltip.acvcape.menu_scale"))
        );
        ButtonWidget var9 = this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("button.acvcape.privacy"), var1x -> this.openPrivacy()).dimensions(var8, var6, var11, 20).build()
        );
        P.tooltip(var9, Text.translatable("tooltip.acvcape.privacy"));
        this.buildPreviewControls(true);
        this.emissiveButton = this.addDrawableChild(
            ButtonWidget.builder(Text.translatable("button.acvcape.emissive"), var1x -> this.openEmissive())
                .dimensions(this.rightX, this.rightRow(2), this.rightW, 20)
                .build()
        );
        this.updateEmissiveButton();
        this.buildAllTiles();
        this.switchTab(this.activeTab);
    }

    private void ensurePreviewPlayer() {
        MinecraftClient var1 = MinecraftClient.getInstance();
        if (this.previewPlayer == null) {
            this.previewPlayer = PreviewPlayerEntity.create();
            this.applyPreviewState();
        }
    }

    private void buildAllTiles() {
        this.allTiles.clear();

        for (VanillaCape var4 : VanillaCape.values()) {
            this.allTiles
                .add(
                    new CapeBrowserScreen.Tile(
                        var4.getTexture(), var4.getDisplayName(), CapeSelection.vanillaSelection(var4.getId()), var4.isMissingTexture(), this.categoriesFor(var4)
                    )
                );
        }
    }

    private int categoriesFor(VanillaCape var1) {
        byte var2 = 0;
        if (var1.isInCategory(VanillaCape.Category.JAVA)) {
            var2 = (byte)(var2 | 1);
        }

        if (var1.isInCategory(VanillaCape.Category.BEDROCK)) {
            var2 = (byte)(var2 | 2);
        }

        if (var1.isInCategory(VanillaCape.Category.DUNGEONS)) {
            var2 = (byte)(var2 | 4);
        }

        return var2;
    }

    private static int displayOf(int var0) {
        for (int var1 = 0; var1 < TAB_ORDER.length; var1++) {
            if (TAB_ORDER[var1] == var0) {
                return var1;
            }
        }

        return 0;
    }

    private void switchTab(int var1) {
        lastTab = this.activeTab = Math.max(0, Math.min(8, var1));
        this.contextTile = null;
        this.scrollOffset = 0.0;
        this.setActiveTab(displayOf(this.activeTab));
        this.searchField.setVisible(this.activeTab != 4);
        this.rebuildTiles();
        if (this.activeTab == 4 && this.ownedState == CapeMenuScreen.OwnedState.IDLE) {
            this.fetchOwnedCapes();
        }
    }

    public static List<Text> capeInfo(int var0, int var1) {
        return capeInfo(var0, var1, false);
    }

    public static List<Text> capeInfo(CustomCape var0) {
        return capeInfo(var0.size(), var0.frames(), var0.hasEmissive());
    }

    public static List<Text> capeInfo(int var0, int var1, boolean var2) {
        ArrayList var3 = new ArrayList();
        if (var0 > 0) {
            var3.add(Text.translatable("label.acvcape.size", formatSize(var0)));
        }

        if (var1 > 1) {
            var3.add(Text.translatable("label.acvcape.animated_frames", var1));
        } else if (var1 == 1) {
            var3.add(Text.translatable("label.acvcape.static"));
        }

        if (var2) {
            var3.add(Text.translatable("label.acvcape.emissive"));
        }

        return var3;
    }

    public static String formatSize(long var0) {
        return var0 >= 1048576L ? String.format(Locale.ROOT, "%.2f MB", var0 / 1048576.0) : String.format(Locale.ROOT, "%.2f KB", var0 / 1024.0);
    }

    private void rebuildTiles() {
        this.tiles.clear();
        switch (this.activeTab) {
            case 0:
                this.tiles.addAll(this.allTiles);
                this.addCustomTiles();
                break;
            case 1:
            case 2:
            case 3:
            default:
                byte var1 = switch (this.activeTab) {
                    case 2 -> 2;
                    case 3 -> 4;
                    default -> 1;
                };

                for (CapeBrowserScreen.Tile var5 : this.allTiles) {
                    if ((var5.categoryMask() & var1) != 0) {
                        this.tiles.add(var5);
                    }
                }
                break;
            case 4:
                for (CapeMenuScreen.OwnedTile var3 : this.ownedTiles) {
                    this.tiles.add(new CapeBrowserScreen.Tile(var3.texture(), var3.alias(), var3.selection(), false, 0));
                }
                break;
            case 5:
                this.addCustomTiles(false);
                break;
            case 6:
                this.addFavoriteTiles();
                break;
            case 7:
                this.addOtherPlayerTiles();
                break;
            case 8:
                this.addCustomTiles(true);
        }

        this.applySearch();
    }

    private CapeBrowserScreen.Tile customTile(CustomCape var1) {
        return new CapeBrowserScreen.Tile(var1.texture(), var1.displayName(), null, CapeSelection.customSelection(var1.hash()), false, 0, false, capeInfo(var1));
    }

    private void addCustomTiles() {
        for (CustomCape var2 : this.manager.getAllCapes().values()) {
            this.tiles.add(this.customTile(var2));
        }
    }

    private void addCustomTiles(boolean var1) {
        for (CustomCape var3 : this.manager.getAllCapes().values()) {
            if (this.manager.isEmissiveCape(var3.hash()) == var1) {
                this.tiles.add(this.customTile(var3));
            }
        }
    }

    private void addOtherPlayerTiles() {
        ArrayList var1 = new ArrayList();

        for (CustomCape var3 : this.manager.getDownloadedCapes()) {
            var1.add(var3.hash());
            this.tiles
                .add(
                    new CapeBrowserScreen.Tile(
                        var3.texture(),
                        var3.displayName(),
                        var3.origin() != null && !var3.origin().isEmpty() ? var3.origin() : "?",
                        CapeSelection.customSelection(var3.hash()),
                        false,
                        0,
                        false,
                        capeInfo(var3)
                    )
                );
        }

        for (CustomCape var5 : this.manager.getTransientCapes()) {
            if (!var1.contains(var5.hash())
                && var5.origin() != null
                && !var5.origin().isEmpty()
                && var5.isLocked()
                && CapeChangerClient.instance().isWornByRemote(var5.hash())) {
                this.tiles
                    .add(
                        new CapeBrowserScreen.Tile(
                            var5.texture(), var5.displayName(), var5.origin(), CapeSelection.customSelection(var5.hash()), false, 0, true, capeInfo(var5)
                        )
                    );
            }
        }
    }

    private void addFavoriteTiles() {
        for (CapeSelection var2 : this.manager.getFavorites()) {
            if (var2.type() == CapeSelection.Type.CUSTOM) {
                CustomCape var5 = this.manager.getCape(var2.hash());
                if (var5 != null && !var5.isLocked()) {
                    this.tiles.add(this.customTile(var5));
                }
            } else {
                for (CapeBrowserScreen.Tile var4 : this.allTiles) {
                    if (var4.selection().equals(var2)) {
                        this.tiles.add(var4);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected CapeSelection activeSelection() {
        return this.manager.getActiveSelection();
    }

    @Override
    protected boolean showsFavorite(CapeBrowserScreen.Tile var1) {
        return this.activeTab == 8 ? true : this.activeTab != 4 && this.activeTab != 7;
    }

    @Override
    protected Text emptyMessage() {
        if (!this.searchQuery.isBlank() && this.activeTab != 4) {
            return Text.translatable("label.acvcape.search_empty");
        }

        switch (this.activeTab) {
            case 4:
                return Text.translatable("label.acvcape.owned_empty");
            case 5:
                return Text.translatable("label.acvcape.custom_empty");
            case 6:
                return Text.translatable("label.acvcape.favorites_empty");
            case 7:
                return Text.translatable("label.acvcape.others_empty");
            case 8:
                return Text.translatable("label.acvcape.emissive_empty");
            default:
                return Text.translatable("label.acvcape.search_empty");
        }
    }

    @Override
    protected Text gridMessage() {
        if (this.activeTab != 4) {
            return null;
        }

        switch (this.ownedState) {
            case IDLE:
            case LOADING:
                return Text.translatable("label.acvcape.owned_loading");
            case DONE:
            default:
                return null;
            case ERROR:
                return Text.translatable("label.acvcape.owned_error").append(" ").append(this.ownedError);
        }
    }

    @Override
    protected void onTileClicked(CapeBrowserScreen.Tile var1, int var2) {
        if (var1.locked()) {
            this.showStatus(Text.translatable("label.acvcape.locked"));
        } else {
            if (this.activeTab == 4) {
                CapeMenuScreen.OwnedTile var3 = this.ownedTileFor(var1);
                if (var3 != null) {
                    this.selectOwned(var3);
                    return;
                }
            }

            this.select(var1.selection());
        }
    }

    @Override
    protected List<CapeBrowserScreen.ContextAction> contextActionsFor(CapeBrowserScreen.Tile var1) {
        if (this.activeTab == 4) {
            return List.of();
        }

        if (this.activeTab == 7) {
            if (var1.locked()) {
                return List.of();
            }

            ArrayList var5 = new ArrayList();
            var5.add(new CapeBrowserScreen.ContextAction(Text.translatable("menu.acvcape.import_custom"), () -> this.importOther(var1)));
            var5.add(this.defaultAction(var1));
            var5.add(new CapeBrowserScreen.ContextAction(Text.translatable("menu.acvcape.delete"), () -> this.confirmDelete(var1, true)));
            return var5;
        } else {
            CapeSelection.Type var2 = var1.selection().type();
            if (var2 != CapeSelection.Type.VANILLA && var2 != CapeSelection.Type.CUSTOM) {
                return List.of();
            }

            boolean var3 = this.manager.isFavorite(var1.selection());
            ArrayList var4 = new ArrayList();
            var4.add(
                new CapeBrowserScreen.ContextAction(Text.translatable(var3 ? "menu.acvcape.unfavorite" : "menu.acvcape.favorite"), () -> this.toggleFavorite(var1))
            );
            var4.add(this.defaultAction(var1));
            if (var2 == CapeSelection.Type.CUSTOM && !var1.locked() && this.manager.findDiskCape(var1.selection().hash()) != null) {
                var4.add(new CapeBrowserScreen.ContextAction(Text.translatable("menu.acvcape.delete"), () -> this.confirmDelete(var1, false)));
            }

            return var4;
        }
    }

    private CapeBrowserScreen.ContextAction defaultAction(CapeBrowserScreen.Tile var1) {
        CapeSelection var2 = var1.selection();
        return this.manager.isDefaultCape(var2) ? new CapeBrowserScreen.ContextAction(Text.translatable("menu.acvcape.unset_default"), () -> {
            this.manager.setDefaultCape(null);
            this.showStatus(Text.translatable("label.acvcape.default_cleared"));
        }) : new CapeBrowserScreen.ContextAction(Text.translatable("menu.acvcape.set_default"), () -> {
            this.manager.setDefaultCape(var2);
            this.showStatus(Text.translatable("label.acvcape.default_set", var1.label()));
        });
    }

    @Override
    protected boolean isDefaultTile(CapeBrowserScreen.Tile var1) {
        return this.manager.isDefaultCape(var1.selection());
    }

    private void confirmDelete(CapeBrowserScreen.Tile var1, boolean var2) {
        this.client.setScreen(new ConfirmScreen(var3 -> {
            if (var3) {
                if (var2) {
                    this.removeOther(var1);
                } else {
                    this.deleteCustom(var1);
                }
            }

            this.client.setScreen(this);
        }, Text.translatable("screen.acvcape.delete_title"), Text.translatable("screen.acvcape.delete_message", var1.label())));
    }

    private void deleteCustom(CapeBrowserScreen.Tile var1) {
        if (this.manager.deleteCustom(var1.selection().hash())) {
            this.showStatus(Text.translatable("label.acvcape.deleted"));
        }

        this.rebuildTiles();
        this.updateEmissiveButton();
    }

    private void importOther(CapeBrowserScreen.Tile var1) {
        try {
            CustomCape var2 = this.manager.importDownloaded(var1.selection().hash());
            this.showStatus(Text.translatable("label.acvcape.imported", var2.displayName()));
        } catch (Exception var3) {
            CapeChanger.LOGGER.error("Falha ao importar capa de outro jogador", var3);
            this.showStatus(Text.translatable("acvcape.error.import_failed"));
        }
    }

    private void removeOther(CapeBrowserScreen.Tile var1) {
        if (this.manager.removeDownloaded(var1.selection().hash())) {
            this.showStatus(Text.translatable("label.acvcape.deleted"));
        }

        this.rebuildTiles();
        this.updateEmissiveButton();
    }

    private void toggleFavorite(CapeBrowserScreen.Tile var1) {
        CapeSelection var2 = var1.selection();
        this.manager.setFavorite(var2, !this.manager.isFavorite(var2));
        if (this.activeTab == 6) {
            this.rebuildTiles();
        }
    }

    private void select(CapeSelection var1) {
        CapeChanger.LOGGER.info("Capa selecionada no menu: " + var1.encode());
        this.manager.setActiveSelection(var1);
        CapeChangerClient var2 = CapeChangerClient.instance();
        if (var1.type() != CapeSelection.Type.CUSTOM && var1.type() != CapeSelection.Type.OWNED) {
            var2.clearCommitPending();
            var2.sendSelectionToServer(var1);
        } else {
            var2.markCommitPending();
        }

        this.updateEmissiveButton();
    }

    private void selectOwned(CapeMenuScreen.OwnedTile var1) {
        CapeSelection var2 = var1.selection();
        this.select(var2);
        OwnedCapeFetcher.activateCapeAsync(var2.hash(), () -> {}, var0 -> CapeChanger.LOGGER.warn("Falha ao ativar capa na conta: " + var0));
    }

    private boolean canEditEmissive() {
        CapeSelection var1 = this.manager.getActiveSelection();
        switch (var1.type()) {
            case VANILLA:
                return VanillaCape.byId(var1.hash()) != null;
            case CUSTOM:
                CustomCape var2 = this.manager.getCape(var1.hash());
                return var2 != null && !var2.isLocked();
            case OWNED:
                return this.manager.getOwnedBytes(var1.hash()) != null;
            default:
                return false;
        }
    }

    private void updateEmissiveButton() {
        if (this.emissiveButton != null) {
            boolean var1 = this.canEditEmissive();
            this.emissiveButton.active = var1;
        }
    }

    private void openEmissive() {
        if (this.canEditEmissive()) {
            this.client.setScreen(new EmissiveEditorScreen(this, this.manager.getActiveSelection()));
        }
    }

    private void openPrivacy() {
        this.client.setScreen(new CapePrivacyScreen(this));
    }

    private void openFolder() {
        Util.getOperatingSystem().open(this.manager.getStorageDirectory().toFile());
    }

    private void openImport() {
        this.client.setScreen(new CapeImportScreen(this));
    }

    private void refresh() {
        this.manager.refreshCapes();
        if (this.activeTab == 4) {
            this.ownedState = CapeMenuScreen.OwnedState.IDLE;
            this.ownedTiles.clear();
            this.fetchOwnedCapes();
        } else {
            this.rebuildTiles();
        }

        this.updateEmissiveButton();
    }

    private void cycleDownloadPolicy() {
        this.manager.setDownloadPolicy(this.manager.getDownloadPolicy().next());
        this.downloadPolicyButton.setMessage(this.downloadPolicyLabel());
    }

    private Text downloadPolicyLabel() {
        DownloadPolicy var1 = this.manager.getDownloadPolicy();
        return Text.translatable("button.acvcape.download_policy", Text.translatable("option.acvcape.download_policy." + var1.name().toLowerCase(Locale.ROOT)));
    }

    @Override
    protected void renderExtras(DrawContext var1, int var2, int var3, float var4) {
        CapeSelection var5 = this.manager.getActiveSelection();

        MutableText var6 = switch (var5.type()) {
            case NONE -> Text.translatable("button.acvcape.no_cape");
            case DEFAULT -> Text.translatable("button.acvcape.reset");
            default -> Text.literal(this.manager.displayName(var5));
        };
        String var7 = this.textRenderer.trimToWidth(var6.getString(), this.previewW - 8);
        P.centered(var1, this.textRenderer, var7, this.previewX + this.previewW / 2, this.previewY + 5, -14006);
    }

    private void fetchOwnedCapes() {
        this.ownedState = CapeMenuScreen.OwnedState.LOADING;
        OwnedCapeFetcher.fetchAsync(new OwnedCapeFetcher.FetchCallback() {
            @Override
            public void onSuccess(List<OwnedCapeFetcher.OwnedCape> var1) {
                CapeMenuScreen.this.manager.setOwnedCache(var1);
                CapeMenuScreen.this.ownedTiles.clear();
                if (var1.isEmpty()) {
                    CapeMenuScreen.this.finishOwned();
                } else {
                    int[] var2 = new int[]{var1.size()};

                    for (OwnedCapeFetcher.OwnedCape var4 : var1) {
                        String var5 = var4.textureUrl;
                        if (var5 != null && !var5.isBlank()) {
                            OwnedCapeFetcher.downloadTexture(var5, var3 -> {
                                try {
                                    String var4x = var4.id;
                                    CapeSelection var5x = CapeSelection.ownedSelection(var4x);
                                    Identifier var6 = Identifier.of("acvcape", "textures/owned/" + var4x.toLowerCase(Locale.ROOT) + ".png");
                                    CapeMenuScreen.registerOwnedTexture(var6, var3);
                                    CapeMenuScreen.this.manager.registerOwnedCape(var4x, var4.alias, var6, var3);
                                    CapeMenuScreen.this.ownedTiles.add(new CapeMenuScreen.OwnedTile(var6, var4.alias, var5x, var4.active));
                                } catch (Exception var7) {
                                    CapeChanger.LOGGER.warn("Falha ao preparar capa da conta: " + var7.getMessage());
                                }

                                var2[0]--;
                                if (var2[0] == 0) {
                                    CapeMenuScreen.this.finishOwned();
                                }
                            }, var2x -> {
                                var2[0]--;
                                if (var2[0] == 0) {
                                    CapeMenuScreen.this.finishOwned();
                                }
                            });
                        } else {
                            var2[0]--;
                            if (var2[0] == 0) {
                                CapeMenuScreen.this.finishOwned();
                            }
                        }
                    }
                }
            }

            @Override
            public void onError(String var1) {
                CapeMenuScreen.this.ownedState = CapeMenuScreen.OwnedState.ERROR;
                CapeMenuScreen.this.ownedError = var1;
            }
        });
    }

    private void finishOwned() {
        this.ownedState = CapeMenuScreen.OwnedState.DONE;
        if (this.activeTab == 4) {
            this.rebuildTiles();
        }

        this.updateEmissiveButton();
    }

    public static void registerOwnedTexture(Identifier var0, byte[] var1) {
        MinecraftClient var2 = MinecraftClient.getInstance();
        if (var2 != null && var2.getTextureManager() != null && !P.has(var0)) {
            try {
                byte[] var3 = CapeStorage.normalize(var1);
                NativeImage var4 = NativeImage.read(new ByteArrayInputStream(var3));
                P.register(var0, P.newTexture(var4));
            } catch (Exception var5) {
                CapeChanger.LOGGER.error("Falha ao registrar textura de capa da conta", var5);
            }
        }
    }

    private CapeMenuScreen.OwnedTile ownedTileFor(CapeBrowserScreen.Tile var1) {
        for (CapeMenuScreen.OwnedTile var3 : this.ownedTiles) {
            if (var3.selection().equals(var1.selection())) {
                return var3;
            }
        }

        return null;
    }

    @Override
    protected boolean onMouseDown(double var1, double var3, int var5) {
        this.mouseHeld = true;
        return super.onMouseDown(var1, var3, var5);
    }

    @Override
    protected boolean onMouseUp(double var1, double var3, int var5) {
        this.mouseHeld = false;
        boolean var6 = super.onMouseUp(var1, var3, var5);
        this.applyPendingRescale();
        return var6;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.mouseHeld) {
            this.applyPendingRescale();
        }
    }

    private void applyPendingRescale() {
        if (this.pendingRescale) {
            this.pendingRescale = false;
            this.manager.getConfig().save();
            this.rescale();
        }
    }

    @Override
    public void close() {
        CapeChangerClient.instance().flushCommit();
        super.close();
    }

    @Override
    public void removed() {
        this.manager.getConfig().save();
        super.removed();
    }

    private enum OwnedState {
        IDLE,
        LOADING,
        DONE,
        ERROR;
    }

    private record OwnedTile(Identifier texture, String alias, CapeSelection selection, boolean active) {
    }
}
