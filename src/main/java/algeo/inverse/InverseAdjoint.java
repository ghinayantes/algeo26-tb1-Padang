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
}