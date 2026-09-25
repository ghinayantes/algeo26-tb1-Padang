package algeo.spl;

import algeo.matrix.Matrix;

public class SPLTest {
    public static void main(String[] args) {
        System.out.println("=== MEMULAI PENGUJIAN SEMUA MODUL SPL ===");

        // Matriks Koefisien A (2x2) dan Vektor Konstanta B (2x1)
        // Persamaan:
        // 2x1 + x1 = 5
        // x1 + 3x2 = 6
        Matrix A = new Matrix(2, 2);
        A.set(0, 0, 2.0);
        A.set(0, 1, 1.0);
        A.set(1, 0, 1.0);
        A.set(1, 1, 3.0);

        Matrix B = new Matrix(2, 1);
        B.set(0, 0, 5.0);
        B.set(1, 0, 6.0);

        // Daftar solver yang akan diuji
        SPLSolver[] solvers = {
            new Gauss(),
            new GaussJordan(),
            new Cramer(),
            new InverseSPL()
        };

        for (SPLSolver solver : solvers) {
            System.out.println("\n-----------------------------------------");
            System.out.println("Menguji Metode: " + solver.getName());
            System.out.println("-----------------------------------------");

            if (solver.isApplicable(A.augment(B))) {
                String hasilSolve = solver.solve(A, B);
                System.out.println(hasilSolve);
            } else {
                System.out.println("Metode tidak aplikatif untuk matriks ini.");
            }
        }
        
        System.out.println("=== PENGUJIAN SELESAI ===");
    }
}