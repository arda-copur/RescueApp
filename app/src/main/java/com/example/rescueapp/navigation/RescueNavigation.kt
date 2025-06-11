package com.example.rescueapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.rescueapp.ui.screens.EmergencyScreen
import com.example.rescueapp.ui.screens.contacts.ContactsScreen
import com.example.rescueapp.ui.screens.home.HomeScreen
import com.example.rescueapp.ui.screens.onboarding.OnboardingScreen
import com.example.rescueapp.ui.screens.routes.RoutesScreen
import com.example.rescueapp.ui.screens.splash.SplashScreen

@Composable
fun RescueNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToOnboarding = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToEmergency = { navController.navigate("emergency") },
                onNavigateToContacts = { navController.navigate("contacts") },
                onNavigateToRoutes = { navController.navigate("routes") }
            )
        }

        composable("emergency") {
            EmergencyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("contacts") {
            ContactsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("routes") {
            RoutesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
