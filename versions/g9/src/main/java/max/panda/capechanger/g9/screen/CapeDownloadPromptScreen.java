package max.panda.capechanger.g9.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import max.panda.capechanger.g9.CapeChangerClient;
import max.panda.capechanger.g9.cape.DownloadPolicy;
import max.panda.capechanger.g9.cape.HeavyCape;
import max.panda.capechanger.g9.platform.AcvScreen;
import max.panda.capechanger.g9.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

@Environment(EnvType.CLIENT)
public final class CapeDownloadPromptScreen extends AcvScreen {
    private static final int LINE_H = 12;
    private final List<CapeChangerClient.PendingDownload> entries = new ArrayList<>();
    private final BiConsumer<Boolean, List<CapeChangerClient.PendingDownload>> callback;
    private boolean answered;
    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;
    private int listY;
    private int listH;
    private double scroll;

    public CapeDownloadPromptScreen(List<CapeChangerClient.PendingDownload> var1, BiConsumer<Boolean, List<CapeChangerClient.PendingDownload>> var2) {
        super(Component.translatable("screen.acvcape.download_prompt"));
        this.entries.addAll(var1);
        this.callback = var2;
    }

    public void addEntries(List<CapeChangerClient.PendingDownload> var1) {
        for (CapeChangerClient.PendingDownload var3 : var1) {
            boolean var4 = false;

            for (CapeChangerClient.PendingDownload var6 : this.entries) {
                if (var6.uuid().equals(var3.uuid()) && var6.hash().equals(var3.hash())) {
                    var4 = true;
                    break;
                }
            }

            if (!var4) {
                this.entries.add(var3);
            }
        }
    }

    public void removePlayer(UUID var1) {
        this.entries.removeIf(var1x -> var1x.uuid().equals(var1));
        if (this.entries.isEmpty() && !this.answered && this.minecraft != null) {
            this.answered = true;
            this.minecraft.gui.setScreen(null);
        }
    }

