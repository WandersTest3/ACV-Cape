package max.panda.capechanger.g7.network;

import max.panda.capechanger.g7.cape.CapeSelection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;

public record CapeSelectionPayload(String playerUuid, CapeSelection selection, String name, int size, int frames, int flags) implements CustomPacketPayload {
    public static final Type<CapeSelectionPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("acvcape", "selection_v2"));
    public static final StreamCodec<FriendlyByteBuf, CapeSelectionPayload> CODEC = StreamCodec.ofMember(CapeSelectionPayload::write, CapeSelectionPayload::read);

    public CapeSelectionPayload(CapeSelection var1, String var2, int var3, int var4, int var5) {
        this("", var1, var2, var3, var4, var5);
    }

    private static CapeSelectionPayload read(FriendlyByteBuf var0) {
        String var1 = var0.readUtf(36);
        String var2 = var0.readUtf(256);
        String var3 = var0.readUtf(64);
        int var4 = var0.readVarInt();
        int var5 = var0.readVarInt();
        int var6 = var0.readVarInt();
        return new CapeSelectionPayload(var1, CapeSelection.decode(var2), var3, var4, var5, var6);
    }

    private void write(FriendlyByteBuf var1) {
        var1.writeUtf(this.playerUuid);
        var1.writeUtf(this.selection.encode());
        var1.writeUtf(CapeNetworking.clip(this.name, 64));
        var1.writeVarInt(this.size);
        var1.writeVarInt(this.frames);
        var1.writeVarInt(this.flags);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
