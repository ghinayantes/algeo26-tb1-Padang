package algeo.spl;
import algeo.matrix.Matrix;

public class GaussJordanElimination implements SPLSolver {
    @Override
    public String solve(Matrix augmentedMatrix) {
        // Kerangka Gauss-Jordan
        
        // 1. Copy logika dari gauss biasa di sini 
        // buat bikin matriksnya jadi segitiga atas dulu
        
        // 2. Lanjut eliminasi mundur
        // bikin nol juga semua angka di atas pivot, sampai matriks kiri sisa diagonal 1 (identitas)
        
        // 3. Kolom yang paling kanan otomatis langsung jadi hasil solusinya
        
        return "Proses Gauss-Jordan belum diimplementasi.";
    }
}
