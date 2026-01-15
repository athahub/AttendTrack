package com.example.absensiapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.absensiapp.utils.distanceMeter
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Lokasi(
    val nama: String,
    val lat: Double,
    val lon: Double
)

@Composable
fun AbsenScreen() {

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val fusedLocationClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    val user = auth.currentUser ?: return
    val uid = user.uid

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var nama by remember { mutableStateOf("") }
    var jarakTerdekat by remember { mutableStateOf<Float?>(null) }
    var lokasiTerdekat by remember { mutableStateOf<String?>(null) }
    var sudahAbsen by remember { mutableStateOf(false) }
    var riwayat by remember { mutableStateOf<List<String>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }

    val radiusValid = 100f

    val lokasiList = listOf(
        Lokasi("Kampus ITB Nobel", -5.1787331870276825, 119.43902492523193)
    )

    LaunchedEffect(Unit) {
        try {
            val userSnap = db.collection("users").document(uid).get().await()
            nama = userSnap.getString("nama") ?: "User"

            val historySnap = db.collection("absen")
                .document(uid)
                .collection("records")
                .orderBy("waktu")
                .get()
                .await()

            riwayat = historySnap.documents.map { it.id }.reversed()
            sudahAbsen = riwayat.contains(today)

            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                var min = Float.MAX_VALUE
                var nearest: String? = null
                lokasiList.forEach {
                    val d = distanceMeter(
                        location.latitude,
                        location.longitude,
                        it.lat,
                        it.lon
                    )
                    if (d < min) {
                        min = d
                        nearest = it.nama
                    }
                }
                jarakTerdekat = min
                lokasiTerdekat = nearest
            }
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Absensi Hari Ini",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = nama,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                if (loading) {
                    CircularProgressIndicator()
                } else {

                    jarakTerdekat?.let { jarak ->
                        Text(
                            text = "Lokasi terdekat: $lokasiTerdekat",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Jarak: ${jarak.toInt()} meter",
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    if (sudahAbsen) {
                        Text(
                            text = "✔ Sudah Hadir Bos!",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    } else {

                        val dalamRadius =
                            jarakTerdekat != null && jarakTerdekat!! <= radiusValid

                        Button(
                            onClick = {
                                if (!dalamRadius) return@Button
                                db.collection("absen")
                                    .document(uid)
                                    .collection("records")
                                    .document(today)
                                    .set(
                                        mapOf(
                                            "status" to "HADIR",
                                            "lokasi" to lokasiTerdekat,
                                            "jarak" to jarakTerdekat,
                                            "waktu" to FieldValue.serverTimestamp()
                                        )
                                    )
                                    .addOnSuccessListener {
                                        Toast.makeText(
                                            context,
                                            "Absen berhasil",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        sudahAbsen = true
                                        riwayat = listOf(today) + riwayat
                                    }
                            },
                            enabled = dalamRadius,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Absen Sekarang")
                        }

                        if (!dalamRadius) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Di luar radius absensi",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Riwayat Absensi",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        if (riwayat.isEmpty()) {
            Text(
                text = "Belum ada riwayat",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            riwayat.forEach {
                Text(
                    text = it,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
