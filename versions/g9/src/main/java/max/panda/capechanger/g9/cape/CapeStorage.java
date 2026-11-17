package max.panda.capechanger.g9.cape;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import max.panda.capechanger.g9.CapeChanger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

@Environment(EnvType.CLIENT)
public final class CapeStorage {
    public static final String FOLDER_NAME = "custom_capes";
    public static final String META_SUFFIX = ".mcmeta";
    private static final String INDEX_FILE = "index.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path customCapesDir;
    private final Path legacyDir;
    private final Path downloadedDir;
    private final Map<String, CustomCape> diskCapes = new LinkedHashMap<>();
    private final Map<String, CustomCape> downloadedCapes = new LinkedHashMap<>();
    private final Map<String, CustomCape> transientCapes = new LinkedHashMap<>();
    private final Set<String> reportedFailures = new HashSet<>();

    public CapeStorage() {
        this.customCapesDir = FabricLoader.getInstance().getGameDir().resolve("custom_capes");
        this.legacyDir = FabricLoader.getInstance().getConfigDir().resolve("capechanger").resolve("custom_capes");
        this.downloadedDir = FabricLoader.getInstance().getConfigDir().resolve("capechanger").resolve("downloaded");

        try {
            Files.createDirectories(this.customCapesDir);
        } catch (IOException var2) {
            CapeChanger.LOGGER.error("Nao foi possivel criar o diretorio de capas personalizadas", var2);
        }
    }

    public Path getDirectory() {
        try {
            Files.createDirectories(this.customCapesDir);
        } catch (IOException var2) {
            CapeChanger.LOGGER.error("Nao foi possivel criar o diretorio de capas personalizadas", var2);
        }

        return this.customCapesDir;
    }

    public synchronized List<CustomCape> refresh() {
        ArrayList var1 = new ArrayList();
        HashSet var2 = new HashSet();

        for (Path var4 : List.of(this.getDirectory(), this.legacyDir)) {
            CapeGif.convertFolder(var4);
            if (Files.isDirectory(var4)) {
                try (Stream<Path> var5 = Files.list(var4)) {
                    for (Path var7 : var5.filter(var0 -> var0.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".png")).sorted().toList()) {
                        this.loadFile(var7, var2, var1);
                    }
                } catch (IOException var10) {
                    CapeChanger.LOGGER.error("Falha ao listar capas personalizadas", var10);
                }
            }
        }