    @Override
    protected void init() {
        this.applyMenuScale();
        this.panelW = Math.min(340, this.width - 24);
        int var1 = Math.max(24, Math.min(96, this.height - 150));
        this.listH = var1;
        this.panelH = 44 + this.listH + 20;
        this.panelX = (this.width - this.panelW) / 2;
        this.panelY = Math.max(8, (this.height - this.panelH - 56) / 2);
        this.listY = this.panelY + 36;
        int var2 = this.panelY + this.panelH + 8;
        int var3 = Math.min(100, (this.panelW - 8) / 3);
        int var4 = this.width / 2;
        this.addRenderableWidget(
            Button.builder(Component.translatable("acvcape.button.always"), var1x -> this.answer(true, DownloadPolicy.ALWAYS))
                .bounds(var4 - var3 - var3 / 2 - 4, var2, var3, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("acvcape.button.accept_once"), var1x -> this.answer(true, null)).bounds(var4 - var3 / 2, var2, var3, 20).build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("acvcape.button.deny"), var1x -> this.answer(false, null)).bounds(var4 + var3 / 2 + 4, var2, var3, 20).build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("acvcape.button.never"), var1x -> this.answer(false, DownloadPolicy.NEVER))
                .bounds(var4 - var3 / 2, var2 + 24, var3, 20)
                .build()
        );
    }

    private void answer(boolean var1, DownloadPolicy var2) {
        if (!this.answered) {
            this.answered = true;
            if (var2 != null) {
                CapeChangerClient.instance().getManager().setDownloadPolicy(var2);
            }

            this.callback.accept(var1, new ArrayList<>(this.entries));
            this.minecraft.gui.setScreen(null);
        }
    }

    @Override
    public void onClose() {
        this.answer(false, null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int maxScroll() {
        return Math.max(0, this.entries.size() * 12 - this.listH);
    }

    @Override
    protected boolean onScroll(double var1, double var3, double var5, double var7) {
        this.scroll = Math.max(0.0, Math.min(this.maxScroll(), this.scroll - var7 * 12.0));
        return true;
    }

    private boolean hasHeavy() {
        for (CapeChangerClient.PendingDownload var2 : this.entries) {
            if (HeavyCape.isHeavy(var2.size())) {
                return true;
            }
        }

        return false;
    }

    private long totalSize() {
        long var1 = 0L;
        ArrayList var3 = new ArrayList();

        for (CapeChangerClient.PendingDownload var5 : this.entries) {
            if (!var3.contains(var5.hash())) {
                var3.add(var5.hash());
                var1 += Math.max(0, var5.size());
            }
        }

        return var1;
    }

    @Override
    protected void renderScreen(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
        this.renderWidgets(var1, var2, var3, var4);
        var1.fill(this.panelX, this.panelY, this.panelX + this.panelW, this.panelY + this.panelH, -535292635);
        P.border(var1, this.panelX, this.panelY, this.panelW, this.panelH, -13358769);
        P.centered(var1, this.font, this.title, this.width / 2, this.panelY + 7, -1);
        boolean var5 = this.hasHeavy();
        if (var5) {
            String var6 = Component.translatable("acvcape.prompt.download_heavy").getString();
            int var7 = this.panelW - 16;
            if (this.font.width(var6) <= var7) {
                P.centered(var1, this.font, Component.literal(var6), this.width / 2, this.panelY + 20, -43691);
            } else {
                int var8 = var6.lastIndexOf(32, var6.length() / 2 + 6);
                if (var8 < 0) {
                    var8 = var6.length() / 2;
                }

                P.centered(
                    var1, this.font, Component.literal(this.font.plainSubstrByWidth(var6.substring(0, var8), var7)), this.width / 2, this.panelY + 16, -43691
                );
                P.centered(
                    var1, this.font, Component.literal(this.font.plainSubstrByWidth(var6.substring(var8 + 1), var7)), this.width / 2, this.panelY + 26, -43691
                );
            }
        } else {
            MutableComponent var16 = this.entries.size() == 1
                ? Component.translatable("acvcape.prompt.download_single")
                : Component.translatable("acvcape.prompt.download_list", this.entries.size());
            P.centered(var1, this.font, var16, this.width / 2, this.panelY + 20, -3355444);
        }

        int var17 = this.panelX + 8;
        int var18 = this.panelX + this.panelW - 8;
        var1.fill(var17 - 2, this.listY - 2, var18 + 2, this.listY + this.listH + 2, 1711276032);
        var1.enableScissor(var17 - 2, this.listY - 1, var18 + 2, this.listY + this.listH + 1);
        int var19 = this.listY - (int)this.scroll;

        for (CapeChangerClient.PendingDownload var10 : this.entries) {
            if (var19 + 12 >= this.listY && var19 <= this.listY + this.listH) {
                String var11 = var10.frames() > 1
                    ? Component.translatable("label.acvcape.animated_frames", var10.frames()).getString()
                    : Component.translatable("label.acvcape.static").getString();
                String var12 = (var10.size() > 0 ? CapeMenuScreen.formatSize(var10.size()) + "  ·  " : "")
                    + var11
                    + (var10.isEmissive() ? "  ·  " + Component.translatable("label.acvcape.emissive").getString() : "")
                    + (var10.isLocked() ? "  ·  " + Component.translatable("label.acvcape.view_only").getString() : "");
                int var13 = this.font.width(var12);
                P.text(var1, this.font, var12, var18 - var13, var19 + 2, HeavyCape.isHeavy(var10.size()) ? -43691 : (var10.frames() > 1 ? -8398593 : -6250326));
                int var14 = var18 - var13 - var17 - 8;
                String var15 = var10.playerName();
                if (var10.capeName() != null && !var10.capeName().isBlank()) {
                    var15 = var15 + "  ·  " + var10.capeName();
                }

                P.text(var1, this.font, this.font.plainSubstrByWidth(var15, Math.max(10, var14)), var17, var19 + 2, -1);
            }

            var19 += 12;
        }

        var1.disableScissor();
        MutableComponent var20 = Component.translatable("acvcape.prompt.total", CapeMenuScreen.formatSize(this.totalSize()));
        P.centered(var1, this.font, var20, this.width / 2, this.listY + this.listH + 6, -14006);
        Ui.buttons(var1, this.font, this, Set.of());
    }

    @Override
    public void removed() {
        super.removed();
    }
}
