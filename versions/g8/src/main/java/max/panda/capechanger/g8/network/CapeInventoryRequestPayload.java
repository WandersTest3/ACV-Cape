package max.panda.capechanger.g8.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record CapeInventoryRequestPayload(String playerUuid) implements CustomPacketPayload {
    public static final Type<CapeInventoryRequestPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("acvcape", "inventory_request"));
    public static final StreamCodec<FriendlyByteBuf, CapeInventoryRequestPayload> CODEC = StreamCodec.ofMember(
        CapeInventoryRequestPayload::write, CapeInventoryRequestPayload::read
    );

    private static CapeInventoryRequestPayload read(FriendlyByteBuf var0) {
        return new CapeInventoryRequestPayload(var0.readUtf(36));
    }

    private void write(FriendlyByteBuf var1) {
        var1.writeUtf(this.playerUuid);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
