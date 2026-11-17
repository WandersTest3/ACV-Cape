package max.panda.capechanger.g9.platform;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class DarkConsumer implements VertexConsumer {
    private final VertexConsumer delegate;

    public DarkConsumer(VertexConsumer var1) {
        this.delegate = var1;
    }

    @Override
    public VertexConsumer addVertex(float var1, float var2, float var3) {
        this.delegate.addVertex(var1, var2, var3);
        return this;
    }

    @Override
    public VertexConsumer setColor(int var1, int var2, int var3, int var4) {
        this.delegate.setColor(0, 0, 0, var4 / 2);
        return this;
    }

    @Override
    public VertexConsumer setColor(int var1) {
        this.delegate.setColor((var1 >>> 24) / 2 << 24);
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float var1) {
        this.delegate.setLineWidth(var1);
        return this;
    }

    @Override
    public VertexConsumer setUv(float var1, float var2) {
        this.delegate.setUv(var1, var2);
        return this;
    }

    @Override
    public VertexConsumer setUv1(int var1, int var2) {
        this.delegate.setUv1(var1, var2);
        return this;
    }

    @Override
    public VertexConsumer setUv2(int var1, int var2) {
        this.delegate.setUv2(var1, var2);
        return this;
    }

    @Override
    public VertexConsumer setUv3(float var1, float var2) {
        this.delegate.setUv3(var1, var2);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float var1, float var2, float var3) {
        this.delegate.setNormal(var1, var2, var3);
        return this;
    }
}
