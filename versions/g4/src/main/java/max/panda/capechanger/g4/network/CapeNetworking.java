package max.panda.capechanger.g4.network;

import max.panda.capechanger.g4.CapeChangerServer;
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
        PayloadTypeRegistry.playC2S().register(CapeSelectionPayload.ID, CapeSelectionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CapeRequestPayload.ID, CapeRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CapeDataPayload.ID, CapeDataPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CapeInventoryRequestPayload.ID, CapeInventoryRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(CapeInventoryPayload.ID, CapeInventoryPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CapeDataPayload.ID, CapeDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CapeSelectionPayload.ID, CapeSelectionPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CapeRequestPayload.ID, CapeRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CapeInventoryRequestPayload.ID, CapeInventoryRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(CapeInventoryPayload.ID, CapeInventoryPayload.CODEC);
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