        this.diskCapes.keySet().removeIf(var1x -> !var2.contains(var1x));
        return var1;
    }

    private void loadFile(Path var1, Set<String> var2, List<CustomCape> var3) {
        try {
            byte[] var4 = Files.readAllBytes(var1);
            String var13 = CapeFormat.sha256(var4);
            if (!var2.add(var13)) {
                return;
            }

            CustomCape var6 = this.diskCapes.get(var13);
            if (var6 != null && var1.equals(var6.file())) {
                return;
            }

            CapeFormat.Layout var7 = validatePng(var4);
            String var8 = var1.getFileName().toString();
            String var9 = var8.substring(0, var8.length() - 4);
            int var10 = var7.animated() ? readFrameTime(var1) : 0;
            CustomCape var11 = new CustomCape(var13, var9, var4, CustomCape.textureFor(var13), var7.frames(), var10, CustomCape.Kind.DISK, var1, null, 0);
            this.diskCapes.put(var13, var11);
            var3.add(var11);
        } catch (Exception var12) {
            String var5 = var1 + ":" + var1.toFile().lastModified();
            if (this.reportedFailures.add(var5)) {
                CapeChanger.LOGGER.error("Falha ao carregar capa personalizada: " + var1, var12);
            }
        }
    }

    private static Path metaPath(Path var0) {
        return var0.resolveSibling(var0.getFileName().toString() + ".mcmeta");
    }

    public static int readFrameTime(Path var0) {
        Path var1 = metaPath(var0);
        if (!Files.isRegularFile(var1)) {
            return 0;
        }

        try {
            String var2 = Files.readString(var1, StandardCharsets.UTF_8);
            JsonObject var3 = (JsonObject)GSON.fromJson(var2, JsonObject.class);
            if (var3 != null && var3.has("animation")) {
                JsonObject var4 = var3.getAsJsonObject("animation");
                return var4 != null && var4.has("frametime") ? CapeFormat.clampFrameTime(var4.get("frametime").getAsInt()) : 0;
            } else {
                return 0;
            }
        } catch (Exception var5) {
            CapeChanger.LOGGER.warn("Metadados de animacao invalidos: " + var1);
            return 0;
        }
    }

    public static void writeAnimationMeta(Path var0, int var1) throws IOException {
        writeFrameTime(var0, CapeFormat.clampFrameTime(var1));
    }

    private static void writeFrameTime(Path var0, int var1) throws IOException {
        JsonObject var2 = new JsonObject();
        var2.addProperty("frametime", var1);
        JsonObject var3 = new JsonObject();
        var3.add("animation", var2);
        Files.writeString(metaPath(var0), GSON.toJson(var3), StandardCharsets.UTF_8);
    }

    public synchronized CustomCape importFromFile(Path var1, String var2) throws IOException, CapeStorage.InvalidCapeException {
        byte[] var3 = Files.readAllBytes(var1);
        byte var4 = 0;
        if (CapeGif.isGif(var3) || CapeGif.isGifPath(var1)) {
            CapeGif.Result var5 = CapeGif.convert(var3);
            var3 = var5.png();
            var4 = (byte)(var5.frames() > 1 ? var5.frameTime() : 0);
        }

        CapeFormat.Layout var12 = validatePng(var3);
        String var6 = CapeFormat.sha256(var3);
        String var7 = var2 != null && !var2.isBlank() ? var2 : stem(var1, var6);
        String var8 = sanitizeFileName(var7);
        Path var9 = this.getDirectory().resolve(var8 + ".png");
        if (Files.exists(var9) && !CapeFormat.sha256(Files.readAllBytes(var9)).equals(var6)) {
            int var10 = 2;

            while (Files.exists(var9 = this.getDirectory().resolve(var8 + "_" + var10++ + ".png"))) {
            }
        }

        if (!Files.exists(var9)) {
            Files.write(var9, var3);
            Path var13 = metaPath(var1);
            if (var4 > 0 && var12.animated()) {
                writeFrameTime(var9, var4);
            } else if (var12.animated() && Files.isRegularFile(var13)) {
                Files.copy(var13, metaPath(var9));
            }
        }

        int var14 = var12.animated() ? readFrameTime(var9) : 0;
        CustomCape var11 = new CustomCape(var6, var7, var3, CustomCape.textureFor(var6), var12.frames(), var14, CustomCape.Kind.DISK, var9, null, 0);
        this.diskCapes.put(var6, var11);
        return var11;
    }

    public synchronized CustomCape writeNewCustom(String var1, byte[] var2, int var3) throws IOException, CapeStorage.InvalidCapeException {
        CapeFormat.Layout var4 = validatePng(var2);
        String var5 = CapeFormat.sha256(var2);
        String var6 = sanitizeFileName(var1);
        Path var7 = this.getDirectory().resolve(var6 + ".png");
        int var8 = 2;

        while (Files.exists(var7) && !CapeFormat.sha256(Files.readAllBytes(var7)).equals(var5)) {
            var7 = this.getDirectory().resolve(var6 + "_" + var8++ + ".png");
        }

        if (!Files.exists(var7)) {
            Files.write(var7, var2);
        }

        if (var4.animated()) {
            writeFrameTime(var7, CapeFormat.clampFrameTime(var3));
        }

        String var9 = var7.getFileName().toString();
        CustomCape var10 = new CustomCape(
            var5, var9.substring(0, var9.length() - 4), var2, CustomCape.textureFor(var5), var4.frames(), var3, CustomCape.Kind.DISK, var7, null, 0
        );
        this.diskCapes.put(var5, var10);
        return var10;
    }

    public synchronized CustomCape overwriteCustom(CustomCape var1, byte[] var2) throws IOException, CapeStorage.InvalidCapeException {
        CapeFormat.Layout var3 = validatePng(var2);
        String var4 = CapeFormat.sha256(var2);
        Files.write(var1.file(), var2);
        this.diskCapes.remove(var1.hash());
        CustomCape var5 = new CustomCape(
            var4, var1.displayName(), var2, CustomCape.textureFor(var4), var3.frames(), var1.frameTime(), CustomCape.Kind.DISK, var1.file(), null, 0
        );
        this.diskCapes.put(var4, var5);
        return var5;
    }

    private static String stem(Path var0, String var1) {
        Path var2 = var0.getFileName();
        if (var2 == null) {
            return var1.substring(0, 8);
        }

        String var3 = var2.toString();
        if (var3.toLowerCase(Locale.ROOT).endsWith(".png")) {
            var3 = var3.substring(0, var3.length() - 4);
        }

        return var3.isBlank() ? var1.substring(0, 8) : var3;
    }

    public synchronized void putTransient(CustomCape var1) {
        this.transientCapes.put(var1.hash(), var1);
    }

    public synchronized List<CustomCape> clearTransient() {
        ArrayList var1 = new ArrayList<>(this.transientCapes.values());
        this.transientCapes.clear();
        return var1;
    }

    public synchronized List<CustomCape> getTransientCapes() {
        return new ArrayList<>(this.transientCapes.values());
    }

    public synchronized List<CustomCape> loadDownloaded() {
        ArrayList var1 = new ArrayList();
        Path var2 = this.downloadedDir.resolve("index.json");
        if (!Files.isRegularFile(var2)) {
            return var1;
        }

        try {
            JsonObject var3 = (JsonObject)GSON.fromJson(Files.readString(var2, StandardCharsets.UTF_8), JsonObject.class);
            JsonArray var4 = var3 != null && var3.has("capes") ? var3.getAsJsonArray("capes") : null;
            if (var4 == null) {
                return var1;
            }

            for (JsonElement var6 : var4) {
                try {
                    JsonObject var7 = var6.getAsJsonObject();
                    String var8 = var7.get("hash").getAsString();
                    if (CapeFormat.isHash(var8) && !this.downloadedCapes.containsKey(var8)) {
                        Path var9 = this.downloadedDir.resolve(var8 + ".png");
                        if (Files.isRegularFile(var9)) {
                            byte[] var10 = Files.readAllBytes(var9);
                            if (CapeFormat.sha256(var10).equals(var8)) {
                                CapeFormat.Layout var11 = validatePng(var10);
                                String var12 = var7.has("name") ? var7.get("name").getAsString() : var8.substring(0, 8);
                                String var13 = var7.has("player") ? var7.get("player").getAsString() : "";
                                int var14 = var7.has("frametime") ? var7.get("frametime").getAsInt() : 0;
                                CustomCape var15 = new CustomCape(
                                    var8, var12, var10, CustomCape.textureFor(var8), var11.frames(), var14, CustomCape.Kind.DOWNLOADED, var9, var13, 0
                                );
                                this.downloadedCapes.put(var8, var15);
                                var1.add(var15);
                            }
                        }
                    }
                } catch (Exception var16) {
                    CapeChanger.LOGGER.warn("Entrada invalida em capas baixadas: " + var16.getMessage());
                }
            }
        } catch (Exception var17) {
            CapeChanger.LOGGER.error("Falha ao ler capas baixadas", var17);
        }

        return var1;
    }

    private void saveDownloadedIndex() {
        try {
            Files.createDirectories(this.downloadedDir);
            JsonArray var1 = new JsonArray();

            for (CustomCape var3 : this.downloadedCapes.values()) {
                JsonObject var4 = new JsonObject();
                var4.addProperty("hash", var3.hash());
                var4.addProperty("name", var3.displayName());
                var4.addProperty("player", var3.origin() == null ? "" : var3.origin());
                var4.addProperty("frametime", var3.frameTime());
                var1.add(var4);
            }

            JsonObject var6 = new JsonObject();
            var6.add("capes", var1);
            Files.writeString(this.downloadedDir.resolve("index.json"), GSON.toJson(var6), StandardCharsets.UTF_8);
        } catch (IOException var5) {
            CapeChanger.LOGGER.error("Falha ao salvar capas baixadas", var5);
        }
    }

    public synchronized CustomCape saveDownloaded(CustomCape var1, String var2) {
        CustomCape var3 = this.downloadedCapes.get(var1.hash());
        if (var3 != null) {
            return var3;
        }

        try {
            Files.createDirectories(this.downloadedDir);
            Path var4 = this.downloadedDir.resolve(var1.hash() + ".png");
            Files.write(var4, var1.pngBytes());
            CustomCape var5 = var1.withKind(CustomCape.Kind.DOWNLOADED, var4, var2, 0);
            this.downloadedCapes.put(var1.hash(), var5);
            this.saveDownloadedIndex();
            return var5;
        } catch (IOException var6) {
            CapeChanger.LOGGER.error("Falha ao salvar capa baixada", var6);
            return null;
        }
    }

    public synchronized boolean removeDownloaded(String var1) {
        CustomCape var2 = this.downloadedCapes.remove(var1);
        if (var2 == null) {
            return false;
        }

        try {
            Files.deleteIfExists(this.downloadedDir.resolve(var1 + ".png"));
        } catch (IOException var4) {
            CapeChanger.LOGGER.warn("Falha ao excluir capa baixada: " + var4.getMessage());
        }

        this.saveDownloadedIndex();
        return true;
    }

    public synchronized boolean deleteCustom(String var1) {
        CustomCape var2 = this.diskCapes.get(var1);
        if (var2 != null && var2.file() != null) {
            try {
                Files.deleteIfExists(var2.file());
                Files.deleteIfExists(metaPath(var2.file()));
                String var3 = var2.file().getFileName().toString();
                if (var3.toLowerCase(Locale.ROOT).endsWith(".png")) {
                    Files.deleteIfExists(var2.file().resolveSibling(var3.substring(0, var3.length() - 4) + ".gif"));
                }
            } catch (IOException var4) {
                CapeChanger.LOGGER.warn("Falha ao excluir capa personalizada: " + var4.getMessage());
                return false;
            }

            this.diskCapes.remove(var1);
            return true;
        } else {
            return false;
        }
    }

    public synchronized List<CustomCape> getDownloadedCapes() {
        return new ArrayList<>(this.downloadedCapes.values());
    }

    public synchronized CustomCape findDiskCape(String var1) {
        return this.diskCapes.get(var1);
    }

    public synchronized CustomCape findDownloaded(String var1) {
        return this.downloadedCapes.get(var1);
    }

    public static CapeFormat.Layout validatePng(byte[] var0) throws CapeStorage.InvalidCapeException {
        if (var0 != null && var0.length <= 16777216) {
            BufferedImage var1;
            try {
                var1 = ImageIO.read(new ByteArrayInputStream(var0));
            } catch (IOException var5) {
                throw new CapeStorage.InvalidCapeException("Arquivo nao e uma imagem PNG valida");
            }

            if (var1 == null) {
                throw new CapeStorage.InvalidCapeException("Arquivo nao e uma imagem PNG valida");
            } else {
                int var2 = var1.getWidth();
                int var3 = var1.getHeight();
                CapeFormat.Layout var4 = CapeFormat.analyze(var2, var3);
                if (var4 == null) {
                    throw new CapeStorage.InvalidCapeException(
                        "Tamanho invalido " + var2 + "x" + var3 + " (use 64x32, 22x17 ou multiplos; animadas: quadros empilhados)"
                    );
                } else {
                    return var4;
                }
            }
        } else {
            throw new CapeStorage.InvalidCapeException("Arquivo muito grande (maximo 16 MB)");
        }
    }

    public static BufferedImage normalizeImage(BufferedImage var0) {
        CapeFormat.Layout var1 = CapeFormat.analyze(var0.getWidth(), var0.getHeight());
        if (var1 != null && var1.compact()) {
            int var2 = var1.frameW() / 22;
            int var3 = 64 * var2;
            int var4 = 32 * var2;
            BufferedImage var5 = new BufferedImage(var3, var4 * var1.frames(), 2);
            Graphics2D var6 = var5.createGraphics();

            for (int var7 = 0; var7 < var1.frames(); var7++) {
                BufferedImage var8 = var0.getSubimage(0, var7 * var1.frameH(), var1.frameW(), var1.frameH());
                var6.drawImage(var8, 0, var7 * var4, null);
            }

            var6.dispose();
            return var5;
        } else {
            return var0;
        }
    }

    public static byte[] encodePng(BufferedImage var0) throws IOException {
        ByteArrayOutputStream var1 = new ByteArrayOutputStream();
        ImageIO.write(var0, "png", var1);
        return var1.toByteArray();
    }

    public static byte[] normalize(byte[] var0) throws IOException {
        BufferedImage var1 = ImageIO.read(new ByteArrayInputStream(var0));
        if (var1 == null) {
            return var0;
        }

        CapeFormat.Layout var2 = CapeFormat.analyze(var1.getWidth(), var1.getHeight());
        return var2 != null && var2.compact() ? encodePng(normalizeImage(var1)) : var0;
    }

    public static byte[] emissiveOverlay(byte[] var0) throws IOException {
        CapeEmissive.Mask var1 = CapeEmissive.read(var0);
        if (var1 != null && !var1.isEmpty()) {
            BufferedImage var2 = ImageIO.read(new ByteArrayInputStream(var0));
            if (var2 != null && var2.getWidth() == var1.width() && var2.getHeight() == var1.height()) {
                BufferedImage var3 = new BufferedImage(var2.getWidth(), var2.getHeight(), 2);

                for (int var4 = 0; var4 < var2.getHeight(); var4++) {
                    for (int var5 = 0; var5 < var2.getWidth(); var5++) {
                        var3.setRGB(var5, var4, var1.get(var5, var4) ? var2.getRGB(var5, var4) : 0);
                    }
                }

                return encodePng(normalizeImage(var3));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private static String sanitizeFileName(String var0) {
        String var1 = var0.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return var1.isBlank() ? "cape" : var1;
    }

    public static String sha256(byte[] var0) {
        return CapeFormat.sha256(var0);
    }

    public synchronized Map<String, CustomCape> getCustomCapes() {
        return new LinkedHashMap<>(this.diskCapes);
    }

    public synchronized CustomCape get(String var1) {
        CustomCape var2 = this.diskCapes.get(var1);
        if (var2 != null) {
            return var2;
        }

        var2 = this.downloadedCapes.get(var1);
        return var2 != null ? var2 : this.transientCapes.get(var1);
    }

    @Environment(EnvType.CLIENT)
    public static final class InvalidCapeException extends Exception {
        public InvalidCapeException(String var1) {
            super(var1);
        }
    }
}
