package algeo.spl;
import algeo.matrix.Matrix;

public class InverseSPL implements SPLSolver {
    @Override
    public String solve(Matrix augmentedMatrix) {
        // Kerangka metode matriks balikan (invers)
        
        // 1. Sama kayak cramer, pisahin dulu matriks A di kiri dan vektor hasil b di kanan
        
        // 2. Cari invers dari matriks A (Nunggu fungsi invers dari sahla stabil)
        
        // 3. Kalikan matriks invers A tadi dengan vektor b pakai fungsi perkalian matriks
        // Hasil perkaliannya langsung jadi solusi akhir
        
        return "Metode matriks balikan belum diimplementasi.";
    }
}
