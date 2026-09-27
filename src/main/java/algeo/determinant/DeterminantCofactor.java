package algeo.determinant;
import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;

public class DeterminantCofactor {

    //Batas toleransi rounding
    private static final double EPSILON = 1e-9;

    private DeterminantCofactor() {
    }

    public static double cofactorExpansion(Matrix m) {
        if (m.getRows() != m.getCols()) {
            throw new MatrixException("Determinan hanya bisa dihitung untuk matriks persegi (rows == cols).");
        }

        int n = m.getRows();

        // Basis
        if (n == 1) {
            return m.get(0, 0);
        }

        // Basis
        if (n == 2) {
            return m.get(0, 0) * m.get(1, 1) - m.get(0, 1) * m.get(1, 0);
        }

        // Rekursif, ekspansi di baris/kolom dengan nol terbanyak
        int[] line = findBestLine(m);
        boolean isRow = line[0] == 1;
        int idx = line[1];

        double det = 0;
        for (int k = 0; k < n; k++) {
            int i = isRow ? idx : k;
            int j = isRow ? k : idx;
            double a = m.get(i, j);
            if (Math.abs(a) <= EPSILON) {
                continue; // suku bernilai 0, minor tidak perlu dihitung
            }
            double sign = ((i + j) % 2 == 0) ? 1.0 : -1.0; // (-1)^(i+j)
            double minorDet = cofactorExpansion(m.getSubmatrix(i, j));
            det += sign * a * minorDet;
        }

        // rounding
        double rounded = Math.round(det);
        if (Math.abs(det - rounded) < EPSILON) {
            return rounded;
        }
        return det;
    }

    // cari yang paling banyak 0
    private static int[] findBestLine(Matrix m) {
        int n = m.getRows();
        int bestType = 1;
        int bestIndex = 0;
        int bestZeroCount = -1;

        for (int i = 0; i < n; i++) {
            int zeroCount = 0;
            for (int j = 0; j < n; j++) {
                if (Math.abs(m.get(i, j)) <= EPSILON) {
                    zeroCount++;
                }
            }
            if (zeroCount > bestZeroCount) {
                bestZeroCount = zeroCount;
                bestIndex = i;
            }
        }

        for (int j = 0; j < n; j++) {
            int zeroCount = 0;
            for (int i = 0; i < n; i++) {
                if (Math.abs(m.get(i, j)) <= EPSILON) {
                    zeroCount++;
                }
            }
            if (zeroCount > bestZeroCount) {
                bestType = 0;
                bestZeroCount = zeroCount;
                bestIndex = j;
            }
        }

        return new int[] {bestType, bestIndex, bestZeroCount};
    }

    // class untuk menampilkan step-by-step
    public static String cofactorExpansionWithSteps(Matrix m) {
        if (m.getRows() != m.getCols()) {
            throw new MatrixException("Determinan hanya bisa dihitung untuk matriks persegi (rows == cols).");
        }

        int n = m.getRows();
        StringBuilder steps = new StringBuilder();
        
        steps.append("=========================================================\n");
        steps.append(" PROSES HITUNG DETERMINAN : METODE EKSPANSI KOFAKTOR\n");
        steps.append("=========================================================\n\n");
 
        steps.append("Matriks A:\n");
        for (int r = 0; r < m.getRows(); r++) {
            for (int c = 0; c < m.getCols(); c++) {
                steps.append(String.format("%10.6f ", m.get(r, c))); 
            }
            steps.append("\n");
        }
steps.append("\n");

        if (n == 1) {
            steps.append("Matriks berukuran 1x1. Nilai determinan adalah elemen itu sendiri.\n");
            steps.append(String.format("det(A) = %.6f\n", m.get(0, 0)));
            return steps.toString();
        }

        if (n == 2) {
            double det2 = m.get(0, 0) * m.get(1, 1) - m.get(0, 1) * m.get(1, 0);
            steps.append("Matriks berukuran 2x2. Menggunakan rumus langsung (ad - bc):\n");
            steps.append(String.format("det(A) = (%.4f * %.4f) - (%.4f * %.4f)\n",
                    m.get(0, 0), m.get(1, 1), m.get(0, 1), m.get(1, 0)));
            steps.append(String.format("det(A) = %.6f\n", det2));
            return steps.toString();
        }

        int[] line = findBestLine(m);
        boolean isRow = line[0] == 1;
        int idx = line[1];

        steps.append(String.format("Melakukan ekspansi pada %s ke-%d\n", isRow ? "Baris" : "Kolom", idx + 1));
        steps.append(String.format("(Jalur ini dipilih karena memiliki %d buah elemen nol)\n\n", line[2]));

        double det = 0;
        for (int k = 0; k < n; k++) {
            int i = isRow ? idx : k;
            int j = isRow ? k : idx;
            double a = m.get(i, j);

            if (Math.abs(a) <= EPSILON) {
                steps.append(String.format("-> Suku ke-%d: Elemen a(%d,%d) bernilai 0.\n", k + 1, i + 1, j + 1));
                steps.append("   Perhitungan minor dilewati karena matriks akan dikalikan dengan 0.\n\n");
                continue;
            }

            double sign = ((i + j) % 2 == 0) ? 1.0 : -1.0;
            Matrix minor = m.getSubmatrix(i, j);
            double minorDet = cofactorExpansion(minor); // dihitung via versi asli, tidak dicatat ulang
            double term = sign * a * minorDet;
            det += term;

            steps.append(String.format("-> Suku ke-%d: Elemen a(%d,%d) = %.4f\n", k + 1, i + 1, j + 1, a));
            steps.append(String.format("   Tanda kofaktor: %s\n", sign > 0 ? "(+1)" : "(-1)"));
            steps.append(String.format("   Matriks Minor M(%d,%d):\n", i + 1, j + 1));
            for (int r = 0; r < minor.getRows(); r++) {
                steps.append("      "); // Indentasi agar sejajar
                for (int c = 0; c < minor.getCols(); c++) {
                    steps.append(String.format("%10.4f ", minor.get(r, c))); 
                }
                steps.append("\n");
            }
            steps.append(String.format("   det(M) = %.6f\n", minorDet));
            steps.append(String.format("   Nilai Suku = %s * %.4f * %.6f = %.6f\n\n", 
                    sign > 0 ? "(+1)" : "(-1)", a, minorDet, term));
        }

        double rounded = Math.round(det);
        if (Math.abs(det - rounded) < EPSILON) {
            det = rounded;
        }
        
        steps.append("====================================================\n");
        steps.append(String.format("Hasil Akhir (Jumlah seluruh suku hasil ekspansi) = %.6f\n", det));
        return steps.toString();
    }
}