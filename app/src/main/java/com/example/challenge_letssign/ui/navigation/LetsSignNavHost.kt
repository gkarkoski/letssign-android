package com.example.challenge_letssign.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.ui.admin.AdminDashboardScreen
import com.example.challenge_letssign.ui.auth.LoginScreen
import com.example.challenge_letssign.ui.signer.SignerDashboardScreen

object LetsSignRoutes {
    const val LOGIN = "login"
    const val ADMIN_DASHBOARD = "admin_dashboard/{userId}/{userName}"
    const val SIGNER_DASHBOARD = "signer_dashboard/{userId}/{userName}"

    fun adminDashboard(userId: String, userName: String): String =
        "admin_dashboard/${Uri.encode(userId)}/${Uri.encode(userName)}"

    fun signerDashboard(userId: String, userName: String): String =
        "signer_dashboard/${Uri.encode(userId)}/${Uri.encode(userName)}"

    fun resolvePostLoginRoute(user: User): String = when (user.role) {
        UserRole.ADMIN -> adminDashboard(user.id, user.name)
        UserRole.SIGNER -> signerDashboard(user.id, user.name)
    }
}

@Composable
fun LetsSignNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = LetsSignRoutes.LOGIN
    ) {
        composable(route = LetsSignRoutes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { user ->
                    val targetRoute = LetsSignRoutes.resolvePostLoginRoute(user)
                    navController.navigate(targetRoute) {
                        popUpTo(LetsSignRoutes.LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = LetsSignRoutes.ADMIN_DASHBOARD,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            val userName = backStackEntry.arguments?.getString("userName").orEmpty()

            AdminDashboardScreen(
                adminId = userId,
                adminName = userName,
                onLogout = {
                    navController.navigate(LetsSignRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = LetsSignRoutes.SIGNER_DASHBOARD,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("userName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId").orEmpty()
            val userName = backStackEntry.arguments?.getString("userName").orEmpty()

            SignerDashboardScreen(
                signerId = userId,
                signerName = userName,
                onLogout = {
                    navController.navigate(LetsSignRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
