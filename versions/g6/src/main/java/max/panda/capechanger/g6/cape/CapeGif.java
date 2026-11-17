package max.panda.capechanger.g6.cape;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import max.panda.capechanger.g6.CapeChanger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.w3c.dom.Node;

@Environment(EnvType.CLIENT)
public final class CapeGif {
    private CapeGif() {
    }

    public static boolean isGif(byte[] var0) {
        return var0 != null && var0.length > 6 && var0[0] == 71 && var0[1] == 73 && var0[2] == 70;
    }

    public static boolean isGifPath(Path var0) {
        return var0 != null && var0.getFileName() != null && var0.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".gif");
    }

    public static void convertFolder(Path var0) {
        if (Files.isDirectory(var0)) {
            try (Stream<Path> var1 = Files.list(var0)) {
                for (Path var3 : var1.filter(CapeGif::isGifPath).sorted().toList()) {
                    String var4 = var3.getFileName().toString();
                    Path var5 = var3.resolveSibling(var4.substring(0, var4.length() - 4) + ".png");
                    if (!Files.exists(var5)) {
                        try {
                            CapeGif.Result var6 = convert(Files.readAllBytes(var3));
                            Files.write(var5, var6.png());
                            if (var6.frames() > 1) {
                                CapeStorage.writeAnimationMeta(var5, var6.frameTime());
                            }
                        } catch (Exception var8) {
                            CapeChanger.LOGGER.warn("Falha ao converter GIF " + var3 + ": " + var8.getMessage());
                        }
                    }
                }
            } catch (IOException var10) {
                CapeChanger.LOGGER.warn("Falha ao listar GIFs: " + var10.getMessage());
            }
        }
    }

    public static CapeGif.Result convert(byte[] var0) throws IOException, CapeStorage.InvalidCapeException {
        Iterator var1 = ImageIO.getImageReadersByFormatName("gif");
        if (!var1.hasNext()) {
            throw new CapeStorage.InvalidCapeException("GIF nao suportado neste sistema");
        }

        ImageReader var2 = (ImageReader)var1.next();
        ArrayList var3 = new ArrayList();
        ArrayList<Integer> var4 = new ArrayList<>();

        try (ImageInputStream var5 = ImageIO.createImageInputStream(new ByteArrayInputStream(var0))) {
            var2.setInput(var5, false);
            int var6 = var2.getNumImages(true);
            if (var6 <= 0) {
                throw new CapeStorage.InvalidCapeException("GIF sem quadros");
            }

            int[] var7 = screenSize(var2.getStreamMetadata());
            int var8 = var7[0] > 0 ? var7[0] : var2.getWidth(0);
            int var9 = var7[1] > 0 ? var7[1] : var2.getHeight(0);
            BufferedImage var10 = new BufferedImage(var8, var9, 2);

            for (int var11 = 0; var11 < var6; var11++) {
                BufferedImage var12 = var2.read(var11);
                IIOMetadataNode var13 = (IIOMetadataNode)var2.getImageMetadata(var11).getAsTree("javax_imageio_gif_image_1.0");
                int var14 = 0;
                int var15 = 0;
                int var16 = 10;
                String var17 = "none";

                for (Node var18 = var13.getFirstChild(); var18 != null; var18 = var18.getNextSibling()) {
                    if (var18 instanceof IIOMetadataNode var19) {
                        if ("ImageDescriptor".equals(var19.getNodeName())) {
                            var14 = parse(var19.getAttribute("imageLeftPosition"), 0);
                            var15 = parse(var19.getAttribute("imageTopPosition"), 0);
                        } else if ("GraphicControlExtension".equals(var19.getNodeName())) {
                            var16 = parse(var19.getAttribute("delayTime"), 10);
                            var17 = var19.getAttribute("disposalMethod");
                        }
                    }
                }

                BufferedImage var43 = "restoreToPrevious".equals(var17) ? copy(var10) : null;
                Graphics2D var44 = var10.createGraphics();
                var44.drawImage(var12, var14, var15, null);
                var44.dispose();
                var3.add(copy(var10));
                var4.add(var16 <= 0 ? 10 : var16);
                if (!"restoreToBackgroundColor".equals(var17)) {
                    if (var43 != null) {
                        var10 = var43;
                    }
                } else {
                    for (int var20 = Math.max(0, var15); var20 < Math.min(var9, var15 + var12.getHeight()); var20++) {
                        for (int var21 = Math.max(0, var14); var21 < Math.min(var8, var14 + var12.getWidth()); var21++) {
                            var10.setRGB(var21, var20, 0);
                        }
                    }
                }
            }
        } finally {
            var2.dispose();
        }

        int var29 = ((BufferedImage)var3.get(0)).getWidth();
        int var30 = ((BufferedImage)var3.get(0)).getHeight();
        CapeFormat.Layout var31 = CapeFormat.analyze(var29, var30);
        if (var31 == null || var31.frames() != 1) {
            int var32 = Math.max(1, Math.round(var29 / 64.0F));
            var29 = 64 * var32;
            var30 = 32 * var32;
        }

        long var33 = 0L;

        for (int var36 : var4) {
            var33 += var36;
        }

        int var35 = 1;

        while (true) {
            int var37 = (var3.size() + var35 - 1) / var35;
            if (var37 <= 256 && (long)var29 * var30 * var37 <= 16000000L) {
                BufferedImage var38 = new BufferedImage(var29, var30 * var37, 2);
                Graphics2D var39 = var38.createGraphics();
                var39.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

                for (int var40 = 0; var40 < var37; var40++) {
                    var39.drawImage((Image)var3.get(var40 * var35), 0, var40 * var30, var29, var30, null);
                }

                var39.dispose();
                byte[] var41 = CapeStorage.encodePng(var38);
                if (var41.length <= 16777216 || var37 <= 1) {
                    if (var41.length > 16777216) {
                        throw new CapeStorage.InvalidCapeException("Arquivo muito grande (maximo 16 MB)");
                    }

                    int var42 = (int)Math.round((double)var33 / var3.size() * var35 / 5.0);
                    return new CapeGif.Result(var41, CapeFormat.clampFrameTime(Math.max(1, var42)), var37);
                }
            }

            var35++;
        }
    }

    private static int[] screenSize(IIOMetadata var0) {
        int[] var1 = new int[]{0, 0};
        if (var0 != null) {
            try {
                Node var2 = var0.getAsTree("javax_imageio_gif_stream_1.0");

                for (Node var3 = var2.getFirstChild(); var3 != null; var3 = var3.getNextSibling()) {
                    if ("LogicalScreenDescriptor".equals(var3.getNodeName()) && var3 instanceof IIOMetadataNode var4) {
                        var1[0] = parse(var4.getAttribute("logicalScreenWidth"), 0);
                        var1[1] = parse(var4.getAttribute("logicalScreenHeight"), 0);
                    }
                }
            } catch (Exception var5) {
            }
        }

        return var1;
    }

    private static int parse(String var0, int var1) {
        try {
            return var0 != null && !var0.isEmpty() ? Integer.parseInt(var0) : var1;
        } catch (NumberFormatException var3) {
            return var1;
        }
    }

    private static BufferedImage copy(BufferedImage var0) {
        BufferedImage var1 = new BufferedImage(var0.getWidth(), var0.getHeight(), 2);
        Graphics2D var2 = var1.createGraphics();
        var2.drawImage(var0, 0, 0, null);
        var2.dispose();
        return var1;
    }

    public record Result(byte[] png, int frameTime, int frames) {
    }
}
