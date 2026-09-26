package algeo.common;

import algeo.matrix.MatrixException;

public class PointSetUtils {

    private static final double EPSILON = 1e-9;

    private PointSetUtils() {
    }

    // wadah x dan y yang sudah terurut
    public static class SortedPoints {
        public final double[] x;
        public final double[] y;

        public SortedPoints(double[] x, double[] y) {
            this.x = x;
            this.y = y;
        }
    }

    // validasi input dan urutin titik berdasarkan x menaik
    public static SortedPoints validateAndSort(double[] xPoints, double[] yPoints, int maxPoints) {

        // cek null dan panjang array 
        if (xPoints == null || yPoints == null || xPoints.length != yPoints.length) {
            throw new MatrixException("Jumlah titik x dan y harus sama dan tidak boleh null.");
        }
        // minimal 
        if (xPoints.length < 2) {
            throw new MatrixException("Minimal dibutuhkan 2 titik data.");
        }
        // tidak lebih dri max
        if (xPoints.length > maxPoints) {
            throw new MatrixException("Jumlah titik data melebihi batas maksimal (" + maxPoints + " titik).");
        }

        int m = xPoints.length;

        // sorting selection sort 
        double[] sortedX = xPoints.clone();
        double[] sortedY = yPoints.clone();
        for (int i = 0; i < m - 1; i++) {
            int minIdx = i;
            for (int j = i + 1; j < m; j++) {
                if (sortedX[j] < sortedX[minIdx]) minIdx = j;
            }
            double tx = sortedX[i]; sortedX[i] = sortedX[minIdx]; sortedX[minIdx] = tx;
            double ty = sortedY[i]; sortedY[i] = sortedY[minIdx]; sortedY[minIdx] = ty;
        }

        // Cek kalau ada x yang duplikat
        for (int i = 0; i < m - 1; i++) {
            if (Math.abs(sortedX[i + 1] - sortedX[i]) < EPSILON) {
                throw new MatrixException("Terdapat nilai x yang duplikat pada titik data: x = " + sortedX[i]);
            }
        }

        return new SortedPoints(sortedX, sortedY);
    }
}