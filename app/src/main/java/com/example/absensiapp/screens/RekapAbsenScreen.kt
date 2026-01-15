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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RekapAbsen(
    val uid: String,
    val nama: String
)

@Composable
fun RekapAbsenScreen() {

    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    val today = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    var loading by remember { mutableStateOf(true) }
    var list by remember { mutableStateOf<List<RekapAbsen>>(emptyList()) }

    suspend fun loadData() {
        loading = true
        val hasil = mutableListOf<RekapAbsen>()

        val usersSnap = db.collection("users").get().await()
        for (user in usersSnap.documents) {
            val uid = user.id
            val nama = user.getString("nama") ?: "Unknown"

            val absenDoc = db.collection("absen")
                .document(uid)
                .collection("records")
                .document(today)
                .get()
                .await()

            if (absenDoc.exists()) {
                hasil.add(RekapAbsen(uid, nama))
            }
        }

        list = hasil
        loading = false
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "User Yang Sudah Absen Hari Ini",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                scope.launch {
                    list.forEach {
                        db.collection("absen")
                            .document(it.uid)
                            .collection("records")
                            .document(today)
                            .delete()
                    }

                    Toast.makeText(
                        context,
                        "Absen hari ini berhasil di-reset",
                        Toast.LENGTH_SHORT
                    ).show()

                    loadData()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("RESET ABSEN HARI INI")
        }

        Spacer(Modifier.height(16.dp))

        if (loading) {
            CircularProgressIndicator()
        } else if (list.isEmpty()) {
            Text(
                text = "Belum ada user yang absen",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(list) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Text(
                            text = item.nama,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
