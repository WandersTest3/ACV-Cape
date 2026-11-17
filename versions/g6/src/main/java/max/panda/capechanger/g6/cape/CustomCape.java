package max.panda.capechanger.g6.cape;

import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class CustomCape {
    public static final int FLAG_NO_USE = 1;
    public static final int FLAG_EMISSIVE = 2;
    private final String hash;
    private final String displayName;
    private final byte[] pngBytes;
    private final Identifier texture;
    private final int frames;
    private final int frameTime;
    private final CustomCape.Kind kind;
    private final Path file;
    private final String origin;
    private final int flags;
    private Boolean emissive;

    public CustomCape(String var1, String var2, byte[] var3, Identifier var4, int var5, int var6) {
        this(var1, var2, var3, var4, var5, var6, CustomCape.Kind.REMOTE, null, null, 0);
    }

    public CustomCape(String var1, String var2, byte[] var3, Identifier var4, int var5, int var6, CustomCape.Kind var7, Path var8, String var9, int var10) {
        this.hash = var1;
        this.displayName = var2;
        this.pngBytes = var3;
        this.texture = var4;
        this.frames = Math.max(1, var5);
        this.frameTime = CapeFormat.clampFrameTime(var6);
        this.kind = var7;
        this.file = var8;
        this.origin = var9;
        this.flags = var10;
    }

    public static Identifier textureFor(String var0) {
        return Identifier.of("acvcape", "textures/custom/" + var0);
    }

    public static Identifier emissiveTextureFor(String var0) {
        return Identifier.of("acvcape", "textures/custom/" + var0 + "_emissive");
    }

    public CustomCape withKind(CustomCape.Kind var1, Path var2, String var3, int var4) {
        return new CustomCape(this.hash, this.displayName, this.pngBytes, this.texture, this.frames, this.frameTime, var1, var2, var3, var4);
    }

    public CustomCape withName(String var1) {
        return new CustomCape(this.hash, var1, this.pngBytes, this.texture, this.frames, this.frameTime, this.kind, this.file, this.origin, this.flags);
    }

    public String hash() {
        return this.hash;
    }

    public String displayName() {
        return this.displayName;
    }

    public byte[] pngBytes() {
        return this.pngBytes;
    }

    public Identifier texture() {
        return this.texture;
    }

    public int frames() {
        return this.frames;
    }

    public int frameTime() {
        return this.frameTime;
    }

    public boolean isAnimated() {
        return this.frames > 1;
    }

    public CustomCape.Kind kind() {
        return this.kind;
    }

    public Path file() {
        return this.file;
    }

    public String origin() {
        return this.origin;
    }

    public int flags() {
        return this.flags;
    }

    public boolean hasEmissive() {
        if (this.emissive == null) {
            CapeEmissive.Mask var1 = null;

            try {
                var1 = this.pngBytes == null ? null : CapeEmissive.read(this.pngBytes);
            } catch (Exception var3) {
            }

            this.emissive = var1 != null && !var1.isEmpty();
        }

        return this.emissive;
    }

    public boolean isLocked() {
        return (this.flags & 1) != 0;
    }

    public int size() {
        return this.pngBytes == null ? 0 : this.pngBytes.length;
    }

    public enum Kind {
        DISK,
        DOWNLOADED,
        REMOTE;
    }
}
