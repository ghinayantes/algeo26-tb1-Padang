package algeo.determinant;

import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;

public class DeterminantOBE {
    private static final double EPSILON = 1e-9;
    private DeterminantOBE() {}  

    public static double determinantOBE(Matrix A) {
        if (A.getRows() != A.getCols()) {
            throw new MatrixException("Determinan hanya untuk matriks persegi.");
        }
        int rows = A.getRows();
        Matrix mat = A.copy();
        double det = 1.0;

        for (int i = 0; i < rows; i++) {
            int maxRow = i;
            for (int k = i + 1; k < rows; k++) {
                if (Math.abs(mat.get(k, i)) > Math.abs(mat.get(maxRow, i))) {
                    maxRow = k;
                }
            }
            if (Math.abs(mat.get(maxRow, i)) <= EPSILON) {
                return 0.0;
            }
            if (maxRow != i) {
                mat.swapRows(i, maxRow);
                det = -det;
            }
            double pivot = mat.get(i, i);
            det *= pivot;
            for (int j = i + 1; j < rows; j++) {
                double factor = mat.get(j, i) / pivot;
                for (int c = i; c < rows; c++) {
                    mat.set(j, c, mat.get(j, c) - factor * mat.get(i, c));
                }
            }
        }
        return det;
    }

    // class untuk menampilkan step-by-step 
    public static String determinantOBEWithSteps(Matrix A) {
        if (A.getRows() != A.getCols()) {
            throw new MatrixException("Determinan hanya untuk matriks persegi.");
        }
        int rows = A.getRows();
        Matrix mat = A.copy();
        double det = 1.0;
        StringBuilder steps = new StringBuilder();
 
        steps.append("=========================================================\n");
        steps.append(" PROSES HITUNG DETERMINAN : METODE REDUKSI BARIS (OBE)\n");
        steps.append("=========================================================\n\n");
 
        steps.append("Matriks Awal:\n");
        for (int r = 0; r < mat.getRows(); r++) {
            for (int c = 0; c < mat.getCols(); c++) {
                steps.append(String.format("%10.4f ", mat.get(r, c)));
            }
            steps.append("\n");
        }
        steps.append("\n");
 
        for (int i = 0; i < rows; i++) {
            int maxRow = i;
            for (int k = i + 1; k < rows; k++) {
                if (Math.abs(mat.get(k, i)) > Math.abs(mat.get(maxRow, i))) {
                    maxRow = k;
                }
            }
 
            if (Math.abs(mat.get(maxRow, i)) <= EPSILON) {
                steps.append(String.format("-> Pivot pada kolom ke-%d bernilai 0.\n", i + 1));
                steps.append("   Karena ada kolom tanpa pivot tak-nol, determinan matriks adalah 0.\n\n");
                steps.append("=========================================================\n");
                steps.append("Hasil Akhir Determinan = 0.000000\n");
                return steps.toString();
            }
 
            if (maxRow != i) {
                mat.swapRows(i, maxRow);
                det = -det;
                steps.append(String.format("[Tukar Baris] Baris %d ditukar dengan Baris %d\n", i + 1, maxRow + 1));
                steps.append("   (Partial pivoting. Tanda determinan berbalik: det = -det)\n");
                
                // Cetak matriks setelah tukar baris dengan indentasi
                for (int r = 0; r < mat.getRows(); r++) {
                    steps.append("   ");
                    for (int c = 0; c < mat.getCols(); c++) {
                        steps.append(String.format("%10.4f ", mat.get(r, c)));
                    }
                    steps.append("\n");
                }
                steps.append("\n");
            }
 
            double pivot = mat.get(i, i);
            det *= pivot;
            steps.append(String.format("-> Pivot diagonal a(%d,%d) = %.4f  |  det sementara = %.4f\n", 
                    i + 1, i + 1, pivot, det));
 
            boolean hasElimination = false;
            for (int j = i + 1; j < rows; j++) {
                double factor = mat.get(j, i) / pivot;
                if (Math.abs(factor) > EPSILON) {
                    hasElimination = true;
                    steps.append(String.format("   Operasi OBE: Baris %d = Baris %d - (%.4f) * Baris %d\n", 
                            j + 1, j + 1, factor, i + 1));
                    for (int c = i; c < rows; c++) {
                        mat.set(j, c, mat.get(j, c) - factor * mat.get(i, c));
                    }
                }
            }
            
            // Hanya cetak matriks jika ada operasi OBE yang terjadi pada baris ini
            if (hasElimination) {
                steps.append("   Matriks setelah OBE:\n");
                for (int r = 0; r < mat.getRows(); r++) {
                    steps.append("   ");
                    for (int c = 0; c < mat.getCols(); c++) {
                        steps.append(String.format("%10.4f ", mat.get(r, c)));
                    }
                    steps.append("\n");
                }
                steps.append("\n");
            } else if (i < rows - 1) {
                steps.append("   (Tidak ada operasi karena elemen di bawah pivot sudah 0)\n\n");
            }
        }
 
        steps.append("=========================================================\n");
        steps.append("Matriks telah menjadi matriks segitiga atas.\n");
        steps.append(String.format("Hasil Akhir (Hasil kali diagonal utama yang telah disesuaikan tanda) = %.6f\n", det));
        return steps.toString();
    }
}