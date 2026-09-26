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

    // class untuk menampilkan step-by-step
    public static String cofactorExpansionWithSteps(Matrix m) {
        if (m.getRows() != m.getCols()) {
            throw new MatrixException("Determinan hanya bisa dihitung untuk matriks persegi (rows == cols).");
        }

        int n = m.getRows();
        StringBuilder steps = new StringBuilder();
        steps.append("=== PROSES DETERMINAN (Metode Ekspansi Kofaktor) ===\n\n");
        steps.append("Matriks:\n").append(m).append("\n\n");

        if (n == 1) {
            steps.append("Matriks 1x1, determinan = elemen itu sendiri.\n");
            steps.append(String.format("Determinan = %.6f\n", m.get(0, 0)));
            return steps.toString();
        }

        if (n == 2) {
            double det2 = m.get(0, 0) * m.get(1, 1) - m.get(0, 1) * m.get(1, 0);
            steps.append(String.format("Matriks 2x2, rumus langsung: (%.4f * %.4f) - (%.4f * %.4f)\n",
                    m.get(0, 0), m.get(1, 1), m.get(0, 1), m.get(1, 0)));
            steps.append(String.format("Determinan = %.6f\n", det2));
            return steps.toString();
        }

        int[] line = findBestLine(m);
        boolean isRow = line[0] == 1;
        int idx = line[1];

        steps.append(String.format("Ekspansi kofaktor sepanjang %s ke-%d (%s dengan elemen nol terbanyak: %d nol):\n\n",
                isRow ? "baris" : "kolom", idx + 1, isRow ? "baris" : "kolom", line[2]));

        double det = 0;
        for (int k = 0; k < n; k++) {
            int i = isRow ? idx : k;
            int j = isRow ? k : idx;
            double a = m.get(i, j);

            if (Math.abs(a) <= EPSILON) {
                steps.append(String.format("Suku %d: elemen a(%d,%d) = 0 -> kontribusi 0, minor tidak dihitung\n\n",
                        k + 1, i + 1, j + 1));
                continue;
            }

            double sign = ((i + j) % 2 == 0) ? 1.0 : -1.0;
            Matrix minor = m.getSubmatrix(i, j);
            double minorDet = cofactorExpansion(minor); // dihitung via versi asli, tidak dicatat ulang
            double term = sign * a * minorDet;
            det += term;

            steps.append(String.format("Suku %d: tanda=%s, elemen a(%d,%d)=%.4f, minor M(%d,%d):\n",
                    k + 1, sign > 0 ? "+" : "-", i + 1, j + 1, a, i + 1, j + 1));
            steps.append(minor).append("\n");
            steps.append(String.format("det(minor) = %.6f  ->  kontribusi = %s%.4f * %.6f = %.6f\n\n",
                    minorDet, sign > 0 ? "+" : "-", a, minorDet, term));
        }

        double rounded = Math.round(det);
        if (Math.abs(det - rounded) < EPSILON) {
            det = rounded;
        }
        steps.append(String.format("Determinan = jumlah seluruh kontribusi = %.6f\n", det));
        return steps.toString();
    }

    // Cari baris/kolom dengan elemen nol terbanyak, return {1 = baris / 0 = kolom, indeks, jumlah nol}
    private static int[] findBestLine(Matrix m) {
        int n = m.getRows();
        int[] best = {1, 0, -1};

        for (int i = 0; i < n; i++) {
            int zeros = 0;
            for (int j = 0; j < n; j++) {
                if (Math.abs(m.get(i, j)) <= EPSILON) zeros++;
            }
            if (zeros > best[2]) {
                best = new int[]{1, i, zeros};
            }
        }

        for (int j = 0; j < n; j++) {
            int zeros = 0;
            for (int i = 0; i < n; i++) {
                if (Math.abs(m.get(i, j)) <= EPSILON) zeros++;
            }
            if (zeros > best[2]) {
                best = new int[]{0, j, zeros};
            }
        }
        return best;
    }
}
