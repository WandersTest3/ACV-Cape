package max.panda.capechanger.g5.cape;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class CapeEmissive {
    private static final byte[] TYPE = new byte[]{101, 109, 73, 115};
    private static final int MAX_SIDE = 8192;

    private CapeEmissive() {
    }

    private static int readInt(byte[] var0, int var1) {
        return (var0[var1] & 0xFF) << 24 | (var0[var1 + 1] & 0xFF) << 16 | (var0[var1 + 2] & 0xFF) << 8 | var0[var1 + 3] & 0xFF;
    }

    private static boolean isPng(byte[] var0) {
        return var0 != null && var0.length > 33 && (var0[0] & 255) == 137 && var0[1] == 80 && var0[2] == 78 && var0[3] == 71;
    }

    private static boolean isType(byte[] var0, int var1, byte[] var2) {
        for (int var3 = 0; var3 < 4; var3++) {
            if (var0[var1 + var3] != var2[var3]) {
                return false;
            }
        }

        return true;
    }

    public static CapeEmissive.Mask read(byte[] var0) {
        if (!isPng(var0)) {
            return null;
        }

        int var1 = readInt(var0, 16);
        int var2 = readInt(var0, 20);
        int var3 = 8;

        while (var3 + 12 <= var0.length) {
            int var4 = readInt(var0, var3);
            if (var4 < 0 || var3 + 12 + var4 > var0.length) {
                return null;
            }

            if (isType(var0, var3 + 4, TYPE) && var4 >= 8) {
                int var5 = readInt(var0, var3 + 8);
                int var6 = readInt(var0, var3 + 12);
                if (var5 == var1 && var6 == var2 && var5 > 0 && var6 > 0 && var5 <= 8192 && var6 <= 8192) {
                    try {
                        Inflater var7 = new Inflater();
                        var7.setInput(var0, var3 + 16, var4 - 8);
                        ByteArrayOutputStream var8 = new ByteArrayOutputStream();
                        byte[] var9 = new byte[8192];
                        int var10 = (var5 * var6 + 7) / 8;

                        while (!var7.finished() && var8.size() <= var10) {
                            int var11 = var7.inflate(var9);
                            if (var11 == 0 && (var7.needsInput() || var7.needsDictionary())) {
                                break;
                            }

                            var8.write(var9, 0, var11);
                        }

                        var7.end();
                        BitSet var13 = BitSet.valueOf(var8.toByteArray());
                        if (var13.length() > var5 * var6) {
                            var13.clear(var5 * var6, var13.length());
                        }

                        return new CapeEmissive.Mask(var5, var6, var13);
                    } catch (DataFormatException var12) {
                        return null;
                    }
                }

                return null;
            }

            var3 += 12 + var4;
        }

        return null;
    }

    public static boolean hasMask(byte[] var0) {
        CapeEmissive.Mask var1 = read(var0);
        return var1 != null && !var1.isEmpty();
    }

    public static byte[] write(byte[] var0, CapeEmissive.Mask var1) {
        if (!isPng(var0)) {
            return var0;
        }

        ByteArrayOutputStream var2 = new ByteArrayOutputStream(var0.length + 256);
        var2.write(var0, 0, 8);
        int var3 = 8;

        while (var3 + 12 <= var0.length) {
            int var4 = readInt(var0, var3);
            if (var4 < 0 || var3 + 12 + var4 > var0.length) {
                break;
            }

            boolean var5 = isType(var0, var3 + 4, new byte[]{73, 69, 78, 68});
            if (var5 && var1 != null && !var1.isEmpty()) {
                writeChunk(var2, var1);
            }

            if (!isType(var0, var3 + 4, TYPE)) {
                var2.write(var0, var3, 12 + var4);
            }

            var3 += 12 + var4;
            if (var5) {
                break;
            }
        }

        return var2.toByteArray();
    }

    private static void writeChunk(ByteArrayOutputStream var0, CapeEmissive.Mask var1) {
        Deflater var2 = new Deflater(9);
        var2.setInput(var1.bits().toByteArray());
        var2.finish();
        ByteArrayOutputStream var3 = new ByteArrayOutputStream();
        byte[] var4 = new byte[8192];

        while (!var2.finished()) {
            int var5 = var2.deflate(var4);
            var3.write(var4, 0, var5);
        }

        var2.end();
        byte[] var11 = var3.toByteArray();
        ByteBuffer var6 = ByteBuffer.allocate(8 + var11.length);
        var6.putInt(var1.width());
        var6.putInt(var1.height());
        var6.put(var11);
        byte[] var7 = var6.array();
        CRC32 var8 = new CRC32();
        var8.update(TYPE);
        var8.update(var7);
        ByteBuffer var9 = ByteBuffer.allocate(8);
        var9.putInt(var7.length);
        var9.put(TYPE);
        var0.write(var9.array(), 0, 8);
        var0.write(var7, 0, var7.length);
        ByteBuffer var10 = ByteBuffer.allocate(4);
        var10.putInt((int)var8.getValue());
        var0.write(var10.array(), 0, 4);
    }

    public record Mask(int width, int height, BitSet bits) {
        public boolean get(int var1, int var2) {
            return var1 >= 0 && var2 >= 0 && var1 < this.width && var2 < this.height && this.bits.get(var2 * this.width + var1);
        }

        public void set(int var1, int var2, boolean var3) {
            if (var1 >= 0 && var2 >= 0 && var1 < this.width && var2 < this.height) {
                this.bits.set(var2 * this.width + var1, var3);
            }
        }

        public boolean isEmpty() {
            return this.bits.isEmpty();
        }

        public int count() {
            return this.bits.cardinality();
        }

        public CapeEmissive.Mask copy() {
            return new CapeEmissive.Mask(this.width, this.height, (BitSet)this.bits.clone());
        }

        public static CapeEmissive.Mask empty(int var0, int var1) {
            return new CapeEmissive.Mask(var0, var1, new BitSet(var0 * var1));
        }
    }
}
