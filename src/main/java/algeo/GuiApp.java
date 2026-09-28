package algeo;

import algeo.determinant.DeterminantCofactor;
import algeo.determinant.DeterminantOBE;
import algeo.gui.CurvePage;
import algeo.gui.MatrixPage;
import algeo.gui.Theme;
import algeo.interpolation.PolynomialInterpolation;
import algeo.inverse.InverseAdjoint;
import algeo.inverse.InverseAugmented;
import algeo.io.IOUtils;
import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;
import algeo.spl.Cramer;
import algeo.spl.Gauss;
import algeo.spl.GaussJordan;
import algeo.spl.InverseSPL;
import algeo.spl.SPLSolver;
import algeo.spline.CubicSplineRegression;
import algeo.spline.NaturalCubicSpline;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class GuiApp extends Application {

    private static final int MAX_MATRIX_SIZE = 11;
    private static final int MAX_INTERPOLATION_POINTS = 10;
    private static final int MAX_REGRESSION_POINTS = IOUtils.MAX_CURVE_POINTS;

    private final BorderPane root = new BorderPane();
    private final Label pageTitle = Theme.label("", "page-title");
    private final Label pageDesc = Theme.caption("");
    private final ScrollPane contentScroll = new ScrollPane();

    private final Map<String, Page> pages = new LinkedHashMap<>();
    private final List<Button> navButtons = new ArrayList<>();

    private static final class Page {
        final String group, title, description;
        final Supplier<Node> factory;
        Node node;

        Page(String group, String title, String description, Supplier<Node> factory) {
            this.group = group;
            this.title = title;
            this.description = description;
            this.factory = factory;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        registerPages();

        VBox header = new VBox(4, pageTitle, pageDesc);
        header.setPadding(new Insets(28, 32, 8, 32));

        contentScroll.setFitToWidth(true);
        contentScroll.getStyleClass().add("content-scroll");
        contentScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        VBox main = new VBox(header, contentScroll);
        VBox.setVgrow(contentScroll, Priority.ALWAYS);
        main.getStyleClass().add("paper-bg");

        root.setLeft(buildSidebar());
        root.setCenter(main);

        show(pages.keySet().iterator().next());

        Scene scene = new Scene(root, 1280, 820, Theme.PAPER);
        Theme.install(scene);
        stage.setTitle("Kalkulator Aljabar Linier \u00B7 Kelompok Padang");
        stage.setMinWidth(1060);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    private Node buildSidebar() {
        Label brand = Theme.label("Aljabar Linier \ndan Geometri", "brand-title");
        Label sub = Theme.label("Sistem Persamaan Linier, \nDeterminan, dan Penerapan\n\nIF2123 \u00B7 Kelompok Padang", "brand-sub");
        VBox brandBox = new VBox(2, brand, sub);
        brandBox.setPadding(new Insets(26, 20, 22, 20));

        VBox nav = new VBox(2);
        nav.setPadding(new Insets(0, 10, 0, 10));
        String currentGroup = null;
        for (Map.Entry<String, Page> e : pages.entrySet()) {
            Page p = e.getValue();
            if (!p.group.equals(currentGroup)) {
                currentGroup = p.group;
                Label g = Theme.label(p.group, "nav-group-label");
                g.setPadding(new Insets(nav.getChildren().isEmpty() ? 0 : 18, 10, 6, 10));
                nav.getChildren().add(g);
            }
            Button b = new Button(p.title);
            b.setUserData(e.getKey());
            b.getStyleClass().add("nav-button");
            b.setMaxWidth(Double.MAX_VALUE);
            b.setAlignment(Pos.CENTER_LEFT);
            b.setWrapText(true);
            b.setOnAction(ev -> show(e.getKey()));
            navButtons.add(b);
            nav.getChildren().add(b);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // logo itb
        HBox logoBox = new HBox();
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(0, 20, 0, 20)); 

       try {
            Image img = new Image(getClass().getResourceAsStream("/itb + stei.png"), 0, 80, true, true); 
            
            ImageView logoView = new ImageView(img);
            
            logoView.setFitHeight(40); 
            logoView.setPreserveRatio(true);
            logoView.setSmooth(true); 

            logoBox.getChildren().add(logoView);
        } catch (Exception e) {
            System.err.println("Gagal memuat logo: " + e.getMessage());
        }

        Label foot = Theme.label("Semester I Tahun 2026/2027", "sidebar-footer");
        foot.setPadding(new Insets(16, 20, 20, 20));

        VBox side = new VBox(brandBox, nav, spacer, logoBox, foot);
        side.setPrefWidth(232);
        side.setMinWidth(232);
        side.getStyleClass().add("sidebar");
        return side;
    }

    private void show(String key) {
        Page p = pages.get(key);
        if (p.node == null) {
            Node content = p.factory.get();
            VBox wrap = new VBox(content);
            wrap.setPadding(new Insets(12, 32, 32, 32));
            wrap.getStyleClass().add("paper-bg");
            p.node = wrap;
        }
        pageTitle.setText(p.title);
        pageDesc.setText(p.description);
        contentScroll.setContent(p.node);
        contentScroll.setVvalue(0);

        for (Button b : navButtons) {
            Theme.setActive(b, key.equals(b.getUserData()));
        }
    }

    private void registerPages() {
        pages.put("spl", new Page("Matriks", "Sistem persamaan linier (SPL)",
                "Menyelesaikan persamaan Ax = b dengan eliminasi Gauss, Gauss-Jordan, matriks balikan, atau kaidah Cramer.",
                this::splPage));
        pages.put("det", new Page("Matriks", "Determinan Matriks",
                "Menghitung det(A) melalui metode reduksi baris (OBE) atau ekspansi kofaktor.",
                this::determinantPage));
        pages.put("inv", new Page("Matriks", "Matriks balikan (Invers)",
                "Mencari A\u207B\u00B9 dengan metode augmentasi [A | I] atau matriks adjoin.",
                this::inversePage));
        pages.put("poly", new Page("Kurva", "Interpolasi polinomial",
                "Bentuk kurva polinomial tunggal yang melewati seluruh titik data secara presisi.",
                this::polynomialPage));
        pages.put("spline", new Page("Kurva", "Interpolasi spline kubik natural",
                "Bentuk kurva mulus dari potongan polinomial kubik S(x) antar titik yang kontinu hingga turunan kedua, dengan S''(x) = 0 di kedua ujung.",
                this::splinePage));
        pages.put("reg", new Page("Kurva", "Regresi spline kubik",
                "Bentuk kurva pendekatan data non-linear melalui potongan polinomial bersimpul menggunakan Metode Kuadrat Terkecil.",
                this::regressionPage));
    }

    private Node splPage() {
        String[] methods = {"Gauss", "Gauss-Jordan", "Matriks balikan", "Kaidah Cramer"};
        return new MatrixPage(methods, true, MAX_MATRIX_SIZE, "Selesaikan", (m, method) -> {
            SPLSolver solver;
            switch (method) {
                case "Gauss": solver = new Gauss(); break;
                case "Gauss-Jordan": solver = new GaussJordan(); break;
                case "Matriks balikan": solver = new InverseSPL(); break;
                default: solver = new Cramer();
            }
            if (!solver.isApplicable(m)) {
                throw new MatrixException(solver.getName()
                        + " membutuhkan jumlah persamaan yang sama dengan jumlah variabel.\nSolusi: Gunakan eliminasi Gauss atau Gauss-Jordan.");
            }
            String result = solver.solve(m);
            String file = "Metode Penyelesaian SPL: " + solver.getName() + "\n\n"
                    + "Input Matriks (Augmented):\n" + IOUtils.matrixToString(m) + "\n"
                    + "Hasil SPL:\n" + result;
            return new MatrixPage.Result(result, file);
        });
    }

    private Node determinantPage() {
        String[] methods = {"Reduksi baris (OBE)", "Ekspansi kofaktor"};
        return new MatrixPage(methods, false, MAX_MATRIX_SIZE, "Hitung determinan", (m, method) -> {
            boolean obe = method.startsWith("Reduksi");
            String steps;
            String resultLine;
            try {
                double det = obe ? DeterminantOBE.determinantOBE(m) : DeterminantCofactor.cofactorExpansion(m);
                steps = obe ? DeterminantOBE.determinantOBEWithSteps(m) : DeterminantCofactor.cofactorExpansionWithSteps(m);
                resultLine = "Determinan = " + IOUtils.format3(det);
            } catch (MatrixException ex) {
                throw new MatrixException("Matriks tidak memiliki determinan: " + ex.getMessage());
            }
            String file = "Metode Perhitungan Determinan: " + method + "\n\n"
                    + "Input Matriks:\n" + IOUtils.matrixToString(m) + "\n"
                    + "Langkah-langkah Perhitungan:\n" + steps + "\n" + resultLine + "\n";
            return new MatrixPage.Result(steps + "\n" + resultLine, file);
        });
    }

    private Node inversePage() {
        String[] methods = {"Augmentasi [A | I]", "Adjoin"};
        return new MatrixPage(methods, false, MAX_MATRIX_SIZE, "Hitung balikan", (m, method) -> {
            boolean aug = method.startsWith("Augmentasi");
            Matrix inv;
            String steps;
            try {
                inv = aug ? InverseAugmented.inverse(m) : InverseAdjoint.inverse(m);
                steps = aug ? InverseAugmented.inverseWithSteps(m) : InverseAdjoint.inverseWithSteps(m);
            } catch (MatrixException ex) {
                throw new MatrixException("Matriks tidak memiliki balikan: " + ex.getMessage());
            }
            String file = "Metode Perhitungan Matriks Balikan: " + method + "\n\n"
                    + "Input Matriks:\n" + IOUtils.matrixToString(m) + "\n"
                    + "Langkah-langkah Perhitungan:\n" + steps + "\n"
                    + "Hasil Matriks Balikan:\n" + IOUtils.matrixToString(inv);
            return new MatrixPage.Result(steps + "\nMatriks balikan:\n" + IOUtils.matrixToString(inv), file);
        });
    }

    private Node polynomialPage() {
        return new CurvePage("P", MAX_INTERPOLATION_POINTS, false, (xs, ys, knots) -> {
            PolynomialInterpolation p = new PolynomialInterpolation(xs, ys);
            return new SimpleModel(p::evaluate, p.getDomain(), p.getXValues(), p.getYValues(), new double[0],
                    "Derajat polinomial: " + p.getDegree() + "\n" + p.toEquationString(),
                    "Interpolasi Polinomial");
        });
    }

    private Node splinePage() {
        return new CurvePage("S", MAX_INTERPOLATION_POINTS, false, (xs, ys, knots) -> {
            NaturalCubicSpline s = new NaturalCubicSpline(xs, ys);
            StringBuilder sb = new StringBuilder("Turunan kedua di tiap titik (M_i):\n");
            double[] m = s.getSecondDerivatives();
            for (int i = 0; i < m.length; i++) {
                sb.append("  M_").append(i).append(" = ").append(IOUtils.format3(m[i])).append('\n');
            }
            sb.append("\nPersamaan tiap segmen:\n").append(s.toFullEquationString());
            return new SimpleModel(s::evaluate, s.getDomain(), s.getXValues(), s.getYValues(), new double[0],
                    sb.toString(), "Interpolasi Splina Kubik Natural");
        });
    }

    private Node regressionPage() {
        return new CurvePage("\u0177", MAX_REGRESSION_POINTS, true, (xs, ys, knots) -> {
            CubicSplineRegression r = new CubicSplineRegression(xs, ys, knots);
            StringBuilder sb = new StringBuilder();
            sb.append("Jumlah knot K = ").append(r.getKnots().length)
              .append(", jumlah basis M = ").append(4 + r.getKnots().length)
              .append(", jumlah data N = ").append(r.getXValues().length).append('\n');
            if (r.getKnots().length > 0) {
                sb.append("Posisi knot:");
                for (double k : r.getKnots()) sb.append(' ').append(IOUtils.format3(k));
                sb.append('\n');
            }
            sb.append('\n').append(r.toFullEquationString()).append('\n');
            sb.append("\nJumlah kuadrat residu ||y - X\u03B2||\u00B2 = ").append(IOUtils.format3(r.getResidualSumOfSquares()));
            return new SimpleModel(r::evaluate, r.getDomain(), r.getXValues(), r.getYValues(), r.getKnots(),
                    sb.toString(), "Regresi Splina Kubik");
        });
    }

    private static final class SimpleModel implements CurvePage.CurveModel {
        private final java.util.function.DoubleUnaryOperator f;
        private final double[] domain, xs, ys, knots;
        private final String summary, method;

        SimpleModel(java.util.function.DoubleUnaryOperator f, double[] domain, double[] xs, double[] ys,
                    double[] knots, String summary, String method) {
            this.f = f;
            this.domain = domain;
            this.xs = xs;
            this.ys = ys;
            this.knots = knots;
            this.summary = summary;
            this.method = method;
        }

        public double evaluate(double x) { return f.applyAsDouble(x); }
        public double[] domain() { return domain; }
        public double[] xs() { return xs; }
        public double[] ys() { return ys; }
        public double[] knots() { return knots; }
        public String summary() { return summary; }
        public String methodName() { return method; }
    }
}
