package algeo.gui;

import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.InputStream;
import java.util.List;

public final class Theme {

    private Theme() {
    }

    // palet warna
    public static final Color PAPER = Color.web("#EAF1F1");
    public static final Color SURFACE = Color.WHITE;
    public static final Color SIDEBAR = Color.web("#253759");
    public static final Color INK = Color.web("#253759");
    public static final Color MUTED = Color.web("#466273");
    public static final Color LINE = Color.web("#D2E2E2");
    public static final Color ACCENT = Color.web("#EDAE49");
    public static final Color ACCENT_HOVER = Color.web("#C9943E");
    public static final Color ACCENT_SOFT = Color.web("#FCF3E4");
    public static final Color EVAL = Color.web("#C4841D");
    public static final Color EVAL_SOFT = Color.web("#FBF1DF");
    public static final Color DANGER = Color.web("#B4413B");
    public static final Color DANGER_SOFT = Color.web("#F8E6E4");

    // font
    static {
        loadEmbeddedFonts();
    }

    private static void loadEmbeddedFonts() {
        String[] files = {
                "fonts/Poppins-Regular.ttf",
                "fonts/Poppins-SemiBold.ttf",
                "fonts/Poppins-Bold.ttf",
        };
        for (String path : files) {
            try (InputStream in = Theme.class.getResourceAsStream(path)) {
                if (in != null) {
                    Font.loadFont(in, 12); 
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static final String SANS = pickFamily(List.of("Poppins", "Segoe UI", "Inter", "Helvetica Neue", "Noto Sans", "DejaVu Sans"));
    public static final String SANS_SEMIBOLD = pickFamily(List.of("Poppins SemiBold", "Poppins", "Segoe UI Semibold", "Segoe UI", "Inter", "Helvetica Neue", "Noto Sans", "DejaVu Sans"));
    public static final String MONO = pickFamily(List.of("Cascadia Mono", "Consolas", "JetBrains Mono", "Menlo", "DejaVu Sans Mono"));

    private static String pickFamily(List<String> candidates) {
        List<String> installed = Font.getFamilies();
        for (String c : candidates) {
            if (installed.contains(c)) return c;
        }
        return Font.getDefault().getFamily();
    }

    public static Font sans(double size) {
        return Font.font(SANS, FontWeight.NORMAL, size);
    }

    public static Font sans(double size, FontWeight weight) {
        return Font.font(SANS, weight, size);
    }

    public static Font mono(double size) {
        return Font.font(MONO, FontWeight.NORMAL, size);
    }

    static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");

    public static void setActive(Node n, boolean active) {
        n.pseudoClassStateChanged(ACTIVE, active);
    }

    public static void install(Scene scene) {
        scene.getStylesheets().add(Theme.class.getResource("theme.css").toExternalForm());
    }

    // kartu section tabel
    public static VBox card(double spacing, Node... children) {
        VBox box = new VBox(spacing, children);
        box.getStyleClass().add("card");
        return box;
    }

    // tombol
    public static Button primaryButton(String label) {
        Button b = new Button(label);
        b.getStyleClass().add("button-primary");
        return b;
    }

    public static Button secondaryButton(String label) {
        Button b = new Button(label);
        b.getStyleClass().add("button-secondary");
        return b;
    }

    public static Button ghostButton(String label) {
        Button b = new Button(label);
        b.getStyleClass().add("button-ghost");
        return b;
    }

    // input
    public static TextField field(String prompt, double width) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(width);
        tf.getStyleClass().add("text-field");
        return tf;
    }

    public static TextField cellField(double width) {
        TextField tf = new TextField();
        tf.setPrefWidth(width);
        tf.setAlignment(Pos.CENTER_RIGHT);
        tf.getStyleClass().addAll("text-field", "mono-field");
        return tf;
    }

    public static void markAsTarget(TextField tf) {
        tf.getStyleClass().add("cell-field-target");
    }

    public static TextArea outputArea() {
        TextArea ta = new TextArea();
        ta.setEditable(false);
        ta.setWrapText(false);
        ta.setPrefRowCount(10);
        ta.getStyleClass().add("output-area");
        return ta;
    }

    // teks
    public static Label heading(String s) {
        Label l = new Label(s);
        l.getStyleClass().add("heading");
        return l;
    }

    public static Label caption(String s) {
        Label l = new Label(s);
        l.setWrapText(true);
        l.getStyleClass().add("caption");
        return l;
    }

    public static Label label(String s, String... styleClasses) {
        Label l = new Label(s);
        l.getStyleClass().addAll(styleClasses);
        return l;
    }

    public static HBox labeled(String labelText, Node control) {
        Label l = new Label(labelText);
        l.getStyleClass().add("field-label");
        HBox box = new HBox(10, l, control);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // pesan status inline
    public enum Tone { INFO, WARN, ERROR }

    public static Label statusLabel() {
        Label l = new Label();
        l.setWrapText(true);
        l.setMaxWidth(Double.MAX_VALUE);
        l.getStyleClass().add("status-label");
        l.setVisible(false);
        l.setManaged(false);
        return l;
    }

    public static void showStatus(Label l, Tone tone, String message) {
        l.setText(message);
        l.getStyleClass().removeAll("status-info", "status-warn", "status-error");
        l.getStyleClass().add(tone == Tone.ERROR ? "status-error" : tone == Tone.WARN ? "status-warn" : "status-info");
        l.setVisible(true);
        l.setManaged(true);
    }

    public static void clearStatus(Label l) {
        l.setVisible(false);
        l.setManaged(false);
    }
}