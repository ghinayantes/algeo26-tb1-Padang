package algeo.gui;

import algeo.io.IOUtils;
import algeo.matrix.MatrixException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.File;


// Halaman generik untuk Interpolasi Polinomial, Natural Cubic Spline, dan Regresi Spline Kubik

public class CurvePage extends HBox {

    public interface CurveModel {
        double evaluate(double x);
        double[] domain();
        double[] xs();
        double[] ys();
        double[] knots();         
        String summary();          
        String methodName();       
    }

    public interface ModelBuilder {
        CurveModel build(double[] xs, double[] ys, double[] knots);
    }

    private final String fnName;
    private final ModelBuilder builder;
    private final boolean usesKnots;

    private final PointsEditor points;
    private final TextField knotsField;
    private final TextField evalField;
    private final Label buildStatus = Theme.statusLabel();
    private final Label evalStatus = Theme.statusLabel();
    private final PlotView plot;
    private final TextArea output = Theme.outputArea();
    private final StringBuilder evalLog = new StringBuilder();

    private CurveModel model;

    public CurvePage(String fnName, int maxPoints, boolean usesKnots, ModelBuilder builder) {
        super(20);
        this.fnName = fnName;
        this.builder = builder;
        this.usesKnots = usesKnots;
        this.points = new PointsEditor(maxPoints, Math.min(4, maxPoints));
        this.plot = new PlotView(fnName);
        this.knotsField = Theme.field("contoh: 2.5 5 7.5", 260);
        this.evalField = Theme.field("nilai x", 140);

        // kolom input
        Button loadBtn = Theme.secondaryButton("Muat titik dari .txt");
        loadBtn.setOnAction(e -> loadFile());
        Button clearBtn = Theme.secondaryButton("Kosongkan");
        clearBtn.setOnAction(e -> {
            points.clear();
            if (usesKnots) knotsField.clear();
            model = null;
            plot.clear();
            output.clear();
            evalLog.setLength(0);
            evalField.clear();
            Theme.clearStatus(buildStatus);
            Theme.clearStatus(evalStatus);
        });
        HBox dataActions = new HBox(8, clearBtn, loadBtn);

        VBox dataCard = Theme.card(12, Theme.heading("Titik data"), dataActions, points);

        VBox knotsCard = null;
        if (usesKnots) {
            knotsCard = Theme.card(10,
                    Theme.heading("Knot"),
                    Theme.caption("Masukkan Posisi \u03BE (dipisah spasi) dan pastikan nilainya berada di dalam domain\nBiarkan kosong untuk regresi kubik biasa"),
                    knotsField);
        }

        Button buildBtn = Theme.primaryButton("Buat kurva");
        buildBtn.setOnAction(e -> buildModel());
        buildBtn.setMaxWidth(Double.MAX_VALUE);

        Button evalBtn = Theme.secondaryButton("Hitung " + fnName + "(x)");
        evalBtn.setOnAction(e -> evaluate());
        evalField.setOnAction(e -> evaluate());
        HBox evalRow = new HBox(8, evalField, evalBtn);
        evalRow.setAlignment(Pos.CENTER_LEFT);

        VBox evalCard = Theme.card(10,
                Theme.heading("Prediksi Nilai"),
                Theme.caption("Catatan: Nilai x di luar rentang data akan tetap dihitung, tetapi akurasi prediksinya tidak dijamin (Ekstrapolasi)"),
                evalRow,
                evalStatus);

        VBox left = new VBox(16);
        left.getChildren().add(dataCard);
        if (knotsCard != null) left.getChildren().add(knotsCard);
        left.getChildren().addAll(buildBtn, buildStatus, evalCard);
        left.setMinWidth(340);
        left.setPrefWidth(340);
        left.setMaxWidth(340);

        // Kolom grafik dan hasil 
        VBox plotCard = Theme.card(0, plot);
        plotCard.setPadding(new Insets(8));
        VBox.setVgrow(plot, Priority.ALWAYS);

        Button saveBtn = Theme.secondaryButton("Simpan hasil .txt");
        saveBtn.setOnAction(e -> save());
        VBox resultCard = Theme.card(12, header("Hasil", saveBtn), output);

        VBox right = new VBox(16, plotCard, resultCard);
        HBox.setHgrow(right, Priority.ALWAYS);

        getChildren().addAll(left, right);
        output.setPromptText("Persamaan dan hasil prediksi " + fnName + " akan muncul di sini");
        output.setWrapText(true);
    }

