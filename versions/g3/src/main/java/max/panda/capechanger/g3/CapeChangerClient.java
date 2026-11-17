package max.panda.capechanger.g3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import max.panda.capechanger.g3.cape.CapeConfig;
import max.panda.capechanger.g3.cape.CapeEmissive;
import max.panda.capechanger.g3.cape.CapeFormat;
import max.panda.capechanger.g3.cape.CapeManager;
import max.panda.capechanger.g3.cape.CapeSelection;
import max.panda.capechanger.g3.cape.CapeStorage;
import max.panda.capechanger.g3.cape.CustomCape;
import max.panda.capechanger.g3.cape.DownloadPolicy;
import max.panda.capechanger.g3.cape.HeavyCape;
import max.panda.capechanger.g3.cape.OwnedCapeFetcher;
import max.panda.capechanger.g3.cape.RemoteCape;
import max.panda.capechanger.g3.cape.SkinWrap;
import max.panda.capechanger.g3.network.CapeChunks;
import max.panda.capechanger.g3.network.CapeDataPayload;
import max.panda.capechanger.g3.network.CapeInventoryPayload;
import max.panda.capechanger.g3.network.CapeInventoryRequestPayload;
import max.panda.capechanger.g3.network.CapeNetworking;
import max.panda.capechanger.g3.network.CapeRequestPayload;
import max.panda.capechanger.g3.network.CapeSelectionPayload;
import max.panda.capechanger.g3.platform.ModIconButton;
import max.panda.capechanger.g3.platform.P;
import max.panda.capechanger.g3.screen.CapeDownloadPromptScreen;
import max.panda.capechanger.g3.screen.CapeMenuScreen;
import max.panda.capechanger.g3.screen.PlayerCapesScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.Join;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AfterInit;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;

@Environment(EnvType.CLIENT)
public final class CapeChangerClient implements ClientModInitializer {
    public static final int STATUS_SERVER_NO_MOD = 4;
    private static CapeChangerClient INSTANCE;
    private CapeConfig config;
    private CapeStorage storage;
    private CapeManager manager;
    private KeyBinding openMenuKey;
    private final Map<UUID, RemoteCape> remotePlayers = new HashMap<>();
    private final Map<String, CapeChunks.Assembly> incoming = new HashMap<>();
    private final Set<String> requested = new HashSet<>();
    private final Map<String, CapeChangerClient.Origin> requestOrigins = new HashMap<>();
    private final Set<String> denied = new HashSet<>();
    private final Deque<CapeChangerClient.PendingDownload> prompts = new ArrayDeque<>();
    private CapeDownloadPromptScreen activePrompt;
    private boolean commitPending;
    private UUID inventoryTarget;
    private Consumer<CapeInventoryPayload> inventoryListener;
    private final Set<String> unresolvedLogged = new HashSet<>();
    private final Set<String> appliedLogged = new HashSet<>();
    private final Set<String> hookLogged = new HashSet<>();

    public static CapeChangerClient instance() {
        return INSTANCE;
    }

