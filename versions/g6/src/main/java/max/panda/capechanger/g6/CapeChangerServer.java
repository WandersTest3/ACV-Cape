package max.panda.capechanger.g6;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import max.panda.capechanger.g6.cape.CapeFormat;
import max.panda.capechanger.g6.cape.CapeSelection;
import max.panda.capechanger.g6.network.CapeChunks;
import max.panda.capechanger.g6.network.CapeDataPayload;
import max.panda.capechanger.g6.network.CapeInventoryPayload;
import max.panda.capechanger.g6.network.CapeInventoryRequestPayload;
import max.panda.capechanger.g6.network.CapeNetworking;
import max.panda.capechanger.g6.network.CapeRequestPayload;
import max.panda.capechanger.g6.network.CapeSelectionPayload;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class CapeChangerServer {
    private static final int MAX_CACHE = 64;
    private static final int MAX_PENDING = 512;
    private static final int MAX_INVENTORY_PENDING = 256;
    private static CapeChangerServer INSTANCE;
    private MinecraftServer server;
    private final Map<UUID, CapeChangerServer.SelectionInfo> playerSelections = new LinkedHashMap<>();
    private final Map<UUID, CapeChunks.Assembly> uploads = new HashMap<>();
    private final Map<String, Set<UUID>> pendingRequests = new HashMap<>();
    private final Map<UUID, Set<UUID>> inventoryRequests = new HashMap<>();
    private final Map<String, CapeChangerServer.StoredCape> capeCache = new LinkedHashMap<String, CapeChangerServer.StoredCape>(16, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Entry<String, CapeChangerServer.StoredCape> var1) {
            return this.size() > 64;
        }
    };

    private CapeChangerServer() {
    }

    public static CapeChangerServer instance() {
        if (INSTANCE == null) {
            INSTANCE = new CapeChangerServer();
        }

        return INSTANCE;
    }

    public static void register() {
        CapeNetworking.registerServer();
        CapeChangerServer var0 = instance();
        ServerLifecycleEvents.SERVER_STARTED.register((ServerStarted)var1 -> {
            var0.reset();
            var0.server = var1;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register((ServerStopped)var1 -> {
            var0.reset();
            var0.server = null;
        });
        ServerPlayConnectionEvents.JOIN.register((Join)(var1, var2, var3) -> {
            var0.server = var3;
            var0.sendExistingSelections(var1.player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((Disconnect)(var1, var2) -> var0.forget(var1.player.getUuid()));
    }

    private void reset() {
        this.playerSelections.clear();
        this.uploads.clear();
        this.pendingRequests.clear();
        this.inventoryRequests.clear();
        this.capeCache.clear();
    }

    private void forget(UUID var1) {
        this.playerSelections.remove(var1);
        this.uploads.remove(var1);
        this.inventoryRequests.remove(var1);
        this.inventoryRequests.values().removeIf(var1x -> {
            var1x.remove(var1);
            return var1x.isEmpty();
        });
        this.pendingRequests.values().removeIf(var1x -> {
            var1x.remove(var1);
            return var1x.isEmpty();
        });
        if (this.server != null) {
            CapeSelectionPayload var2 = new CapeSelectionPayload(var1.toString(), CapeSelection.defaultSelection(), "", 0, 0, 0);

            for (ServerPlayerEntity var4 : this.server.getPlayerManager().getPlayerList()) {
                if (!var4.getUuid().equals(var1) && ServerPlayNetworking.canSend(var4, CapeSelectionPayload.ID)) {
                    ServerPlayNetworking.send(var4, var2);
                }
            }
        }
    }

    private void sendExistingSelections(ServerPlayerEntity var1) {
        if (ServerPlayNetworking.canSend(var1, CapeSelectionPayload.ID)) {
            UUID var2 = var1.getUuid();

            for (Entry var4 : this.playerSelections.entrySet()) {
                if (!((UUID)var4.getKey()).equals(var2)) {
                    CapeChangerServer.SelectionInfo var5 = (CapeChangerServer.SelectionInfo)var4.getValue();
                    ServerPlayNetworking.send(
                        var1, new CapeSelectionPayload(((UUID)var4.getKey()).toString(), var5.selection(), var5.name(), var5.size(), var5.frames(), var5.flags())
                    );
                }
            }
        }
    }

    private static CapeSelection sanitize(CapeSelection var0) {
        if (var0 == null) {
            return CapeSelection.defaultSelection();
        }

        switch (var0.type()) {
            case NONE:
                return var0;
            case CUSTOM:
                return CapeFormat.isHash(var0.hash()) ? var0 : CapeSelection.defaultSelection();
            case VANILLA:
                String var1 = var0.hash();
                boolean var2 = !var1.isEmpty() && var1.length() <= 48;

                for (int var3 = 0; var2 && var3 < var1.length(); var3++) {
                    char var4 = var1.charAt(var3);
                    var2 = Character.isLetterOrDigit(var4) || var4 == '_';
                }

                return var2 ? var0 : CapeSelection.defaultSelection();
            default:
                return CapeSelection.defaultSelection();
        }
    }

    private static UUID parseUuid(String var0) {
        if (var0 != null && !var0.isEmpty()) {
            try {
                return UUID.fromString(var0);
            } catch (IllegalArgumentException var2) {
                return null;
            }
        } else {
            return null;
        }
    }

    public void handleSelectionUpdate(ServerPlayerEntity var1, CapeSelectionPayload var2) {
        UUID var3 = var1.getUuid();
        CapeSelection var4 = sanitize(var2.selection());
        boolean var5 = var4.isCustom();
        CapeChangerServer.SelectionInfo var6 = new CapeChangerServer.SelectionInfo(
            var4,
            CapeNetworking.clip(var2.name(), 64),
            var5 ? Math.max(0, Math.min(16777216, var2.size())) : 0,
            var5 ? Math.max(1, Math.min(256, var2.frames())) : 0,
            var2.flags() & 3
        );
        if (var4.isDefault()) {
            this.playerSelections.remove(var3);
        } else {
            this.playerSelections.put(var3, var6);
        }

        if (this.server != null) {
            CapeSelectionPayload var7 = new CapeSelectionPayload(var3.toString(), var6.selection(), var6.name(), var6.size(), var6.frames(), var6.flags());

            for (ServerPlayerEntity var9 : this.server.getPlayerManager().getPlayerList()) {
                if (!var9.getUuid().equals(var3) && ServerPlayNetworking.canSend(var9, CapeSelectionPayload.ID)) {
                    ServerPlayNetworking.send(var9, var7);
                }
            }
        }
    }

    public void handleCapeChunk(ServerPlayerEntity var1, CapeDataPayload var2) {
        if (CapeChunks.Assembly.accepts(var2)) {
            UUID var3 = var1.getUuid();
            CapeChunks.Assembly var4 = this.uploads.get(var3);
            if (var4 == null || !var4.matches(var2)) {
                var4 = new CapeChunks.Assembly(var2);
                this.uploads.put(var3, var4);
            }

            if (!var4.add(var2)) {
                this.uploads.remove(var3);
            } else if (var4.complete()) {
                this.uploads.remove(var3);
                byte[] var5 = var4.join();
                if (CapeFormat.inspect(var5) != null && CapeFormat.sha256(var5).equals(var4.hash())) {
                    CapeChangerServer.StoredCape var6 = new CapeChangerServer.StoredCape(var4.hash(), var4.frameTime(), var4.flags() & 1, var5);
                    this.capeCache.put(var6.hash(), var6);
                    Set<UUID> var7 = this.pendingRequests.remove(var6.hash());
                    if (var7 != null && this.server != null) {
                        for (UUID var9 : var7) {
                            ServerPlayerEntity var10 = this.server.getPlayerManager().getPlayer(var9);
                            if (var10 != null) {
                                this.sendCape(var10, var6);
                            }
                        }
                    }
                } else {
                    CapeChanger.LOGGER.warn("Capa invalida recebida de " + var1.getUuid());
                }
            }
        }
    }

    public void handleCapeRequest(ServerPlayerEntity var1, String var2, String var3) {
        if (CapeFormat.isHash(var2)) {
            CapeChangerServer.StoredCape var4 = this.capeCache.get(var2);
            if (var4 != null) {
                this.sendCape(var1, var4);
            } else if (this.pendingRequests.size() < 512 || this.pendingRequests.containsKey(var2)) {
                Set var5 = this.pendingRequests.computeIfAbsent(var2, var0 -> new HashSet<>());
                boolean var6 = var5.isEmpty();
                var5.add(var1.getUuid());
                UUID var7 = parseUuid(var3);
                if (var7 != null && !var7.equals(var1.getUuid()) && this.server != null) {
                    ServerPlayerEntity var8 = this.server.getPlayerManager().getPlayer(var7);
                    if (var8 != null && ServerPlayNetworking.canSend(var8, CapeRequestPayload.ID)) {
                        ServerPlayNetworking.send(var8, new CapeRequestPayload(var2, ""));
                    }
                }

                if (var6) {
                    this.askOwnersToUpload(var2, var1.getUuid(), var7);
                }
            }
        }
    }

    private void askOwnersToUpload(String var1, UUID var2, UUID var3) {
        if (this.server != null) {
            for (Entry var5 : this.playerSelections.entrySet()) {
                CapeSelection var6 = ((CapeChangerServer.SelectionInfo)var5.getValue()).selection();
                if (!((UUID)var5.getKey()).equals(var2) && !((UUID)var5.getKey()).equals(var3) && var6.isCustom() && var1.equals(var6.hash())) {
                    ServerPlayerEntity var7 = this.server.getPlayerManager().getPlayer((UUID)var5.getKey());
                    if (var7 != null && ServerPlayNetworking.canSend(var7, CapeRequestPayload.ID)) {
                        ServerPlayNetworking.send(var7, new CapeRequestPayload(var1, ""));
                    }
                }
            }
        }
    }

    public void handleInventoryRequest(ServerPlayerEntity var1, String var2) {
        UUID var3 = parseUuid(var2);
        if (ServerPlayNetworking.canSend(var1, CapeInventoryPayload.ID)) {
            ServerPlayerEntity var4 = var3 != null && this.server != null ? this.server.getPlayerManager().getPlayer(var3) : null;
            if (var4 == null || var4 == var1) {
                ServerPlayNetworking.send(var1, CapeInventoryPayload.status(var2 == null ? "" : var2, 3));
            } else if (!ServerPlayNetworking.canSend(var4, CapeInventoryRequestPayload.ID)) {
                ServerPlayNetworking.send(var1, CapeInventoryPayload.status(var2, 2));
            } else if (this.inventoryRequests.size() < 256 || this.inventoryRequests.containsKey(var3)) {
                this.inventoryRequests.computeIfAbsent(var3, var0 -> new HashSet<>()).add(var1.getUuid());
                ServerPlayNetworking.send(var4, new CapeInventoryRequestPayload(var1.getUuid().toString()));
            }
        }
    }

    public void handleInventoryResponse(ServerPlayerEntity var1, CapeInventoryPayload var2) {
        UUID var3 = parseUuid(var2.playerUuid());
        Set var4 = this.inventoryRequests.get(var1.getUuid());
        if (var3 != null && var4 != null && var4.remove(var3) && this.server != null) {
            if (var4.isEmpty()) {
                this.inventoryRequests.remove(var1.getUuid());
            }

            ServerPlayerEntity var5 = this.server.getPlayerManager().getPlayer(var3);
            if (var5 != null && ServerPlayNetworking.canSend(var5, CapeInventoryPayload.ID)) {
                int var6 = var2.status() != 0 && var2.status() != 1 ? 2 : var2.status();
                if (var6 != 0) {
                    ServerPlayNetworking.send(var5, CapeInventoryPayload.status(var1.getUuid().toString(), var6));
                } else {
                    ServerPlayNetworking.send(var5, var2.withPlayer(var1.getUuid().toString()));
                }
            }
        }
    }

    private void sendCape(ServerPlayerEntity var1, CapeChangerServer.StoredCape var2) {
        if (ServerPlayNetworking.canSend(var1, CapeDataPayload.ID)) {
            for (CapeDataPayload var5 : CapeChunks.split(var2.hash(), var2.frameTime(), var2.flags(), var2.bytes())) {
                ServerPlayNetworking.send(var1, var5);
            }
        }
    }

    private record SelectionInfo(CapeSelection selection, String name, int size, int frames, int flags) {
    }

    private record StoredCape(String hash, int frameTime, int flags, byte[] bytes) {
    }
}
