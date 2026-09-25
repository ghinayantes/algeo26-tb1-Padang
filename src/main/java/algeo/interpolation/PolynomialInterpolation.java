package algeo.interpolation;

import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;

public class PolynomialInterpolation {

    private static final double EPSILON = 1e-9;
    private static final int MAX_POINTS = 10;

    private final double[] x;
    private final double[] y;
    private final double[] coefficients; 
    private final int degree;

    public PolynomialInterpolation(double[] xPoints, double[] yPoints) {
        // validasi input
        if ( xPoints == null || yPoints == null || xPoints.length != yPoints.length) {
            throw new MatrixException("Jumlah titik n dan y harus sama dan tidak boleh null");
        }

        if (xPoints.length < 2) {
            throw new MatrixException("Minimal 2 titik data untuk interpolasi polinomial");
        }

        if (xPoints.length > MAX_POINTS) {
            throw new MatrixException("Jumlah titik data melebihi batas maksimal (" + MAX_POINTS + " titik).");
        }

        int n = xPoints.length;

        // urutkan pasangan (x, y) menaik
        double[] sortedX = xPoints.clone();
        double[] sortedY = yPoints.clone();
        for (int i = 0; i < n - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < n; j++) {
                if (sortedX[j] < sortedX[minIdx]) minIdx = j;
            }
            double tempX = sortedX[i]; sortedX[i] = sortedX[minIdx]; sortedX[minIdx] = tempX;
            double tempY = sortedY[i]; sortedY[i] = sortedY[minIdx]; sortedY[minIdx] = tempY;
        }

        // cek x duplikat
        for (int i = 0; i < n - 1; i++) {
            if (Math.abs(sortedX[i + 1] - sortedX[i]) < EPSILON) {
                throw new MatrixException("Terdapat nilai x yang duplikat pada titik data: x = " + sortedX[i]);
            }
        }
 
        this.x = sortedX;
        this.y = sortedY;
        this.degree = n - 1;
        
        // buat spl
        Matrix augmented = new Matrix(n, n + 1);
        for (int i = 0; i < n; i++) {
            double power = 1.0;
            for (int j = 0; j < n; j++) {
                augmented.set(i, j, power);
                power *= sortedX[i];
            }
            augmented.set(i, n, sortedY[i]);
        }

        // selesaikan spl dgn gauss dan partial pivoting
        this.coefficients = solveLinearSystem(augmented, n);
    }

    // subtitusi mundur
    private double[] solveLinearSystem(Matrix augmented, int n) {
        Matrix mat = augmented.copy();
 
        // Eliminasi maju dengan partial pivoting
        for (int i = 0; i < n; i++) {
            int pivotRow = mat.getPivotRow(i, i);
            if (Math.abs(mat.get(pivotRow, i)) <= EPSILON) {
                throw new MatrixException(
                    "Sistem persamaan singular - kemungkinan ada titik data yang tidak valid.");
            }
            if (pivotRow != i) {
                mat.swapRows(i, pivotRow);
            }
            for (int r = i + 1; r < n; r++) {
                double factor = mat.get(r, i) / mat.get(i, i);
                if (Math.abs(factor) > EPSILON) {
                    for (int c = i; c <= n; c++) {
                        mat.set(r, c, mat.get(r, c) - factor * mat.get(i, c));
                    }
                }
            }
        }

        // subtitusi mundur 
        double[] result = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            double sum = mat.get(i, n);
            for (int j = i + 1; j < n; j++) {
                sum -= mat.get(i, j) * result[j];
            }
            result[i] = sum / mat.get(i, i);
        }
        return result;
    }

    // evaluasi 
    public double evaluate(double xt) {
        double result = coefficients[degree];
        for (int i = degree - 1; i >= 0; i--) {
            result = result * xt + coefficients[i];
        }
        return result;
    }

    public double[] getXValues() {
    return x.clone();
    }

    public double[] getYValues() {
        return y.clone();
    }

    public double[] getDomain() {
        return new double[] { x[0], x[x.length - 1] };
    }

    public double[] getCoefficients() {
        return coefficients.clone();
    }
 
    public int getDegree() {
        return degree;
    }

    // format output
    public String toEquationString() {
        StringBuilder sb = new StringBuilder("P(x) = ");
        boolean first = true;
        for (int i = degree; i >= 0; i--) {
            double coef = coefficients[i];
            if (Math.abs(coef) < EPSILON && degree > 0) continue; // lewati suku dengan koefisien ~0
 
            if (!first) {
                sb.append(coef >= 0 ? " + " : " - ");
            } else if (coef < 0) {
                sb.append("-");
            }
            double absCoef = Math.abs(coef);
            String formatted = String.format("%.3f", absCoef);
 
            if (i == 0) {
                sb.append(formatted);
            } else if (i == 1) {
                sb.append(formatted).append("x");
            } else {
                sb.append(formatted).append("x^").append(i);
            }
            first = false;
        }
        if (first) sb.append("0"); // kasus semua koefisien 0
        return sb.toString();
    }
}
