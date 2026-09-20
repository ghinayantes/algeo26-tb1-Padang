package algeo.spl;
import algeo.matrix.Matrix;

public class GaussianElimination implements SPLSolver {
    @Override
    public String solve(Matrix augmentedMatrix) {
        StringBuilder steps = new StringBuilder();
        int rows = augmentedMatrix.getRows();
        int cols = augmentedMatrix.getCols();

        // Tahap 1: Eliminasi maju (Bikin matriks jadi bentuk segitiga atas)
        for (int i = 0; i < rows; i++) {
            
            // To do : Nanti tambahin partial pivoting di sini
            
            double pivot = augmentedMatrix.getElement(i, i);
            
            if (pivot != 0) {
                // bikin angka pivotnya jadi 1 biar substitusi nanti lebih gampang
                if (pivot != 1) {
                    steps.append(String.format("Baris %d dibagi %.2f\n", i + 1, pivot));
                    // Mengubah angka di memori matriks
                    // augmentedMatrix.multiplyRow(i, 1.0 / pivot); 
                }

                // Bikin nol semua angka di bawah pivot (proses eliminasi)
                for (int j = i + 1; j < rows; j++) {
                    double factor = augmentedMatrix.getElement(j, i);
                    if (factor != 0) {
                        steps.append(String.format("Baris %d dikurangi (%.2f) * Baris %d\n", j + 1, factor, i + 1));
                        // Ini operasi matematis utamanya
                        // augmentedMatrix.subtractRow(j, i, factor); 
                     }
                }
            }
        }
        
        // To do : Nanti tambahin substitusi mundur di sini
        // Ngitung nilai x, y, z dari baris paling bawah naik ke atas
        
        return steps.toString();
    }
}
