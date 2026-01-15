package com.example.absensiapp.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// =====================
// MODEL DATA ADMIN
// =====================
data class RiwayatAbsenAdmin(
    val uid: String,
    val nama: String,
    val tanggal: String,
    val lokasi: String?,
    val jarak: Float?
)

@Composable
fun AdminScreen() {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var riwayat by remember { mutableStateOf<List<RiwayatAbsenAdmin>>(emptyList()) }

    // =====================
    // LOAD SEMUA RIWAYAT ABSEN
    // =====================
    suspend fun loadRiwayat() {
        loading = true
        val hasil = mutableListOf<RiwayatAbsenAdmin>()

        val usersSnap = db.collection("users").get().await()

        for (user in usersSnap.documents) {
            val uid = user.id
            val nama = user.getString("nama") ?: "Unknown"

            val absenSnap = db.collection("absen")
                .document(uid)
                .collection("records")
                .get()
                .await()

            for (record in absenSnap.documents) {
                hasil.add(
                    RiwayatAbsenAdmin(
                        uid = uid,
                        nama = nama,
                        tanggal = record.id,
                        lokasi = record.getString("lokasi"),
                        jarak = record.getDouble("jarak")?.toFloat()
                    )
                )
            }
        }

        riwayat = hasil.sortedByDescending { it.tanggal }
        loading = false
    }

    // Load awal
    LaunchedEffect(Unit) {
        loadRiwayat()
    }

    // =====================
    // UI
    // =====================
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Riwayat Absensi Semua User",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        // =====================
        // TOMBOL RESET ABSEN
        // =====================
        Button(
            onClick = {
                scope.launch {
                    riwayat.forEach { item ->
                        db.collection("absen")
                            .document(item.uid)
                            .collection("records")
                            .document(item.tanggal)
                            .delete()
                    }

                    Toast.makeText(
                        context,
                        "Semua data absensi di-reset",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadRiwayat()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("RESET SEMUA ABSEN")
        }

        Spacer(Modifier.height(16.dp))

        // =====================
        // LIST RIWAYAT
        // =====================
        if (loading) {
            CircularProgressIndicator()
        } else if (riwayat.isEmpty()) {
            Text(
                text = "Belum ada user yang absen",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(riwayat) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(item.nama, fontWeight = FontWeight.Bold)
                            Text("Tanggal: ${item.tanggal}")
                            Text("Lokasi: ${item.lokasi ?: "-"}")
                            Text(
                                "Jarak: ${item.jarak?.toInt() ?: 0} meter",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
