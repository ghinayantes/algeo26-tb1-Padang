# Tugas Besar 1<br>IF2123 Aljabar Linier dan Geometri

*Sistem Persamaan Linier, Determinan, dan Penerapannya*<br>
*Semester I Tahun 2026/2027*

Program ini dikembangkan sebagai pemenuhan Tugas Besar 1 mata kuliah IF2123 Aljabar Linier dan Geometri pada Semester I Tahun 2026/2027. Proyek ini menerapkan materi perkuliahan dalam sebuah kalkulator yang dapat menyelesaikan sistem persamaan linier, menghitung determinan dan invers matriks, serta membentuk model interpolasi dan regresi spline dari data titik. Program menyediakan antarmuka grafis (JavaFX) dan antarmuka terminal (CLI), dengan hasil perhitungan yang dapat ditampilkan beserta langkahnya dan dapat disimpan sebagai berkas teks `.txt`.

## Anggota Kelompok

| Nama | NIM |
|---|---|
| Ghina Emelia Yantes | 13525119 |
| Sahla Nailah Salsabilla | 13525134 |
| Nayla Putri Ghaisani | 13525140 |

## Fitur

- **Sistem Persamaan Linier (SPL):** eliminasi Gauss, Gauss-Jordan, metode matriks balikan, dan kaidah Cramer.
- **Determinan:** reduksi baris (OBE) atau ekspansi kofaktor.
- **Matriks balikan:** augmentasi `[A | I]` dengan Gauss-Jordan atau metode adjoin.
- **Interpolasi polinomial:** membentuk polinomial yang melalui semua titik data.
- **Interpolasi spline kubik natural:** membentuk kurva kubik per segmen dengan syarat natural pada kedua ujung.
- **Regresi spline kubik:** membentuk kurva pendekatan kuadrat terkecil dengan pilihan knot.
- **Grafik dan prediksi:** tersedia pada antarmuka grafis untuk ketiga fitur kurva.
- **Baca dan simpan berkas `.txt`:** muat data masukan dari berkas dan simpan hasil perhitungan.

## Kebutuhan

- Java Development Kit (JDK) 21 atau lebih baru.
- Apache Maven 3.6.3 atau lebih baru.
- Lingkungan desktop dengan dukungan JavaFX untuk menjalankan antarmuka grafis.

Periksa instalasi Java dan Maven:

```bash
java --version
mvn --version
```

## Menjalankan program

Jalankan perintah dari direktori utama proyek.

### Antarmuka grafis (JavaFX)

```bash
mvn javafx:run
```

Jendela kalkulator akan terbuka. Gunakan navigasi di sisi kiri untuk memilih fitur.

### Antarmuka terminal (CLI)

```bash
mvn exec:java
```

Ikuti menu bernomor yang ditampilkan di terminal. Untuk keluar, pilih **7. Keluar**.

### Kompilasi dan pengemasan

```bash
mvn clean compile
mvn clean package
```

Perintah pertama menghapus hasil build sebelumnya lalu mengompilasi kode. Perintah kedua mengompilasi dan mengemas proyek; hasil build berada di direktori `target/`.

## Alur penggunaan

1. Jalankan program dengan antarmuka grafis atau CLI.
2. Pilih fitur yang ingin digunakan. Untuk SPL, determinan, invers, dan regresi, pilih metode atau parameter yang tersedia.
3. Masukkan data secara langsung atau muat dari berkas `.txt`.
4. Jalankan perhitungan. Untuk interpolasi dan regresi, masukkan nilai `x` untuk melihat nilai taksiran; antarmuka grafis juga menampilkan kurva.
5. Periksa hasil dan langkah perhitungan, lalu simpan hasil ke berkas `.txt` bila diperlukan.

## Format berkas masukan

Berkas masukan berupa teks biasa. Setiap baris berisi angka yang dipisahkan oleh satu atau lebih spasi atau tab. Gunakan titik (`.`) sebagai pemisah desimal dan jangan menyisipkan baris kosong di antara data.

### Matriks

