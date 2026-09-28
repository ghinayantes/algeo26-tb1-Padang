package algeo.gui;

import algeo.io.IOUtils;
import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.io.File;

public class MatrixPage extends VBox {

    private static final long MAX_VISIBLE_FILE_MATRIX_CELLS = 1_011;

    public static final class Result {
        final String display;
        final String fileContent;

        public Result(String display, String fileContent) {
            this.display = display;
            this.fileContent = fileContent;
        }
    }

    public interface Solver {
        Result solve(Matrix m, String method);
    }

    private final boolean augmented;
    private final int maxSize;
    private final Solver solver;
    private final String[] methods;
    private String method;
    private boolean loadedFromFile;
    private boolean settingFileDimensions;
    private Matrix largeFileMatrix;

    private final Spinner<Integer> rowsSpinner;
    private final Spinner<Integer> colsSpinner; // hanya untuk augmented
    private final GridPane grid = new GridPane();
    private TextField[][] cells;
    private final Label status = Theme.statusLabel();
    private final TextArea output = Theme.outputArea();
    private String lastFileContent;

    public MatrixPage(String[] methods, boolean augmented, int maxSize, String solveLabel, Solver solver) {
        super(16);
        this.methods = methods;
        this.method = methods[0];
        this.augmented = augmented;
        this.maxSize = maxSize;
        this.solver = solver;

        // Metode 
        HBox segmented = segmented();

        // Ukuran
        rowsSpinner = spinner(1, IOUtils.MAX_FILE_MATRIX_ROWS, 3);
        colsSpinner = spinner(1, IOUtils.MAX_FILE_MATRIX_ROWS, 3);
        HBox sizeRow = augmented
                ? new HBox(20, Theme.labeled("Persamaan", rowsSpinner), Theme.labeled("Variabel", colsSpinner))
                : new HBox(20, Theme.labeled("Ukuran n \u00D7 n", rowsSpinner));
        sizeRow.setAlignment(Pos.CENTER_LEFT);
        rowsSpinner.valueProperty().addListener((o, a, b) -> dimensionChanged(rowsSpinner));
        colsSpinner.valueProperty().addListener((o, a, b) -> dimensionChanged(colsSpinner));

        Button loadBtn = Theme.secondaryButton("Muat dari .txt");
        loadBtn.setOnAction(e -> loadFile());
        Button clearBtn = Theme.secondaryButton("Kosongkan");
        clearBtn.setOnAction(e -> clearGrid());
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox topRow = new HBox(10, sizeRow, spacer, clearBtn, loadBtn);
        topRow.setAlignment(Pos.CENTER_LEFT);

        grid.setHgap(6);
        grid.setVgap(6);
        grid.getStyleClass().add("grid-surface");
        grid.setPadding(new Insets(0, 0, 8, 0));
        VBox gridHolder = new VBox(grid);
        gridHolder.getStyleClass().add("grid-surface");
        ScrollPane gridScroll = new ScrollPane(gridHolder);
        gridScroll.viewportBoundsProperty().addListener((o, a, b) -> {
            gridHolder.setMinWidth(b.getWidth());
            gridHolder.setMinHeight(b.getHeight());
        });
        gridScroll.getStyleClass().add("scroll-transparent");
        gridScroll.setFitToHeight(true);
        gridScroll.setPadding(new Insets(4, 0, 4, 0));

        Button solveBtn = Theme.primaryButton(solveLabel);
        solveBtn.setOnAction(e -> solve());

        VBox inputCard = Theme.card(16,
                Theme.heading("Metode"), segmented,
                sectionDivider(),
                topRow, gridScroll,
                solveBtn, status);

        Button saveBtn = Theme.secondaryButton("Simpan hasil .txt");
        saveBtn.setOnAction(e -> save());
        Label resTitle = Theme.heading("Hasil");
        HBox sp2 = new HBox();
        HBox.setHgrow(sp2, Priority.ALWAYS);
        HBox resHeader = new HBox(8, resTitle, sp2, saveBtn);
        resHeader.setAlignment(Pos.CENTER_LEFT);
        output.setPrefRowCount(14);
        output.setPromptText("Hasil perhitungan akan muncul di sini");
        VBox resultCard = Theme.card(12, resHeader, output);

        getChildren().addAll(inputCard, resultCard);
        rebuildGrid(null);
    }

