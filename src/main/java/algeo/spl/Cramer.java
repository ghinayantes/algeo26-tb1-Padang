package algeo.spl;

import algeo.matrix.Matrix;
import algeo.determinant.DeterminantOBE;

public class Cramer implements SPLSolver {
    @Override
    public String solve(Matrix augmentedMatrix) {
        StringBuilder steps = new StringBuilder();
        int rows = augmentedMatrix.getRows();
        int cols = augmentedMatrix.getCols();

        steps.append("=== PROSES KAIDAH CRAMER ===\n\n");

        // 1. Cek syarat matriks persegi (N x N)
        if (cols != rows + 1) {
            steps.append("Kaidah Cramer hanya dapat digunakan jika jumlah persamaan sama dengan jumlah variabel (matriks persegi).\n");
            return steps.toString();
        }

        int n = rows;

        // Memisahkan matriks koefisien A (N x N) dan vektor konstanta b (array 1D)
        Matrix matA = new Matrix(n, n);
        double[] b = new double[n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matA.set(i, j, augmentedMatrix.get(i, j));
            }
            b[i] = augmentedMatrix.get(i, n);
        }

        // 2. Hitung determinan matriks utama A 
        double detA = DeterminantOBE.determinantOBE(matA);
        steps.append(String.format("Determinan matriks utama det(A) = %.4f\n\n", detA));

        // Pengecekan det(A) = 0
        if (Math.abs(detA) < 1e-9) {
            steps.append("Karena det(A) = 0, Kaidah Cramer tidak dapat digunakan (SPL tidak memiliki solusi tunggal).\n");
            return steps.toString();
        }

        // 3. Hitung det(Ai) dan variabel x_i untuk setiap kolom
        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            Matrix matAi = matA.copy();
            matAi.setColumn(i, b); // Mengganti kolom ke-i dengan vektor b

            double detAi = DeterminantOBE.determinantOBE(matAi);
            x[i] = detAi / detA;

            steps.append(String.format("det(A%d) = %.4f  -->  x%d = det(A%d) / det(A) = %.4f / %.4f = %.4f\n",
                    i + 1, detAi, i + 1, i + 1, detAi, detA, x[i]));
        }

        // 4. Cetak solusi akhir
        steps.append("\nHasil Solusi:\n");
        for (int i = 0; i < n; i++) {
            steps.append(String.format("x%d = %.4f\n", i + 1, x[i]));
        }

        return steps.toString();
    }
}
