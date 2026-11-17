package max.panda.capechanger.g8.screen;

import java.awt.GraphicsEnvironment;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.function.Consumer;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import max.panda.capechanger.g8.CapeChanger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

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
        MemoryStack var0 = MemoryStack.stackPush();

        Path var1;
        try {
            PointerBuffer var2 = var0.mallocPointer(2);
            ByteBuffer var3 = var0.UTF8("*.png");
            var2.put(0, var3);
            var2.put(1, var0.UTF8("*.gif"));
            String var4 = TinyFileDialogs.tinyfd_openFileDialog("Select cape PNG/GIF", "", var2, "Cape images (*.png, *.gif)", false);
            var1 = var4 != null && !var4.isBlank() ? Path.of(var4) : null;
        } catch (Throwable var6) {
            if (var0 != null) {
                try {
                    var0.close();
                } catch (Throwable var5) {
                    var6.addSuppressed(var5);
                }
            }

            throw var6;
        }

        if (var0 != null) {
            var0.close();
        }

        return var1;
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
