package algeo.determinant;

import algeo.matrix.Matrix;

// Alur:
//   1. Reduksi ke bentuk segitiga atas via OBE.
//   2. Setiap swap baris → tanda determinan dikali -1.
//   3. Determinan = hasil kali pivot diagonal.
//   4. Kalau ada pivot ≈ 0 → determinan = 0 (matriks singular).

public double determinantOBE {
    if (this.rows != this.cols) {
        throw new MatrixException("Determinan hanya untuk matriks persegi.");
    }

    int n = this.rows;
    Matrix matriks = this.copy();
    double det = 1.0;
}
