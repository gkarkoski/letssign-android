package com.example.challenge_letssign.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.challenge_letssign.ui.admin.details.DocumentDetailsScreen
import com.example.challenge_letssign.ui.admin.positioning.PdfPositioningScreen
import com.example.challenge_letssign.ui.admin.preparation.DocumentPreparationScreen
import com.example.challenge_letssign.ui.admin.review.DocumentReviewScreen
import com.example.challenge_letssign.ui.auth.LoginScreen
import com.example.challenge_letssign.ui.signer.SignerDashboardScreen
import com.example.challenge_letssign.ui.signer.signing.SignerDocumentSignScreen

object LetsSignRoutes {
    const val LOGIN = "login"
    const val ADMIN_DASHBOARD = "admin_dashboard/{userId}/{userName}"
    const val SIGNER_DASHBOARD = "signer_dashboard/{userId}/{userName}"
    const val DOCUMENT_PREPARATION = "document_preparation/{adminId}?documentId={documentId}"
    const val PDF_POSITIONING = "pdf_positioning/{documentId}"
    const val DOCUMENT_REVIEW = "document_review/{documentId}"
    const val DOCUMENT_DETAILS = "document_details/{documentId}"
    const val SIGNER_DOCUMENT_SIGN = "signer_document_sign/{documentId}/{signerId}"

    fun adminDashboard(userId: String, userName: String): String =
        "admin_dashboard/${encodeSegment(userId)}/${encodeSegment(userName)}"

    fun signerDashboard(userId: String, userName: String): String =
        "signer_dashboard/${encodeSegment(userId)}/${encodeSegment(userName)}"

    fun documentPreparation(adminId: String, documentId: String? = null): String =
        if (documentId.isNullOrBlank()) {
            "document_preparation/${encodeSegment(adminId)}"
        } else {
            "document_preparation/${encodeSegment(adminId)}?documentId=${encodeSegment(documentId)}"
        }

    fun pdfPositioning(documentId: String): String =
        "pdf_positioning/${encodeSegment(documentId)}"

    fun documentReview(documentId: String): String =
        "document_review/${encodeSegment(documentId)}"

    fun documentDetails(documentId: String): String =
        "document_details/${encodeSegment(documentId)}"

    fun signerDocumentSign(documentId: String, signerId: String): String =
        "signer_document_sign/${encodeSegment(documentId)}/${encodeSegment(signerId)}"

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
        startDestination = LetsSignRoutes.LOGIN,
        // Transições especificadas em fluxo_telas.md:
        // Avançar: empurra da direita para a esquerda, 300 ms
        // Voltar: empurra da esquerda para a direita, 300 ms
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        }
    ) {
        composable(
            route = LetsSignRoutes.LOGIN,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
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
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
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
                },
                onDocumentClick = { documentId ->
                    navController.navigate(LetsSignRoutes.documentDetails(documentId))
                },
                onSignDocumentClick = { documentId ->
                    navController.navigate(
                        LetsSignRoutes.signerDocumentSign(
                            documentId = documentId,
                            signerId = com.example.challenge_letssign.data.local.seed.DatabaseSeeder.MOCK_SIGNER_2_ID
                        )
                    )
                }
            )
        }

        composable(
            route = LetsSignRoutes.DOCUMENT_PREPARATION,
            arguments = listOf(
                navArgument("adminId") { type = NavType.StringType },
                navArgument("documentId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val adminId = backStackEntry.arguments?.getString("adminId").orEmpty()
            val existingDocId = backStackEntry.arguments?.getString("documentId")

            DocumentPreparationScreen(
                ownerAdminId = adminId,
                existingDocumentId = existingDocId,
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
                onContinueToReview = { docId ->
                    navController.navigate(LetsSignRoutes.documentReview(docId))
                }
            )
        }

        composable(
            route = LetsSignRoutes.DOCUMENT_REVIEW,
            arguments = listOf(
                navArgument("documentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId").orEmpty()

            DocumentReviewScreen(
                documentId = documentId,
                onBack = {
                    navController.popBackStack()
                },
                onPreviewDocumentPdf = {
                    navController.popBackStack()
                },
                onTrackSentDocument = { docId ->
                    navController.navigate(LetsSignRoutes.documentDetails(docId)) {
                        popUpTo(LetsSignRoutes.ADMIN_DASHBOARD) { inclusive = false }
                    }
                },
                onDocumentSentSuccess = {
                    navController.popBackStack(
                        route = LetsSignRoutes.ADMIN_DASHBOARD,
                        inclusive = false
                    )
                }
            )
        }

        composable(
            route = LetsSignRoutes.DOCUMENT_DETAILS,
            arguments = listOf(
                navArgument("documentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId").orEmpty()

            DocumentDetailsScreen(
                documentId = documentId,
                onBack = {
                    navController.popBackStack()
                },
                onEditDraftSignersAndFile = { docId ->
                    navController.navigate(LetsSignRoutes.documentPreparation("admin", docId))
                },
                onOpenPositioningOrViewPdf = { docId ->
                    navController.navigate(LetsSignRoutes.pdfPositioning(docId))
                }
            )
        }

        composable(
            route = LetsSignRoutes.SIGNER_DASHBOARD,
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
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
                },
                onOpenDocumentForSigning = { docId ->
                    navController.navigate(LetsSignRoutes.signerDocumentSign(docId, userId))
                },
                onOpenSignedDocumentDetails = { docId ->
                    navController.navigate(LetsSignRoutes.documentDetails(docId))
                }
            )
        }

        composable(
            route = LetsSignRoutes.SIGNER_DOCUMENT_SIGN,
            arguments = listOf(
                navArgument("documentId") { type = NavType.StringType },
                navArgument("signerId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getString("documentId").orEmpty()
            val signerId = backStackEntry.arguments?.getString("signerId").orEmpty()

            SignerDocumentSignScreen(
                documentId = documentId,
                signerId = signerId,
                onBack = {
                    navController.popBackStack()
                },
                onViewSignedDocuments = {
                    navController.popBackStack()
                }
            )
        }
    }
}
