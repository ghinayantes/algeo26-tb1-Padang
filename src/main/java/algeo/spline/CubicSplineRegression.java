package algeo.spline;

import algeo.common.PointSetUtils;
import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;

public class CubicSplineRegression {

    private static final int MAX_POINTS = 1000;

    private final double[] x;
    private final double[] y;
    private final double[] knots;
    private final double[] beta;

    /**
     * Konstruktor Regresi Spline Kubik.
     * Data x dan y akan divalidasi serta diurutkan secara otomatis via PointSetUtils.
     * 
     * @param xPoints Array data x
     * @param yPoints Array data y
     * @param knots   Array titik knot internal
     */
    public CubicSplineRegression(double[] xPoints, double[] yPoints, double[] knots) {
        // 1. Validasi & sorting titik (x, y) menggunakan utilitas bersama
        PointSetUtils.SortedPoints sorted = PointSetUtils.validateAndSort(xPoints, yPoints, MAX_POINTS);
        this.x = sorted.x;
        this.y = sorted.y;

        if (knots == null) {
            throw new MatrixException("Array knot tidak boleh null.");
        }

        this.knots = knots.clone();

        int n = this.x.length;
        int k = this.knots.length;
        int m = 4 + k; // Jumlah basis = 4 (polinomial standar) + K (truncated power)

        if (n < m) {
            throw new MatrixException("Jumlah titik data (" + n + ") harus lebih besar atau sama dengan jumlah fungsi basis (" + m + ").");
        }

        this.beta = new double[m];
        buildRegression();
    }

    private void buildRegression() {
        int n = x.length;
        int k = knots.length;
        int m = 4 + k;

        // 1. Konstruksi Matriks Desain X (N x M)
        Matrix X = new Matrix(n, m);
        for (int i = 0; i < n; i++) {
            double xi = x[i];

            // Polinomial Standar Global= h0=1, h1=x, h2=x^2, h3=x^3
            X.set(i, 0, 1.0);
            X.set(i, 1, xi);
            X.set(i, 2, xi * xi);
            X.set(i, 3, xi * xi * xi);

            // Truncated Power Basis= (x - knot_j)_+^3
            for (int j = 0; j < k; j++) {
                double diff = xi - knots[j];
                double truncatedVal = (diff > 0) ? Math.pow(diff, 3) : 0.0;
                X.set(i, 4 + j, truncatedVal);
            }
        }

        // 2. Konstruksi Matriks Respon Y (N x 1)
        Matrix Y = new Matrix(n, 1);
        for (int i = 0; i < n; i++) {
            Y.set(i, 0, y[i]);
        }

        // 3. Formulasi Persamaan Normal= (X^T * X) * beta = X^T * Y
        Matrix XT = X.transpose();
        Matrix XTX = XT.multiply(X);
        Matrix XTY = XT.multiply(Y);

        // 4. Buat Matriks Augmented [ (X^T * X) | (X^T * Y) ] berukuran M x (M + 1)
        Matrix augmented = new Matrix(m, m + 1);
        for (int r = 0; r < m; r++) {
            for (int c = 0; c < m; c++) {
                augmented.set(r, c, XTX.get(r, c));
            }
            augmented.set(r, m, XTY.get(r, 0));
        }

        // 5. Selesaikan SPL menggunakan Gauss-Jordan internal
        Matrix rref = solveGaussJordan(augmented);
        for (int i = 0; i < m; i++) {
            this.beta[i] = rref.get(i, m);
        }
    }

    /**
     * Evaluasi/Prediksi nilai y_hat pada titik xt
     */
    public double evaluate(double xt) {
        double result = beta[0] + beta[1] * xt + beta[2] * Math.pow(xt, 2) + beta[3] * Math.pow(xt, 3);
        for (int j = 0; j < knots.length; j++) {
            double diff = xt - knots[j];
            if (diff > 0) {
                result += beta[4 + j] * Math.pow(diff, 3);
            }
        }
        return result;
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

    /**
     * Format output string persamaan regresi spline kubik
     */
    public String toFullEquationString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("f(x) = %.4f + %.4fx + %.4fx^2 + %.4fx^3", beta[0], beta[1], beta[2], beta[3]));
        for (int j = 0; j < knots.length; j++) {
            double b = beta[4 + j];
            if (Math.abs(b) > 1e-9) {
                sb.append(String.format(" + %.4f(x - %.4f)_+^3", b, knots[j]));
            }
        }
        return sb.toString();
    }

    // Solver Gauss-Jordan internal untuk menyelesaikan augmented matrix
    private Matrix solveGaussJordan(Matrix augmented) {
        Matrix mat = augmented.copy();
        int rows = mat.getRows();
        int cols = mat.getCols();
        int numVars = cols - 1;

        int pivotRow = 0;
        for (int col = 0; col < numVars && pivotRow < rows; col++) {
            int maxRow = pivotRow;
            for (int r = pivotRow + 1; r < rows; r++) {
                if (Math.abs(mat.get(r, col)) > Math.abs(mat.get(maxRow, col))) {
                    maxRow = r;
                }
            }

            if (Math.abs(mat.get(maxRow, col)) < 1e-9) continue;

            if (maxRow != pivotRow) {
                mat.swapRows(pivotRow, maxRow);
            }

            double pivotVal = mat.get(pivotRow, col);
            for (int c = col; c < cols; c++) {
                double val = mat.get(pivotRow, c) / pivotVal;
                mat.set(pivotRow, c, Math.abs(val) < 1e-9 ? 0.0 : val);
            }

            for (int r = 0; r < rows; r++) {
                if (r != pivotRow) {
                    double factor = mat.get(r, col);
                    if (Math.abs(factor) > 1e-9) {
                        for (int c = col; c < cols; c++) {
                            double newVal = mat.get(r, c) - (factor * mat.get(pivotRow, c));
                            mat.set(r, c, Math.abs(newVal) < 1e-9 ? 0.0 : newVal);
                        }
                    }
                }
            }
            pivotRow++;
        }
        return mat;
    }
}
