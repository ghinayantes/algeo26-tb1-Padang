package algeo.io;

import algeo.matrix.Matrix;
import algeo.matrix.MatrixException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class IOUtils {

    private IOUtils() {
    }

    // menyesuaikan tanda koma dan minus
    public static double parseDouble(String raw) {
        String cleaned = raw.trim()
                            .replace(',', '.')
                            .replaceAll("[\u2012\u2013\u2014\u2015\u2212]", "-");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            throw new MatrixException("Input '" + raw + "' bukan angka yang valid.");
        }
    }

    // membulatkan angka ke maksimal tiga desimal 
    public static String format3(double value) {
        double rounded = Math.round(value * 1000.0) / 1000.0;
        if (rounded == 0.0) rounded = 0.0;
        return String.format("%.3f", rounded);
    }

    // konversi ke string
    public static String matrixToString(Matrix m) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.getRows(); i++) {
            for (int j = 0; j < m.getCols(); j++) {
                if (j > 0) sb.append("\t");
                sb.append(format3(m.get(i, j)));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // mencetak matriks
    public static void printMatrix(Matrix m) {
        System.out.print(matrixToString(m));
    }

    // membaca input 
    public static Matrix readMatrixManual(Scanner sc, int rows, int cols) {
        Matrix m = new Matrix(rows, cols);
        System.out.println("Masukkan elemen matriks baris per baris (pisahkan dengan spasi):");
        for (int i = 0; i < rows; i++) {
            System.out.print("Baris " + (i + 1) + ": ");
            String line = sc.nextLine().trim();
            
            // abaikan masukan baris kosong yang tidak disengaja
            while (line.isEmpty()) {
                line = sc.nextLine().trim();
            }
            
            String[] tokens = line.split("\\s+");
            if (tokens.length != cols) {
                throw new MatrixException("Baris " + (i + 1) + " harus punya " + cols + " elemen. Tadi ada " + tokens.length + ".");
            }
            for (int j = 0; j < cols; j++) {
                m.set(i, j, parseDouble(tokens[j]));
            }
        }
        return m;
    }

    // membaca matriks augmented 
    public static Matrix readAugmentedManual(Scanner sc, int rows, int vars) {
        return readMatrixManual(sc, rows, vars + 1);
    }

    // membaca input n pasang titik koordinat
    public static double[][] readPointsManual(Scanner sc, int n) {
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            System.out.print("Titik ke-" + (i + 1) + " (format: x y): ");
            String[] tokens = sc.nextLine().trim().split("\\s+");
            if (tokens.length != 2) {
                throw new MatrixException("Format salah! Harus ada 'x y' (2 angka). Ketemu: " + tokens.length + " angka.");
            }
            xs[i] = parseDouble(tokens[0]);
            ys[i] = parseDouble(tokens[1]);
        }
        return new double[][] { xs, ys };
    }

    // membaca data matriks dari file teks
    public static Matrix readMatrixFromFile(String path) {
        List<String> lines = readNonEmptyLines(path);
        int rows = lines.size();
        if (rows == 0) {
            throw new MatrixException("File '" + path + "' kosong / tidak ada matriksnya.");
        }
        String[] firstTokens = lines.get(0).trim().split("\\s+");
        int cols = firstTokens.length;

        Matrix m = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            String[] tokens = lines.get(i).trim().split("\\s+");
            if (tokens.length != cols) {
                throw new MatrixException("Format berantakan! Baris " + (i + 1)
                        + " punya " + tokens.length + " angka, padahal harusnya " + cols + ".");
            }
            for (int j = 0; j < cols; j++) {
                m.set(i, j, parseDouble(tokens[j]));
            }
        }
        return m;
    }

    // membaca pasangan titik koordinat dari file txt
    public static double[][] readPointsFromFile(String path) {
        List<String> lines = readNonEmptyLines(path);
        int n = lines.size();
        if (n == 0) {
            throw new MatrixException("File '" + path + "' tidak ada data titiknya.");
        }
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            String[] tokens = lines.get(i).trim().split("\\s+");
            if (tokens.length != 2) {
                throw new MatrixException("Baris " + (i + 1) + " salah format! Cuma boleh isi 'x y' (2 angka).");
            }
            xs[i] = parseDouble(tokens[0]);
            ys[i] = parseDouble(tokens[1]);
        }
        return new double[][] { xs, ys };
    }

    // membaca isi file dengan mengabaikan baris kosong di bagian paling akhir
    private static List<String> readNonEmptyLines(String path) {
        List<String> raw;
        try {
            raw = Files.readAllLines(Paths.get(path));
        } catch (FileNotFoundException | java.nio.file.NoSuchFileException e) {
            throw new MatrixException("File '" + path + "' tidak ketemu.");
        } catch (IOException e) {
            throw new MatrixException("Gagal buka file '" + path + "': " + e.getMessage());
        }

        // hapus sisa baris kosong di bagian paling bawah file
        int end = raw.size();
        while (end > 0 && raw.get(end - 1).trim().isEmpty()) {
            end--;
        }

        List<String> result = new ArrayList<>();
        for (int i = 0; i < end; i++) {
            String line = raw.get(i);
            if (line.trim().isEmpty()) {
                throw new MatrixException("Ada baris kosong nyelip di baris ke-" + (i + 1) + ". Format rusak!");
            }
            result.add(line);
        }
        return result;
    }

    // menyimpan string konten ke dalam file teks
    public static void saveToFile(String path, String content) {
        try (PrintWriter writer = new PrintWriter(path)) {
            writer.print(content);
        } catch (IOException e) {
            throw new MatrixException("Gagal nyimpen ke file '" + path + "': " + e.getMessage());
        }
    }

    // membaca input bilangan bulat dari keyboard beserta validasinya
    public static int readInt(Scanner sc, String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            throw new MatrixException("'" + line + "' bukan angka bilangan bulat (int) yang benar.");
        }
    }
}