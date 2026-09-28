package algeo.spline;

import algeo.common.PointSetUtils;
import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;

public class CubicSplineRegression {

    // batas maksimal titik data 
    private static final int MAX_POINTS = 10;

    // toleransi relatif untuk mendeteksi pivot bernilai nol
    private static final double RANK_TOLERANCE = 1e-11;

    private final double[] x;
    private final double[] y;
    private final double[] knots;
    private final double[] beta;
    // koefisien pada basis terskala
    private double[] gamma;
    // titik tengah rentang data
    private double center;
    // setengah lebar rentang data
    private double halfRange;

    public CubicSplineRegression(double[] xPoints, double[] yPoints, double[] knots) {
        PointSetUtils.SortedPoints sorted = PointSetUtils.validateAndSort(xPoints, yPoints, MAX_POINTS);
        this.x = sorted.x;
        this.y = sorted.y;
        this.knots = validateKnots(knots, this.x);

        int n = this.x.length;
        int m = 4 + this.knots.length;

        // memastikan sistem bersifat overdetermined sesuai spesifikasi
        if (n <= m) {
            throw new MatrixException("Jumlah titik data (" + n + ") harus lebih besar dari jumlah fungsi basis M = 4 + K = "
                    + m + ". Tambah titik data atau kurangi knot.");
        }

        this.beta = new double[m];
        buildRegression();
    }

    // melakukan validasi pada array knot
    private static double[] validateKnots(double[] knots, double[] sortedX) {
        if (knots == null) {
            throw new MatrixException("Array knot tidak boleh null.");
        }
        double[] k = knots.clone();
        for (double v : k) {
            if (Double.isNaN(v) || Double.isInfinite(v)) {
                throw new MatrixException("Posisi knot harus berupa angka berhingga.");
            }
        }
        java.util.Arrays.sort(k);
        for (int i = 0; i + 1 < k.length; i++) {
            if (k[i + 1] - k[i] < 1e-12) {
                throw new MatrixException("Knot tidak boleh duplikat (xi = " + k[i] + "). Spec mensyaratkan xi_1 < xi_2 < ... < xi_K.");
            }
        }
        double xMin = sortedX[0];
        double xMax = sortedX[sortedX.length - 1];
        for (double v : k) {
            // Memastikan knot berada tepat di dalam rentang titik data.
            if (v <= xMin || v >= xMax) {
                throw new MatrixException("Knot xi = " + v + " harus berada di dalam rentang data, yaitu "
                        + xMin + " < xi < " + xMax + ".");
            }
        }
        return k;
    }

