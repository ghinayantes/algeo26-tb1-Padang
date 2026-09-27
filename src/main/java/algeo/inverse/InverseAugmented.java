package algeo.inverse;

import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;;


public class InverseAugmented {

    private static final double EPSILON = 1e-9;

    private  InverseAugmented() {

    }

    public static Matrix inverse(Matrix a) {
        
        if (a.getRows() != a.getCols()) {
            throw new MatrixException("Invers hanya bisa dihitung untuk matriks persegi");
        }

        int n = a.getRows();

        // bentuk matriks augmented
        Matrix augmented = new Matrix(n, 2 * n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmented.set(i, j, a.get(i, j));
            }
            augmented.set(i, n + i, 1.0);

        }

        // fase eliminasi maju
        for (int i = 0; i < n; i++) {
            int pivotRow = augmented.getPivotRow(i, i);

            // cek singularitas
            if (Math.abs(augmented.get(pivotRow, i)) <= EPSILON) {
                throw new MatrixException("Matriks singular, invers tidak terdefinisi");
            }

            if (pivotRow != i) {
                augmented.swapRows(i, pivotRow);
            }

            // normalisasi pivot menjadi 1 di setiap (i, i)
            double pivotValue = augmented.get(i, i);
            for (int c = 0; c < 2 * n; c++) {
                augmented.set(i, c, augmented.get(i, c) / pivotValue);
            }

            for (int r = i + 1; r < n; r++) {
                double factor = augmented.get(r, i);
                if (Math.abs(factor) > EPSILON) {
                    for (int c = 0; c < 2 * n; c++) {
                        double newVal = augmented.get(r, c) - factor * augmented.get(i, c);
                        augmented.set(r, c, newVal);
                    }
                }
            }
        }

        // fase eliminasi mundur
        for (int i = n - 1; i >= 0; i--) {
            for (int r = i - 1; r >= 0; r--) {
                double factor = augmented.get(r, i);
                if (Math.abs(factor) > EPSILON) {
                    for (int c = 0; c < 2 * n; c++) {
                        double newVal = augmented.get(r, c) - factor * augmented.get(i, c);
                        augmented.set(r, c, newVal);
                    }
                }
            }
        }