    private HBox segmented() {
        HBox box = new HBox(4);
        box.getStyleClass().add("segmented-box");
        box.setMaxWidth(Region.USE_PREF_SIZE);
        Button[] buttons = new Button[methods.length];
        for (int i = 0; i < methods.length; i++) {
            Button b = new Button(methods[i]);
            b.getStyleClass().add("segment-button");
            final String m = methods[i];
            b.setOnAction(e -> {
                method = m;
                paintSegments(buttons);
            });
            buttons[i] = b;
        }
        paintSegments(buttons);
        box.getChildren().addAll(buttons);
        return box;
    }

    private void paintSegments(Button[] buttons) {
        for (Button b : buttons) {
            Theme.setActive(b, b.getText().equals(method));
        }
    }

    private Region sectionDivider() {
        Region r = new Region();
        r.setMinHeight(1);
        r.setMaxHeight(1);
        r.getStyleClass().add("divider");
        return r;
    }

    private Spinner<Integer> spinner(int min, int max, int init) {
        Spinner<Integer> s = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, init));
        s.setEditable(true);
        s.setPrefWidth(84);
        s.getEditor().getStyleClass().addAll("text-field", "mono-field");
        return s;
    }

    private int rows() { return rowsSpinner.getValue(); }

    private void dimensionChanged(Spinner<Integer> changed) {
        if (settingFileDimensions) return;
        loadedFromFile = false;
        largeFileMatrix = null;
        if (changed.getValue() > maxSize) {
            settingFileDimensions = true;
            rowsSpinner.getValueFactory().setValue(Math.min(rowsSpinner.getValue(), maxSize));
            colsSpinner.getValueFactory().setValue(Math.min(colsSpinner.getValue(), maxSize));
            settingFileDimensions = false;
            rebuildGrid(null);
            Theme.showStatus(status, Theme.Tone.WARN,
                    "Input manual dibatasi maksimal " + maxSize + " baris dan variabel");
            return;
        }
        rebuildGrid(null);
    }

    private int cols() { return augmented ? colsSpinner.getValue() + 1 : rowsSpinner.getValue(); }

    private void rebuildGrid(Matrix m) {
        int r = rows(), c = cols();
        TextField[][] old = cells;
        cells = new TextField[r][c];
        grid.getChildren().clear();

        int varCount = augmented ? c - 1 : c;
        for (int j = 0; j < c; j++) {
            String head = (augmented && j == c - 1) ? "b" : (augmented ? "x" + (j + 1) : String.valueOf(j + 1));
            Label h = Theme.label(head, "matrix-col-header");
            GridPane.setHalignment(h, javafx.geometry.HPos.CENTER);
            grid.add(h, gridCol(j, varCount), 0);
        }
        for (int i = 0; i < r; i++) {
            for (int j = 0; j < c; j++) {
                TextField tf = Theme.cellField(66);
                if (m != null) {
                    tf.setText(plain(m.get(i, j)));
                } else if (old != null && i < old.length) {
                    int oldVar = augmented ? old[0].length - 1 : old[0].length;
                    boolean isB = augmented && j == c - 1;
                    if (isB) tf.setText(old[i][old[0].length - 1].getText());
                    else if (j < oldVar) tf.setText(old[i][j].getText());
                }
                if (augmented && j == c - 1) Theme.markAsTarget(tf);
                cells[i][j] = tf;
                grid.add(tf, gridCol(j, varCount), i + 1);
            }
        }
        if (augmented) {
            Region bar = new Region();
            bar.setMinWidth(2);
            bar.setPrefWidth(2);
            bar.getStyleClass().add("augmented-separator");
            grid.add(bar, varCount, 1, 1, r);
        }
    }

    // algoritma untuk "Kosongkan"
    private void clearGrid() {
        loadedFromFile = false;
        largeFileMatrix = null;
        if (cells.length == 0) {
            settingFileDimensions = true;
            rowsSpinner.getValueFactory().setValue(3);
            colsSpinner.getValueFactory().setValue(3);
            settingFileDimensions = false;
            rebuildGrid(null);
        }
        for (TextField[] row : cells) {
            for (TextField tf : row) {
                tf.setText("");
            }
        }
        output.clear();
        lastFileContent = null;
        Theme.clearStatus(status);
    }

    private int gridCol(int j, int varCount) {
        return (augmented && j >= varCount) ? j + 1 : j; // sisipkan kolom pemisah sebelum b
    }

    // pesan error input 
    private Matrix readGrid() {
    if (loadedFromFile && largeFileMatrix != null) return largeFileMatrix;
    if (cells.length == 0) throw new MatrixException("Tidak ada matriks yang dimuat atau dimasukkan");
    if (!loadedFromFile && (cells.length > IOUtils.MAX_MANUAL_MATRIX_SIZE
            || (augmented ? cells[0].length - 1 : cells[0].length) > IOUtils.MAX_MANUAL_MATRIX_SIZE)) {
        throw new MatrixException("Input manual dibatasi maksimal " + IOUtils.MAX_MANUAL_MATRIX_SIZE
                + " × " + IOUtils.MAX_MANUAL_MATRIX_SIZE
                + " (kolom konstanta SPL tidak dihitung sebagai variabel)");
    }
    Matrix m = new Matrix(cells.length, cells[0].length);
    StringBuilder errorBuilder = new StringBuilder();

    for (int i = 0; i < cells.length; i++) {
        for (int j = 0; j < cells[0].length; j++) {
            String t = cells[i][j].getText().trim();
            if (t.isEmpty()) {
                if (errorBuilder.length() > 0) {
                    errorBuilder.append("\n");
                }
                errorBuilder.append("Sel baris ").append(i + 1)
                            .append(", kolom ").append(j + 1)
                            .append(" masih kosong");
            } else {
                try {
                    m.set(i, j, IOUtils.parseDouble(t));
                } catch (NumberFormatException e) {
                    if (errorBuilder.length() > 0) {
                        errorBuilder.append("\n");
                    }
                    errorBuilder.append("Format angka tidak valid pada baris ").append(i + 1)
                                .append(", kolom ").append(j + 1).append(".");
                }
            }
        }
    }

    if (errorBuilder.length() > 0) {
        throw new MatrixException(errorBuilder.toString());
    }

    return m;
}

    private void loadFile() {
        File f = FileDialogs.open(window());
        if (f == null) return;
        try {
            Matrix m = IOUtils.readMatrixFromFile(f.getAbsolutePath());
            if (augmented) {
                if (m.getCols() < 2) throw new MatrixException("File SPL minimal punya 2 kolom (koefisien dan konstanta)");
                if (m.getRows() > IOUtils.MAX_FILE_MATRIX_ROWS || m.getCols() - 1 > IOUtils.MAX_FILE_MATRIX_ROWS)
                    throw new MatrixException("Ukuran matriks SPL pada berkas melebihi batas "
                            + IOUtils.MAX_FILE_MATRIX_ROWS + " × " + (IOUtils.MAX_FILE_MATRIX_ROWS + 1));
            } else {
                if (m.getRows() != m.getCols()) throw new MatrixException("Matriks pada file harus persegi");
                if (m.getRows() > IOUtils.MAX_FILE_MATRIX_ROWS)
                    throw new MatrixException("Ukuran matriks pada berkas melebihi batas " + IOUtils.MAX_FILE_MATRIX_ROWS);
            }
            boolean tooLargeToDisplay = (long) m.getRows() * m.getCols() > MAX_VISIBLE_FILE_MATRIX_CELLS;
            if (tooLargeToDisplay) {
                largeFileMatrix = m;
                loadedFromFile = true;
                Theme.showStatus(status, Theme.Tone.INFO,
                        "Matriks " + m.getRows() + " × " + m.getCols()
                                + " dimuat dari berkas dan akan diproses langsung karena ukurannya terlalu besar untuk ditampilkan di kisi.");
                return;
            }
            settingFileDimensions = true;
            if (augmented) colsSpinner.getValueFactory().setValue(m.getCols() - 1);
            rowsSpinner.getValueFactory().setValue(m.getRows());
            largeFileMatrix = null;
            rebuildGrid(m);
            loadedFromFile = true;
            settingFileDimensions = false;
            Theme.showStatus(status, Theme.Tone.INFO,
                    "Matriks berukuran "+ m.getRows() + " × " + m.getCols() + " dimuat dari " + f.getName());
        } catch (MatrixException ex) {
            settingFileDimensions = false;
            Theme.showStatus(status, Theme.Tone.ERROR, ex.getMessage());
        }
    }

    private void solve() {
        try {
            Matrix m = readGrid();
            Result r = solver.solve(m, method);
            output.setText(r.display);
            lastFileContent = r.fileContent;
            Theme.clearStatus(status);
        } catch (MatrixException ex) {
            Theme.showStatus(status, Theme.Tone.ERROR, ex.getMessage());
        }
    }

    private void save() {
        if (lastFileContent == null) {
            Theme.showStatus(status, Theme.Tone.WARN, "Belum ada hasil untuk disimpan");
            return;
        }
        String msg = FileDialogs.save(window(), lastFileContent);
        if (msg != null) Theme.showStatus(status, msg.startsWith("Gagal") ? Theme.Tone.ERROR : Theme.Tone.INFO, msg);
    }

    private Window window() {
        return getScene() == null ? null : getScene().getWindow();
    }

    private static String plain(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) return String.valueOf((long) v);
        return String.valueOf(v);
    }

}
