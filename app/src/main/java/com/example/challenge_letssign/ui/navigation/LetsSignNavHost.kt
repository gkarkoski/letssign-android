package com.example.challenge_letssign.ui.navigation

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
import com.example.challenge_letssign.ui.admin.positioning.PdfPositioningScreen
import com.example.challenge_letssign.ui.admin.preparation.DocumentPreparationScreen
import com.example.challenge_letssign.ui.auth.LoginScreen
import com.example.challenge_letssign.ui.signer.SignerDashboardScreen

object LetsSignRoutes {
    const val LOGIN = "login"
    const val ADMIN_DASHBOARD = "admin_dashboard/{userId}/{userName}"
    const val SIGNER_DASHBOARD = "signer_dashboard/{userId}/{userName}"
    const val DOCUMENT_PREPARATION = "document_preparation/{adminId}"
    const val PDF_POSITIONING = "pdf_positioning/{documentId}"

    fun adminDashboard(userId: String, userName: String): String =
        "admin_dashboard/${encodeSegment(userId)}/${encodeSegment(userName)}"

    fun signerDashboard(userId: String, userName: String): String =
        "signer_dashboard/${encodeSegment(userId)}/${encodeSegment(userName)}"

    fun documentPreparation(adminId: String): String =
        "document_preparation/${encodeSegment(adminId)}"

    fun pdfPositioning(documentId: String): String =
        "pdf_positioning/${encodeSegment(documentId)}"

    fun resolvePostLoginRoute(user: User): String = when (user.role) {
        UserRole.ADMIN -> adminDashboard(user.id, user.name)
        UserRole.SIGNER -> signerDashboard(user.id, user.name)
    }

    private fun encodeSegment(value: String): String =
        java.net.URLEncoder.encode(value, Charsets.UTF_8.name()).replace("+", "%20")
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
                },
                onNewDocumentClick = {
                    navController.navigate(LetsSignRoutes.documentPreparation(userId))
                }
            )
        }

        composable(
            route = LetsSignRoutes.DOCUMENT_PREPARATION,
            arguments = listOf(
                navArgument("adminId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val adminId = backStackEntry.arguments?.getString("adminId").orEmpty()

            DocumentPreparationScreen(
                ownerAdminId = adminId,
                onCancel = {
                    navController.popBackStack()
                },
                onDraftPrepared = { document ->
                    navController.navigate(LetsSignRoutes.pdfPositioning(document.id))
                }
            )
        }

        composable(
            route = LetsSignRoutes.PDF_POSITIONING,
            arguments = listOf(
                navArgument("documentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId").orEmpty()

            PdfPositioningScreen(
                documentId = documentId,
                onBack = {
                    navController.popBackStack()
                },
                onContinueToReview = { _ ->
                    // No Milestone 6 redirecionará para a Tela de Review e Disparo
                    navController.popBackStack(
                        route = LetsSignRoutes.ADMIN_DASHBOARD,
                        inclusive = false
                    )
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
