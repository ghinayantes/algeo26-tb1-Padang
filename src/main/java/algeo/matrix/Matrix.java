package algeo.matrix;

public class Matrix {
    //private agar ukurannya tidak bisa diubah diluar class
    private int rows;
    private int cols;
    private double[][] spl; //elemen matrix desimal, array of array

    //CONSTRUCTOR

    //Membuat matriks baru berukuran rows x cols
    public Matrix(int rows, int cols) {
        if (rows <= 0 || cols <= 0) {
            throw new MatrixException("Dimensi matriks harus lebih besar dari 0."); //Exception Handling make Unchecked Exception (extends di MatrixException.java)
        }
        this.rows = rows;
        this.cols = cols;
        this.spl = new double[rows][cols];
    }

    //Membuat matriks dari array 2D
    //spl.length = banyak baris
    //spl[0].length = banyak kolom di baris pertama
    public Matrix(double[][] spl) {
        if (spl == null || spl.length == 0 || spl[0].length == 0) {
            throw new MatrixException("Data matriks tidak boleh kosong.");
        }
        this.rows = spl.length;
        this.cols = spl[0].length;
        this.spl = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            if (spl[i].length != cols) {
                throw new MatrixException("Setiap baris matriks harus memiliki jumlah kolom yang sama.");
            }
            System.arraycopy(spl[i], 0, this.spl[i], 0, cols);
            //(array asal, indeks mulai di array asal, array tujuan, index mulai di array tujuan, jumlah kolomm yang disalin)
        }
    }

    //VALIDASI INDEX

    private void validateRowIndex(int row) {
        if (row < 0 || row >= rows) {
            throw new MatrixException("Index baris " + row + " di luar batas (0 - " + (rows - 1) + ").");
        }
    }

    private void validateColIndex(int col) {
        if (col < 0 || col >= cols) {
            throw new MatrixException("Index kolom " + col + " di luar batas (0 - " + (cols - 1) + ").");
        }
    }

    //GETTER
    public int getRows() {
        return this.rows;
    }

    public int getCols() {
        return this.cols;
    }

    //Mengambil elemen
    public double get(int row, int col) {
        validateRowIndex(row);
        validateColIndex(col);
        return this.spl[row][col];
    }

    //Mengambil 1 baris matriks sebagai array 1D
    public double[] getRow(int rowIndex) {
        validateRowIndex(rowIndex);
        double[] row = new double[cols]; //size baru sebesar banyak cols di rowIndex
        System.arraycopy(this.spl[rowIndex], 0, row, 0, cols);
        return row;
    }

    //Mengambil 1 kolom matriks sebagai array 1D
    public double[] getColumn(int colIndex) {
        validateColIndex(colIndex);
        double[] col = new double[rows]; //size baru sebesar banyak rows
        for (int i = 0; i < rows; i++) {
            col[i] = this.spl[i][colIndex];
        }
        return col;
    }

    //SETTER

    //Mengubah nilai elemen
    public void set(int row, int col, double element) {
        validateRowIndex(row);
        validateColIndex(col);
        this.spl[row][col] = element;
    }

    //Mengubah nilai 1 baris penuh menggunakan array 1D
    public void setRow(int rowIndex, double[] newRow) {
        validateRowIndex(rowIndex);
        if (newRow.length != cols) {
            throw new MatrixException("Panjang baris baru tidak sesuai dengan jumlah kolom matriks awal.");
        }
        System.arraycopy(newRow, 0, this.spl[rowIndex], 0, cols);
    }

    //Mengubah/mengatur nilai 1 kolom penuh
    public void setColumn(int colIndex, double[] newCol) {
        validateColIndex(colIndex);
        if (newCol.length != rows) {
            throw new MatrixException("Panjang array kolom tidak sesuai dengan jumlah baris matriks.");
        }
        for (int i = 0; i < rows; i++) {
            this.spl[i][colIndex] = newCol[i];
        }
    }

    //OPERASI DASAR

    //Menyalin matriks
    public Matrix copy() {
        Matrix newMatrix = new Matrix(this.rows, this.cols);
        for (int i = 0; i < this.rows; i++) {
            System.arraycopy(this.spl[i], 0, newMatrix.spl[i], 0, this.cols);
        }
        return newMatrix;
    }

    //Menukar dua baris
    public void swapRows(int row1, int row2) {
        validateRowIndex(row1);
        validateRowIndex(row2);
        if (row1 != row2) {
            double[] temp = this.spl[row1];
            this.spl[row1] = this.spl[row2];
            this.spl[row2] = temp;
        }
    }

    //Penjumlahan Matriks
    public Matrix add(Matrix m) {
        if (this.rows != m.rows || this.cols != m.cols) {
            throw new MatrixException("Dimensi matriks harus sama untuk operasi penjumlahan.");
        }
        Matrix result = new Matrix(this.rows, this.cols);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                result.set(i, j, this.spl[i][j] + m.get(i, j));
            }
        }
        return result;
    }

    //Pengurangan Matriks
    public Matrix subtract(Matrix m) {
        if (this.rows != m.rows || this.cols != m.cols) {
            throw new MatrixException("Dimensi matriks harus sama untuk operasi pengurangan.");
        }
        Matrix result = new Matrix(this.rows, this.cols);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                result.set(i, j, this.spl[i][j] - m.get(i, j));
            }
        }
        return result;
    }

    //Perkalian Matriks dengan Skalar
    public Matrix multiplyByScalar(double scalar) {
        Matrix result = new Matrix(this.rows, this.cols);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                result.set(i, j, this.spl[i][j] * scalar);
            }
        }
        return result;
    }

    //Perkalian Dua Matriks
    public Matrix multiply(Matrix m) {
        if (this.cols != m.rows) {
            throw new MatrixException("Ukuran kolom matriks pertama harus sama dengan ukuran baris matriks kedua.");
        }
        Matrix result = new Matrix(this.rows, m.cols);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                double sum = 0;
                for (int k = 0; k < this.cols; k++) {
                    sum += this.spl[i][k] * m.get(k, j);
                }
                result.set(i, j, sum);
            }
        }
        return result;
    }

    //Transpose Matriks
    public Matrix transpose() {
        Matrix transposed = new Matrix(this.cols, this.rows);
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                transposed.set(j, i, this.spl[i][j]);
            }
        }
        return transposed;
    }

    //Membentuk submatrix dengan menghapus excludeRow dan excludeCol
    public Matrix getSubmatrix(int excludeRow, int excludeCol) {
        validateRowIndex(excludeRow);
        validateColIndex(excludeCol);
        if (this.rows <= 1 || this.cols <= 1) {
            throw new MatrixException("Tidak bisa membentuk submatrix dari matriks berukuran 1xN atau Nx1.");
        }
        Matrix submatrix = new Matrix(this.rows - 1, this.cols - 1);
        int row = 0;
        for (int i = 0; i < this.rows; i++) {
            if (i == excludeRow) continue;
            int col = 0;
            for (int j = 0; j < this.cols; j++) {
                if (j == excludeCol) continue;
                submatrix.set(row, col, this.spl[i][j]);
                col++; //Naikkan indeks kolom setelah mengisi
            }
            row++; //Naikkan indeks baris setelah 1 baris selesai diisi
        }
        return submatrix;
    }

    //ELIMINASI / PIVOTING

    //Batas toleransi perbandingan double (untuk equals, hashCode, dan pengecekan pivot nol)
    private static final double EPSILON = 1e-9;

    //Mencari indeks baris dengan |elemen| terbesar pada kolom col, mulai dari startRow ke bawah
    public int getPivotRow(int startRow, int col) {
        validateRowIndex(startRow);
        validateColIndex(col);
        int pivotRow = startRow;
        double maxAbs = Math.abs(this.spl[startRow][col]);
        for (int i = startRow + 1; i < this.rows; i++) {
            double absVal = Math.abs(this.spl[i][col]);
            if (absVal > maxAbs) {
                maxAbs = absVal;
                pivotRow = i;
            }
        }
        return pivotRow;
    }

    //Partial pivoting: menukar baris startRow dengan baris ber-|elemen| terbesar pada kolom col.
    //Setelah swap, baris pivot (nilai absolut terbesar) berada di startRow.
    //Return value: indeks ASAL baris yang ditukar ke startRow (sebelum swap) - berguna untuk
    //tracking permutasi/perubahan tanda determinant, BUKAN posisi pivot setelah swap (itu = startRow).
    public int partialPivot(int startRow, int col) {
        int pivotRow = getPivotRow(startRow, col);
        if (Math.abs(this.spl[pivotRow][col]) <= EPSILON) {
            throw new MatrixException("Tidak ada elemen pivot yang tak nol pada kolom " + col
                    + " mulai dari baris " + startRow + ".");
        }
        swapRows(startRow, pivotRow);
        return pivotRow;
    }

    //OVERRIDE UNTUK TESTING/DEBUGGING

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                if (j > 0) sb.append("\t");
                sb.append(String.format("%.6g", this.spl[i][j]));
            }
            if (i < this.rows - 1) sb.append("\n");
        }
        return sb.toString();
    }

    //Membandingkan dua matriks elemen demi elemen dengan toleransi EPSILON
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Matrix m = (Matrix) obj;
        if (this.rows != m.rows || this.cols != m.cols) return false;
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                if (Math.abs(this.spl[i][j] - m.spl[i][j]) > EPSILON) return false;
            }
        }
        return true;
    }

    //Hash dibuat toleran terhadap EPSILON yang sama dengan equals(), supaya dua matrix
    //yang equals() == true juga PASTI punya hashCode() yang sama (kontrak wajib Java).
    //Nilai dibulatkan ke kelipatan EPSILON sebelum di-hash, bukan pakai bit exact double.
    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < this.rows; i++) {
            for (int j = 0; j < this.cols; j++) {
                long rounded = Math.round(this.spl[i][j] / EPSILON);
                result = 31 * result + (int) (rounded ^ (rounded >>> 32));
            }
        }
        result = 31 * result + this.rows;
        result = 31 * result + this.cols;
        return result;
    }
}