Satu baris berisi satu baris matriks. Semua baris harus memiliki jumlah angka yang sama. Untuk SPL, kolom terakhir adalah konstanta pada ruas kanan (matriks augmented). Untuk determinan dan invers, masukkan matriks persegi.

Contoh matriks augmented SPL:

```text
2 1 5
1 -1 1
```

### Data titik

Satu baris berisi pasangan `x y`.

```text
0 1
1 2.5
2 5
```

Data contoh tersedia di direktori [`test/`](test/). Antarmuka grafis menyediakan tombol untuk memilih berkas `.txt`; pada CLI, pilih sumber data dari berkas lalu masukkan jalur berkasnya.

## Struktur proyek secara garis besar

```text
.
├── src/
│   └── main/
│       ├── java/algeo/                     # Kode sumber Java
│       └── resources/                      # Gaya, font, dan aset gambar
├── test/                                   # Contoh berkas data masukan
├── docs/                                   # Dokumen proyek
├── pom.xml                                 # Konfigurasi Maven dan JavaFX
└── README.md
```

Kode utama berada di `src/main/java/algeo`. `App.java` menjalankan antarmuka terminal dan `GuiApp.java` menjalankan antarmuka JavaFX. Paket `matrix`, `spl`, `determinant`, `inverse`, `interpolation`, dan `spline` menangani operasi numerik; paket `gui` mengatur komponen visual dan `io` menangani masukan serta keluaran.

## Struktur proyek lengkap

```text
.
├── README.md
├── docs/
│   └── .gitkeep
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── algeo/
│       │       ├── App.java
│       │       ├── GuiApp.java
│       │       ├── common/
│       │       │   └── PointSetUtils.java
│       │       ├── determinant/
│       │       │   ├── DeterminantCofactor.java
│       │       │   └── DeterminantOBE.java
│       │       ├── gui/
│       │       │   ├── CurvePage.java
│       │       │   ├── FileDialogs.java
│       │       │   ├── MatrixPage.java
│       │       │   ├── PlotView.java
│       │       │   ├── PointsEditor.java
│       │       │   └── Theme.java
│       │       ├── interpolation/
│       │       │   └── PolynomialInterpolation.java
│       │       ├── inverse/
│       │       │   ├── InverseAdjoint.java
│       │       │   └── InverseAugmented.java
│       │       ├── io/
│       │       │   └── IOUtils.java
│       │       ├── matrix/
│       │       │   ├── Matrix.java
│       │       │   └── MatrixException.java
│       │       ├── spl/
│       │       │   ├── Cramer.java
│       │       │   ├── Gauss.java
│       │       │   ├── GaussJordan.java
│       │       │   ├── InverseSPL.java
│       │       │   └── SPLSolver.java
│       │       └── spline/
│       │           ├── CubicSplineRegression.java
│       │           └── NaturalCubicSpline.java
│       └── resources/
│           ├── algeo/
│           │   └── gui/
│           │       ├── fonts/
│           │       │   ├── Poppins-Bold.ttf
│           │       │   ├── Poppins-Regular.ttf
│           │       │   └── Poppins-SemiBold.ttf
│           │       └── theme.css
│           ├── itb + stei.png
│           ├── itb.png
│           └── stei.png
└── test/
    ├── det_kasus1.txt
    ├── det_kasus2.txt
    ├── det_kasus3.txt
    ├── interpolasi_cubicspline.txt
    ├── interpolasi_polinomial1.txt
    ├── interpolasi_polinomial2.txt
    ├── interpolasi_polinomial3.txt
    ├── inverse_kasus1.txt
    ├── inverse_kasus2.txt
    ├── regresi_kasus1.txt
    ├── spl_aplikasi.txt
    ├── spl_augmented1.txt
    ├── spl_augmented2.txt
    ├── spl_kasus1.txt
    ├── spl_kasus2.txt
    ├── spl_kasus3.txt
    ├── spl_kasus4.txt
    ├── spl_umum1.txt
    └── spl_umum2.txt
```

> Direktori `.github/modernize/java-upgrade/` juga menyimpan konfigurasi dan log otomatis dari proses peningkatan versi Java.
