package com.example.absensiapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.absensiapp.screens.LoginScreen
import com.example.absensiapp.screens.MainScreen
import com.example.absensiapp.screens.RegisterScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable("main") {
            MainScreen(rootNavController = navController)
        }
    }
}
