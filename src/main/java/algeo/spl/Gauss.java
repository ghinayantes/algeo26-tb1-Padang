package algeo.spl;

import algeo.matrix.Matrix;

public class Gauss implements SPLSolver {
    @Override
    public String getName() {
        return "Eliminasi Gauss";
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
        // Buat salinan matriks agar data awal inputan pengguna tidak rusak
        Matrix mat = augmentedMatrix.copy();
        StringBuilder steps = new StringBuilder();
        int rows = mat.getRows();
        int cols = mat.getCols();

        steps.append("=== PROSES ELIMINASI GAUSS ===\n\n");

        // 1. Eliminasi maju + Partial pivoting
        for (int i = 0; i < rows; i++) {
            
            // 1.1 Partial pivoting
            // Cari baris dari baris ke-i ke bawah yang punya nilai mutlak terbesar di kolom ke-i
            int maxRow = i;
            for (int k = i + 1; k < rows; k++) {
                if (Math.abs(mat.get(k, i)) > Math.abs(mat.get(maxRow, i))) {
                    maxRow = k;
                }
            }

            // Jika baris terbesar bukan di posisi i, tukar barisnya
            if (maxRow != i && Math.abs(mat.get(maxRow, i)) > 1e-9) {
                mat.swapRows(i, maxRow);
                steps.append(String.format("Tukar Baris %d dengan Baris %d (Partial Pivoting)\n", i + 1, maxRow + 1));
            }

            double pivot = mat.get(i, i);

            // Jika pivot 0 (atau mendekati 0), lewati agar tidak error pembagian nol
            if (Math.abs(pivot) < 1e-9) {
                continue;
            }

            // 1.2 Normalisasi pivot (Ubah angka pivot di diagonal utama jadi 1)
            if (Math.abs(pivot - 1.0) > 1e-9) {
                steps.append(String.format("Baris %d dibagi %.2f\n", i + 1, pivot));
                for (int c = i; c < cols; c++) {
                    mat.set(i, c, mat.get(i, c) / pivot);
                }
            }

            // 1.3 Eliminasi baris di bawah pivot (Bikin angka di bawah pivot jadi 0)
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

        // 2. Cek konsistensi SPL (Cek kasus tidak ada solusi)
        // Baris [0 0 0 ... | k] dengan k != 0 artinya kontradiksi (0 = k)
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

        // 3. Substitusi mundur (Ngitung nilai x1, x2, x3 dari bawah ke atas)
        steps.append("\n=== TAHAP SUBSTITUSI MUNDUR ===\n");
        
        int numVars = cols - 1;
        double[] x = new double[numVars];

        for (int i = rows - 1; i >= 0; i--) {
            double sum = mat.get(i, cols - 1);
            
            // Kurangi nilai variabel yang sudah didapat di baris bawahnya
            for (int j = i + 1; j < numVars; j++) {
                sum -= mat.get(i, j) * x[j];
            }

            double coef = mat.get(i, i);
            if (Math.abs(coef) > 1e-9) {
                x[i] = sum / coef;
            } else {
                x[i] = 0; // Penanganan variabel bebas dasar
            }
        }

        // 4. Cetak solusi akhir
        steps.append("\nHasil Solusi:\n");
        for (int i = 0; i < numVars; i++) {
            steps.append(String.format("x%d = %.4f\n", i + 1, x[i]));
        }

        return steps.toString();
    }
}
