package max.panda.capechanger.g9.cape;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import max.panda.capechanger.g9.CapeChanger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

@Environment(EnvType.CLIENT)
public final class OwnedCapeFetcher {
    private static final String PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
    private static final String CAPE_PUT_URL = "https://api.minecraftservices.com/minecraft/profile/capes/active";
    private static final Gson GSON = new Gson();

    public static void fetchAsync(OwnedCapeFetcher.FetchCallback var0) {
        CompletableFuture.runAsync(() -> {
            try {
                List var1 = fetch();
                Minecraft.getInstance().execute(() -> var0.onSuccess(var1));
            } catch (Exception var3) {
                CapeChanger.LOGGER.error("Falha ao buscar capas da conta", var3);
                String var2 = var3.getMessage() != null ? var3.getMessage() : "Unknown error";
                Minecraft.getInstance().execute(() -> var0.onError(var2));
            }
        });
    }

    private static List<OwnedCapeFetcher.OwnedCape> fetch() throws IOException {
        Minecraft var1 = Minecraft.getInstance();
        User var2 = var1.getUser();
        String var3 = var2.getAccessToken();
        if (var3 != null && !var3.isBlank()) {
            URL var4 = URI.create("https://api.minecraftservices.com/minecraft/profile").toURL();
            HttpURLConnection var5 = (HttpURLConnection)var4.openConnection();
            var5.setRequestMethod("GET");
            var5.setRequestProperty("Authorization", "Bearer " + var3);
            var5.setConnectTimeout(8000);
            var5.setReadTimeout(8000);
            int var6 = var5.getResponseCode();
            if (var6 == 401) {
                throw new IOException("Unauthorized — re-login required.");
            }

            if (var6 == 204) {
                return new ArrayList<>();
            }

            if (var6 != 200) {
                throw new IOException("Mojang API returned HTTP " + var6);
            }

            InputStreamReader var7 = new InputStreamReader(var5.getInputStream(), StandardCharsets.UTF_8);

            JsonObject var0;
            try {
                var0 = (JsonObject)GSON.fromJson(var7, JsonObject.class);
            } catch (Throwable var18) {
                try {
                    var7.close();
                } catch (Throwable var17) {
                    var18.addSuppressed(var17);
                }

                throw var18;
            }

            var7.close();
            ArrayList var19 = new ArrayList();
            if (var0 != null && var0.has("capes")) {
                for (JsonElement var10 : var0.getAsJsonArray("capes")) {
                    JsonObject var11 = var10.getAsJsonObject();
                    String var12 = var11.has("id") ? var11.get("id").getAsString() : "";
                    String var13 = var11.has("alias") ? var11.get("alias").getAsString() : var12;
                    String var14 = var11.has("state") ? var11.get("state").getAsString() : "";
                    boolean var15 = "active".equalsIgnoreCase(var14);
                    String var16 = "";
                    if (var11.has("url")) {
                        var16 = var11.get("url").getAsString();
                    }

                    if (!var12.isEmpty()) {
                        var19.add(new OwnedCapeFetcher.OwnedCape(var12, var13, var16, var15));
                    }
                }

                return var19;
            } else {
                return var19;
            }
        } else {
            throw new IOException("No access token. Are you logged in with a Microsoft account?");
        }
    }

    public static void activateCapeAsync(String var0, Runnable var1, Consumer<String> var2) {
        CompletableFuture.runAsync(() -> {
            try {
                activateCape(var0);
                Minecraft.getInstance().execute(var1);
            } catch (Exception var5) {
                CapeChanger.LOGGER.error("Falha ao ativar capa na conta", var5);
                String var4 = var5.getMessage() != null ? var5.getMessage() : "Unknown error";
                Minecraft.getInstance().execute(() -> var2.accept(var4));
            }
        });
    }

    private static void activateCape(String var0) throws IOException {
        Minecraft var1 = Minecraft.getInstance();
        User var2 = var1.getUser();
        String var3 = var2.getAccessToken();
        if (var3 != null && !var3.isBlank()) {
            byte[] var4 = ("{\"capeId\":\"" + var0 + "\"}").getBytes(StandardCharsets.UTF_8);
            URL var5 = URI.create("https://api.minecraftservices.com/minecraft/profile/capes/active").toURL();
            HttpURLConnection var6 = (HttpURLConnection)var5.openConnection();
            var6.setRequestMethod("PUT");
            var6.setRequestProperty("Authorization", "Bearer " + var3);
            var6.setRequestProperty("Content-Type", "application/json");
            var6.setDoOutput(true);
            var6.setConnectTimeout(8000);
            var6.setReadTimeout(8000);

            try (OutputStream var7 = var6.getOutputStream()) {
                var7.write(var4);
            }

            int var12 = var6.getResponseCode();
            if (var12 == 401) {
                throw new IOException("Unauthorized — re-login required.");
            } else if (var12 < 200 || var12 >= 300) {
                throw new IOException("Mojang API returned HTTP " + var12);
            }
        } else {
            throw new IOException("No access token.");
        }
    }

    public static void removeCapeAsync(Runnable var0, Consumer<String> var1) {
        CompletableFuture.runAsync(() -> {
            try {
                removeCape();
                Minecraft.getInstance().execute(var0);
            } catch (Exception var4) {
                CapeChanger.LOGGER.error("Falha ao remover capa da conta", var4);
                String var3 = var4.getMessage() != null ? var4.getMessage() : "Unknown error";
                Minecraft.getInstance().execute(() -> var1.accept(var3));
            }
        });
    }

    private static void removeCape() throws IOException {
        Minecraft var0 = Minecraft.getInstance();
        User var1 = var0.getUser();
        String var2 = var1.getAccessToken();
        if (var2 != null && !var2.isBlank()) {
            URL var3 = URI.create("https://api.minecraftservices.com/minecraft/profile/capes/active").toURL();
            HttpURLConnection var4 = (HttpURLConnection)var3.openConnection();
            var4.setRequestMethod("DELETE");
            var4.setRequestProperty("Authorization", "Bearer " + var2);
            var4.setConnectTimeout(8000);
            var4.setReadTimeout(8000);
            var4.getResponseCode();
        } else {
            throw new IOException("No access token.");
        }
    }

    public static void downloadTexture(String var0, Consumer<byte[]> var1, Consumer<String> var2) {
        CompletableFuture.runAsync(() -> {
            try {
                URL var3 = URI.create(var0).toURL();
                HttpURLConnection var8 = (HttpURLConnection)var3.openConnection();
                var8.setConnectTimeout(8000);
                var8.setReadTimeout(8000);
                int var5 = var8.getResponseCode();
                if (var5 != 200) {
                    throw new IOException("HTTP " + var5);
                }

                byte[] var6 = var8.getInputStream().readAllBytes();
                Minecraft.getInstance().execute(() -> var1.accept(var6));
            } catch (Exception var7) {
                CapeChanger.LOGGER.error("Falha ao baixar textura de capa", var7);
                String var4 = var7.getMessage() != null ? var7.getMessage() : "Download failed";
                Minecraft.getInstance().execute(() -> var2.accept(var4));
            }
        });
    }

    public interface FetchCallback {
        void onSuccess(List<OwnedCapeFetcher.OwnedCape> var1);

        void onError(String var1);
    }

    public static final class OwnedCape {
        public final String id;
        public final String alias;
        public final String textureUrl;
        public final boolean active;

        public OwnedCape(String var1, String var2, String var3, boolean var4) {
            this.id = var1;
            this.alias = var2;
            this.textureUrl = var3;
            this.active = var4;
        }
    }
}
