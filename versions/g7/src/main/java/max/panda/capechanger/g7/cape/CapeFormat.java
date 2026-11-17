package max.panda.capechanger.g7.cape;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class CapeFormat {
    public static final int MAX_BYTES = 16777216;
    public static final int MAX_PIXELS = 16000000;
    public static final int MAX_FRAMES = 256;
    public static final int DEFAULT_FRAME_TIME = 2;
    public static final int MIN_FRAME_TIME = 1;
    public static final int MAX_FRAME_TIME = 200;

    private CapeFormat() {
    }

    public static CapeFormat.Layout analyze(int var0, int var1) {
        if (var0 > 0 && var1 > 0 && (long)var0 * var1 <= 16000000L) {
            if (var0 >= 64 && var0 % 64 == 0) {
                int var2 = var0 / 2;
                if (var1 % var2 == 0) {
                    int var5 = var1 / var2;
                    return var5 <= 256 ? new CapeFormat.Layout(var0, var2, var5, false) : null;
                }
            }

            if (var0 >= 22 && var0 % 22 == 0) {
                int var4 = 17 * (var0 / 22);
                if (var1 % var4 == 0) {
                    int var3 = var1 / var4;
                    return var3 <= 256 ? new CapeFormat.Layout(var0, var4, var3, true) : null;
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public static CapeFormat.Layout inspect(byte[] var0) {
        if (var0 != null && var0.length >= 33 && var0.length <= 16777216) {
            byte[] var1 = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};

            for (int var2 = 0; var2 < var1.length; var2++) {
                if (var0[var2] != var1[var2]) {
                    return null;
                }
            }

            if (var0[12] == 73 && var0[13] == 72 && var0[14] == 68 && var0[15] == 82) {
                int var4 = readInt(var0, 16);
                int var3 = readInt(var0, 20);
                return analyze(var4, var3);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static int readInt(byte[] var0, int var1) {
        return (var0[var1] & 0xFF) << 24 | (var0[var1 + 1] & 0xFF) << 16 | (var0[var1 + 2] & 0xFF) << 8 | var0[var1 + 3] & 0xFF;
    }

    public static String sha256(byte[] var0) {
        try {
            byte[] var1 = MessageDigest.getInstance("SHA-256").digest(var0);
            StringBuilder var2 = new StringBuilder();

            for (byte var6 : var1) {
                var2.append(String.format("%02x", var6));
            }

            return var2.toString();
        } catch (NoSuchAlgorithmException var7) {
            throw new IllegalStateException(var7);
        }
    }

    public static boolean isHash(String var0) {
        if (var0 != null && var0.length() == 64) {
            for (int var1 = 0; var1 < var0.length(); var1++) {
                char var2 = var0.charAt(var1);
                if ((var2 < '0' || var2 > '9') && (var2 < 'a' || var2 > 'f')) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static int clampFrameTime(int var0) {
        return var0 <= 0 ? 2 : Math.max(1, Math.min(200, var0));
    }

    public record Layout(int frameW, int frameH, int frames, boolean compact) {
        public boolean animated() {
            return this.frames > 1;
        }

        public int normalizedW() {
            return this.compact ? this.frameW / 22 * 64 : this.frameW;
        }

        public int normalizedH() {
            return this.compact ? this.frameW / 22 * 32 : this.frameH;
        }
    }
}
