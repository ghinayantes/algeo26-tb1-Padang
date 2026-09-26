package algeo.spline;

import algeo.common.PointSetUtils;
import algeo.matrix.MatrixException;

public class NaturalCubicSpline {

    private static final int MAX_POINTS = 10;

    private final double[] x; 
    private final double[] y; 
    private final int n;      

    // koefisien tiap segmen kubik S_j(x) = a_j + b_j(x - x_j) + c_j(x - x_j)^2 + d_j(x - x_j)^3
    private final double[] a;
    private final double[] b;
    private final double[] c; // Catatan: S_j''(x_j) = 2 * c_j
    private final double[] d;

    public NaturalCubicSpline(double[] xPoints, double[] yPoints) {

        // panggil point set utility
        PointSetUtils.SortedPoints sorted = PointSetUtils.validateAndSort(xPoints, yPoints, MAX_POINTS);
        this.x = sorted.x;
        this.y = sorted.y;
        this.n = this.x.length - 1;

        this.a = new double[n + 1];
        this.b = new double[n];
        this.c = new double[n + 1];
        this.d = new double[n];

        buildSpline();
    }

    // algoritma tridiagonal (R. L. Burden and J. D. Faires, Numerical Analysis, 9th ed)
    private void buildSpline() {

        // hitung h_i dan a_i
        double[] h = new double[n];
        for (int i = 0; i < n; i++) {
            h[i] = x[i + 1] - x[i];
            a[i] = y[i];
        }
        a[n] = y[n];

        // hitung ruas kanan (alpha)
        double[] alpha = new double[n + 1];
        for (int i = 1; i < n; i++) {
            alpha[i] = (3.0 / h[i]) * (a[i + 1] - a[i]) - (3.0 / h[i - 1]) * (a[i] - a[i - 1]);
        }

        // forward sweep (eliminasi tridiagonal)
        double[] l = new double[n + 1];
        double[] mu = new double[n + 1];
        double[] z = new double[n + 1];

        l[0] = 1.0;
        mu[0] = 0.0;
        z[0] = 0.0;

        for (int i = 1; i < n; i++) {
            l[i] = 2.0 * (x[i + 1] - x[i - 1]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l[i];
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
        }

        l[n] = 1.0;
        z[n] = 0.0;
        c[n] = 0.0; // syarat batas natural

        // back substitution untuk cari c, lalu turunkan b dan d
        for (int j = n - 1; j >= 0; j--) {
            c[j] = z[j] - mu[j] * c[j + 1];
            b[j] = (a[j + 1] - a[j]) / h[j] - h[j] * (c[j + 1] + 2.0 * c[j]) / 3.0;
            d[j] = (c[j + 1] - c[j]) / (3.0 * h[j]);
        }
    }

    // evaluasi S(xt) pada segmen yang sesuai
    public double evaluate(double xt) {
        int j = findSegment(xt);
        double dx = xt - x[j];
        return a[j] + b[j] * dx + c[j] * dx * dx + d[j] * dx * dx * dx;
    }

    private int findSegment(double xt) {
        if (xt <= x[0]) return 0;
        if (xt >= x[n]) return n - 1;
        for (int j = 0; j < n; j++) {
            if (xt >= x[j] && xt <= x[j + 1]) return j;
        }
        return n - 1;
    }

    // turunan kedua pada tiap knot (S''(x_i) = 2 * c_i)
    public double[] getSecondDerivatives() {
        double[] secondDeriv = new double[n + 1];
        for (int i = 0; i <= n; i++) {
            secondDeriv[i] = 2.0 * c[i];
        }
        return secondDeriv;
    }

    public double[] getDomain() {
        return new double[] { x[0], x[n] };
    }

    public double[] getSegmentDomain(int j) {
        validateSegmentIndex(j);
        return new double[] { x[j], x[j + 1] };
    }

    public int getSegmentCount() {
        return n;
    }

    public double[] getXValues() {
        return x.clone();
    }

    public double[] getYValues() {
        return y.clone();
    }

    private void validateSegmentIndex(int j) {
        if (j < 0 || j >= n) {
            throw new MatrixException("Index segmen " + j + " di luar batas (0 - " + (n - 1) + ").");
        }
    }

    // format persamaan segmen 
    public String getSegmentEquationString(int j) {
        validateSegmentIndex(j);
        return String.format(
            "S_%d(x) = %.3f + %.3f(x - %.3f) + %.3f(x - %.3f)^2 + %.3f(x - %.3f)^3  untuk x in [%.3f, %.3f]",
            j, a[j], b[j], x[j], c[j], x[j], d[j], x[j], x[j], x[j + 1]
        );
    }

    // seluruh persamaan segmen gabungan
    public String toFullEquationString() {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < n; j++) {
            sb.append(getSegmentEquationString(j));
            if (j < n - 1) sb.append("\n");
        }
        return sb.toString();
    }
}