package algeo.spl;

import algeo.matrix.Matrix;

public class GaussJourdanElimination implements SPLSolver {
    @Override
    public String solve(Matrix augmentedMatrix) {
        Matrix mat = augmentedMatrix.copy();
        StringBuilder steps = new StringBuilder();
        int rows = mat.getRows();
        int cols = mat.getCols();

        steps.append("=== PROSES ELIMINASI GAUSS-JORDAN ===\n\n");

        // 1. Eliminasi maju (Bikin nol di bawah pivot, sama seperti Gauss)
        for (int i = 0; i < rows; i++) {
            
            // 1.1 Partial pivoting
            int maxRow = i;
            for (int k = i + 1; k < rows; k++) {
                if (Math.abs(mat.get(k, i)) > Math.abs(mat.get(maxRow, i))) {
                    maxRow = k;
                }
            }

            if (maxRow != i && Math.abs(mat.get(maxRow, i)) > 1e-9) {
                mat.swapRows(i, maxRow);
                steps.append(String.format("Tukar Baris %d dengan Baris %d (Partial pivoting)\n", i + 1, maxRow + 1));
            }

            double pivot = mat.get(i, i);

            if (Math.abs(pivot) < 1e-9) {
                continue;
            }

            // 1.2 Normalisasi pivot jadi 1
            if (Math.abs(pivot - 1.0) > 1e-9) {
                steps.append(String.format("Baris %d dibagi %.2f\n", i + 1, pivot));
                for (int c = i; c < cols; c++) {
                    mat.set(i, c, mat.get(i, c) / pivot);
                }
            }

            // 1.3 Bikin nol angka di bawah pivot
            for (int j = i + 1; j < rows; j++) {
                double factor = mat.get(j, i);
                if (Math.abs(factor) > 1e-9) {
                    steps.append(String.format("Baris %d dikurangi (%.2f) * Baris %d\n", j + 1, factor, i + 1));
                    for (int c = i; c < cols; c++) {
                        double newVal = mat.get(j, c) - (factor * mat.get(i, c));
                        mat.set(j, c, newVal);
                    }
                }
            }
        }

        // 2. Eliminasi mundur (Bikin nol angka di atas pivot)
        steps.append("\n--- Eliminasi Mundur (Zeroing di atas pivot) ---\n");
        for (int i = rows - 1; i >= 0; i--) {
            double pivot = mat.get(i, i);
            if (Math.abs(pivot) < 1e-9) continue;

            // Perulangan j dari (i - 1) mundur ke atas sampai baris ke-0
            for (int j = i - 1; j >= 0; j--) {
                double factor = mat.get(j, i);
                if (Math.abs(factor) > 1e-9) {
                    steps.append(String.format("Baris %d dikurangi (%.2f) * Baris %d\n", j + 1, factor, i + 1));
                    for (int c = i; c < cols; c++) {
                        double newVal = mat.get(j, c) - (factor * mat.get(i, c));
                        mat.set(j, c, newVal);
                    }
                }
            }
        }

        // 3. Cek konsistensi SPL (Cek kasus tidak ada solusi)
        for (int i = 0; i < rows; i++) {
            boolean allZeros = true;
            for (int j = 0; j < cols - 1; j++) {
                if (Math.abs(mat.get(i, j)) > 1e-9) {
                    allZeros = false;
                    break;
                }
            }
            double rhs = mat.get(i, cols - 1);
            if (allZeros && Math.abs(rhs) > 1e-9) {
                steps.append("\nSistem Persamaan Linear tidak memiliki solusi (Kontradiksi).\n");
                return steps.toString();
            }
        }

        // 4. Baca solusi langsung dr kolom terakhir
        steps.append("\nHasil Solusi (Langsung dari Matriks Tereduksi):\n");
        int numVars = cols - 1;
        for (int i = 0; i < numVars; i++) {
            double val = (i < rows) ? mat.get(i, cols - 1) : 0;
            steps.append(String.format("x%d = %.4f\n", i + 1, val));
        }

        return steps.toString();
    }
}
