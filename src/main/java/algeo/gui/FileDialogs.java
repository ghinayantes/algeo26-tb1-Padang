package algeo.gui;

import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

final class FileDialogs {

    private FileDialogs() {
    }

    static File open(Window owner) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Pilih file input (.txt)");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        File testDir = new File("test");
        if (testDir.isDirectory()) chooser.setInitialDirectory(testDir.getAbsoluteFile());
        return chooser.showOpenDialog(owner);
    }

    /** @return pesan status, atau null kalau pengguna membatalkan. */
    static String save(Window owner, String content) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Simpan hasil");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text files", "*.txt"));
        chooser.setInitialFileName("hasil.txt");
        File file = chooser.showSaveDialog(owner);
        if (file == null) return null;
        try {
            Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
            return "Tersimpan ke " + file.getAbsolutePath();
        } catch (Exception ex) {
            return "Gagal menyimpan: " + ex.getMessage();
        }
    }
}
