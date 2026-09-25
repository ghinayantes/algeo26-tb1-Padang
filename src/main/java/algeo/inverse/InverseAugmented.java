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
}
