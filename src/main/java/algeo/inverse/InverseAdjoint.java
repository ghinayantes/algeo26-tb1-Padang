package algeo.inverse;

import algeo.determinant.DeterminantOBE;
import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;

public final class InverseAdjoint {

    private static final double EPSILON = 1e-9;

    private InverseAdjoint() {
    }

    //Menghitung kofaktor elemen (row, col)
    public static double cofactor(Matrix m, int row, int col) {
        if (m.getRows() != m.getCols()) {
            throw new MatrixException("Kofaktor hanya untuk matriks persegi.");
        }
        if (m.getRows() == 1) {
            return 1.0; // minor matriks 1x1 = det matriks kosong = 1
        }
        double minorDet = DeterminantOBE.determinantOBE(m.getSubmatrix(row, col));

        double sign = ((row + col) % 2 == 0) ? 1.0 : -1.0;
        return sign * minorDet;
    }

    //Menghitung matriks kofaktor lengkap (n x n)
    public static Matrix cofactorMatrix(Matrix m) {
        if (m.getRows() != m.getCols()) {
            throw new MatrixException("Matriks kofaktor hanya untuk matriks persegi.");
        }
        int n = m.getRows();
        Matrix cof = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                cof.set(i, j, cofactor(m, i, j));
            }
        }
        return cof;
    }

    //Menghitung adjoint matriks (transpose dari matriks kofaktor)
    public static Matrix adjoint(Matrix m) {
        if (m.getRows() != m.getCols()) {
            throw new MatrixException("Adjoint hanya untuk matriks persegi.");
        }
        return cofactorMatrix(m).transpose();
    }

    //Menghitung invers matriks 
    public static Matrix inverse(Matrix m) {
        if (m.getRows() != m.getCols()) {
            throw new MatrixException("Invers hanya untuk matriks persegi.");
        }
        double det = DeterminantOBE.determinantOBE(m);
        if (Math.abs(det) <= EPSILON) {
            throw new MatrixException(
                "Matriks singular, tidak memiliki invers.");
        }
        Matrix adj = adjoint(m);
        return adj.multiplyByScalar(1.0 / det);
    }

    // class untuk menampilkan step-by-step 
    public static String inverseWithSteps(Matrix m) {
        if (m.getRows() != m.getCols()) {
            throw new MatrixException("Invers hanya untuk matriks persegi.");
        }
 
        StringBuilder steps = new StringBuilder();
        steps.append("=========================================================\n");
        steps.append(" PROSES MENCARI INVERS MATRIKS : METODE ADJOIN\n");
        steps.append("=========================================================\n\n");
        
        steps.append("Matriks A:\n");
        for (int r = 0; r < m.getRows(); r++) {
            for (int c = 0; c < m.getCols(); c++) {
                steps.append(String.format("%10.4f ", m.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");
 
        double det = DeterminantOBE.determinantOBE(m);
        steps.append("[Langkah 1] Menghitung Determinan\n");
        steps.append(String.format("det(A) = %.4f\n\n", det));
 
        if (Math.abs(det) <= EPSILON) {
            steps.append("Karena det(A) = 0, matriks ini adalah matriks singular.\n");
            steps.append("Matriks singular tidak memiliki invers.\n");
            throw new MatrixException("Matriks singular, tidak memiliki invers.");
        }
 
        int n = m.getRows();
        Matrix cof = new Matrix(n, n);
        steps.append("[Langkah 2] Membentuk Matriks Kofaktor (C)\n");
        steps.append("(Tiap elemen C(i,j) = (-1)^(i+j) * det(Minor(i,j)))\n");
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double c = cofactor(m, i, j);
                cof.set(i, j, c);
            }
        }
        
        for (int r = 0; r < cof.getRows(); r++) {
            steps.append("   "); // Indentasi
            for (int c = 0; c < cof.getCols(); c++) {
                steps.append(String.format("%10.4f ", cof.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");
 
        Matrix adj = cof.transpose();
        steps.append("[Langkah 3] Membentuk Matriks Adjoin\n");
        steps.append("(Matriks Adjoin adalah hasil transpose dari Matriks Kofaktor)\n");
        for (int r = 0; r < adj.getRows(); r++) {
            steps.append("   ");
            for (int c = 0; c < adj.getCols(); c++) {
                steps.append(String.format("%10.4f ", adj.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");
 
        Matrix result = adj.multiplyByScalar(1.0 / det);
        steps.append("[Langkah 4] Menghitung Invers Matriks (A^-1)\n");
        steps.append("A^-1 = (1 / det(A)) * Adjoin\n");
        steps.append(String.format("A^-1 = (1 / %.4f) * Adjoin\n\n", det));
        
        steps.append("Hasil Akhir Invers Matriks A:\n");
        for (int r = 0; r < result.getRows(); r++) {
            for (int c = 0; c < result.getCols(); c++) {
                steps.append(String.format("%10.4f ", result.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");
        steps.append("=========================================================\n");
 
        return steps.toString();
    }
}