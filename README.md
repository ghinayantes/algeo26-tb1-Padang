# Aljabar Linier dan Geometri Tubes 1 Template

Template awal Tugas Besar 1 IF2123 Aljabar Linier dan Geometri Semester I 2026/2027. Proyek menggunakan Java, Maven, dan JavaFX.

Ruang lingkup tugas meliputi sistem persamaan linier, determinan, matriks balikan, interpolasi polinomial, interpolasi splina kubik natural, dan regresi splina kubik. Ketentuan lengkap mengikuti dokumen spesifikasi tugas besar.

## Requirements

- Java 17 atau lebih baru
- Maven 3.6.3 atau lebih baru

Periksa instalasi dengan perintah berikut.

```bash
java --version
mvn --version
```

## Struktur direktori

```text
.
├── bin
├── docs
├── src
│   └── main
│       └── java
│           └── algeo
├── test
├── pom.xml
└── README.md
```

Kode program diletakkan di dalam `src/main/java/algeo`. Kelas utama program adalah `algeo.App`.

- `bin`: berkas hasil kompilasi atau JAR final
- `docs`: laporan tugas besar
- `src`: kode sumber program
- `test`: berkas kasus uji

## Menjalankan program

Kompilasi proyek:

```bash
mvn clean compile
```

Jalankan program CLI:

```bash
mvn exec:java
```

Buat berkas JAR:

```bash
mvn clean package
```

Berkas JAR akan tersedia di dalam direktori `target`.

Untuk menggunakan JavaFX, sesuaikan kelas `App.java`, kemudian jalankan:

```bash
mvn clean javafx:run
```

Lengkapi kembali README kelompok dengan deskripsi program, alur penggunaan, dan cara menjalankan program sebelum pengumpulan.