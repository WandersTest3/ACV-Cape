package max.panda.capechanger.g3.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.VertexConsumer;

@Environment(EnvType.CLIENT)
public final class DarkConsumer implements VertexConsumer {
    private final VertexConsumer delegate;

    public DarkConsumer(VertexConsumer var1) {
        this.delegate = var1;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z) {
        this.delegate.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha) {
        this.delegate.color(0, 0, 0, alpha / 2);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v) {
        this.delegate.texture(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v) {
        this.delegate.overlay(u, v);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v) {
        this.delegate.light(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        this.delegate.normal(x, y, z);
        return this;
    }
}
