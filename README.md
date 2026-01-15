<div align ="center">
  
 # AttendTrack

**Aplikasi Absensi Berbasis Lokasi (Android)**

AttendTrack adalah aplikasi absensi berbasis lokasi yang dikembangkan menggunakan Android dan Jetpack Compose. Aplikasi ini memungkinkan pengguna melakukan absensi hanya apabila berada dalam radius lokasi yang telah ditentukan, sehingga meningkatkan akurasi dan mengurangi kecurangan absensi.
</div>

---

## 📱 Fitur Utama
- **Login & Register Pengguna**
  - Menggunakan Firebase Authentication
- **Absensi Berbasis Lokasi**
  - Validasi absensi menggunakan GPS
  - Absensi hanya dapat dilakukan dalam radius lokasi tertentu
- **Status Kehadiran**
  - Menampilkan status hadir/belum hadir pada hari berjalan
- **Riwayat Absensi**
  - Menampilkan riwayat absensi pengguna
- **Rekap Absensi Harian**
  - Menampilkan daftar seluruh user yang telah absen
- **Reset Absensi**
  - Menghapus data absensi harian agar pengguna dapat absen kembali

---

## 🧭 Alur Singkat Aplikasi
1. Pengguna melakukan **Login / Register**
2. Sistem memverifikasi akun melalui **Firebase Authentication**
3. Pengguna masuk ke halaman **Home**
4. Pengguna melakukan **Absensi** dengan validasi lokasi (GPS)
5. Data absensi disimpan di **Firebase Cloud Firestore**
6. Admin dapat melihat **Rekap Absensi** dan melakukan **Reset Absen**

---

## 🛠 Teknologi yang Digunakan
- **Platform**: Android
- **Bahasa Pemrograman**: Kotlin
- **UI Framework**: Jetpack Compose (Material 3)
- **Autentikasi**: Firebase Authentication
- **Database**: Firebase Cloud Firestore
- **Lokasi**: Google Play Services – Fused Location Provider
- **Version Control**: Git & GitHub

---

