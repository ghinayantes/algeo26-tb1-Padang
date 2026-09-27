package algeo.spl;

import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;
import algeo.determinant.DeterminantOBE;
import algeo.inverse.InverseAugmented;

public class InverseSPL implements SPLSolver {
    @Override
    public String getName() {
        return "Metode Matriks Balikan";
    }

    @Override
    public boolean isApplicable(Matrix augmentedMatrix) {
        int rows = augmentedMatrix.getRows();
        int cols = augmentedMatrix.getCols();
        // Matriks balikan hanya bisa jika matriks koefisien persegi (cols == rows + 1)
        return cols == rows + 1;
    }

    @Override
    public double[] solveToVector(Matrix augmentedMatrix) {
        int rows = augmentedMatrix.getRows();
        int cols = augmentedMatrix.getCols();
        if (cols != rows + 1) return null;

        int n = rows;
        Matrix matA = new Matrix(n, n);
        Matrix matB = new Matrix(n, 1);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matA.set(i, j, augmentedMatrix.get(i, j));
            }
            matB.set(i, 0, augmentedMatrix.get(i, n));
        }

        Matrix invA;
        try {
            invA = InverseAugmented.inverse(matA);
        } catch (MatrixException e) {
            return null;
        }

        Matrix matX = invA.multiply(matB);
        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = matX.get(i, 0);
        }
        return x;
    }

    @Override
    public String solve(Matrix augmentedMatrix) {
        StringBuilder steps = new StringBuilder();
        int rows = augmentedMatrix.getRows();
        int cols = augmentedMatrix.getCols();

        steps.append("=========================================================\n");
        steps.append(" PENYELESAIAN SPL : METODE MATRIKS BALIKAN (INVERS)\n");
        steps.append("=========================================================\n\n");

        // 1. Cek syarat matriks persegi
        if (cols != rows + 1) {
            steps.append("-> Metode Matriks Balikan hanya dapat digunakan jika jumlah persamaan\n");
            steps.append("   sama dengan jumlah variabel (matriks koefisien harus persegi).\n");
            return steps.toString();
        }

        int n = rows;

        // Memisahkan matriks koefisien A (N x N) dan vektor b sebagai Matriks (N x 1)
        Matrix matA = new Matrix(n, n);
        Matrix matB = new Matrix(n, 1);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matA.set(i, j, augmentedMatrix.get(i, j));
            }
            matB.set(i, 0, augmentedMatrix.get(i, n));
        }

        steps.append("[Langkah 1] Memisahkan Matriks Koefisien (A) dan Vektor Konstanta (b)\n");
        steps.append("Matriks Koefisien A:\n");
        for (int r = 0; r < matA.getRows(); r++) {
            steps.append("   ");
            for (int c = 0; c < matA.getCols(); c++) {
                steps.append(String.format("%10.4f ", matA.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");

        steps.append("Vektor Konstanta b:\n");
        for (int r = 0; r < matB.getRows(); r++) {
            steps.append("   ");
            for (int c = 0; c < matB.getCols(); c++) {
                steps.append(String.format("%10.4f ", matB.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");

        // 2. Cek Determinan A
        steps.append("[Langkah 2] Memeriksa Determinan Matriks A\n");
        double detA = DeterminantOBE.determinantOBE(matA);
        steps.append(String.format("   det(A) = %.4f\n\n", detA));

        if (Math.abs(detA) < 1e-9) {
            steps.append("-> Karena det(A) = 0, matriks A adalah matriks singular (tidak memiliki invers).\n");
            steps.append("   Penyelesaian dengan Metode Matriks Balikan tidak dapat dilanjutkan.\n");
            return steps.toString();
        }

        // 3. Menghitung matriks invers A dengan augmentasi [A|I]
        steps.append("[Langkah 3] Mencari Invers Matriks A (A^-1)\n");
        Matrix invA = InverseAugmented.inverse(matA);
        
        // Memanggil proses step-by-step dari kelas InverseAugmented
        steps.append(InverseAugmented.inverseWithSteps(matA)).append("\n");

        // 4. Menghitung solusi
        steps.append("[Langkah 4] Menghitung Solusi (X = A^-1 * b)\n");
        Matrix matX = invA.multiply(matB);
        steps.append("Matriks X:\n");
        for (int r = 0; r < matX.getRows(); r++) {
            steps.append("   ");
            for (int c = 0; c < matX.getCols(); c++) {
                steps.append(String.format("%10.4f ", matX.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");

        // 5. Cetak Solusi Akhir
        steps.append("=========================================================\n");
        steps.append("Hasil Akhir Penyelesaian SPL:\n");
        for (int i = 0; i < n; i++) {
            steps.append(String.format("x_%d = %.4f\n", i + 1, matX.get(i, 0)));
        }
        steps.append("=========================================================\n");

        return steps.toString();
    }
}
