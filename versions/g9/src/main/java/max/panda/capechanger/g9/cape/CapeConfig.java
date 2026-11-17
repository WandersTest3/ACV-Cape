package max.panda.capechanger.g9.cape;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import max.panda.capechanger.g9.CapeChanger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class CapeConfig {
    public static final float MIN_SPEED = 0.25F;
    public static final float MAX_SPEED = 2.0F;
    public static final int MIN_MENU_SCALE = 75;
    public static final int MAX_MENU_SCALE = 150;
    public static final int MIN_SENSITIVITY = 0;
    public static final int MAX_SENSITIVITY = 10;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    private CapeConfig.Data data = new CapeConfig.Data();

    public CapeConfig() {
        this.file = FabricLoader.getInstance().getConfigDir().resolve("capechanger").resolve("config.json");
    }

    public void load() {
        if (!Files.exists(this.file)) {
            this.save();
        } else {
            try {
                String var1 = Files.readString(this.file, StandardCharsets.UTF_8);
                CapeConfig.Data var2 = (CapeConfig.Data)GSON.fromJson(var1, CapeConfig.Data.class);
                if (var2 != null) {
                    if (var2.favorites == null) {
                        var2.favorites = new ArrayList<>();
                    }

                    if (var2.animationSpeed <= 0.0F) {
                        var2.animationSpeed = 1.0F;
                    }

                    if (var2.menuScale <= 0) {
                        var2.menuScale = 100;
                    }

                    if (var2.emissiveCapes == null) {
                        var2.emissiveCapes = new ArrayList<>();
                    }

                    if (var2.defaultCape == null) {
                        var2.defaultCape = "";
                    }

                    this.data = var2;
                }
            } catch (Exception var3) {
                CapeChanger.LOGGER.error("Falha ao carregar config do CapeChanger", var3);
            }
        }
    }

    public void save() {
        try {
            Files.createDirectories(this.file.getParent());
            Files.writeString(this.file, GSON.toJson(this.data), StandardCharsets.UTF_8);
        } catch (IOException var2) {
            CapeChanger.LOGGER.error("Falha ao salvar config do CapeChanger", var2);
        }
    }

    public CapeSelection getSelection() {
        return CapeSelection.decode(this.data.selection);
    }

    public void setSelection(CapeSelection var1) {
        this.data.selection = var1.encode();
        this.save();
    }

    public DownloadPolicy getDownloadPolicy() {
        try {
            return DownloadPolicy.valueOf(this.data.downloadPolicy);
        } catch (Exception var2) {
            return DownloadPolicy.ASK;
        }
    }

    public void setDownloadPolicy(DownloadPolicy var1) {
        this.data.downloadPolicy = var1.name();
        this.save();
    }

    public float getAnimationSpeed() {
        return Math.max(0.25F, Math.min(2.0F, this.data.animationSpeed));
    }

    public void setAnimationSpeed(float var1) {
        this.data.animationSpeed = Math.max(0.25F, Math.min(2.0F, var1));
    }

    public int getMenuScale() {
        return Math.max(75, Math.min(150, this.data.menuScale));
    }

    public void setMenuScale(int var1) {
        this.data.menuScale = Math.max(75, Math.min(150, var1));
    }

    public int getBucketSensitivity() {
        return Math.max(0, Math.min(10, this.data.bucketSensitivity));
    }

    public void setBucketSensitivity(int var1) {
        this.data.bucketSensitivity = Math.max(0, Math.min(10, var1));
    }

    public int getColorSensitivity() {
        return Math.max(0, Math.min(10, this.data.colorSensitivity));
    }

    public void setColorSensitivity(int var1) {
        this.data.colorSensitivity = Math.max(0, Math.min(10, var1));
    }

    public boolean isAllowInventory() {
        return this.data.allowInventory;
    }

    public void setAllowInventory(boolean var1) {
        this.data.allowInventory = var1;
        this.save();
    }

    public boolean isAllowUse() {
        return this.data.allowUse;
    }

    public void setAllowUse(boolean var1) {
        this.data.allowUse = var1;
        this.save();
    }

    public boolean isInvisible() {
        return this.data.invisible;
    }

    public void setInvisible(boolean var1) {
        this.data.invisible = var1;
        this.save();
    }

    public CapeSelection getDefaultCape() {
        if (this.data.defaultCape != null && !this.data.defaultCape.isBlank()) {
            CapeSelection var1 = CapeSelection.decode(this.data.defaultCape);
            return var1.type() != CapeSelection.Type.VANILLA && var1.type() != CapeSelection.Type.CUSTOM ? null : var1;
        } else {
            return null;
        }
    }

    public void setDefaultCape(CapeSelection var1) {
        this.data.defaultCape = var1 == null ? "" : var1.encode();
        this.save();
    }

    public boolean isEditorGrid() {
        return this.data.editorGrid;
    }

    public void setEditorGrid(boolean var1) {
        this.data.editorGrid = var1;
        this.save();
    }

    public boolean isEmissiveCape(String var1) {
        return var1 != null && this.data.emissiveCapes.contains(var1);
    }

    public void addEmissiveCape(String var1) {
        if (var1 != null && !this.data.emissiveCapes.contains(var1)) {
            this.data.emissiveCapes.add(var1);
            this.save();
        }
    }

    public void removeEmissiveCape(String var1) {
        if (this.data.emissiveCapes.remove(var1)) {
            this.save();
        }
    }

    public void replaceEmissiveCape(String var1, String var2) {
        int var3 = this.data.emissiveCapes.indexOf(var1);
        if (var3 >= 0) {
            if (this.data.emissiveCapes.contains(var2)) {
                this.data.emissiveCapes.remove(var3);
            } else {
                this.data.emissiveCapes.set(var3, var2);
            }

            this.save();
        }
    }

    public void replaceDefaultCape(CapeSelection var1, CapeSelection var2) {
        if (var1 != null && var1.encode().equals(this.data.defaultCape)) {
            this.setDefaultCape(var2);
        }
    }

    public List<CapeSelection> getFavorites() {
        ArrayList var1 = new ArrayList();

        for (String var3 : this.data.favorites) {
            CapeSelection var4 = CapeSelection.decode(var3);
            if (var4.type() == CapeSelection.Type.VANILLA || var4.type() == CapeSelection.Type.CUSTOM) {
                var1.add(var4);
            }
        }

        return var1;
    }

    public boolean isFavorite(CapeSelection var1) {
        return this.data.favorites.contains(var1.encode());
    }

    public void setFavorite(CapeSelection var1, boolean var2) {
        String var3 = var1.encode();
        boolean var4 = this.data.favorites.contains(var3);
        if (var2 && !var4) {
            this.data.favorites.add(var3);
            this.save();
        } else if (!var2 && var4) {
            this.data.favorites.remove(var3);
            this.save();
        }
    }

    public void replaceFavorite(CapeSelection var1, CapeSelection var2) {
        int var3 = this.data.favorites.indexOf(var1.encode());
        if (var3 >= 0) {
            if (this.data.favorites.contains(var2.encode())) {
                this.data.favorites.remove(var3);
            } else {
                this.data.favorites.set(var3, var2.encode());
            }

            this.save();
        }
    }

    @Environment(EnvType.CLIENT)
    private static final class Data {
        String selection = "default";
        String downloadPolicy = "ASK";
        List<String> favorites = new ArrayList<>();
        float animationSpeed = 1.0F;
        int menuScale = 100;
        int bucketSensitivity = 0;
        int colorSensitivity = 0;
        boolean allowInventory = true;
        boolean allowUse = true;
        boolean invisible = false;
        String defaultCape = "";
        List<String> emissiveCapes = new ArrayList<>();
        boolean editorGrid = true;
    }
}
