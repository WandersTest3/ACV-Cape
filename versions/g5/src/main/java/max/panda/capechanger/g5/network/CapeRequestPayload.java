package max.panda.capechanger.g5.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record CapeRequestPayload(String hash, String target) implements CustomPayload {
    public static final Id<CapeRequestPayload> ID = new Id<>(Identifier.of("acvcape", "request_v2"));
    public static final PacketCodec<PacketByteBuf, CapeRequestPayload> CODEC = PacketCodec.of(CapeRequestPayload::write, CapeRequestPayload::read);

    private static CapeRequestPayload read(PacketByteBuf var0) {
        return new CapeRequestPayload(var0.readString(64), var0.readString(36));
    }

    private void write(PacketByteBuf var1) {
        var1.writeString(this.hash);
        var1.writeString(this.target);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
