package algeo.spl;

import algeo.matrix.Matrix;

public class GaussJordan implements SPLSolver {
    private static final double EPSILON = 1e-9;

    @Override
    public String getName() {
        return "Eliminasi Gauss-Jordan";
    }

    @Override
    public boolean isApplicable(Matrix augmentedMatrix) {
        // Gauss-Jordan bisa dipakai untuk SPL m x n (tidak harus persegi)
        return augmentedMatrix.getCols() >= 2;
    }

    @Override
    public double[] solveToVector(Matrix augmentedMatrix) {
        Matrix mat = augmentedMatrix.copy();
        int rows = mat.getRows();
        int cols = mat.getCols();
        int numVars = cols - 1;
        int[] pivotCol = new int[rows];

        int rank = reduce(mat, pivotCol, new StringBuilder());
        if (isInconsistent(mat) || rank < numVars) return null;

        double[] x = new double[numVars];
        for (int i = 0; i < rank; i++) {
            x[pivotCol[i]] = mat.get(i, cols - 1);
        }
        return x;
    }

    @Override
    public String solve(Matrix augmentedMatrix) {
        Matrix mat = augmentedMatrix.copy();
        StringBuilder steps = new StringBuilder();
        int rows = mat.getRows();
        int cols = mat.getCols();
        int numVars = cols - 1;
        int[] pivotCol = new int[rows];

        steps.append("=========================================================\n");
        steps.append(" PENYELESAIAN SPL : METODE ELIMINASI GAUSS-JORDAN\n");
        steps.append("=========================================================\n\n");

        steps.append("[Langkah 1] Membentuk Matriks Augmented [A|b] awal:\n");
        appendAugmentedMatrix(steps, mat);
        steps.append("\n");

        // 1 & 2. Eliminasi maju lalu eliminasi mundur sampai bentuk eselon baris tereduksi
        steps.append("[Langkah 2] Eliminasi Maju (Membentuk Eselon Baris)\n");
        int rank = reduce(mat, pivotCol, steps);

        // 3. Cek konsistensi SPL (Cek kasus tidak ada solusi)
        steps.append("[Langkah 4] Evaluasi Konsistensi SPL\n");
        if (isInconsistent(mat)) {
            steps.append("-> Ditemukan baris kontradiksi (0 = k, dengan k != 0).\n");
            steps.append("   Sistem Persamaan Linear TIDAK MEMILIKI SOLUSI.\n");
            steps.append("=========================================================\n");
            return steps.toString();
        }
        steps.append("-> SPL konsisten (memiliki solusi).\n\n");

        // 4. Tandai variabel bebas, tiap variabel bebas jadi parameter t1, t2, ...
        boolean[] isPivot = new boolean[numVars];
        for (int i = 0; i < rank; i++) {
            isPivot[pivotCol[i]] = true;
        }
        int[] paramIdx = new int[numVars];
        int numFree = 0;
        for (int j = 0; j < numVars; j++) {
            if (!isPivot[j]) paramIdx[j] = numFree++;
        }

        // sol[j][0] = konstanta, sol[j][k + 1] = koefisien parameter t(k+1)
        double[][] sol = new double[numVars][numFree + 1];
        for (int j = 0; j < numVars; j++) {
            if (!isPivot[j]) sol[j][paramIdx[j] + 1] = 1.0;
        }

        // 5. Baca solusi langsung dr matriks tereduksi
        // Baris pivot: x_p = rhs - (koef variabel bebas * parameternya)
        for (int i = 0; i < rank; i++) {
            int p = pivotCol[i];
            sol[p][0] = mat.get(i, cols - 1);
            for (int c = 0; c < numVars; c++) {
                if (!isPivot[c]) {
                    sol[p][paramIdx[c] + 1] = -mat.get(i, c);
                }
            }
        }

        steps.append("=========================================================\n");
        if (numFree == 0) {
            steps.append("Hasil Akhir (Solusi Tunggal dari Matriks Tereduksi):\n");
        } else {
            steps.append("Hasil Akhir (Solusi Parametrik / Tak Hingga Banyaknya):\n");
        }
        for (int j = 0; j < numVars; j++) {
            steps.append(String.format("x_%d = %s\n", j + 1, formatExpression(sol[j])));
        }
        steps.append("=========================================================\n");

        return steps.toString();
    }