    public void onInitializeClient() {
        INSTANCE = this;
        this.config = new CapeConfig();
        this.storage = new CapeStorage();
        this.config.load();
        this.manager = new CapeManager(this.config, this.storage);
        this.manager.initialize();
        this.openMenuKey = KeyBindingHelper.registerKeyBinding(P.createKey("key.acvcape.open_menu", 82, "category.acvcape"));
        ClientPlayNetworking.registerGlobalReceiver(CapeSelectionPayload.ID, (var1, var2) -> var2.client().execute(() -> this.handleRemoteSelection(var1)));
        ClientPlayNetworking.registerGlobalReceiver(CapeDataPayload.ID, (var1, var2) -> var2.client().execute(() -> this.handleRemoteCapeData(var1)));
        ClientPlayNetworking.registerGlobalReceiver(CapeRequestPayload.ID, (var1, var2) -> var2.client().execute(() -> this.handleUploadRequest(var1)));
        ClientPlayNetworking.registerGlobalReceiver(
            CapeInventoryRequestPayload.ID, (var1, var2) -> var2.client().execute(() -> this.handleInventoryRequest(var1))
        );
        ClientPlayNetworking.registerGlobalReceiver(CapeInventoryPayload.ID, (var1, var2) -> var2.client().execute(() -> this.handleInventoryData(var1)));
        ClientPlayConnectionEvents.JOIN.register((Join)(var1, var2, var3) -> this.onJoin());
        ClientPlayConnectionEvents.DISCONNECT.register((Disconnect)(var1, var2) -> this.onDisconnect());
        ScreenEvents.AFTER_INIT.register((AfterInit)(var0, var1, var2, var3) -> {
            if (var1 instanceof GameMenuScreen && !Screens.getButtons(var1).isEmpty()) {
                Screens.getButtons(var1).add(new ModIconButton(4, 4, var2x -> var0.setScreen(new CapeMenuScreen(var1))));
            }
        });
        UseEntityCallback.EVENT.register((UseEntityCallback)(var0, var1, var2, var3, var4) -> {
            if (var1.isClient() && var3 instanceof PlayerEntity && var3 != var0) {
                MinecraftClient var5 = MinecraftClient.getInstance();
                if (var5.player == var0 && var5.currentScreen == null) {
                    var5.setScreen(new PlayerCapesScreen((PlayerEntity)var3));
                    return P.fail();
                } else {
                    return P.pass();
                }
            } else {
                return P.pass();
            }
        });
        CapeChanger.LOGGER.info("ACV Cape cliente iniciado");
        ClientTickEvents.END_CLIENT_TICK.register((EndTick)var1 -> {
            SkinWrap.tick(var1);
            this.manager.flushPending();
            this.manager.tickAnimations();
            this.processPrompts(var1);
            if (this.openMenuKey.wasPressed() && var1.currentScreen == null && var1.player != null) {
                var1.setScreen(new CapeMenuScreen(null));
            }
        });
    }

    private void onJoin() {
        this.resetNetworkState();
        this.commitPending = false;
        CapeSelection var1 = this.manager.getActiveSelection();
        if (!var1.isDefault() && !this.config.isInvisible()) {
            this.sendSelectionToServer(var1);
        }
    }

    private void onDisconnect() {
        this.resetNetworkState();
        this.commitPending = false;
        this.manager.clearTransient();
    }

    private void resetNetworkState() {
        this.remotePlayers.clear();
        this.incoming.clear();
        this.requested.clear();
        this.requestOrigins.clear();
        this.denied.clear();
        this.prompts.clear();
        this.activePrompt = null;
        this.inventoryTarget = null;
        this.inventoryListener = null;
    }

