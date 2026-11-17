package max.panda.capechanger.g4.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record CapeDataPayload(String hash, int frameTime, int flags, int index, int total, byte[] bytes) implements CustomPayload {
    public static final Id<CapeDataPayload> ID = new Id<>(Identifier.of("acvcape", "data_v2"));
    public static final PacketCodec<PacketByteBuf, CapeDataPayload> CODEC = PacketCodec.of(CapeDataPayload::write, CapeDataPayload::read);

    private static CapeDataPayload read(PacketByteBuf var0) {
        String var1 = var0.readString(64);
        int var2 = var0.readVarInt();
        int var3 = var0.readVarInt();
        int var4 = var0.readVarInt();
        int var5 = var0.readVarInt();
        byte[] var6 = var0.readByteArray(30000);
        return new CapeDataPayload(var1, var2, var3, var4, var5, var6);
    }

    private void write(PacketByteBuf var1) {
        var1.writeString(this.hash);
        var1.writeVarInt(this.frameTime);
        var1.writeVarInt(this.flags);
        var1.writeVarInt(this.index);
        var1.writeVarInt(this.total);
        var1.writeByteArray(this.bytes);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
