package algeo.spl;

import algeo.matrix.Matrix;
import algeo.determinant.DeterminantOBE;
import algeo.determinant.DeterminantCofactor;

public class InverseSPL implements SPLSolver {
    @Override
    public String getName() {
        return "Metode Matriks Balikan";
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

        double detA = algeo.determinant.DeterminantOBE.determinantOBE(matA);
        if (Math.abs(detA) < 1e-9) return null;

        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            Matrix matAi = matA.copy();
            matAi.setColumn(i, b);
            double detAi = algeo.determinant.DeterminantOBE.determinantOBE(matAi);
            x[i] = detAi / detA;
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

        // 2. Cek Determinan A
        double detA = DeterminantOBE.determinantOBE(matA);
        steps.append(String.format("Determinan matriks utama det(A) = %.4f\n", detA));

        if (Math.abs(detA) < 1e-9) {
            steps.append("Karena det(A) = 0, matriks tidak memiliki invers (singular). Metode Matriks Balikan tidak dapat digunakan.\n");
            return steps.toString();
        }

        // 3. Menghitung matriks invers A
        // A^(-1) = (1 / detA) * Adj(A)
        Matrix cofactorMatrix = new Matrix(n, n);
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                Matrix sub = matA.getSubmatrix(i, j); // Sesuai API Sahla: getSubmatrix
                double sign = ((i + j) % 2 == 0) ? 1.0 : -1.0;
                double subDet = DeterminantCofactor.cofactorExpansion(sub);
                cofactorMatrix.set(i, j, sign * subDet);
            }
        }

        // Adjoin = Transpose Matriks Kofaktor
        Matrix adjA = cofactorMatrix.transpose();

        // Invers = (1 / detA) * Adjoin
        Matrix invA = adjA.multiplyByScalar(1.0 / detA);

        steps.append("\nMatriks Invers A^(-1) berhasil dihitung.\n");

        // 4. Menghitung solusi
        Matrix matX = invA.multiply(matB);

        // 5. Cetak Solusi Akhir
        steps.append("\nHasil Solusi (X = A^(-1) * b):\n");
        for (int i = 0; i < n; i++) {
            steps.append(String.format("x%d = %.4f\n", i + 1, matX.get(i, 0)));
        }

        return steps.toString();
    }
}
