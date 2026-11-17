package max.panda.capechanger.g9.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record CapeRequestPayload(String hash, String target) implements CustomPacketPayload {
    public static final Type<CapeRequestPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("acvcape", "request_v2"));
    public static final StreamCodec<FriendlyByteBuf, CapeRequestPayload> CODEC = StreamCodec.ofMember(CapeRequestPayload::write, CapeRequestPayload::read);

    private static CapeRequestPayload read(FriendlyByteBuf var0) {
        return new CapeRequestPayload(var0.readUtf(64), var0.readUtf(36));
    }

    private void write(FriendlyByteBuf var1) {
        var1.writeUtf(this.hash);
        var1.writeUtf(this.target);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
