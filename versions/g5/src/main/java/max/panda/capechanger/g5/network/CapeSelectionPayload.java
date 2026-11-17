package max.panda.capechanger.g5.network;

import max.panda.capechanger.g5.cape.CapeSelection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record CapeSelectionPayload(String playerUuid, CapeSelection selection, String name, int size, int frames, int flags) implements CustomPayload {
    public static final Id<CapeSelectionPayload> ID = new Id<>(Identifier.of("acvcape", "selection_v2"));
    public static final PacketCodec<PacketByteBuf, CapeSelectionPayload> CODEC = PacketCodec.of(CapeSelectionPayload::write, CapeSelectionPayload::read);

    public CapeSelectionPayload(CapeSelection var1, String var2, int var3, int var4, int var5) {
        this("", var1, var2, var3, var4, var5);
    }

    private static CapeSelectionPayload read(PacketByteBuf var0) {
        String var1 = var0.readString(36);
        String var2 = var0.readString(256);
        String var3 = var0.readString(64);
        int var4 = var0.readVarInt();
        int var5 = var0.readVarInt();
        int var6 = var0.readVarInt();
        return new CapeSelectionPayload(var1, CapeSelection.decode(var2), var3, var4, var5, var6);
    }

    private void write(PacketByteBuf var1) {
        var1.writeString(this.playerUuid);
        var1.writeString(this.selection.encode());
        var1.writeString(CapeNetworking.clip(this.name, 64));
        var1.writeVarInt(this.size);
        var1.writeVarInt(this.frames);
        var1.writeVarInt(this.flags);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