    private void buildRegression() {
        int n = x.length;
        int k = knots.length;
        int m = 4 + k;

        // melakukan stabilisasi numerik melalui penskalaan variabel
        center = (x[0] + x[n - 1]) / 2.0;
        halfRange = (x[n - 1] - x[0]) / 2.0;

        // 1. Membangun matriks desain observasi.
        Matrix X = new Matrix(n, m);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                X.set(i, j, scaledBasis(j, x[i]));
            }
        }

        // 2. Membangun vektor target observasi Y.
        Matrix Y = new Matrix(n, 1);
        for (int i = 0; i < n; i++) {
            Y.set(i, 0, y[i]);
        }

        // 3. Menyusun matriks augmented untuk Persamaan Normal.
        Matrix XT = X.transpose();
        Matrix XTX = XT.multiply(X);
        Matrix XTY = XT.multiply(Y);

        Matrix augmented = new Matrix(m, m + 1);
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < m; c++) {
                augmented.set(r, c, XTX.get(r, c));
            }
            augmented.set(r, m, XTY.get(r, 0));
        }

        // 4. Menyelesaikan sistem Persamaan Normal menggunakan eliminasi Gauss-Jordan.
        gamma = solveGaussJordan(augmented);

        // 5. Mengonversi kembali koefisien gamma ke bentuk beta yang sebenarnya.
        for (int p = 0; p <= 3; p++) {
            double a = gamma[p] / Math.pow(halfRange, p);
            for (int q = 0; q <= p; q++) {
                beta[q] += a * binom(p, q) * Math.pow(-center, p - q);
            }
        }
        double s3 = halfRange * halfRange * halfRange;
        for (int j = 0; j < k; j++) {
            beta[4 + j] = gamma[4 + j] / s3;
        }
    }

    private static int binom(int n, int r) {
        int[][] table = { {1}, {1, 1}, {1, 2, 1}, {1, 3, 3, 1} };
        return table[n][r];
    }

    // Menghitung nilai fungsi basis dalam variabel yang telah diskalakan.
    private double scaledBasis(int j, double xv) {
        if (j < 4) {
            double u = (xv - center) / halfRange;
            return Math.pow(u, j);
        }
        double diff = (xv - knots[j - 4]) / halfRange;
        return diff > 0 ? diff * diff * diff : 0.0;
    }

    // Memprediksi nilai Y melalui perhitungan pada bentuk ter-skala demi menjaga presisi numerik.
    public double evaluate(double xt) {
        double result = 0.0;
        for (int j = 0; j < gamma.length; j++) {
            result += gamma[j] * scaledBasis(j, xt);
        }
        return result;
    }

    public double[] getDomain() {
        return new double[] { x[0], x[x.length - 1] };
    }

    public double[] getCoefficients() {
        return beta.clone();
    }

    public double[] getXValues() {
        return x.clone();
    }

    public double[] getYValues() {
        return y.clone();
    }

    public double[] getKnots() {
        return knots.clone();
    }

    // Menghitung nilai total error kuadrat residu.
    public double getResidualSumOfSquares() {
        double sse = 0.0;
        for (int i = 0; i < x.length; i++) {
            double e = y[i] - evaluate(x[i]);
            sse += e * e;
        }
        return sse;
    }

    public String toFullEquationString() {
        double[] b = cleanedCoefficients();
        StringBuilder sb = new StringBuilder("f(x) = ");
        sb.append(fmt(b[0]));
        appendTerm(sb, b[1], "x");
        appendTerm(sb, b[2], "x^2");
        appendTerm(sb, b[3], "x^3");
        for (int j = 0; j < knots.length; j++) {
            appendTerm(sb, b[4 + j], "(x - " + fmt(knots[j]) + ")_+^3");
        }
        return sb.toString();
    }

    // Membersihkan koefisien yang bernilai sangat kecil akibat sisa pembulatan komputasi.
    private double[] cleanedCoefficients() {
        double yScale = 0.0;
        for (double v : y) yScale = Math.max(yScale, Math.abs(v));
        double xScale = Math.max(Math.abs(x[0]), Math.abs(x[x.length - 1]));
        double[] b = beta.clone();
        for (int j = 0; j < b.length; j++) {
            double hMax = (j < 4) ? Math.pow(Math.max(xScale, 1.0), j)
                                  : Math.pow(x[x.length - 1] - knots[j - 4], 3);
            if (Math.abs(b[j]) * hMax < 1e-10 * Math.max(yScale, 1e-300)) {
                b[j] = 0.0;
            }
        }
        return b;
    }

    private static void appendTerm(StringBuilder sb, double coef, String term) {
        sb.append(coef < 0 ? " - " : " + ").append(fmt(Math.abs(coef))).append(term);
    }

    // Memformat cetakan angka untuk menjaga batas desimal.
    private static String fmt(double v) {
        double a = Math.abs(v);
        if (a != 0 && (a < 1e-3 || a >= 1e7)) {
            return String.format("%.4e", v);
        }
        return String.format("%.4f", v);
    }

    // Menyelesaikan SPL matriks dengan algoritma Gauss-Jordan partial pivoting.
    private double[] solveGaussJordan(Matrix augmented) {
        Matrix mat = augmented.copy();
        int n = mat.getRows();

        double scale = 0.0;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                scale = Math.max(scale, Math.abs(mat.get(r, c)));
            }
        }
        double tol = RANK_TOLERANCE * Math.max(scale, 1.0) * n;

        for (int col = 0; col < n; col++) {
            int pivot = mat.getPivotRow(col, col);
            if (Math.abs(mat.get(pivot, col)) <= tol) {
                throw new MatrixException("Matriks X^T X singular: kolom-kolom matriks desain tidak bebas linear, "
                        + "sehingga solusi beta tidak unik. Biasanya karena ada knot yang tidak diapit cukup titik data.");
            }
            if (pivot != col) {
                mat.swapRows(col, pivot);
            }
            double pv = mat.get(col, col);
            for (int c = col; c <= n; c++) {
                mat.set(col, c, mat.get(col, c) / pv);
            }
            for (int r = 0; r < n; r++) {
                if (r == col) continue;
                double factor = mat.get(r, col);
                if (factor == 0.0) continue;
                for (int c = col; c <= n; c++) {
                    mat.set(r, c, mat.get(r, c) - factor * mat.get(col, c));
                }
            }
        }

        double[] sol = new double[n];
        for (int r = 0; r < n; r++) {
            sol[r] = mat.get(r, n);
        }
        return sol;
    }
}