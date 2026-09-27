package algeo.spl;

import algeo.matrix.Matrix;
import algeo.determinant.DeterminantOBE;

public class Cramer implements SPLSolver {
    @Override
    public String getName() {
        return "Kaidah Cramer";
    }

    @Override
    public boolean isApplicable(Matrix augmentedMatrix) {
        int rows = augmentedMatrix.getRows();
        int cols = augmentedMatrix.getCols();
        // Cramer hanya bisa jika matriks persegi (cols == rows + 1)
        return cols == rows + 1;
    }

    @Override
    public double[] solveToVector(Matrix augmentedMatrix) {
        int rows = augmentedMatrix.getRows();
        int cols = augmentedMatrix.getCols();
        if (cols != rows + 1) return null;

        int n = rows;
        Matrix matA = new Matrix(n, n);
        double[] b = new double[n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matA.set(i, j, augmentedMatrix.get(i, j));
            }
            b[i] = augmentedMatrix.get(i, n);
        }

        double detA = DeterminantOBE.determinantOBE(matA);
        if (Math.abs(detA) < 1e-9) return null;

        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            Matrix matAi = matA.copy();
            matAi.setColumn(i, b);
            double detAi = DeterminantOBE.determinantOBE(matAi);
            x[i] = detAi / detA;
        }
        return x;
    }

    @Override
    public String solve(Matrix augmentedMatrix) {
        StringBuilder steps = new StringBuilder();
        int rows = augmentedMatrix.getRows();
        int cols = augmentedMatrix.getCols();

        steps.append("=========================================================\n");
        steps.append(" PENYELESAIAN SPL : KAIDAH CRAMER\n");
        steps.append("=========================================================\n\n");

        // 1. Cek syarat matriks persegi (N x N)
        if (cols != rows + 1) {
            steps.append("-> Kaidah Cramer hanya dapat digunakan jika jumlah persamaan\n");
            steps.append("   sama dengan jumlah variabel (matriks koefisien harus persegi).\n");
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
        steps.append("[Langkah 1] Membentuk Matriks Koefisien (A) dan Menghitung det(A)\n");
        steps.append("Matriks Koefisien A:\n");
        for (int r = 0; r < matA.getRows(); r++) {
            steps.append("   ");
            for (int c = 0; c < matA.getCols(); c++) {
                steps.append(String.format("%10.4f ", matA.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");
        
        double detA = DeterminantOBE.determinantOBE(matA);
        steps.append(String.format("det(A) = %.4f\n\n", detA));

        // Pengecekan det(A) = 0
        if (Math.abs(detA) < 1e-9) {
            steps.append("Karena det(A) = 0, SPL ini tidak memiliki solusi tunggal.\n");
            steps.append("Kaidah Cramer tidak dapat dilanjutkan.\n");
            return steps.toString();
        }

        // 3. Hitung det(Ai) dan variabel x_i untuk setiap kolom
        steps.append("[Langkah 2] Membentuk Matriks A_i dan Menghitung Nilai Variabel\n\n");
        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            Matrix matAi = matA.copy();
            matAi.setColumn(i, b); // Mengganti kolom ke-i dengan vektor b

            double detAi = DeterminantOBE.determinantOBE(matAi);
            x[i] = detAi / detA;

            steps.append(String.format("-> Matriks A_%d (Kolom ke-%d diganti dengan vektor konstanta b):\n", i + 1, i + 1));
            for (int r = 0; r < matAi.getRows(); r++) {
                steps.append("   ");
                for (int c = 0; c < matAi.getCols(); c++) {
                    steps.append(String.format("%10.4f ", matAi.get(r, c)));
                }
                steps.append("\n");
            }
            
            steps.append(String.format("   det(A_%d) = %.4f\n", i + 1, detAi));
            steps.append(String.format("   x_%d = det(A_%d) / det(A) = %.4f / %.4f = %.4f\n\n",
                    i + 1, i + 1, detAi, detA, x[i]));
        }

        // 4. Cetak solusi akhir
        steps.append("=========================================================\n");
        steps.append("Hasil Akhir Penyelesaian SPL:\n");
        for (int i = 0; i < n; i++) {
            steps.append(String.format("x_%d = %.4f\n", i + 1, x[i]));
        }

        return steps.toString();
    }
}