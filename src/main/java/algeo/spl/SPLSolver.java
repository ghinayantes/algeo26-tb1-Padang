package algeo.spl;
import algeo.matrix.Matrix;

public interface SPLSolver {
    // Semua algoritma SPL wajib pakai fungsi ini biar cara manggilnya seragam di menu utama
    String solve(Matrix augmentedMatrix);
}
