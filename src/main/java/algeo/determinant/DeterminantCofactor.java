package algeo.determinant;
import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;

public class DeterminantCofactor {

    //Batas toleransi rounding
    private static final double EPSILON = 1e-9;

    private DeterminantCofactor() {
    }

    /**
     * Menghitung determinan matriks persegi menggunakan ekspansi kofaktor
     * (Laplace expansion) sepanjang baris pertama.
     *
     * @param m matriks persegi (rows == cols)
     * @return nilai determinan
     * @throws MatrixException jika matriks tidak persegi
     */
    public static double cofactorExpansion(Matrix m) {
        if (m.getRows() != m.getCols()) {
            throw new MatrixException("Determinan hanya bisa dihitung untuk matriks persegi (rows == cols).");
        }

        int n = m.getRows();

        // Basis
        if (n == 1) {
            return m.get(0, 0);
        }

        // Basis
        if (n == 2) {
            return m.get(0, 0) * m.get(1, 1) - m.get(0, 1) * m.get(1, 0);
        }

        // Rekursif
        double det = 0;
        for (int j = 0; j < n; j++) {
            double sign = (j % 2 == 0) ? 1.0 : -1.0; // (-1)^(0+j)
            double minorDet = cofactorExpansion(m.getSubmatrix(0, j));
            det += sign * m.get(0, j) * minorDet;
        }

        // rounding
        double rounded = Math.round(det);
        if (Math.abs(det - rounded) < EPSILON) {
            return rounded;
        }
        return det;
    }
}