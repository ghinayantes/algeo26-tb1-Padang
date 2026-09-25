package algeo.spl;
import algeo.matrix.Matrix;

public class CramerRule implements SPLSolver {
    @Override
    public String solve(Matrix augmentedMatrix) {
        // Kerangka cramer
        
        // 1. Pisahin matriks augmented jadi matriks utama (A) di kiri dan vektor hasil (b) di kanan
        
        // 2. Hitung determinan matriks A (Nunggu fungsi determinan dari sahla, kalau determinannya 0, langsung stop karena cramer gabisa dipake)
        
        // 3. Looping per kolom
        // Ganti kolom ke-i di matriks A pakai vektor b, terus hitung determinannya.
        // Nilai variabel ke-i = (Determinan matriks yang baru diganti) / (Determinan A awal)
        
        return "Kaidah cramer belum diimplementasi.";
    }
}
