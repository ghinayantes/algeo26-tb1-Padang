package algeo.spl;

import algeo.matrix.Matrix;

public class GaussJordan implements SPLSolver {

    @Override
    public String solve(Matrix augmentedMatrix) {
        Matrix mat = augmentedMatrix.copy();
        StringBuilder steps = new StringBuilder();
        int rows = mat.getRows();
        int cols = mat.getCols();
        int numVars = cols - 1;

        steps.append("=== PROSES ELIMINASI GAUSS-JORDAN ===\n\n");

        int pivotRow = 0;

        // 1. Proses Mengubah Matriks menjadi Eselon Baris Tereduksi (RREF)
        for (int col = 0; col < numVars && pivotRow < rows; col++) {

            // 1.1 Partial Pivoting: Cari baris dengan nilai mutlak terbesar di kolom ini
            int maxRow = pivotRow;
            for (int r = pivotRow + 1; r < rows; r++) {
                if (Math.abs(mat.get(r, col)) > Math.abs(mat.get(maxRow, col))) {
                    maxRow = r;
                }
            }

            // Jika nilai terbesar di kolom ini adalah 0, lanjut ke kolom berikutnya (Variabel Bebas)
            if (Math.abs(mat.get(maxRow, col)) < 1e-9) {
                continue;
            }

            // Tukar baris jika pivot tidak berada di pivotRow
            if (maxRow != pivotRow) {
                mat.swapRows(pivotRow, maxRow);
                steps.append(String.format("Tukar Baris %d dengan Baris %d (Partial pivoting)\n", pivotRow + 1, maxRow + 1));
            }

            // 1.2 Normalisasi Baris Pivot agar elemen pivot menjadi 1.0
            double pivotVal = mat.get(pivotRow, col);
            steps.append(String.format("Baris %d dibagi %.4f\n", pivotRow + 1, pivotVal));
            for (int c = col; c < cols; c++) {
                double val = mat.get(pivotRow, c) / pivotVal;
                mat.set(pivotRow, c, Math.abs(val) < 1e-9 ? 0.0 : val); // Bersihkan dari -0.0
            }

            // 1.3 Eliminasi elemen di ATAS dan di BAWAH pivot menjadi 0
            for (int r = 0; r < rows; r++) {
                if (r != pivotRow) {
                    double factor = mat.get(r, col);
                    if (Math.abs(factor) > 1e-9) {
                        steps.append(String.format("Baris %d dikurangi (%.4f) * Baris %d\n", r + 1, factor, pivotRow + 1));
                        for (int c = col; c < cols; c++) {
                            double newVal = mat.get(r, c) - (factor * mat.get(pivotRow, c));
                            mat.set(r, c, Math.abs(newVal) < 1e-9 ? 0.0 : newVal);
                        }
                    }
                }
            }

            pivotRow++;
        }

        // 2. Cek Konsistensi SPL (Kasus Tidak Ada Solusi: Baris [0 0 0 | c] dimana c != 0)
        for (int i = 0; i < rows; i++) {
            boolean allZeros = true;
            for (int j = 0; j < numVars; j++) {
                if (Math.abs(mat.get(i, j)) > 1e-9) {
                    allZeros = false;
                    break;
                }
            }
            double rhs = mat.get(i, cols - 1);
            if (allZeros && Math.abs(rhs) > 1e-9) {
                steps.append("\nSistem Persamaan Linear tidak memiliki solusi (Kontradiksi / Inconsistent).\n");
                return steps.toString();
            }
        }

        // 3. Cek Solusi Banyak / Parametrik
        if (pivotRow < numVars) {
            steps.append("\nSistem Persamaan Linear memiliki SOLUSI BANYAK (Parametrik).\n");
            steps.append("Matriks Eselon Baris Tereduksi:\n");
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    steps.append(String.format("%.4f\t", mat.get(i, j)));
                }
                steps.append("\n");
            }
            return steps.toString();
        }

        // 4. Solusi Unik (Membaca hasil dari kolom terakhir)
        steps.append("\nHasil Solusi Unik:\n");
        for (int i = 0; i < numVars; i++) {
            double val = mat.get(i, cols - 1);
            steps.append(String.format("x%d = %.4f\n", i + 1, Math.abs(val) < 1e-9 ? 0.0 : val));
        }

        return steps.toString();
    }
}
