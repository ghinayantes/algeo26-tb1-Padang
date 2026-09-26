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

        steps.append("=== PROSES METODE MATRIKS BALIKAN (INVERS) ===\n\n");

        // 1. Cek syarat matriks persegi
        if (cols != rows + 1) {
            steps.append("Metode Matriks Balikan hanya dapat digunakan jika jumlah persamaan sama dengan jumlah variabel (matriks persegi).\n");
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

        steps.append("Matriks koefisien A:\n").append(matA).append("\n\n");
        steps.append("Vektor konstanta b:\n").append(matB).append("\n\n");

        // 2. Cek Determinan A
        double detA = DeterminantOBE.determinantOBE(matA);
        steps.append(String.format("Determinan matriks utama det(A) = %.4f\n\n", detA));

        if (Math.abs(detA) < 1e-9) {
            steps.append("Karena det(A) = 0, matriks tidak memiliki invers (singular). Metode Matriks Balikan tidak dapat digunakan.\n");
            return steps.toString();
        }

        // 3. Menghitung matriks invers A dengan augmentasi [A|I]
        Matrix invA = InverseAugmented.inverse(matA);
        steps.append(InverseAugmented.inverseWithSteps(matA)).append("\n");

        // 4. Menghitung solusi
        Matrix matX = invA.multiply(matB);
        steps.append("X = A^(-1) * b:\n").append(matX).append("\n");

        // 5. Cetak Solusi Akhir
        steps.append("\nHasil Solusi:\n");
        for (int i = 0; i < n; i++) {
            steps.append(String.format("x%d = %.4f\n", i + 1, matX.get(i, 0)));
        }

        return steps.toString();
    }
}
