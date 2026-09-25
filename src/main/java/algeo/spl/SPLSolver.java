package algeo.spl;

import algeo.matrix.Matrix;

public interface SPLSolver {
    /**
     * Mengembalikan nama metode (misal: "Metode Eliminasi Gauss", "Kaidah Cramer").
     * Berguna untuk pencetakan menu atau header laporan.
     */
    String getName();
    /**
     * Mengecek apakah metode ini bisa digunakan untuk matriks teraugmentasi yang diberikan.
     * -Gauss/GaussJordan: selalu return true (bisa untuk semua matriks).
     * -InverseSPL/Cramer: return true hanya jika m = n dan det(A) != 0.
     */
    boolean isApplicable(Matrix augmentedMatrix);
    /**
     * Mengembalikan string hasil penyelesaian yang siap dicetak/disimpan ke file
     * (Menangani solusi unik, solusi parametrik/banyak, maupun tidak ada solusi).
     */
    String solve(Matrix augmentedMatrix);
    /**
     * Mengembalikan array angka double[] [x1, x2, ..., xn] jika solusinya unik.
     * Mengembalikan null jika solusi parametrik atau tidak ada solusi.
     * Sangat berguna untuk fitur Interpolasi Polinomial dan Regresi.
     */
    double[] solveToVector(Matrix augmentedMatrix);
    /**
     * Overload default method agar bisa langsung dipanggil jika A dan B terpisah.
     */
    default String solve(Matrix A, Matrix B) {
        return solve(A.augment(B));
    }
}