    // Reduksi ke bentuk eselon baris tereduksi, return rank. pivotCol[i] = kolom pivot baris ke-i
    private int reduce(Matrix mat, int[] pivotCol, StringBuilder steps) {
        int rows = mat.getRows();
        int cols = mat.getCols();
        int numVars = cols - 1;
        int r = 0;

        // 1. Eliminasi maju (Bikin nol di bawah pivot, sama seperti Gauss)
        for (int c = 0; c < numVars && r < rows; c++) {

            // 1.1 Partial pivoting
            int maxRow = r;
            for (int k = r + 1; k < rows; k++) {
                if (Math.abs(mat.get(k, c)) > Math.abs(mat.get(maxRow, c))) {
                    maxRow = k;
                }
            }

            // Kolom ini tidak punya pivot, pindah ke kolom berikutnya (baris tetap)
            if (Math.abs(mat.get(maxRow, c)) < EPSILON) {
                steps.append(String.format("-> Kolom ke-%d tidak memiliki pivot tak-nol. Lanjut ke kolom berikutnya.\n", c + 1));
                continue;
            }

            boolean changed = false;

            if (maxRow != r) {
                mat.swapRows(r, maxRow);
                steps.append(String.format("-> [Tukar Baris] Baris %d ditukar dengan Baris %d (Partial pivoting)\n", r + 1, maxRow + 1));
                changed = true;
            }

            // 1.2 Normalisasi pivot jadi 1
            double pivot = mat.get(r, c);
            if (Math.abs(pivot - 1.0) > EPSILON) {
                steps.append(String.format("-> [Normalisasi] Baris %d dibagi %.4f (pivot menjadi 1)\n", r + 1, pivot));
                for (int j = c; j < cols; j++) {
                    mat.set(r, j, mat.get(r, j) / pivot);
                }
                changed = true;
            }

            // 1.3 Bikin nol angka di bawah pivot
            for (int j = r + 1; j < rows; j++) {
                double factor = mat.get(j, c);
                if (Math.abs(factor) > EPSILON) {
                    steps.append(String.format("   Operasi OBE: Baris %d = Baris %d - (%.4f) * Baris %d\n", j + 1, j + 1, factor, r + 1));
                    for (int k = c; k < cols; k++) {
                        double newVal = mat.get(j, k) - (factor * mat.get(r, k));
                        mat.set(j, k, newVal);
                    }
                    changed = true;
                }
            }

            if (changed) {
                steps.append("   Matriks [A|b] sementara:\n");
                appendAugmentedMatrix(steps, mat);
                steps.append("\n");
            }

            pivotCol[r] = c;
            r++;
        }
        int rank = r;

        // 2. Eliminasi mundur (Bikin nol angka di atas pivot)
        steps.append("[Langkah 3] Eliminasi Mundur (Membentuk Eselon Baris Tereduksi)\n");
        boolean backChangedGlobal = false;
        
        for (int i = rank - 1; i >= 0; i--) {
            int c = pivotCol[i];
            boolean changed = false;

            // Perulangan j dari (i - 1) mundur ke atas sampai baris ke-0
            for (int j = i - 1; j >= 0; j--) {
                double factor = mat.get(j, c);
                if (Math.abs(factor) > EPSILON) {
                    steps.append(String.format("   Operasi OBE: Baris %d = Baris %d - (%.4f) * Baris %d\n", j + 1, j + 1, factor, i + 1));
                    for (int k = c; k < cols; k++) {
                        double newVal = mat.get(j, k) - (factor * mat.get(i, k));
                        mat.set(j, k, newVal);
                    }
                    changed = true;
                    backChangedGlobal = true;
                }
            }
            if (changed) {
                steps.append("   Matriks [A|b] sementara:\n");
                appendAugmentedMatrix(steps, mat);
                steps.append("\n");
            }
        }
        
        if (!backChangedGlobal) {
            steps.append("-> Tidak ada operasi eliminasi mundur (elemen di atas pivot sudah nol).\n\n");
        }

        return rank;
    }

    private boolean isInconsistent(Matrix mat) {
        int rows = mat.getRows();
        int cols = mat.getCols();
        for (int i = 0; i < rows; i++) {
            boolean allZeros = true;
            for (int j = 0; j < cols - 1; j++) {
                if (Math.abs(mat.get(i, j)) > EPSILON) {
                    allZeros = false;
                    break;
                }
            }
            if (allZeros && Math.abs(mat.get(i, cols - 1)) > EPSILON) {
                return true;
            }
        }
        return false;
    }

    // Ubah {konstanta, koef t1, koef t2, ...} jadi string, contoh: "2.0000 - 1.5000t1 + t2"
    private String formatExpression(double[] v) {
        StringBuilder sb = new StringBuilder();
        double constant = Math.abs(v[0]) < EPSILON ? 0.0 : v[0];
        boolean hasParam = false;
        for (int k = 1; k < v.length; k++) {
            if (Math.abs(v[k]) > EPSILON) hasParam = true;
        }

        if (!hasParam || constant != 0.0) {
            sb.append(String.format("%.4f", constant));
        }
        for (int k = 1; k < v.length; k++) {
            double coef = v[k];
            if (Math.abs(coef) < EPSILON) continue;

            if (sb.length() == 0) {
                if (coef < 0) sb.append("-");
            } else {
                sb.append(coef < 0 ? " - " : " + ");
            }
            if (Math.abs(Math.abs(coef) - 1.0) > EPSILON) {
                sb.append(String.format("%.4f", Math.abs(coef)));
            }
            sb.append("t").append(k);
        }
        return sb.toString();
    }
    
    // Fungsi bantuan untuk mencetak matriks [A|b] secara rapi
    private void appendAugmentedMatrix(StringBuilder steps, Matrix mat) {
        int rows = mat.getRows();
        int cols = mat.getCols();
        for (int r = 0; r < rows; r++) {
            steps.append("   ");
            for (int c = 0; c < cols; c++) {
                if (c == cols - 1) steps.append(" | ");
                steps.append(String.format("%10.4f ", mat.get(r, c)));
            }
            steps.append("\n");
        }
    }
}
