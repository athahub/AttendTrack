package com.example.absensiapp.screens


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    val user = auth.currentUser
    val uid = user?.uid ?: return

    var nama by remember { mutableStateOf("") }
    var sudahAbsen by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    val today = LocalDate.now().toString()

    LaunchedEffect(Unit) {
        try {
            // Ambil nama user
            val userSnap = db.collection("users").document(uid).get().await()
            nama = userSnap.getString("nama") ?: "User"

            // Cek status absen hari ini
            val absenSnap = db.collection("absen")
                .document(uid)
                .collection("records")
                .get()
                .await()

            sudahAbsen = absenSnap.documents.any { it.id == today }
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        when {
            loading -> {
                CircularProgressIndicator()
            }

            else -> {
                Text(
                    text = "Selamat Datang",
                    fontSize = 16.sp
                )

                Text(
                    text = nama,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Status Kehadiran Hari Ini")
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (sudahAbsen) "✔ Sudah Hadir" else "❌ Belum Hadir",
                            color = if (sudahAbsen) Color(0xFF2E7D32) else Color.Red,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = {
                        auth.signOut()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
            }
        }
    }
}
