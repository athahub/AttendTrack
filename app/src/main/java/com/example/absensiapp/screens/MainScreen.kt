package com.example.absensiapp.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import androidx.compose.material.icons.filled.List


@Composable
fun MainScreen(
    rootNavController: NavController
) {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        val snap = db.collection("users").document(uid).get().await()
        isAdmin = snap.getString("role") == "admin"
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = currentRoute == "home",
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = currentRoute == "absen",
                    onClick = { navController.navigate("absen") },
                    icon = { Icon(Icons.Default.CheckCircle, null) },
                    label = { Text("Absen") }
                )

                NavigationBarItem(
                    selected = currentRoute == "rekap",
                    onClick = {
                        navController.navigate("rekap") {
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.List, null) },
                    label = { Text("Rekap") }
                )


                if (isAdmin) {
                    NavigationBarItem(
                        selected = currentRoute == "admin",
                        onClick = { navController.navigate("admin") },
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("Admin") }
                    )
                }
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {

            composable("home") {
                HomeScreen(
                    onLogout = {
                        auth.signOut()
                        rootNavController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable("absen") {
                AbsenScreen()
            }
            composable("rekap") {
                RekapAbsenScreen()
            }


            if (isAdmin) {
                composable("admin") {
                    AdminScreen()
                }
            }
        }
    }
}
