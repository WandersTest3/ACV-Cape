package max.panda.capechanger.g3.network;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.CustomPayload.Id;
import net.minecraft.util.Identifier;

public record CapeInventoryPayload(
    String playerUuid,
    int status,
    int flags,
    String active,
    List<CapeInventoryPayload.CustomEntry> customs,
    List<String> favorites,
    List<CapeInventoryPayload.OwnedEntry> owned
) implements CustomPayload {
    public static final int STATUS_OK = 0;
    public static final int STATUS_PRIVATE = 1;
    public static final int STATUS_UNAVAILABLE = 2;
    public static final int STATUS_OFFLINE = 3;
    public static final int MAX_CUSTOMS = 80;
    public static final int MAX_FAVORITES = 60;
    public static final int MAX_OWNED = 30;
    public static final int MAX_URL = 160;
    public static final Id<CapeInventoryPayload> ID = new Id<>(Identifier.of("acvcape", "inventory"));
    public static final PacketCodec<PacketByteBuf, CapeInventoryPayload> CODEC = PacketCodec.of(CapeInventoryPayload::write, CapeInventoryPayload::read);

    public static CapeInventoryPayload status(String var0, int var1) {
        return new CapeInventoryPayload(var0, var1, 0, "default", List.of(), List.of(), List.of());
    }

    public CapeInventoryPayload withPlayer(String var1) {
        return new CapeInventoryPayload(var1, this.status, this.flags, this.active, this.customs, this.favorites, this.owned);
    }

    private static CapeInventoryPayload read(PacketByteBuf var0) {
        String var1 = var0.readString(36);
        int var2 = var0.readVarInt();
        int var3 = var0.readVarInt();
        String var4 = var0.readString(256);
        int var5 = Math.min(var0.readVarInt(), 80);
        ArrayList var6 = new ArrayList();

        for (int var7 = 0; var7 < var5; var7++) {
            var6.add(new CapeInventoryPayload.CustomEntry(var0.readString(64), var0.readString(32), var0.readVarInt(), var0.readVarInt(), var0.readVarInt()));
        }

        int var12 = Math.min(var0.readVarInt(), 60);
        ArrayList var8 = new ArrayList();

        for (int var9 = 0; var9 < var12; var9++) {
            var8.add(var0.readString(256));
        }

        int var13 = Math.min(var0.readVarInt(), 30);
        ArrayList var10 = new ArrayList();

        for (int var11 = 0; var11 < var13; var11++) {
            var10.add(new CapeInventoryPayload.OwnedEntry(var0.readString(64), var0.readString(32), var0.readString(160), var0.readVarInt() != 0));
        }

        return new CapeInventoryPayload(var1, var2, var3, var4, var6, var8, var10);
    }

    private void write(PacketByteBuf var1) {
        var1.writeString(this.playerUuid);
        var1.writeVarInt(this.status);
        var1.writeVarInt(this.flags);
        var1.writeString(CapeNetworking.clip(this.active, 256));
        int var2 = Math.min(this.customs.size(), 80);
        var1.writeVarInt(var2);

        for (int var3 = 0; var3 < var2; var3++) {
            CapeInventoryPayload.CustomEntry var4 = this.customs.get(var3);
            var1.writeString(var4.hash());
            var1.writeString(CapeNetworking.clip(var4.name(), 32));
            var1.writeVarInt(var4.size());
            var1.writeVarInt(var4.frames());
            var1.writeVarInt(var4.frameTime());
        }

        int var7 = Math.min(this.favorites.size(), 60);
        var1.writeVarInt(var7);

        for (int var8 = 0; var8 < var7; var8++) {
            var1.writeString(CapeNetworking.clip(this.favorites.get(var8), 256));
        }

        int var9 = Math.min(this.owned.size(), 30);
        var1.writeVarInt(var9);

        for (int var5 = 0; var5 < var9; var5++) {
            CapeInventoryPayload.OwnedEntry var6 = this.owned.get(var5);
            var1.writeString(CapeNetworking.clip(var6.id(), 64));
            var1.writeString(CapeNetworking.clip(var6.alias(), 32));
            var1.writeString(CapeNetworking.clip(var6.url(), 160));
            var1.writeVarInt(var6.active() ? 1 : 0);
        }
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public record CustomEntry(String hash, String name, int size, int frames, int frameTime) {
    }

    public record OwnedEntry(String id, String alias, String url, boolean active) {
    }
}
