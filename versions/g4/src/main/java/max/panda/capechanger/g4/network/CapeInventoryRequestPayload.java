package max.panda.capechanger.g4.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record CapeInventoryRequestPayload(String playerUuid) implements CustomPayload {
    public static final Id<CapeInventoryRequestPayload> ID = new Id<>(Identifier.of("acvcape", "inventory_request"));
    public static final PacketCodec<PacketByteBuf, CapeInventoryRequestPayload> CODEC = PacketCodec.of(
        CapeInventoryRequestPayload::write, CapeInventoryRequestPayload::read
    );

    private static CapeInventoryRequestPayload read(PacketByteBuf var0) {
        return new CapeInventoryRequestPayload(var0.readString(36));
    }

    private void write(PacketByteBuf var1) {
        var1.writeString(this.playerUuid);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
