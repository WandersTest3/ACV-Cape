package max.panda.capechanger.g9.cape;

import com.mojang.blaze3d.platform.NativeImage;
import max.panda.capechanger.g9.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.texture.DynamicTexture;

@Environment(EnvType.CLIENT)
public final class AnimatedCapeTexture {
    private final DynamicTexture texture;
    private final NativeImage strip;
    private final int frameW;
    private final int frameH;
    private final int frames;
    private final int frameTime;
    private int current;
    private boolean closed;

    public AnimatedCapeTexture(NativeImage var1, int var2, int var3, int var4, int var5) {
        this.texture = P.newTexture(new NativeImage(var2, var3, false));
        this.strip = var1;
        this.frameW = var2;
        this.frameH = var3;
        this.frames = Math.max(1, var4);
        this.frameTime = Math.max(1, var5);
        this.current = -1;
        this.show(0);
    }

    public DynamicTexture texture() {
        return this.texture;
    }

    public void update(double var1) {
        if (!this.closed) {
            int var3 = (int)((long)Math.floor(var1 / this.frameTime) % this.frames);
            if (var3 != this.current) {
                this.show(var3);
            }
        }
    }

    private void show(int var1) {
        NativeImage var2 = this.texture.getPixels();
        if (var2 != null) {
            this.current = var1;
            this.strip.copyRect(var2, 0, var1 * this.frameH, 0, 0, this.frameW, this.frameH, false, false);
            this.texture.upload();
        }
    }

    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.strip.close();
        }
    }
}