    private HBox header(String title, javafx.scene.Node action) {
        Label l = Theme.heading(title);
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox h = new HBox(8, l, spacer, action);
        h.setAlignment(Pos.CENTER_LEFT);
        return h;
    }

    private void loadFile() {
        File f = FileDialogs.open(window());
        if (f == null) return;
        try {
            points.setPoints(IOUtils.readPointsFromFile(f.getAbsolutePath()));
            Theme.showStatus(buildStatus, Theme.Tone.INFO, "Dimuat dari " + f.getName());
        } catch (MatrixException ex) {
            Theme.showStatus(buildStatus, Theme.Tone.ERROR, ex.getMessage());
        }
    }

    private void buildModel() {
        Theme.clearStatus(evalStatus);
        try {
            double[][] pts = points.readPoints();
            double[] knots = usesKnots ? parseKnots(knotsField.getText()) : new double[0];
            model = builder.build(pts[0], pts[1], knots);
            double[] d = model.domain();
            evalLog.setLength(0);
            output.setText(model.summary());
            plot.setModel(model::evaluate, model.xs(), model.ys(), d[0], d[1], model.knots());
            Theme.showStatus(buildStatus, Theme.Tone.INFO,
                    "Kurva dibuat dari " + model.xs().length + " titik\nDomain: [" + IOUtils.format3(d[0]) + ", " + IOUtils.format3(d[1]) + "]");
        } catch (MatrixException ex) {
            model = null;
            plot.clear();
            output.clear();
            Theme.showStatus(buildStatus, Theme.Tone.ERROR, ex.getMessage());
        }
    }

    private static double[] parseKnots(String text) {
        String t = text.trim();
        if (t.isEmpty()) return new double[0];
        String[] tokens = t.split("[\\s;]+");
        double[] k = new double[tokens.length];
        for (int i = 0; i < tokens.length; i++) k[i] = IOUtils.parseDouble(tokens[i]);
        return k;
    }

    private void evaluate() {
        if (model == null) {
            Theme.showStatus(evalStatus, Theme.Tone.WARN, "Buat kurva terlebih dahulu");
            return;
        }
        try {
            double x = IOUtils.parseDouble(evalField.getText());
            double y = model.evaluate(x);
            double[] d = model.domain();
            boolean outside = x < d[0] || x > d[1];
            String line = fnName + "(" + IOUtils.format3(x) + ") = " + IOUtils.format3(y) + (outside ? "   [ekstrapolasi]" : "");
            evalLog.append(line).append('\n');
            output.appendText((output.getText().endsWith("\n") ? "" : "\n") + line + "\n");
            plot.addEvalPoint(x, y);
            if (outside) {
                Theme.showStatus(evalStatus, Theme.Tone.WARN,
                        "x = " + IOUtils.format3(x) + " berada di luar domain [" + IOUtils.format3(d[0]) + ", " + IOUtils.format3(d[1])
                                + "]\nNilai " + fnName + "(x) adalah ekstrapolasi sehingga akurasinya tidak dijamin");
            } else {
                Theme.showStatus(evalStatus, Theme.Tone.INFO, line);
            }
        } catch (MatrixException ex) {
            Theme.showStatus(evalStatus, Theme.Tone.ERROR, ex.getMessage());
        }
    }

    private void save() {
        if (model == null) {
            Theme.showStatus(buildStatus, Theme.Tone.WARN, "Belum ada hasil untuk disimpan.");
            return;
        }
        StringBuilder pts = new StringBuilder();
        double[] xs = model.xs(), ys = model.ys();
        for (int i = 0; i < xs.length; i++) {
            pts.append(IOUtils.format3(xs[i])).append('\t').append(IOUtils.format3(ys[i])).append('\n');
        }
        double[] d = model.domain();
        String content = "Metode: " + model.methodName() + "\n\n"
                + "Input Titik Sampel:\n" + pts + "\n"
                + "Domain: [" + IOUtils.format3(d[0]) + ", " + IOUtils.format3(d[1]) + "]\n\n"
                + model.summary() + "\n\n"
                + "Hasil Evaluasi:\n" + (evalLog.length() == 0 ? "(tidak ada)\n" : evalLog.toString());
        String msg = FileDialogs.save(window(), content);
        if (msg != null) Theme.showStatus(buildStatus, msg.startsWith("Gagal") ? Theme.Tone.ERROR : Theme.Tone.INFO, msg);
    }

    private Window window() {
        return getScene() == null ? null : getScene().getWindow();
    }
}