package algeo;

import algeo.determinant.DeterminantCofactor;
import algeo.determinant.DeterminantOBE;
import algeo.interpolation.PolynomialInterpolation;
import algeo.inverse.InverseAdjoint;
import algeo.inverse.InverseAugmented;
import algeo.io.IOUtils;
import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;
import algeo.spl.Cramer;
import algeo.spl.Gauss;
import algeo.spl.GaussJordan;
import algeo.spl.InverseSPL;
import algeo.spl.SPLSolver;
import algeo.spline.CubicSplineRegression;
import algeo.spline.NaturalCubicSpline;

import java.util.Scanner;

public class App {

    private static final int MAX_INTERPOLATION_POINTS = 10;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        boolean running = true;

        while (running) {
            printMainMenu();
            try {
                int choice = IOUtils.readInt(sc, "Pilih menu: ");
                switch (choice) {
                    case 1: handleSPL(sc); break;
                    case 2: handleDeterminant(sc); break;
                    case 3: handleInverse(sc); break;
                    case 4: handlePolynomialInterpolation(sc); break;
                    case 5: handleNaturalCubicSpline(sc); break;
                    case 6: handleCubicSplineRegression(sc); break;
                    case 7: running = false; System.out.println("\nTerima kasih telah menggunakan program ini. Sampai jumpa!"); break;
                    default: System.out.println("Pilihan tidak tersedia, silakan coba lagi.");
                }
            } catch (MatrixException e) {
                System.out.println("\nGagal memproses: " + e.getMessage() + "\n");
            } catch (Exception e) {
                System.out.println("\nTerjadi kesalahan sistem: " + e.getMessage() + "\n");
            }
        }
        sc.close();
    }

    private static void printMainMenu() {
        System.out.println("\n======================================================");
        System.out.println("      Kalkulator Aljabar Linier dan Geometri");
        System.out.println("Sistem Persamaan Linier, Determinan, dan Penerapannya");
        System.out.println("                  Kelompok Padang");
        System.out.println("======================================================\n");
        System.out.println("MENU UTAMA");
        System.out.println("1. Sistem Persamaan Linear");
        System.out.println("2. Determinan Matriks");
        System.out.println("3. Invers Matriks");
        System.out.println("4. Interpolasi Polinomial");
        System.out.println("5. Interpolasi Spline Kubik Natural");
        System.out.println("6. Regresi Spline Kubik");
        System.out.println("7. Keluar");
    }

    private static void handleSPL(Scanner sc) {
        System.out.println("\nSUB-MENU SPL");
        System.out.println("1. Eliminasi Gauss");
        System.out.println("2. Eliminasi Gauss-Jordan");
        System.out.println("3. Invers Matriks");
        System.out.println("4. Aturan Cramer");
        System.out.println("0. Kembali");
        int method = IOUtils.readInt(sc, "Pilih metode penyelesaian: ");
        if (method == 0) return;

        SPLSolver solver;
        switch (method) {
            case 1: solver = new Gauss(); break;
            case 2: solver = new GaussJordan(); break;
            case 3: solver = new InverseSPL(); break;
            case 4: solver = new Cramer(); break;
            default: System.out.println("Pilihan tidak tersedia."); return;
        }

        Matrix augmented = obtainAugmentedMatrix(sc);

        if (!solver.isApplicable(augmented)) {
            System.out.println("\nMetode " + solver.getName() + " gagal digunakan. Pastikan jumlah persamaan sama dengan jumlah variabel.");
            return;
        }

        String result = solver.solve(augmented);
        System.out.println("\n" + result);

        String fileContent = "Penyelesaian Sistem Persamaan Linear menggunakan " + solver.getName() + "\n\n"
                + "Matriks Input:\n" + IOUtils.matrixToString(augmented) + "\n"
                + "Hasil Penyelesaian:\n" + result;
        offerSave(sc, fileContent);
    }

    private static void handleDeterminant(Scanner sc) {
        System.out.println("\nSUB-MENU DETERMINAN");
        System.out.println("1. Metode Reduksi Baris / OBE");
        System.out.println("2. Metode Ekspansi Kofaktor");
        System.out.println("0. Kembali");
        int method = IOUtils.readInt(sc, "Pilih metode perhitungan: ");
        if (method == 0) return;
        if (method != 1 && method != 2) {
            System.out.println("Pilihan tidak tersedia.");
            return;
        }

        Matrix m = obtainSquareMatrix(sc);

        String methodName = (method == 1) ? "Reduksi Baris / OBE" : "Ekspansi Kofaktor";
        double det;
        String steps;
        try {
            det = (method == 1) ? DeterminantOBE.determinantOBE(m) : DeterminantCofactor.cofactorExpansion(m);
            steps = (method == 1) ? DeterminantOBE.determinantOBEWithSteps(m) : DeterminantCofactor.cofactorExpansionWithSteps(m);
        } catch (MatrixException e) {
            System.out.println("\nMatriks ini tidak memiliki nilai determinan. Alasan: " + e.getMessage());
            return;
        }

        System.out.println("\nMetode Perhitungan: " + methodName);
        System.out.println("Determinan: " + IOUtils.format3(det) + "\n");
        System.out.println(steps);

        String fileContent = "Perhitungan Determinan menggunakan " + methodName + "\n\n"
                + "Matriks Input:\n" + IOUtils.matrixToString(m) + "\n"
                + "Determinan: " + IOUtils.format3(det) + "\n"
                + "Langkah-langkah Penyelesaian:\n" + steps + "\n";
        offerSave(sc, fileContent);
    }

    private static void handleInverse(Scanner sc) {
        System.out.println("\nSUB-MENU INVERS");
        System.out.println("1. Metode Augmentasi [A | I] dengan Eliminasi Gauss-Jordan");
        System.out.println("2. Metode Adjoin");
        System.out.println("0. Kembali");
        int method = IOUtils.readInt(sc, "Pilih metode perhitungan: ");
        if (method == 0) return;
        if (method != 1 && method != 2) {
            System.out.println("Pilihan tidak tersedia.");
            return;
        }

        Matrix m = obtainSquareMatrix(sc);

        String methodName = (method == 1) ? "Augmentasi [A | I]" : "Matriks Adjoin";
        String steps;
        try {
            steps = (method == 1) ? InverseAugmented.inverseWithSteps(m) : InverseAdjoint.inverseWithSteps(m);
        } catch (MatrixException e) {
            System.out.println("\nMatriks ini tidak memiliki invers.\nAlasan: " + e.getMessage());
            return;
        }

        System.out.println("\nMetode Perhitungan: " + methodName + "\n");
        System.out.println(steps);

        String fileContent = "Pencarian Invers Matriks menggunakan " + methodName + "\n\n"
                + "Matriks Input:\n" + IOUtils.matrixToString(m) + "\n"
                + "Langkah-langkah Penyelesaian:\n" + steps + "\n";
        offerSave(sc, fileContent);
    }

    private static void handlePolynomialInterpolation(Scanner sc) {
        System.out.println("\nINTERPOLASI POLINOMIAL");
        double[][] points = obtainPoints(sc, MAX_INTERPOLATION_POINTS);
        PolynomialInterpolation p = new PolynomialInterpolation(points[0], points[1]);

        double[] domain = p.getDomain();
        System.out.printf("\nRentang data yang valid: [%s, %s]%n", IOUtils.format3(domain[0]), IOUtils.format3(domain[1]));
        System.out.println("Persamaan kurva yang terbentuk:\n" + p.toEquationString());

        StringBuilder evalLog = new StringBuilder();
        while (true) {
            System.out.print("\nMasukkan nilai x yang ingin dihitung (kosongkan lalu tekan Enter untuk berhenti): ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) break;
            
            double xt = IOUtils.parseDouble(line);
            double result = p.evaluate(xt);
            String note = extrapolationNote(xt, domain);
            
            System.out.printf("P(%s) = %s%s%n", IOUtils.format3(xt), IOUtils.format3(result), note);
            evalLog.append("P(").append(IOUtils.format3(xt)).append(") = ").append(IOUtils.format3(result)).append(note).append("\n");
        }

        String fileContent = "Hasil Interpolasi Polinomial\n\n"
                + "Data Titik Sampel:\n" + pointsToString(points) + "\n"
                + String.format("Rentang Data: [%s, %s]%n%n", IOUtils.format3(domain[0]), IOUtils.format3(domain[1]))
                + "Persamaan Kurva:\n" + p.toEquationString() + "\n\n"
                + "Catatan Perhitungan:\n" + (evalLog.length() == 0 ? "Tidak ada percobaan perhitungan.\n" : evalLog.toString());
        offerSave(sc, fileContent);
    }

    private static void handleNaturalCubicSpline(Scanner sc) {
        System.out.println("\nINTERPOLASI SPLINE KUBIK NATURAL");
        double[][] points = obtainPoints(sc, MAX_INTERPOLATION_POINTS);
        NaturalCubicSpline s = new NaturalCubicSpline(points[0], points[1]);

        double[] domain = s.getDomain();
        double[] secondDeriv = s.getSecondDerivatives();

        System.out.printf("\nRentang data yang valid: [%s, %s]%n", IOUtils.format3(domain[0]), IOUtils.format3(domain[1]));
        System.out.println("Persamaan kurva di setiap segmen antar titik:\n" + s.toFullEquationString());

        StringBuilder evalLog = new StringBuilder();
        while (true) {
            System.out.print("\nMasukkan nilai x yang ingin dihitung (kosongkan lalu tekan Enter untuk berhenti): ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) break;
            
            double xt = IOUtils.parseDouble(line);
            double result = s.evaluate(xt);
            String note = extrapolationNote(xt, domain);
            
            System.out.printf("S(%s) = %s%s%n", IOUtils.format3(xt), IOUtils.format3(result), note);
            evalLog.append("S(").append(IOUtils.format3(xt)).append(") = ").append(IOUtils.format3(result)).append(note).append("\n");
        }

        StringBuilder secondDerivStr = new StringBuilder();
        for (int i = 0; i < secondDeriv.length; i++) {
            secondDerivStr.append("Titik ke-").append(i + 1).append(" = ").append(IOUtils.format3(secondDeriv[i])).append("\n");
        }

        String fileContent = "Hasil Interpolasi Spline Kubik Natural\n\n"
                + "Data Titik Sampel:\n" + pointsToString(points) + "\n"
                + String.format("Rentang Data: [%s, %s]%n%n", IOUtils.format3(domain[0]), IOUtils.format3(domain[1]))
                + "Nilai Turunan Kedua di Setiap Titik:\n" + secondDerivStr + "\n"
                + "Persamaan Kurva:\n" + s.toFullEquationString() + "\n\n"
                + "Catatan Perhitungan:\n" + (evalLog.length() == 0 ? "Tidak ada percobaan perhitungan.\n" : evalLog.toString());
        offerSave(sc, fileContent);
    }

    private static void handleCubicSplineRegression(Scanner sc) {
        System.out.println("\nREGRESI SPLINE KUBIK");
        double[][] points = obtainPoints(sc, IOUtils.MAX_CURVE_POINTS);

        int k = IOUtils.readInt(sc, "\nMasukkan jumlah knot yang diinginkan: ");
        if (k < 0) {
            System.out.println("Jumlah knot tidak boleh negatif.");
            return;
        }
        
        double[] knots = new double[k];
        for (int i = 0; i < k; i++) {
            System.out.print("Masukkan nilai knot ke-" + (i + 1) + ": ");
            knots[i] = IOUtils.parseDouble(sc.nextLine().trim());
        }

        CubicSplineRegression reg = new CubicSplineRegression(points[0], points[1], knots);

        System.out.print("\nDaftar knot: ");
        for (double kn : reg.getKnots()) System.out.print(IOUtils.format3(kn) + " ");
        
        System.out.println("\n\nPersamaan kurva regresi yang terbentuk:\n" + reg.toFullEquationString());

        StringBuilder evalLog = new StringBuilder();
        while (true) {
            System.out.print("\nMasukkan nilai x yang ingin diprediksi (kosongkan lalu tekan Enter untuk berhenti): ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) break;
            
            double xt = IOUtils.parseDouble(line);
            double result = reg.evaluate(xt);
            String note = extrapolationNote(xt, reg.getDomain());
            
            System.out.printf("Prediksi y(%s) = %s%s%n", IOUtils.format3(xt), IOUtils.format3(result), note);
            evalLog.append("Prediksi y(").append(IOUtils.format3(xt)).append(") = ").append(IOUtils.format3(result)).append(note).append("\n");
        }

        StringBuilder knotsStr = new StringBuilder();
        for (double kn : reg.getKnots()) knotsStr.append(IOUtils.format3(kn)).append(" ");

        String fileContent = "Hasil Regresi Spline Kubik\n\n"
                + "Data Titik Sampel:\n" + pointsToString(points) + "\n"
                + "Daftar Batas Segmen (Knot): " + knotsStr + "\n\n"
                + "Persamaan Kurva Regresi:\n" + reg.toFullEquationString() + "\n\n"
                + "Catatan Prediksi:\n" + (evalLog.length() == 0 ? "Tidak ada percobaan prediksi.\n" : evalLog.toString());
        offerSave(sc, fileContent);
    }

    private static int askInputSource(Scanner sc) {
        System.out.println("\nPilih sumber data:");
        System.out.println("1. Ketik langsung via Keyboard");
        System.out.println("2. Baca dari file .txt");
        return IOUtils.readInt(sc, "Pilihan Anda: ");
    }

    private static Matrix obtainSquareMatrix(Scanner sc) {
        int source = askInputSource(sc);
        Matrix m;
        if (source == 1) {
            int n = IOUtils.readInt(sc, "Masukkan ukuran sisi matriks persegi: ");
            m = IOUtils.readMatrixManual(sc, n, n);
        } else if (source == 2) {
            System.out.print("Masukkan lokasi file .txt (contoh: data/matriks.txt): ");
            String path = sc.nextLine().trim();
            m = IOUtils.readMatrixFromFile(path);
        } else {
            throw new MatrixException("Pilihan sumber data tidak valid.");
        }
        if (m.getRows() != m.getCols()) {
            throw new MatrixException("Proses ini membutuhkan matriks persegi, sedangkan matriks Anda berukuran " + m.getRows() + "x" + m.getCols() + ".");
        }
        return m;
    }

    private static Matrix obtainAugmentedMatrix(Scanner sc) {
        int source = askInputSource(sc);
        if (source == 1) {
            int rows = IOUtils.readInt(sc, "Masukkan jumlah persamaan: ");
            int vars = IOUtils.readInt(sc, "Masukkan jumlah variabel bebas: ");
            System.out.println("Silakan masukkan nilai koefisien diikuti dengan hasil persamaannya.");
            return IOUtils.readAugmentedManual(sc, rows, vars);
        } else if (source == 2) {
            System.out.print("Masukkan lokasi file .txt (contoh: data/spl.txt): ");
            String path = sc.nextLine().trim();
            return IOUtils.readMatrixFromFile(path);
        } else {
            throw new MatrixException("Pilihan sumber data tidak valid.");
        }
    }

    private static double[][] obtainPoints(Scanner sc, int maxPoints) {
        int source = askInputSource(sc);
        if (source == 1) {
            int n = IOUtils.readInt(sc, "Masukkan jumlah titik data (maksimal " + maxPoints + " titik): ");
            return IOUtils.readPointsManual(sc, n);
        } else if (source == 2) {
            System.out.print("Masukkan lokasi file .txt (contoh: data/titik.txt): ");
            String path = sc.nextLine().trim();
            return IOUtils.readPointsFromFile(path);
        } else {
            throw new MatrixException("Pilihan sumber data tidak valid.");
        }
    }

    private static String extrapolationNote(double xt, double[] domain) {
        if (xt < domain[0] || xt > domain[1]) {
            return "  [Peringatan: Nilai x berada di luar domain data / Ekstrapolasi]";
        }
        return "";
    }

    private static String pointsToString(double[][] points) {
        StringBuilder sb = new StringBuilder();
        double[] xs = points[0];
        double[] ys = points[1];
        for (int i = 0; i < xs.length; i++) {
            sb.append(IOUtils.format3(xs[i])).append("\t").append(IOUtils.format3(ys[i])).append("\n");
        }
        return sb.toString();
    }

    private static void offerSave(Scanner sc, String content) {
        System.out.print("\nApakah Anda ingin menyimpan hasil ini ke dalam file .txt? (y/n): ");
        String answer = sc.nextLine().trim().toLowerCase();
        if (answer.equals("y") || answer.equals("yes") || answer.equals("ya")) {
            System.out.print("Masukkan nama file untuk menyimpan (contoh: hasil_perhitungan.txt): ");
            String filename = sc.nextLine().trim();
            if (!filename.toLowerCase().endsWith(".txt")) {
                filename += ".txt";
            }
            IOUtils.saveToFile(filename, content);
            System.out.println("Hasil telah berhasil disimpan ke " + filename);
        }
    }
}