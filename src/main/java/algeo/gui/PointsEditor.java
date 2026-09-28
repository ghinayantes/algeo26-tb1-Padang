package algeo.gui;

import algeo.io.IOUtils;
import algeo.matrix.MatrixException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

// tabel titik (x, y)
public class PointsEditor extends VBox {

    private final int maxPoints;
    private final List<TextField[]> rows = new ArrayList<>();
    private final GridPane grid = new GridPane();
    private final Button addBtn;
    private final Label counter;

    public PointsEditor(int maxPoints, int initialRows) {
        super(10);
        this.maxPoints = maxPoints;

        grid.setHgap(8);
        grid.setVgap(6);
        grid.getStyleClass().add("grid-surface");
        grid.setPadding(new Insets(0, 4, 0, 0));

        ScrollPane scroller = new ScrollPane(grid);
        scroller.setFitToWidth(true);
        scroller.setMaxHeight(300);
        scroller.getStyleClass().add("scroll-transparent");
        scroller.setPadding(Insets.EMPTY);

        addBtn = Theme.secondaryButton("+ Tambah titik");
        addBtn.setOnAction(e -> addRow("", ""));
        counter = Theme.caption("");
        HBox footer = new HBox(12, addBtn, counter);
        footer.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(scroller, footer);
        for (int i = 0; i < initialRows; i++) addRow("", "");
    }

    private void addRow(String x, String y) {
        if (rows.size() >= maxPoints) return;
        TextField xf = Theme.cellField(110);
        TextField yf = Theme.cellField(110);
        HBox.setHgrow(xf, Priority.ALWAYS);
        HBox.setHgrow(yf, Priority.ALWAYS);
        xf.setText(x);
        yf.setText(y);
        rows.add(new TextField[]{xf, yf});
        rebuild();
    }

    private void rebuild() {
        grid.getChildren().clear();
        Label hx = Theme.label("x", "matrix-col-header");
        Label hy = Theme.label("y", "matrix-col-header");
        grid.add(hx, 1, 0);
        grid.add(hy, 2, 0);
        for (int i = 0; i < rows.size(); i++) {
            TextField[] r = rows.get(i);
            Label idx = Theme.label(String.valueOf(i + 1), "row-index-label");
            idx.setMinWidth(18);
            Button del = Theme.ghostButton("\u2715");
            final int row = i;
            del.setOnAction(e -> {
                if (rows.size() > 1) {
                    rows.remove(row);
                    rebuild();
                }
            });
            grid.add(idx, 0, i + 1);
            grid.add(r[0], 1, i + 1);
            grid.add(r[1], 2, i + 1);
            grid.add(del, 3, i + 1);
        }
        addBtn.setDisable(rows.size() >= maxPoints);
        counter.setText(rows.size() + " dari maks " + maxPoints + " titik");
    }

    // hapus baris
    public void clear() {
        for (TextField[] row : rows) {
            row[0].setText("");
            row[1].setText("");
        }
    }

    // isi tabel dari file
    public void setPoints(double[][] pts) {
        int n = pts[0].length;
        if (n > maxPoints) {
            throw new MatrixException("File berisi " + n + " titik, melebihi batas " + maxPoints + " titik untuk modul ini.");
        }
        rows.clear();
        for (int i = 0; i < n; i++) {
            TextField xf = Theme.cellField(110);
            TextField yf = Theme.cellField(110);
            xf.setText(plain(pts[0][i]));
            yf.setText(plain(pts[1][i]));
            rows.add(new TextField[]{xf, yf});
        }
        rebuild();
    }

    // baca baris
    public double[][] readPoints() {
        List<double[]> list = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            String xs = rows.get(i)[0].getText().trim();
            String ys = rows.get(i)[1].getText().trim();
            if (xs.isEmpty() && ys.isEmpty()) continue;
            if (xs.isEmpty() || ys.isEmpty()) {
                throw new MatrixException("Titik ke-" + (i + 1) + " belum lengkap (x dan y harus diisi)");
            }
            list.add(new double[]{IOUtils.parseDouble(xs), IOUtils.parseDouble(ys)});
        }
        double[] x = new double[list.size()], y = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            x[i] = list.get(i)[0];
            y[i] = list.get(i)[1];
        }
        return new double[][]{x, y};
    }

    // Tampilkan angka dari file tanpa pembulatan
    private static String plain(double v) {
        if (v == Math.rint(v) && Math.abs(v) < 1e15) return String.valueOf((long) v);
        return String.valueOf(v);
    }
}