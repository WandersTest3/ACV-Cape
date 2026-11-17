package max.panda.capechanger.g6.screen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntConsumer;
import max.panda.capechanger.g6.CapeChangerClient;
import max.panda.capechanger.g6.cape.CapeManager;
import max.panda.capechanger.g6.cape.CapeSelection;
import max.panda.capechanger.g6.compat.WaveyCompat;
import max.panda.capechanger.g6.platform.AcvScreen;
import max.panda.capechanger.g6.platform.P;
import max.panda.capechanger.g6.platform.PreviewPlayerEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public abstract class CapeBrowserScreen extends AcvScreen {
    protected static final int PAD = 8;
    protected static final int GAP = 4;
    protected static final int BTN_H = 20;
    protected static final int ROW = 24;
    protected static final int TILE_W = 86;
    protected static final int TILE_H = 110;
    protected static final int TILE_GAP = 4;
    protected static final int COLOR_BACKGROUND = -1090519040;
    protected static final int COLOR_HEADER = -15987696;
    protected static final int COLOR_PANEL = -871362540;
    protected static final int COLOR_PANEL_EDGE = -14013901;
    protected static final int COLOR_TILE = -15000798;
    protected static final int COLOR_TILE_HOVER = -14013898;
    protected static final int COLOR_TILE_EDGE = -13882315;
    protected static final int COLOR_TILE_EDGE_HOVER = -11184801;
    protected static final int COLOR_SELECTED = -14006;
    protected static final int COLOR_ACCENT = -10777345;
    protected static final int COLOR_TEXT = -1;
    protected static final int COLOR_TEXT_DIM = -6250326;
    protected static final int COLOR_WARNING = -30652;
    protected static final int COLOR_LOCK = -2075307;
    private static boolean playerCrouching;
    private static boolean playerWalking = true;
    private static boolean playerElytra;
    private static boolean playerCapeOnly;
    protected final Screen parent;
    protected final CapeManager manager;
    protected final List<CapeBrowserScreen.Tile> tiles = new ArrayList<>();
    protected final List<CapeBrowserScreen.Tile> filteredTiles = new ArrayList<>();
    protected final List<ButtonWidget> tabButtons = new ArrayList<>();
    protected String searchQuery = "";
    protected double scrollOffset;
    protected int maxScroll;
    protected int columns;
    protected TextFieldWidget searchField;
    protected ButtonWidget crouchButton;
    protected ButtonWidget walkButton;
    protected ButtonWidget elytraButton;
    protected ButtonWidget capeOnlyButton;
    protected PreviewPlayerEntity previewPlayer;
    private PreviewPlayerEntity iconPlayer;
    protected float previewYaw = 25.0F;
    protected boolean draggingPreview;
    protected boolean draggingScrollbar;
    protected int animTick;
    protected int leftX;
    protected int leftW;
    protected int rightX;
    protected int rightW;
    protected int gridX;
    protected int gridY;
    protected int gridW;
    protected int gridH;
    protected int previewX;
    protected int previewY;
    protected int previewW;
    protected int previewH;
    protected int footerY;
    protected int bottomY;
    protected CapeBrowserScreen.Tile contextTile;
    protected List<CapeBrowserScreen.ContextAction> contextActions = List.of();
    protected int contextX;
    protected int contextY;
    protected Text statusText;
    protected int statusTicks;
    protected int hoverMouseX;
    protected int hoverMouseY;
    protected boolean badgeHover;
    protected int tabRowSplit;

    protected CapeBrowserScreen(Text var1, Screen var2) {
        super(var1);
        this.parent = var2;
        this.manager = CapeChangerClient.instance().getManager();
    }

    protected void prepareLayout(int var1) {
        this.applyMenuScale();
        int var2 = this.width - 16;
        this.rightW = Math.max(130, Math.min(200, var2 * 32 / 100));
        this.leftW = var2 - this.rightW - 8;
        this.leftX = 8;
        this.rightX = this.leftX + this.leftW + 8;
        this.bottomY = this.height - 8 - 20;
        this.footerY = var1 > 0 ? this.bottomY - (var1 - 1) * 24 : this.bottomY + 24;
        this.gridX = this.leftX;
        this.gridY = 74 + this.tabRowOffset();
        this.gridW = this.leftW;
        this.gridH = Math.max(60, this.footerY - 4 - this.gridY);
        this.previewX = this.rightX;
        this.previewY = 28;
        this.previewW = this.rightW;
        this.previewH = Math.max(60, Math.min(this.previewW, this.bottomY - 72 - 4 - this.previewY));
        this.columns = Math.max(1, (this.gridW - 8 + 4) / 90);
    }

    protected int rightRow(int var1) {
        return this.bottomY - (3 - var1) * 24;
    }

    protected int tabRowOffset() {
        return this.tabRowSplit > 0 ? 24 : 0;
    }

    protected void buildTabs(List<Text> var1, int var2, IntConsumer var3) {
        this.tabButtons.clear();
        if (this.tabRowSplit > 0 && this.tabRowSplit < var1.size()) {
            this.buildTabRow(var1.subList(0, this.tabRowSplit), 0, 28, var2, var3);
            this.buildTabRow(var1.subList(this.tabRowSplit, var1.size()), this.tabRowSplit, 52, var2, var3);
        } else {
            this.buildTabRow(var1, 0, 28, var2, var3);
        }
    }

    private void buildTabRow(List<Text> var1, int var2, int var3, int var4, IntConsumer var5) {
        int var6 = var1.size();
        int var7 = this.leftW - 4 * (var6 - 1);
        int[] var8 = new int[var6];
        int var9 = 0;

        for (int var10 = 0; var10 < var6; var10++) {
            var8[var10] = this.textRenderer.getWidth(((Text)var1.get(var10)).getString()) + 12;
            var9 += var8[var10];
        }

        int var16 = this.leftX;
        int var11 = 0;

        for (int var12 = 0; var12 < var6; var12++) {
            int var13 = var12 == var6 - 1 ? var7 - var11 : Math.max(20, var8[var12] * var7 / Math.max(1, var9));
            int var14 = var12 + var2;
            ButtonWidget var15 = ButtonWidget.builder((Text)var1.get(var12), var2x -> var5.accept(var14)).dimensions(var16, var3, var13, 20).build();
            var15.active = var14 != var4;
            this.tabButtons.add(this.addDrawableChild(var15));
            var16 += var13 + 4;
            var11 += var13;
        }
    }

    protected void setActiveTab(int var1) {
        for (int var2 = 0; var2 < this.tabButtons.size(); var2++) {
            this.tabButtons.get(var2).active = var2 != var1;
        }
    }

    protected void buildSearch() {
        this.searchField = new TextFieldWidget(
            this.textRenderer, this.leftX + 1, 53 + this.tabRowOffset(), this.leftW - 2, 16, Text.translatable("label.acvcape.search")
        );
        this.searchField.setMaxLength(64);
        this.searchField.setPlaceholder(Text.translatable("label.acvcape.search_placeholder"));
        this.searchField.setText(this.searchQuery);
        this.searchField.setChangedListener(var1 -> {
            this.searchQuery = var1;
            this.applySearch();
        });
        this.addDrawableChild(this.searchField);
    }

    protected void buildPreviewControls(boolean var1) {
        int var2 = (this.rightW - 4) / 2;
        this.crouchButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), var1x -> {
            playerCrouching = !playerCrouching;
            this.applyPreviewState();
        }).dimensions(this.rightX, this.rightRow(0), var2, 20).build());
        this.walkButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), var1x -> {
            playerWalking = !playerWalking;
            this.applyPreviewState();
        }).dimensions(this.rightX + var2 + 4, this.rightRow(0), this.rightW - var2 - 4, 20).build());
        this.elytraButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), var1x -> {
            playerElytra = !playerElytra;
            this.applyPreviewState();
        }).dimensions(this.rightX, this.rightRow(1), var2, 20).build());
        this.capeOnlyButton = this.addDrawableChild(ButtonWidget.builder(Text.empty(), var1x -> {
            playerCapeOnly = !playerCapeOnly;
            this.applyPreviewState();
        }).dimensions(this.rightX + var2 + 4, this.rightRow(1), this.rightW - var2 - 4, 20).build());
        if (var1) {
            this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, var1x -> this.close()).dimensions(this.rightX, this.rightRow(3), this.rightW, 20).build());
        }

        this.refreshToggleLabels();
    }

    protected void applySearch() {
        this.filteredTiles.clear();
        String var1 = this.searchQuery.trim().toLowerCase(Locale.ROOT);

        for (CapeBrowserScreen.Tile var3 : this.tiles) {
            if (var1.isEmpty()
                || var3.label().toLowerCase(Locale.ROOT).contains(var1)
                || var3.subLabel() != null && var3.subLabel().toLowerCase(Locale.ROOT).contains(var1)) {
                this.filteredTiles.add(var3);
            }
        }

        int var4 = (this.filteredTiles.size() + this.columns - 1) / this.columns;
        int var5 = var4 * 114 - 4 + 8;
        this.maxScroll = Math.max(0, var5 - this.gridH);
        this.scrollOffset = Math.max(0.0, Math.min(this.scrollOffset, this.maxScroll));
    }

    protected abstract CapeSelection activeSelection();

    protected abstract void onTileClicked(CapeBrowserScreen.Tile var1, int var2);

    protected List<CapeBrowserScreen.ContextAction> contextActionsFor(CapeBrowserScreen.Tile var1) {
        return List.of();
    }

    protected boolean isHighlighted(CapeBrowserScreen.Tile var1) {
        return false;
    }

    protected boolean showsFavorite(CapeBrowserScreen.Tile var1) {
        return false;
    }

    protected boolean isDefaultTile(CapeBrowserScreen.Tile var1) {
        return false;
    }

    protected Set<ClickableWidget> selectedWidgets() {
        HashSet var1 = new HashSet();

        for (ButtonWidget var3 : this.tabButtons) {
            if (!var3.active) {
                var1.add(var3);
            }
        }

        if (this.crouchButton != null) {
            if (playerCrouching) {
                var1.add(this.crouchButton);
            }

            if (playerWalking) {
                var1.add(this.walkButton);
            }

            if (playerElytra) {
                var1.add(this.elytraButton);
            }

            if (playerCapeOnly) {
                var1.add(this.capeOnlyButton);
            }
        }

        return var1;
    }

    protected Text emptyMessage() {
        return Text.translatable("label.acvcape.search_empty");
    }

    protected Text gridMessage() {
        return null;
    }

    protected void showStatus(Text var1) {
        this.statusText = var1;
        this.statusTicks = 80;
    }

    private Text toggleLabel(String var1, boolean var2) {
        return Text.translatable(var1).append(": ").append(ScreenTexts.onOrOff(var2));
    }

    protected void refreshToggleLabels() {
        if (this.crouchButton != null) {
            this.crouchButton.setMessage(this.toggleLabel("button.acvcape.crouch", playerCrouching));
            this.walkButton.setMessage(this.toggleLabel("button.acvcape.walk", playerWalking));
            this.elytraButton.setMessage(this.toggleLabel("button.acvcape.elytra", playerElytra));
            this.capeOnlyButton.setMessage(this.toggleLabel("button.acvcape.cape_only", playerCapeOnly));
        }
    }

    protected void applyPreviewState() {
        this.refreshToggleLabels();
        if (this.previewPlayer != null) {
            this.previewPlayer.applyState(playerCrouching, playerElytra, playerCapeOnly);
        }
    }

    protected boolean isElytraPreview() {
        return playerElytra;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.statusTicks > 0) {
            this.statusTicks--;
        }

        this.animatePreview();
    }

    private void animatePreview() {
        if (this.previewPlayer != null) {
            this.animTick++;
            this.previewPlayer.animate(playerWalking, WaveyCompat.isActive(this.previewPlayer), this.previewYaw, this.animTick);
            WaveyCompat.tick(this.previewPlayer);
        }
    }

    @Override
    protected void drawBackdrop(DrawContext var1, int var2, int var3, float var4) {
        var1.fill(0, 0, this.width, this.height, -938800878);
        this.drawPanel(var1, this.gridX, this.gridY, this.gridW, this.gridH);
        var1.fillGradient(this.previewX, this.previewY, this.previewX + this.previewW, this.previewY + this.previewH, -14016192, -15462110);
        P.border(var1, this.previewX, this.previewY, this.previewW, this.previewH, -13358769);
        var1.fill(0, 0, this.width, 22, -15462113);
        var1.fill(0, 22, this.width, 23, -7643914);
    }

    protected void drawPanel(DrawContext var1, int var2, int var3, int var4, int var5) {
        var1.fill(var2, var3, var2 + var4, var3 + var5, -535292635);
        P.border(var1, var2, var3, var4, var5, -13358769);
    }

    @Override
    protected void renderScreen(DrawContext var1, int var2, int var3, float var4) {
        this.hoverMouseX = var2;
        this.hoverMouseY = var3;
        this.badgeHover = false;
        this.renderWidgets(var1, var2, var3, var4);
        Ui.buttons(var1, this.textRenderer, this, this.selectedWidgets());
        P.centered(var1, this.textRenderer, this.title, this.width / 2, 7, -1);
        CapeBrowserScreen.Tile var5 = null;
        Text var6 = this.gridMessage();
        if (var6 != null && this.filteredTiles.isEmpty()) {
            this.drawWrappedCentered(var1, var6, this.gridX + this.gridW / 2, this.gridY + this.gridH / 2 - 4, this.gridW - 16, -6250326);
        } else {
            var5 = this.renderGrid(var1, var2, var3);
        }

        this.renderPreview(var1, var4);
        this.renderExtras(var1, var2, var3, var4);
        if (this.statusText != null && this.statusTicks > 0) {
            P.centered(var1, this.textRenderer, this.statusText, this.gridX + this.gridW / 2, this.gridY + this.gridH - 14, -14006);
        }

        if (var5 != null && this.contextTile == null && !this.draggingPreview) {
            P.hoverTip(
                var1, this.textRenderer, this.badgeHover ? List.of(Text.translatable("tooltip.acvcape.no_elytra_texture")) : this.tooltipFor(var5), var2, var3
            );
        }

        this.renderContextMenu(var1, var2, var3);
    }

    protected void renderExtras(DrawContext var1, int var2, int var3, float var4) {
    }

    protected void drawWrappedCentered(DrawContext var1, Text var2, int var3, int var4, int var5, int var6) {
        String var7 = var2.getString();
        ArrayList var8 = new ArrayList();
        StringBuilder var9 = new StringBuilder();

        for (String var13 : var7.split(" ")) {
            String var14 = var9.length() == 0 ? var13 : var9 + " " + var13;
            if (this.textRenderer.getWidth(var14) > var5 && var9.length() > 0) {
                var8.add(var9.toString());
                var9 = new StringBuilder(var13);
            } else {
                var9 = new StringBuilder(var14);
            }
        }

        if (var9.length() > 0) {
            var8.add(var9.toString());
        }

        int var15 = var4 - (var8.size() - 1) * 6;

        for (int var16 = 0; var16 < var8.size(); var16++) {
            P.centered(var1, this.textRenderer, (String)var8.get(var16), var3, var15 + var16 * 12, var6);
        }
    }

    protected List<Text> tooltipFor(CapeBrowserScreen.Tile var1) {
        ArrayList var2 = new ArrayList();
        var2.add(Text.literal(var1.label()));
        if (var1.subLabel() != null && !var1.subLabel().isEmpty()) {
            var2.add(Text.translatable("label.acvcape.downloaded_from", var1.subLabel()).withColor(11184810));
        }

        for (Text var4 : var1.info()) {
            var2.add(var4.copy().withColor(11184810));
        }

        if (var1.locked()) {
            var2.add(Text.translatable("label.acvcape.locked").withColor(16733525));
        }

        if (this.isDefaultTile(var1)) {
            var2.add(Text.translatable("label.acvcape.default_marker").withColor(15773744));
        }

        return var2;
    }

    protected CapeBrowserScreen.Tile renderGrid(DrawContext var1, int var2, int var3) {
        if (this.filteredTiles.isEmpty()) {
            this.drawWrappedCentered(var1, this.emptyMessage(), this.gridX + this.gridW / 2, this.gridY + this.gridH / 2 - 4, this.gridW - 16, -6250326);
            return null;
        }

        int var4 = this.columns * 90 - 4;
        int var5 = this.gridX + (this.gridW - 6 - var4) / 2;
        int var6 = this.gridY + 4 - (int)this.scrollOffset;
        CapeSelection var7 = this.activeSelection();
        CapeBrowserScreen.Tile var8 = null;
        var1.enableScissor(this.gridX + 1, this.gridY + 1, this.gridX + this.gridW - 1, this.gridY + this.gridH - 1);

        for (int var9 = 0; var9 < this.filteredTiles.size(); var9++) {
            CapeBrowserScreen.Tile var10 = this.filteredTiles.get(var9);
            int var11 = var5 + var9 % this.columns * 90;
            int var12 = var6 + var9 / this.columns * 114;
            if (var12 + 110 >= this.gridY && var12 <= this.gridY + this.gridH) {
                boolean var13 = var2 >= var11
                    && var2 < var11 + 86
                    && var3 >= var12
                    && var3 < var12 + 110
                    && var3 >= this.gridY
                    && var3 < this.gridY + this.gridH
                    && !this.draggingPreview;
                boolean var14 = var7 != null && var10.selection().equals(var7);
                this.renderTile(var1, var10, var11, var12, var13, var14);
                if (var13) {
                    var8 = var10;
                }
            }
        }

        var1.disableScissor();
        if (this.maxScroll > 0) {
            int var15 = this.gridX + this.gridW - 5;
            int var16 = Math.max(16, this.gridH * this.gridH / (this.gridH + this.maxScroll));
            int var17 = this.gridY + (int)((this.gridH - var16) * (this.scrollOffset / this.maxScroll));
            var1.fill(var15, this.gridY + 1, var15 + 4, this.gridY + this.gridH - 1, 1090519039);
            var1.fill(var15, var17, var15 + 4, var17 + var16, -7643914);
        }

        return var8;
    }

    protected void renderTile(DrawContext var1, CapeBrowserScreen.Tile var2, int var3, int var4, boolean var5, boolean var6) {
        boolean var7 = this.isHighlighted(var2);
        var1.fill(var3, var4, var3 + 86, var4 + 110, var5 ? -14015680 : -14804436);
        int var8 = var6 ? -1003472 : (var7 ? -7643914 : (var5 ? -7643914 : -12963502));
        P.border(var1, var3, var4, 86, 110, var8);
        if (var6 || var7) {
            P.border(var1, var3 + 1, var4 + 1, 84, 108, var8);
        }

        boolean var9 = var2.subLabel() != null && !var2.subLabel().isEmpty();
        int var10 = var9 ? 78 : 90;
        if (var2.texture() != null) {
            this.renderCapeIcon(var1, var2.texture(), var3 + 4, var4 + 4, 78, var10, var5);
        } else {
            P.centered(var1, this.textRenderer, "...", var3 + 43, var4 + var10 / 2, -6250326);
        }

        P.pushOverlay(var1);
        if (this.showsFavorite(var2) && this.manager.isFavorite(var2.selection())) {
            P.centered(var1, this.textRenderer, "★", var3 + 9, var4 + 4, -10496);
        }

        if (var2.locked()) {
            this.drawLock(var1, var3 + 86 - 12, var4 + 4);
        } else if (var2.missingTexture()) {
            int var11 = var3 + 86 - 14;
            int var12 = var4 + 3;
            var1.fill(var11, var12, var11 + 11, var12 + 12, -855677440);
            P.centered(var1, this.textRenderer, Text.literal("!").formatted(Formatting.BOLD), var11 + 6, var12 + 2, -1);
            if (var5 && this.hoverMouseX >= var11 && this.hoverMouseX < var11 + 11 && this.hoverMouseY >= var12 && this.hoverMouseY < var12 + 12) {
                this.badgeHover = true;
            }
        }

        int var13 = var6 ? -1003472 : -1;
        if (var9) {
            P.centered(var1, this.textRenderer, this.textRenderer.trimToWidth(var2.label(), 82), var3 + 43, var4 + 110 - 24, var13);
            P.centered(var1, this.textRenderer, this.textRenderer.trimToWidth(var2.subLabel(), 82), var3 + 43, var4 + 110 - 13, -6250326);
        } else {
            P.centered(var1, this.textRenderer, this.textRenderer.trimToWidth(var2.label(), 82), var3 + 43, var4 + 110 - 13, var13);
        }

        P.popOverlay(var1);
    }

    protected void drawLock(DrawContext var1, int var2, int var3) {
        var1.fill(var2 - 1, var3 - 1, var2 + 9, var3 + 10, -855638016);
        var1.fill(var2 + 2, var3, var2 + 6, var3 + 1, -2075307);
        var1.fill(var2 + 1, var3 + 1, var2 + 2, var3 + 4, -2075307);
        var1.fill(var2 + 6, var3 + 1, var2 + 7, var3 + 4, -2075307);
        var1.fill(var2, var3 + 4, var2 + 8, var3 + 9, -2075307);
        var1.fill(var2 + 3, var3 + 5, var2 + 5, var3 + 7, -16777216);
    }

    protected void renderCapeIcon(DrawContext var1, Identifier var2, int var3, int var4, int var5, int var6, boolean var7) {
        if (var2 != null) {
            float var8 = var7 ? (float)(Util.getMeasuringTimeMs() % 4000L) / 4000.0F * 360.0F : 0.0F;
            P.capeIcon(var1, var2, this.manager.emissiveFor(var2), var3, var4, var5, var6, var8, var7 ? this.iconEntity(var2, var8) : null);
        }
    }

    private PreviewPlayerEntity iconEntity(Identifier var1, float var2) {
        if (!P.entityIcons()) {
            return null;
        }

        if (this.iconPlayer == null) {
            this.iconPlayer = PreviewPlayerEntity.create();
            this.iconPlayer.applyState(false, false, true);
        }

        this.iconPlayer.setForcedCape(var1);
        this.iconPlayer.syncRotation(180.0F + var2);
        return this.iconPlayer;
    }

    protected void renderPreview(DrawContext var1, float var2) {
        if (this.previewPlayer != null) {
            this.previewPlayer.syncRotation(this.previewYaw);
            float var3 = Math.max(6.0F, Math.min(this.previewW / 1.5F, (this.previewH - 12) / 2.15F));
            var1.enableScissor(this.previewX + 1, this.previewY + 1, this.previewX + this.previewW - 1, this.previewY + this.previewH - 1);
            boolean var4 = WaveyCompat.beginFixedGravity();

            try {
                P.entity(
                    var1, this.previewPlayer, this.previewX + 1, this.previewY + 1, this.previewX + this.previewW - 1, this.previewY + this.previewH - 1, var3, 4.0F
                );
            } finally {
                WaveyCompat.endFixedGravity(var4);
            }

            var1.disableScissor();
            P.centered(
                var1,
                this.textRenderer,
                Text.translatable("label.acvcape.drag_hint"),
                this.previewX + this.previewW / 2,
                this.previewY + this.previewH - 11,
                -6250326
            );
        }
    }

    protected String contextLabelWidthSample() {
        String var1 = "";

        for (CapeBrowserScreen.ContextAction var3 : this.contextActions) {
            String var4 = var3.label().getString();
            if (this.textRenderer.getWidth(var4) > this.textRenderer.getWidth(var1)) {
                var1 = var4;
            }
        }

        return var1;
    }

    private int[] contextRect() {
        int var1 = this.textRenderer.getWidth(this.contextLabelWidthSample()) + 16;
        int var2 = 18 * this.contextActions.size();
        int var3 = Math.max(2, Math.min(this.contextX, this.width - var1 - 2));
        int var4 = Math.max(2, Math.min(this.contextY, this.height - var2 - 2));
        return new int[]{var3, var4, var1, var2};
    }

    private int contextIndexAt(double var1, double var3) {
        if (this.contextTile != null && !this.contextActions.isEmpty()) {
            int[] var5 = this.contextRect();
            return !(var1 < var5[0]) && !(var1 >= var5[0] + var5[2]) && !(var3 < var5[1]) && !(var3 >= var5[1] + var5[3]) ? (int)((var3 - var5[1]) / 18.0) : -1;
        } else {
            return -1;
        }
    }

    private void renderContextMenu(DrawContext var1, int var2, int var3) {
        if (this.contextTile != null && !this.contextActions.isEmpty()) {
            int[] var4 = this.contextRect();
            int var5 = this.contextIndexAt(var2, var3);
            P.pushOverlay(var1);

            for (int var6 = 0; var6 < this.contextActions.size(); var6++) {
                int var7 = var4[1] + var6 * 18;
                var1.fill(var4[0], var7, var4[0] + var4[2], var7 + 18, var5 == var6 ? -14015680 : -14804436);
                P.centered(var1, this.textRenderer, this.contextActions.get(var6).label(), var4[0] + var4[2] / 2, var7 + 5, -1);
            }

            P.border(var1, var4[0], var4[1], var4[2], var4[3], -7643914);
            P.popOverlay(var1);
        }
    }

    protected boolean inPreviewArea(double var1, double var3) {
        return var1 >= this.previewX && var1 < this.previewX + this.previewW && var3 >= this.previewY && var3 < this.previewY + this.previewH;
    }

    protected boolean inGrid(double var1, double var3) {
        return var1 >= this.gridX && var1 < this.gridX + this.gridW && var3 >= this.gridY && var3 < this.gridY + this.gridH;
    }

    @Override
    protected boolean onScroll(double var1, double var3, double var5, double var7) {
        if (this.inGrid(var1, var3)) {
            this.contextTile = null;
            this.scrollOffset = Math.max(0.0, Math.min(this.maxScroll, this.scrollOffset - var7 * 114.0 / 2.0));
            return true;
        } else {
            return this.superScroll(var1, var3, var5, var7);
        }
    }

    @Override
    protected boolean onMouseDown(double var1, double var3, int var5) {
        if (this.contextTile != null) {
            int var6 = this.contextIndexAt(var1, var3);
            this.contextTile = null;
            if (var6 >= 0) {
                if (var5 == 0) {
                    this.contextActions.get(var6).action().run();
                }

                return true;
            }
        }

        if (this.superMouseDown(var1, var3, var5)) {
            return true;
        }

        if (var5 == 1) {
            if (this.inGrid(var1, var3)) {
                CapeBrowserScreen.Tile var9 = this.tileAt(var1, var3);
                if (var9 != null) {
                    List var7 = this.contextActionsFor(var9);
                    if (!var7.isEmpty()) {
                        this.contextTile = var9;
                        this.contextActions = var7;
                        this.contextX = (int)var1;
                        this.contextY = (int)var3;
                        return true;
                    }
                }
            }

            return false;
        } else if (var5 != 0) {
            return false;
        } else if (this.maxScroll > 0 && var1 >= this.gridX + this.gridW - 6 && this.inGrid(var1, var3)) {
            this.draggingScrollbar = true;
            this.updateScrollFromMouse(var3);
            return true;
        } else {
            CapeBrowserScreen.Tile var8;
            if (this.inGrid(var1, var3) && (var8 = this.tileAt(var1, var3)) != null) {
                this.onTileClicked(var8, var5);
                return true;
            } else if (this.inPreviewArea(var1, var3)) {
                this.draggingPreview = true;
                return true;
            } else {
                return false;
            }
        }
    }

    protected CapeBrowserScreen.Tile tileAt(double var1, double var3) {
        int var5 = this.columns * 90 - 4;
        int var6 = this.gridX + (this.gridW - 6 - var5) / 2;
        int var7 = this.gridY + 4 - (int)this.scrollOffset;
        int var8 = (int)var1 - var6;
        int var9 = (int)var3 - var7;
        if (var8 >= 0 && var9 >= 0) {
            int var10 = var8 / 90;
            int var11 = var9 / 114;
            if (var10 < this.columns && var8 % 90 < 86 && var9 % 114 < 110) {
                int var12 = var11 * this.columns + var10;
                return var12 < this.filteredTiles.size() ? this.filteredTiles.get(var12) : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private void updateScrollFromMouse(double var1) {
        double var3 = (var1 - this.gridY) / Math.max(1, this.gridH);
        this.scrollOffset = Math.max(0.0, Math.min(this.maxScroll, var3 * this.maxScroll));
    }

    @Override
    protected boolean onMouseDrag(double var1, double var3, int var5, double var6, double var8) {
        if (this.draggingPreview) {
            this.previewYaw = (this.previewYaw - (float)var6 * 1.5F) % 360.0F;
            return true;
        } else if (this.draggingScrollbar) {
            this.updateScrollFromMouse(var3);
            return true;
        } else {
            return this.superMouseDrag(var1, var3, var5, var6, var8);
        }
    }

    @Override
    protected boolean onMouseUp(double var1, double var3, int var5) {
        this.draggingPreview = false;
        this.draggingScrollbar = false;
        return this.superMouseUp(var1, var3, var5);
    }

    @Override
    protected boolean onKey(int var1, int var2, int var3) {
        if (var1 == 256 && this.contextTile != null) {
            this.contextTile = null;
            return true;
        } else if (this.searchField != null && !this.searchField.isFocused() && P.keyMatches(CapeChangerClient.instance().getOpenMenuKey(), var1, var2)) {
            this.close();
            return true;
        } else {
            return this.superKey(var1, var2, var3);
        }
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    public void removed() {
        super.removed();
    }

    @Override
    public boolean shouldPause() {
        return true;
    }

    public record ContextAction(Text label, Runnable action) {
    }

    public record Tile(
        Identifier texture, String label, String subLabel, CapeSelection selection, boolean missingTexture, int categoryMask, boolean locked, List<Text> info
    ) {
        public Tile(Identifier var1, String var2, CapeSelection var3, boolean var4, int var5) {
            this(var1, var2, null, var3, var4, var5, false, List.of());
        }
    }
}
