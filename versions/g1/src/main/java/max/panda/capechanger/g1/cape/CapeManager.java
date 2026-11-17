package max.panda.capechanger.g1.cape;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import max.panda.capechanger.g1.CapeChanger;
import max.panda.capechanger.g1.platform.P;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public final class CapeManager {
    private final CapeConfig config;
    private final CapeStorage storage;
    private CapeSelection activeSelection;
    private double animationTime;
    private final List<CustomCape> pending = new ArrayList<>();
    private final Map<Identifier, AnimatedCapeTexture> animatedTextures = new HashMap<>();
    private final Map<Identifier, Identifier> emissiveTextures = new ConcurrentHashMap<>();
    private final Map<String, Identifier> ownedTextureMap = new ConcurrentHashMap<>();
    private final Map<String, String> ownedAliasMap = new ConcurrentHashMap<>();
    private final Map<String, byte[]> ownedBytesMap = new ConcurrentHashMap<>();
    private final List<OwnedCapeFetcher.OwnedCape> ownedCache = new ArrayList<>();
    private boolean ownedFetched;

    public CapeManager(CapeConfig var1, CapeStorage var2) {
        this.config = var1;
        this.storage = var2;
        this.activeSelection = var1.getSelection();
    }

    public void initialize() {
        for (CustomCape var2 : this.storage.refresh()) {
            this.registerTexture(var2);
        }

        for (CustomCape var4 : this.storage.loadDownloaded()) {
            this.registerTexture(var4);
        }

        this.resolveSelection();
    }

    public List<CustomCape> refreshCapes() {
        List<CustomCape> var1 = this.storage.refresh();

        for (CustomCape var3 : var1) {
            this.registerTexture(var3);
        }

        return var1;
    }

    public CapeConfig getConfig() {
        return this.config;
    }

    public CapeSelection getActiveSelection() {
        return this.activeSelection;
    }

    public void setActiveSelection(CapeSelection var1) {
        this.activeSelection = var1;
        this.config.setSelection(var1);
    }

    public Map<String, CustomCape> getAllCapes() {
        return this.storage.getCustomCapes();
    }

    public List<CustomCape> getDownloadedCapes() {
        return this.storage.getDownloadedCapes();
    }

    public List<CustomCape> getTransientCapes() {
        return this.storage.getTransientCapes();
    }

    public CustomCape getCape(String var1) {
        return this.storage.get(var1);
    }

    public CustomCape findDiskCape(String var1) {
        return this.storage.findDiskCape(var1);
    }

    public CustomCape importFromFile(Path var1, String var2) throws IOException, CapeStorage.InvalidCapeException {
        CustomCape var3 = this.storage.importFromFile(var1, var2);
        this.registerTexture(var3);
        return var3;
    }

    public void registerTransient(CustomCape var1) {
        this.storage.putTransient(var1);
        this.registerTexture(var1);
    }

    public CustomCape saveDownloaded(CustomCape var1, String var2) {
        CustomCape var3 = this.storage.saveDownloaded(var1, var2);
        if (var3 == null) {
            this.registerTransient(var1);
            return var1;
        } else {
            this.registerTexture(var3);
            return var3;
        }
    }

    public boolean removeDownloaded(String var1) {
        boolean var2 = this.storage.removeDownloaded(var1);
        if (var2 && this.activeSelection.isCustom() && var1.equals(this.activeSelection.hash()) && this.storage.get(var1) == null) {
            this.setActiveSelection(this.defaultTarget());
        }

        if (var2 && this.storage.get(var1) == null) {
            this.releaseTexture(CustomCape.textureFor(var1));
            CapeSelection var3 = CapeSelection.customSelection(var1);
            this.config.setFavorite(var3, false);
            this.config.replaceDefaultCape(var3, null);
        }

        return var2;
    }

    public boolean deleteCustom(String var1) {
        boolean var2 = this.storage.deleteCustom(var1);
        if (var2) {
            CapeSelection var3 = CapeSelection.customSelection(var1);
            this.config.removeEmissiveCape(var1);
            this.config.setFavorite(var3, false);
            this.config.replaceDefaultCape(var3, null);
            if (this.activeSelection.isCustom() && var1.equals(this.activeSelection.hash()) && this.storage.get(var1) == null) {
                this.setActiveSelection(this.defaultTarget());
            }

            if (this.storage.get(var1) == null) {
                this.releaseTexture(CustomCape.textureFor(var1));
            }
        }

        return var2;
    }

    public CapeSelection defaultTarget() {
        CapeSelection var1 = this.config.getDefaultCape();
        return var1 != null && this.isAvailable(var1) ? var1 : CapeSelection.defaultSelection();
    }

    public boolean isAvailable(CapeSelection var1) {
        switch (var1.type()) {
            case VANILLA:
                return VanillaCape.byId(var1.hash()) != null;
            case CUSTOM:
                CustomCape var2 = this.storage.get(var1.hash());
                return var2 != null && !var2.isLocked();
            default:
                return true;
        }
    }

    public boolean isDefaultCape(CapeSelection var1) {
        CapeSelection var2 = this.config.getDefaultCape();
        return var2 != null && var2.equals(var1);
    }

    public void setDefaultCape(CapeSelection var1) {
        this.config.setDefaultCape(var1);
    }

    public boolean isEmissiveCape(String var1) {
        return this.config.isEmissiveCape(var1);
    }

    public CustomCape importDownloaded(String var1) throws IOException, CapeStorage.InvalidCapeException {
        CustomCape var2 = this.storage.get(var1);
        if (var2 != null && !var2.isLocked()) {
            CustomCape var3 = this.storage.findDiskCape(var1);
            if (var3 != null) {
                return var3;
            }

            CustomCape var4 = this.storage.writeNewCustom(var2.displayName(), var2.pngBytes(), var2.frameTime());
            this.registerTexture(var4);
            return var4;
        } else {
            throw new CapeStorage.InvalidCapeException("Capa indisponivel");
        }
    }

    public void clearTransient() {
        for (CustomCape var2 : this.storage.clearTransient()) {
            if (this.storage.get(var2.hash()) == null) {
                this.releaseTexture(var2.texture());
            }
        }
    }

    private void releaseTexture(Identifier var1) {
        AnimatedCapeTexture var2 = this.animatedTextures.remove(var1);
        Identifier var3 = this.emissiveTextures.remove(var1);
        AnimatedCapeTexture var4 = var3 == null ? null : this.animatedTextures.remove(var3);
        P.destroy(var1);
        if (var3 != null) {
            P.destroy(var3);
        }

        if (var2 != null) {
            var2.close();
        }

        if (var4 != null) {
            var4.close();
        }
    }

    public DownloadPolicy getDownloadPolicy() {
        return this.config.getDownloadPolicy();
    }

    public void setDownloadPolicy(DownloadPolicy var1) {
        this.config.setDownloadPolicy(var1);
    }

    public float getAnimationSpeed() {
        return this.config.getAnimationSpeed();
    }

    public void setAnimationSpeed(float var1) {
        this.config.setAnimationSpeed(var1);
    }

    public Path getStorageDirectory() {
        return this.storage.getDirectory();
    }

    public List<CapeSelection> getFavorites() {
        return this.config.getFavorites();
    }

    public boolean isFavorite(CapeSelection var1) {
        return this.config.isFavorite(var1);
    }

    public void setFavorite(CapeSelection var1, boolean var2) {
        this.config.setFavorite(var1, var2);
    }

    public void tickAnimations() {
        this.animationTime = this.animationTime + this.config.getAnimationSpeed();
        if (!this.animatedTextures.isEmpty()) {
            for (AnimatedCapeTexture var2 : this.animatedTextures.values()) {
                var2.update(this.animationTime);
            }
        }
    }

    public void flushPending() {
        MinecraftClient var1 = MinecraftClient.getInstance();
        if (var1 != null && var1.getTextureManager() != null && !this.pending.isEmpty()) {
            ArrayList<CustomCape> var2 = new ArrayList<>(this.pending);
            this.pending.clear();

            for (CustomCape var4 : var2) {
                this.registerTexture(var4);
            }
        }
    }

    public void registerOwnedCape(String var1, String var2, Identifier var3, byte[] var4) {
        this.ownedTextureMap.put(var1, var3);
        this.ownedAliasMap.put(var1, var2);
        if (var4 != null) {
            this.ownedBytesMap.put(var1, var4);
        }
    }

    public byte[] getOwnedBytes(String var1) {
        return this.ownedBytesMap.get(var1);
    }

    public synchronized void setOwnedCache(List<OwnedCapeFetcher.OwnedCape> var1) {
        this.ownedCache.clear();
        this.ownedCache.addAll(var1);
        this.ownedFetched = true;
    }

    public synchronized List<OwnedCapeFetcher.OwnedCape> getOwnedCache() {
        return new ArrayList<>(this.ownedCache);
    }

    public synchronized boolean isOwnedFetched() {
        return this.ownedFetched;
    }

    public Identifier emissiveFor(Identifier var1) {
        return var1 == null ? null : this.emissiveTextures.get(var1);
    }

    private void registerTexture(CustomCape var1) {
        MinecraftClient var2 = MinecraftClient.getInstance();
        if (var2 != null && var2.getTextureManager() != null) {
            Identifier var3 = var1.texture();
            if (!P.has(var3)) {
                try {
                    this.upload(var2, var3, CapeStorage.normalize(var1.pngBytes()), var1);
                    byte[] var4 = CapeStorage.emissiveOverlay(var1.pngBytes());
                    if (var4 != null) {
                        Identifier var5 = CustomCape.emissiveTextureFor(var1.hash());
                        if (!P.has(var5)) {
                            this.upload(var2, var5, var4, var1);
                        }

                        this.emissiveTextures.put(var3, var5);
                    }
                } catch (IOException var6) {
                    CapeChanger.LOGGER.error("Falha ao registrar textura para capa: " + var1.displayName(), var6);
                }
            }
        } else {
            this.pending.add(var1);
        }
    }

    private void upload(MinecraftClient var1, Identifier var2, byte[] var3, CustomCape var4) throws IOException {
        NativeImage var5 = NativeImage.read(new ByteArrayInputStream(var3));
        if (var4.isAnimated()) {
            int var6 = var4.frames();
            int var7 = var5.getHeight() / var6;
            if (var7 <= 0 || var7 * var6 != var5.getHeight()) {
                var5.close();
                CapeChanger.LOGGER.error("Capa animada com dimensoes invalidas: " + var4.displayName());
                return;
            }

            AnimatedCapeTexture var8 = new AnimatedCapeTexture(var5, var5.getWidth(), var7, var6, var4.frameTime());
            P.register(var2, var8.texture());
            this.animatedTextures.put(var2, var8);
        } else {
            P.register(var2, P.newTexture(var5));
        }
    }

    public Identifier resolveTexture(CapeSelection var1) {
        if (var1 == null) {
            return null;
        }

        switch (var1.type()) {
            case VANILLA:
                VanillaCape var2 = VanillaCape.byId(var1.hash());
                return var2 == null ? null : var2.getTexture();
            case CUSTOM:
                CustomCape var3 = this.storage.get(var1.hash());
                return var3 == null ? null : var3.texture();
            case OWNED:
                return this.ownedTextureMap.get(var1.hash());
            default:
                return null;
        }
    }

    public String displayName(CapeSelection var1) {
        if (var1 == null) {
            return "";
        }

        switch (var1.type()) {
            case VANILLA:
                VanillaCape var2 = VanillaCape.byId(var1.hash());
                return var2 == null ? var1.hash() : var2.getDisplayName();
            case CUSTOM:
                CustomCape var3 = this.storage.get(var1.hash());
                return var3 == null ? var1.hash() : var3.displayName();
            case OWNED:
                String var4 = this.ownedAliasMap.get(var1.hash());
                return var4 != null ? var4 : var1.hash();
            default:
                return "";
        }
    }

    public byte[] sourceBytes(CapeSelection var1) {
        if (var1 == null) {
            return null;
        }

        switch (var1.type()) {
            case VANILLA:
                VanillaCape var2 = VanillaCape.byId(var1.hash());
                if (var2 == null) {
                    return null;
                } else {
                    try {
                        Optional var11 = MinecraftClient.getInstance().getResourceManager().getResource(var2.getTexture());
                        if (var11.isEmpty()) {
                            return null;
                        } else {
                            try (InputStream var5 = ((Resource)var11.get()).getInputStream()) {
                                return var5.readAllBytes();
                            }
                        }
                    } catch (IOException var10) {
                        return null;
                    }
                }
            case CUSTOM:
                CustomCape var3 = this.storage.get(var1.hash());
                return var3 != null && !var3.isLocked() ? var3.pngBytes() : null;
            case OWNED:
                return this.ownedBytesMap.get(var1.hash());
            default:
                return null;
        }
    }

    public int sourceFrameTime(CapeSelection var1) {
        if (var1 != null && var1.isCustom()) {
            CustomCape var2 = this.storage.get(var1.hash());
            if (var2 != null) {
                return var2.frameTime();
            }
        }

        return 2;
    }

    public CapeSelection saveEmissive(CapeSelection var1, byte[] var2) throws IOException, CapeStorage.InvalidCapeException {
        CustomCape var3 = var1.isCustom() ? this.storage.findDiskCape(var1.hash()) : null;
        CustomCape var4;
        if (var3 != null && var3.file() != null) {
            var4 = this.storage.overwriteCustom(var3, var2);
        } else {
            String var5 = this.displayName(var1);
            var4 = this.storage.writeNewCustom(var5 != null && !var5.isBlank() ? var5 : "cape", var2, this.sourceFrameTime(var1));
        }

        this.registerTexture(var4);
        CapeSelection var6 = CapeSelection.customSelection(var4.hash());
        if (var3 != null) {
            this.config.replaceFavorite(var1, var6);
            this.config.replaceDefaultCape(var1, var6);
            if (!var3.hash().equals(var4.hash())) {
                this.config.replaceEmissiveCape(var3.hash(), var4.hash());
            }
        } else if (var1.type() == CapeSelection.Type.VANILLA) {
            this.config.addEmissiveCape(var4.hash());
        }

        this.setActiveSelection(var6);
        return var6;
    }

    private void resolveSelection() {
        CapeSelection var1 = this.activeSelection;
        boolean var2 = false;
        if (var1.type() == CapeSelection.Type.CUSTOM) {
            var2 = this.storage.get(var1.hash()) == null;
        } else if (var1.type() == CapeSelection.Type.VANILLA) {
            var2 = VanillaCape.byId(var1.hash()) == null;
        }

        if (var2) {
            this.activeSelection = CapeSelection.defaultSelection();
            this.config.setSelection(this.activeSelection);
        }
    }
}
