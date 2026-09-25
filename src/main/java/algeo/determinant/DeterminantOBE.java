package algeo.determinant;

import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;

public class DeterminantOBE {
    private static final double EPSILON = 1e-9;
    private DeterminantOBE() {}  

    public static double determinantOBE(Matrix A) {
        if (A.getRows() != A.getCols()) {
            throw new MatrixException("Determinan hanya untuk matriks persegi.");
        }
        int rows = A.getRows();
        Matrix mat = A.copy();
        double det = 1.0;

        for (int i = 0; i < rows; i++) {
            int maxRow = i;
            for (int k = i + 1; k < rows; k++) {
                if (Math.abs(mat.get(k, i)) > Math.abs(mat.get(maxRow, i))) {
                    maxRow = k;
                }
            }
            if (Math.abs(mat.get(maxRow, i)) <= EPSILON) {
                return 0.0;
            }
            if (maxRow != i) {
                mat.swapRows(i, maxRow);
                det = -det;
            }
            double pivot = mat.get(i, i);
            det *= pivot;
            for (int j = i + 1; j < rows; j++) {
                double factor = mat.get(j, i) / pivot;
                for (int c = i; c < rows; c++) {
                    mat.set(j, c, mat.get(j, c) - factor * mat.get(i, c));
                }
            }
        }
        return det;
    }
}