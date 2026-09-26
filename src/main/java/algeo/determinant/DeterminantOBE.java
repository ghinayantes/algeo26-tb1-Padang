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

    // class untuk menampilkan step-by-step 
    public static String determinantOBEWithSteps(Matrix A) {
        if (A.getRows() != A.getCols()) {
            throw new MatrixException("Determinan hanya untuk matriks persegi.");
        }
        int rows = A.getRows();
        Matrix mat = A.copy();
        double det = 1.0;
        StringBuilder steps = new StringBuilder();
 
        steps.append("=== PROSES DETERMINAN (Metode Reduksi Baris / OBE) ===\n\n");
        steps.append("Matriks awal:\n").append(mat).append("\n\n");
 
        for (int i = 0; i < rows; i++) {
            int maxRow = i;
            for (int k = i + 1; k < rows; k++) {
                if (Math.abs(mat.get(k, i)) > Math.abs(mat.get(maxRow, i))) {
                    maxRow = k;
                }
            }
 
            if (Math.abs(mat.get(maxRow, i)) <= EPSILON) {
                steps.append("Kolom ke-").append(i + 1)
                     .append(" tidak memiliki elemen pivot tak-nol -> determinan = 0\n");
                steps.append("\nHasil Determinan = 0.000000\n");
                return steps.toString();
            }
 
            if (maxRow != i) {
                mat.swapRows(i, maxRow);
                det = -det;
                steps.append(String.format("Tukar Baris %d dengan Baris %d (Partial Pivoting, tanda determinan berbalik)\n",
                        i + 1, maxRow + 1));
                steps.append(mat).append("\n\n");
            }
 
            double pivot = mat.get(i, i);
            det *= pivot;
            steps.append(String.format("Pivot baris %d = %.4f  |  det sementara = %.4f\n", i + 1, pivot, det));
 
            for (int j = i + 1; j < rows; j++) {
                double factor = mat.get(j, i) / pivot;
                if (Math.abs(factor) > EPSILON) {
                    steps.append(String.format("Baris %d dikurangi (%.4f) * Baris %d\n", j + 1, factor, i + 1));
                    for (int c = i; c < rows; c++) {
                        mat.set(j, c, mat.get(j, c) - factor * mat.get(i, c));
                    }
                }
            }
            steps.append(mat).append("\n\n");
        }
 
        steps.append(String.format("Determinan = hasil kali seluruh pivot diagonal = %.6f\n", det));
        return steps.toString();
    }
}

