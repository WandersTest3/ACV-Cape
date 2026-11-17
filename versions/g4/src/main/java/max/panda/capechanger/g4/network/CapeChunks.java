package max.panda.capechanger.g4.network;

import java.util.ArrayList;
import java.util.List;
import max.panda.capechanger.g4.cape.CapeFormat;

public final class CapeChunks {
    public static final int CHUNK_SIZE = 30000;
    public static final int MAX_TOTAL = 560;

    private CapeChunks() {
    }

    public static List<CapeDataPayload> split(String var0, int var1, int var2, byte[] var3) {
        ArrayList var4 = new ArrayList();
        int var5 = Math.max(1, (var3.length + 30000 - 1) / 30000);

        for (int var6 = 0; var6 < var5; var6++) {
            int var7 = var6 * 30000;
            int var8 = Math.min(var3.length, var7 + 30000);
            byte[] var9 = new byte[Math.max(0, var8 - var7)];
            System.arraycopy(var3, var7, var9, 0, var9.length);
            var4.add(new CapeDataPayload(var0, var1, var2, var6, var5, var9));
        }

        return var4;
    }

    public static final class Assembly {
        private final String hash;
        private final int frameTime;
        private final int flags;
        private final byte[][] parts;
        private int received;
        private long size;

        public Assembly(CapeDataPayload var1) {
            this.hash = var1.hash();
            this.frameTime = var1.frameTime();
            this.flags = var1.flags();
            this.parts = new byte[var1.total()][];
        }

        public static boolean accepts(CapeDataPayload var0) {
            return CapeFormat.isHash(var0.hash()) && var0.total() >= 1 && var0.total() <= 560 && var0.index() >= 0 && var0.index() < var0.total();
        }

        public boolean matches(CapeDataPayload var1) {
            return this.hash.equals(var1.hash()) && this.parts.length == var1.total();
        }

        public boolean add(CapeDataPayload var1) {
            if (this.matches(var1) && var1.index() >= 0 && var1.index() < this.parts.length) {
                if (this.parts[var1.index()] == null) {
                    this.parts[var1.index()] = var1.bytes();
                    this.received++;
                    this.size = this.size + var1.bytes().length;
                }

                return this.size <= 16777216L;
            } else {
                return false;
            }
        }

        public boolean complete() {
            return this.received == this.parts.length;
        }

        public String hash() {
            return this.hash;
        }

        public int frameTime() {
            return this.frameTime;
        }

        public int flags() {
            return this.flags;
        }

        public byte[] join() {
            byte[] var1 = new byte[(int)this.size];
            int var2 = 0;

            for (byte[] var6 : this.parts) {
                System.arraycopy(var6, 0, var1, var2, var6.length);
                var2 += var6.length;
            }

            return var1;
        }
    }
}
