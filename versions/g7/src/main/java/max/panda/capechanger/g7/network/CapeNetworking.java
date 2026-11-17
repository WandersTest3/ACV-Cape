package max.panda.capechanger.g7.network;

import max.panda.capechanger.g7.CapeChangerServer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class CapeNetworking {
    private CapeNetworking() {
    }

    public static String clip(String var0, int var1) {
        if (var0 == null) {
            return "";
        } else {
            return var0.length() > var1 ? var0.substring(0, var1) : var0;
        }
    }

    public static void registerCommon() {
        PayloadTypeRegistry.serverboundPlay().register(CapeSelectionPayload.ID, CapeSelectionPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CapeRequestPayload.ID, CapeRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CapeDataPayload.ID, CapeDataPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CapeInventoryRequestPayload.ID, CapeInventoryRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CapeInventoryPayload.ID, CapeInventoryPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CapeDataPayload.ID, CapeDataPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CapeSelectionPayload.ID, CapeSelectionPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CapeRequestPayload.ID, CapeRequestPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CapeInventoryRequestPayload.ID, CapeInventoryRequestPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CapeInventoryPayload.ID, CapeInventoryPayload.CODEC);
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(
            CapeSelectionPayload.ID, (var0, var1) -> CapeChangerServer.instance().handleSelectionUpdate(var1.player(), var0)
        );
        ServerPlayNetworking.registerGlobalReceiver(CapeDataPayload.ID, (var0, var1) -> CapeChangerServer.instance().handleCapeChunk(var1.player(), var0));
        ServerPlayNetworking.registerGlobalReceiver(
            CapeRequestPayload.ID, (var0, var1) -> CapeChangerServer.instance().handleCapeRequest(var1.player(), var0.hash(), var0.target())
        );
        ServerPlayNetworking.registerGlobalReceiver(
            CapeInventoryRequestPayload.ID, (var0, var1) -> CapeChangerServer.instance().handleInventoryRequest(var1.player(), var0.playerUuid())
        );
        ServerPlayNetworking.registerGlobalReceiver(
            CapeInventoryPayload.ID, (var0, var1) -> CapeChangerServer.instance().handleInventoryResponse(var1.player(), var0)
        );
    }
}
