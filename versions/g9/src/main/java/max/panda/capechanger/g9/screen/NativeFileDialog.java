package max.panda.capechanger.g9.screen;

import java.awt.GraphicsEnvironment;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import max.panda.capechanger.g9.CapeChanger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public final class NativeFileDialog {
    private static final String TITLE = "Select cape PNG";
    private static final String FILTER_NAME = "PNG images (*.png)";

    private NativeFileDialog() {
    }

    public static void pickPng(Consumer<Path> var0, Consumer<Boolean> var1) {
        Thread var2 = new Thread(() -> {
            Path var2x = null;
            boolean var3 = false;

            try {
                var2x = openWithTinyFd();
            } catch (Throwable var7) {
                CapeChanger.LOGGER.warn("Seletor nativo indisponivel, usando alternativa: " + var7);

                try {
                    var2x = openWithSwing();
                } catch (Throwable var6) {
                    CapeChanger.LOGGER.error("Falha ao abrir o seletor de arquivos", var6);
                    var3 = true;
                }
            }

            Path var4 = var2x;
            boolean var5 = var3;
            Minecraft.getInstance().execute(() -> {
                if (var4 != null) {
                    var0.accept(var4);
                }

                var1.accept(var5);
            });
        }, "acvcape-file-dialog");
        var2.setDaemon(true);
        var2.start();
    }

    private static Path openWithTinyFd() {
        throw new IllegalStateException("Seletor nativo indisponivel nesta versao");
    }

    private static Path openWithSwing() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            throw new IllegalStateException("Ambiente sem interface grafica");
        }

        Path[] var0 = new Path[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception var2) {
            }

            JFileChooser var1 = new JFileChooser();
            var1.setDialogTitle("Select cape PNG/GIF");
            var1.setFileFilter(new FileNameExtensionFilter("Cape images (*.png, *.gif)", "png", "gif"));
            if (var1.showOpenDialog(null) == 0 && var1.getSelectedFile() != null) {
                var0[0] = var1.getSelectedFile().toPath();
            }
        });
        return var0[0];
    }
}