        // kirimkan matriks sisi kanan sebagai hasil
        Matrix result = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result.set(i, j, augmented.get(i, n + j));
            }
        }
        return result;
    }

    // spesifikasi pembulatan untuk tampilan output
    public static Matrix roundForDisplay(Matrix m, int decimals) {
        double multiplier = Math.pow(10, decimals);
        Matrix rounded = new Matrix(m.getRows(), m.getCols());
        for (int i = 0; i < m.getRows(); i++) {
            for (int j = 0; j < m.getCols(); j++) {
                double value = Math.round(m.get(i, j) * multiplier) / multiplier;
                rounded.set(i, j, value);
            }
        } 
        return rounded;
    }

    // class untuk menampilkan step-by-step 
    public static String inverseWithSteps(Matrix a) {
 
        if (a.getRows() != a.getCols()) {
            throw new MatrixException("Invers hanya bisa dihitung untuk matriks persegi");
        }
 
        int n = a.getRows();
        StringBuilder steps = new StringBuilder();
        
        steps.append("=========================================================\n");
        steps.append(" PROSES MENCARI INVERS MATRIKS : METODE AUGMENTASI [A|I]\n");
        steps.append("=========================================================\n\n");
 
        Matrix augmented = new Matrix(n, 2 * n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmented.set(i, j, a.get(i, j));
            }
            augmented.set(i, n + i, 1.0);
        }
 
        steps.append("[Langkah 1] Membentuk Matriks Augmented [A|I] awal:\n");
        for (int r = 0; r < augmented.getRows(); r++) {
            steps.append("   ");
            for (int c = 0; c < augmented.getCols(); c++) {
                if (c == n) steps.append(" | "); // Garis pemisah visual antara A dan I
                steps.append(String.format("%10.4f ", augmented.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");
        
        steps.append("[Langkah 2] Eliminasi Maju (Membentuk Segitiga Atas)\n");
 
        for (int i = 0; i < n; i++) {
            int pivotRow = augmented.getPivotRow(i, i);
 
            if (Math.abs(augmented.get(pivotRow, i)) <= EPSILON) {
                steps.append(String.format("-> Tidak ada elemen pivot tak-nol pada kolom ke-%d.\n", i + 1));
                steps.append("   Matriks singular, sehingga invers tidak terdefinisi.\n");
                throw new MatrixException("Matriks singular, invers tidak terdefinisi");
            }
 
            boolean changed = false;
            
            if (pivotRow != i) {
                augmented.swapRows(i, pivotRow);
                steps.append(String.format("-> [Tukar Baris] Baris %d ditukar dengan Baris %d\n", i + 1, pivotRow + 1));
                changed = true;
            }
 
            double pivotValue = augmented.get(i, i);
            if (Math.abs(pivotValue - 1.0) > EPSILON) {
                steps.append(String.format("-> [Normalisasi] Baris %d dibagi %.4f (pivot menjadi 1)\n", i + 1, pivotValue));
                for (int c = 0; c < 2 * n; c++) {
                    augmented.set(i, c, augmented.get(i, c) / pivotValue);
                }
                changed = true;
            }
 
            for (int r = i + 1; r < n; r++) {
                double factor = augmented.get(r, i);
                if (Math.abs(factor) > EPSILON) {
                    steps.append(String.format("   Operasi OBE: Baris %d = Baris %d - (%.4f) * Baris %d\n", r + 1, r + 1, factor, i + 1));
                    for (int c = 0; c < 2 * n; c++) {
                        double newVal = augmented.get(r, c) - factor * augmented.get(i, c);
                        augmented.set(r, c, newVal);
                    }
                    changed = true;
                }
            }
            
            if (changed) {
                steps.append("   Matriks [A|I] sementara:\n");
                for (int r = 0; r < augmented.getRows(); r++) {
                    steps.append("   ");
                    for (int c = 0; c < augmented.getCols(); c++) {
                        if (c == n) steps.append(" | ");
                        steps.append(String.format("%10.4f ", augmented.get(r, c)));
                    }
                    steps.append("\n");
                }
                steps.append("\n");
            }
        }
 
        steps.append("[Langkah 3] Eliminasi Mundur (Membentuk Identitas di Sisi Kiri)\n");
        boolean backChanged = false;
        
        for (int i = n - 1; i >= 0; i--) {
            for (int r = i - 1; r >= 0; r--) {
                double factor = augmented.get(r, i);
                if (Math.abs(factor) > EPSILON) {
                    steps.append(String.format("   Operasi OBE: Baris %d = Baris %d - (%.4f) * Baris %d\n", r + 1, r + 1, factor, i + 1));
                    for (int c = 0; c < 2 * n; c++) {
                        double newVal = augmented.get(r, c) - factor * augmented.get(i, c);
                        augmented.set(r, c, newVal);
                    }
                    backChanged = true;
                }
            }
        }
        
        if (backChanged) {
            steps.append("   Matriks [A|I] setelah eliminasi mundur:\n");
        } else {
            steps.append("   (Sisi kiri sudah berupa matriks identitas)\n");
        }
        
        for (int r = 0; r < augmented.getRows(); r++) {
            steps.append("   ");
            for (int c = 0; c < augmented.getCols(); c++) {
                if (c == n) steps.append(" | ");
                steps.append(String.format("%10.4f ", augmented.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");
 
        Matrix result = new Matrix(n, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result.set(i, j, augmented.get(i, n + j));
            }
        }
 
        steps.append("[Langkah 4] Ekstraksi Matriks Invers (A^-1)\n");
        steps.append("Sisi kanan dari [A|I] diambil sebagai matriks balikan A^-1:\n");
        for (int r = 0; r < result.getRows(); r++) {
            for (int c = 0; c < result.getCols(); c++) {
                steps.append(String.format("%10.4f ", result.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("=========================================================\n");
        
        return steps.toString();
    }
}
