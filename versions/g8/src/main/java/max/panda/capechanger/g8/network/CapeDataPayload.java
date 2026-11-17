package max.panda.capechanger.g8.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record CapeDataPayload(String hash, int frameTime, int flags, int index, int total, byte[] bytes) implements CustomPacketPayload {
    public static final Type<CapeDataPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("acvcape", "data_v2"));
    public static final StreamCodec<FriendlyByteBuf, CapeDataPayload> CODEC = StreamCodec.ofMember(CapeDataPayload::write, CapeDataPayload::read);

    private static CapeDataPayload read(FriendlyByteBuf var0) {
        String var1 = var0.readUtf(64);
        int var2 = var0.readVarInt();
        int var3 = var0.readVarInt();
        int var4 = var0.readVarInt();
        int var5 = var0.readVarInt();
        byte[] var6 = var0.readByteArray(30000);
        return new CapeDataPayload(var1, var2, var3, var4, var5, var6);
    }

    private void write(FriendlyByteBuf var1) {
        var1.writeUtf(this.hash);
        var1.writeVarInt(this.frameTime);
        var1.writeVarInt(this.flags);
        var1.writeVarInt(this.index);
        var1.writeVarInt(this.total);
        var1.writeByteArray(this.bytes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