    private static UUID parseUuid(String var0) {
        try {
            return UUID.fromString(var0);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }

    private void handleRemoteSelection(CapeSelectionPayload var1) {
        UUID var2 = parseUuid(var1.playerUuid());
        if (var2 != null) {
            MinecraftClient var3 = MinecraftClient.getInstance();
            if (var3.player == null || !var2.equals(var3.player.getUuid())) {
                CapeSelection var4 = var1.selection();
                this.prompts.removeIf(var1x -> var1x.uuid().equals(var2));
                if (this.activePrompt != null) {
                    this.activePrompt.removePlayer(var2);
                }

                if (!var4.isDefault() && var4.type() != CapeSelection.Type.OWNED) {
                    RemoteCape var5 = new RemoteCape(var4, var1.name(), var1.size(), var1.frames(), var1.flags());
                    this.remotePlayers.put(var2, var5);
                    if (var4.isCustom()) {
                        this.ensureCustomCape(var2, var5);
                    }
                } else {
                    this.remotePlayers.remove(var2);
                }
            }
        }
    }

    private void ensureCustomCape(UUID var1, RemoteCape var2) {
        String var3 = var2.selection().hash();
        if (CapeFormat.isHash(var3) && this.manager.getCape(var3) == null && !this.requested.contains(var3) && !this.denied.contains(var3)) {
            DownloadPolicy var4 = HeavyCape.policy(this.manager, var2);
            String var5 = this.playerName(var1);
            if (var4 == DownloadPolicy.ALWAYS) {
                this.requestCape(var3, var1, var5, var2.name(), !var2.isLocked());
            } else if (var4 == DownloadPolicy.ASK) {
                for (CapeChangerClient.PendingDownload var7 : this.prompts) {
                    if (var7.hash().equals(var3) && var7.uuid().equals(var1)) {
                        return;
                    }
                }

                this.prompts.addLast(new CapeChangerClient.PendingDownload(var1, var5, var3, var2.name(), var2.size(), var2.frames(), var2.flags()));
            }
        }
    }

    public String playerName(UUID var1) {
        ClientPlayNetworkHandler var2 = MinecraftClient.getInstance().getNetworkHandler();
        PlayerListEntry var3 = var2 == null ? null : var2.getPlayerListEntry(var1);
        String var4 = var3 == null ? null : P.profileName(var3.getProfile());
        return var4 != null ? var4 : var1.toString().substring(0, 8);
    }

    private void requestCape(String var1, UUID var2, String var3, String var4, boolean var5) {
        if (this.requested.add(var1)) {
            this.requestOrigins.put(var1, new CapeChangerClient.Origin(var3, var4, var5));
            if (MinecraftClient.getInstance().getNetworkHandler() != null && ClientPlayNetworking.canSend(CapeRequestPayload.ID)) {
                ClientPlayNetworking.send(new CapeRequestPayload(var1, var2 == null ? "" : var2.toString()));
            }
        }
    }

    public void requestPreview(String var1, UUID var2, String var3, String var4) {
        if (CapeFormat.isHash(var1) && this.manager.getCape(var1) == null) {
            this.requested.remove(var1);
            this.requestCape(var1, var2, var3, var4, false);
        }
    }

    private List<CapeChangerClient.PendingDownload> drainPrompts() {
        ArrayList var1 = new ArrayList();

        while (!this.prompts.isEmpty()) {
            CapeChangerClient.PendingDownload var2 = this.prompts.pollFirst();
            if (this.manager.getCape(var2.hash()) == null && !this.requested.contains(var2.hash()) && !this.denied.contains(var2.hash())) {
                var1.add(var2);
            }
        }

        return var1;
    }

    private void processPrompts(MinecraftClient var1) {
        if (this.activePrompt != null && var1.currentScreen != this.activePrompt) {
            this.activePrompt = null;
        }

        if (!this.prompts.isEmpty()) {
            DownloadPolicy var2 = HeavyCape.policy(this.manager, this.prompts);
            if (var2 == DownloadPolicy.NEVER) {
                this.prompts.clear();
            } else if (var2 == DownloadPolicy.ALWAYS) {
                for (CapeChangerClient.PendingDownload var6 : this.drainPrompts()) {
                    this.requestCape(var6.hash(), var6.uuid(), var6.playerName(), var6.capeName(), !var6.isLocked());
                }
            } else if (this.activePrompt != null) {
                this.activePrompt.addEntries(this.drainPrompts());
            } else if (var1.currentScreen == null && var1.player != null) {
                List var3 = this.drainPrompts();
                if (!var3.isEmpty()) {
                    CapeDownloadPromptScreen var4 = new CapeDownloadPromptScreen(var3, (var1x, var2x) -> {
                        this.activePrompt = null;

                        for (CapeChangerClient.PendingDownload var4x : var2x) {
                            if (var1x) {
                                this.requestCape(var4x.hash(), var4x.uuid(), var4x.playerName(), var4x.capeName(), !var4x.isLocked());
                            } else {
                                this.denied.add(var4x.hash());
                            }
                        }
                    });
                    this.activePrompt = var4;
                    var1.setScreen(var4);
                }
            }
        }
    }

    private void handleRemoteCapeData(CapeDataPayload var1) {
        String var2 = var1.hash();
        if (CapeChunks.Assembly.accepts(var1) && this.requested.contains(var2)) {
            CapeChunks.Assembly var3 = this.incoming.get(var2);
            if (var3 == null || !var3.matches(var1)) {
                var3 = new CapeChunks.Assembly(var1);
                this.incoming.put(var2, var3);
            }

            if (!var3.add(var1)) {
                this.incoming.remove(var2);
                this.requested.remove(var2);
            } else if (var3.complete()) {
                this.incoming.remove(var2);
                this.requested.remove(var2);
                CapeChangerClient.Origin var4 = this.requestOrigins.remove(var2);
                byte[] var5 = var3.join();
                if (!CapeFormat.sha256(var5).equals(var2)) {
                    CapeChanger.LOGGER.warn("Hash invalido recebido para capa remota");
                } else {
                    try {
                        CapeFormat.Layout var6 = CapeStorage.validatePng(var5);
                        String var7 = var4 == null ? "" : var4.player();
                        String var8 = var4 != null && var4.capeName() != null && !var4.capeName().isBlank() ? var4.capeName() : var2.substring(0, 8);
                        int var9 = var3.flags() & 1;
                        CustomCape var10 = new CustomCape(
                            var2, var8, var5, CustomCape.textureFor(var2), var6.frames(), var3.frameTime(), CustomCape.Kind.REMOTE, null, var7, var9
                        );
                        if (var4 != null && var4.persist() && var9 == 0) {
                            this.manager.saveDownloaded(var10, var7);
                        } else {
                            this.manager.registerTransient(var10);
                        }
                    } catch (CapeStorage.InvalidCapeException var11) {
                        CapeChanger.LOGGER.warn("Dados de capa invalidos recebidos: " + var11.getMessage());
                    }
                }
            }
        }
    }

    private CapeChangerClient.Outgoing outgoingFor(CapeSelection var1) {
        if (var1.type() == CapeSelection.Type.OWNED) {
            byte[] var3 = this.manager.getOwnedBytes(var1.hash());
            return var3 != null && CapeFormat.inspect(var3) != null
                ? new CapeChangerClient.Outgoing(
                    CapeSelection.customSelection(CapeFormat.sha256(var3)), var3, 0, this.manager.displayName(var1), CapeFormat.inspect(var3).frames()
                )
                : new CapeChangerClient.Outgoing(CapeSelection.defaultSelection(), null, 0, "", 0);
        } else if (var1.type() == CapeSelection.Type.CUSTOM) {
            CustomCape var2 = this.manager.getCape(var1.hash());
            return var2 != null && !var2.isLocked() && CapeFormat.inspect(var2.pngBytes()) != null
                ? new CapeChangerClient.Outgoing(
                    CapeSelection.customSelection(CapeFormat.sha256(var2.pngBytes())), var2.pngBytes(), var2.frameTime(), var2.displayName(), var2.frames()
                )
                : new CapeChangerClient.Outgoing(CapeSelection.defaultSelection(), null, 0, "", 0);
        } else {
            return new CapeChangerClient.Outgoing(var1, null, 0, this.manager.displayName(var1), 0);
        }
    }

    private int privacyFlags() {
        return this.config.isAllowUse() ? 0 : 1;
    }

    private void upload(String var1, int var2, byte[] var3) {
        if (ClientPlayNetworking.canSend(CapeDataPayload.ID)) {
            for (CapeDataPayload var5 : CapeChunks.split(var1, var2, this.privacyFlags(), var3)) {
                ClientPlayNetworking.send(var5);
            }
        }
    }

    private void handleUploadRequest(CapeRequestPayload var1) {
        if (!this.config.isInvisible()) {
            String var2 = var1.hash();
            CapeChangerClient.Outgoing var3 = this.outgoingFor(this.manager.getActiveSelection());
            if (var3.bytes() != null && var3.selection().hash().equals(var2)) {
                this.upload(var2, var3.frameTime(), var3.bytes());
            } else if (this.config.isAllowInventory()) {
                CustomCape var4 = this.manager.findDiskCape(var2);
                if (var4 != null) {
                    this.upload(var2, var4.frameTime(), var4.pngBytes());
                }
            }
        }
    }

    private void handleInventoryRequest(CapeInventoryRequestPayload var1) {
        final String var2 = var1.playerUuid();
        if (this.config.isInvisible()) {
            this.sendInventory(CapeInventoryPayload.status(var2, 2));
        } else if (!this.config.isAllowInventory()) {
            this.sendInventory(CapeInventoryPayload.status(var2, 1));
        } else if (this.manager.isOwnedFetched()) {
            this.sendInventory(this.buildInventory(var2));
        } else {
            OwnedCapeFetcher.fetchAsync(new OwnedCapeFetcher.FetchCallback() {
                @Override
                public void onSuccess(List<OwnedCapeFetcher.OwnedCape> var1) {
                    CapeChangerClient.this.manager.setOwnedCache(var1);
                    CapeChangerClient.this.sendInventory(CapeChangerClient.this.buildInventory(var2));
                }

                @Override
                public void onError(String var1) {
                    CapeChangerClient.this.manager.setOwnedCache(List.of());
                    CapeChangerClient.this.sendInventory(CapeChangerClient.this.buildInventory(var2));
                }
            });
        }
    }

    private CapeInventoryPayload buildInventory(String var1) {
        CapeSelection var2 = this.manager.getActiveSelection();
        String var3 = var2.type() == CapeSelection.Type.OWNED ? var2.encode() : this.outgoingFor(var2).selection().encode();
        ArrayList var4 = new ArrayList();

        for (CustomCape var6 : this.manager.getAllCapes().values()) {
            if (var4.size() >= 80) {
                break;
            }

            var4.add(new CapeInventoryPayload.CustomEntry(var6.hash(), CapeNetworking.clip(var6.displayName(), 32), var6.size(), var6.frames(), var6.frameTime()));
        }

        ArrayList var9 = new ArrayList();

        for (CapeSelection var7 : this.manager.getFavorites()) {
            if (var9.size() >= 60) {
                break;
            }

            var9.add(var7.encode());
        }

        ArrayList var11 = new ArrayList();

        for (OwnedCapeFetcher.OwnedCape var8 : this.manager.getOwnedCache()) {
            if (var11.size() >= 30) {
                break;
            }

            if (var8.textureUrl != null && !var8.textureUrl.isBlank() && var8.textureUrl.length() <= 160) {
                var11.add(new CapeInventoryPayload.OwnedEntry(var8.id, CapeNetworking.clip(var8.alias, 32), var8.textureUrl, var8.active));
            }
        }

        return new CapeInventoryPayload(var1, 0, this.privacyFlags(), var3, var4, var9, var11);
    }

    private void sendInventory(CapeInventoryPayload var1) {
        if (MinecraftClient.getInstance().getNetworkHandler() != null && ClientPlayNetworking.canSend(CapeInventoryPayload.ID)) {
            ClientPlayNetworking.send(var1);
        }
    }

    public void requestInventory(UUID var1, Consumer<CapeInventoryPayload> var2) {
        this.inventoryTarget = var1;
        this.inventoryListener = var2;
        if (MinecraftClient.getInstance().getNetworkHandler() != null && ClientPlayNetworking.canSend(CapeInventoryRequestPayload.ID)) {
            ClientPlayNetworking.send(new CapeInventoryRequestPayload(var1.toString()));
        } else {
            var2.accept(CapeInventoryPayload.status(var1.toString(), 4));
        }
    }

    public void cancelInventory(Consumer<CapeInventoryPayload> var1) {
        if (this.inventoryListener == var1) {
            this.inventoryListener = null;
            this.inventoryTarget = null;
        }
    }

    private void handleInventoryData(CapeInventoryPayload var1) {
        if (this.inventoryListener != null && this.inventoryTarget != null) {
            if (this.inventoryTarget.toString().equals(var1.playerUuid())) {
                this.inventoryListener.accept(var1);
            }
        }
    }

    public void setInvisible(boolean var1) {
        this.config.setInvisible(var1);
        if (var1) {
            this.sendSelectionToServer(CapeSelection.defaultSelection());
        } else {
            this.sendSelectionToServer(this.manager.getActiveSelection());
        }
    }

    public void setAllowUse(boolean var1) {
        this.config.setAllowUse(var1);
        CapeSelection var2 = this.manager.getActiveSelection();
        if (!var2.isDefault()) {
            this.sendSelectionToServer(var2);
        }
    }

    public void setAllowInventory(boolean var1) {
        this.config.setAllowInventory(var1);
    }

    public void logHook(String var1, boolean var2, CapeSelection var3, boolean var4) {
        String var5 = var1 + ":" + var2 + ":" + var4 + ":" + (var3 == null ? "null" : var3.encode());
        if (this.hookLogged.size() < 40 && this.hookLogged.add(var5)) {
            CapeChanger.LOGGER.info("Hook " + var1 + " local=" + var2 + " original=" + var4 + " selecao=" + (var3 == null ? "null" : var3.encode()));
        }
    }

    public void logApplied(String var1, CapeSelection var2) {
        if (this.appliedLogged.size() < 40 && this.appliedLogged.add(var1 + ":" + var2.encode())) {
            CapeChanger.LOGGER.info("Capa aplicada via " + var1 + ": " + var2.encode());
        }
    }

    public void logUnresolved(CapeSelection var1) {
        if (this.unresolvedLogged.size() < 200 && this.unresolvedLogged.add(var1.encode())) {
            CapeChanger.LOGGER.warn("Textura da capa nao encontrada para a selecao: " + var1.encode());
        }
    }

    public CapeManager getManager() {
        return this.manager;
    }

    public CapeConfig getConfig() {
        return this.config;
    }

    public KeyBinding getOpenMenuKey() {
        return this.openMenuKey;
    }

    public CapeSelection getRemoteSelection(UUID var1) {
        RemoteCape var2 = this.remotePlayers.get(var1);
        return var2 == null ? null : var2.selection();
    }

    public boolean isWornByRemote(String var1) {
        for (RemoteCape var3 : this.remotePlayers.values()) {
            if (var3.selection().isCustom() && var3.selection().hash().equals(var1)) {
                return true;
            }
        }

        return false;
    }

    public RemoteCape getRemoteCape(UUID var1) {
        return this.remotePlayers.get(var1);
    }

    public void markCommitPending() {
        this.commitPending = true;
    }

    public void clearCommitPending() {
        this.commitPending = false;
    }

    public void flushCommit() {
        if (this.commitPending) {
            this.commitPending = false;
            this.sendSelectionToServer(this.manager.getActiveSelection());
        }
    }

    public void sendSelectionToServer(CapeSelection var1) {
        MinecraftClient var2 = MinecraftClient.getInstance();
        if (var2.getNetworkHandler() != null && ClientPlayNetworking.canSend(CapeSelectionPayload.ID)) {
            if (this.config.isInvisible()) {
                ClientPlayNetworking.send(new CapeSelectionPayload(CapeSelection.defaultSelection(), "", 0, 0, 0));
            } else {
                CapeChangerClient.Outgoing var3 = this.outgoingFor(var1);
                int var4 = 0;
                if (var3.bytes() != null) {
                    if (!ClientPlayNetworking.canSend(CapeDataPayload.ID)) {
                        return;
                    }

                    var4 = var3.bytes().length;
                    this.upload(var3.selection().hash(), var3.frameTime(), var3.bytes());
                }

                int var5 = this.privacyFlags();
                if (var3.bytes() != null && CapeEmissive.hasMask(var3.bytes())) {
                    var5 |= 2;
                }

                ClientPlayNetworking.send(new CapeSelectionPayload(var3.selection(), CapeNetworking.clip(var3.name(), 64), var4, var3.frames(), var5));
            }
        }
    }

    private record Origin(String player, String capeName, boolean persist) {
    }

    private record Outgoing(CapeSelection selection, byte[] bytes, int frameTime, String name, int frames) {
    }

    public record PendingDownload(UUID uuid, String playerName, String hash, String capeName, int size, int frames, int flags) {
        public boolean isLocked() {
            return (this.flags & 1) != 0;
        }

        public boolean isEmissive() {
            return (this.flags & 2) != 0;
        }
    }
}
