package com.kodetubes.algeo.spl;
import com.kodetubes.algeo.matrix.Matrix;

public interface SPLSolver {
    // Semua algoritma SPL wajib pakai fungsi ini biar cara manggilnya seragam di menu utama
    String solve(Matrix augmentedMatrix);
}
