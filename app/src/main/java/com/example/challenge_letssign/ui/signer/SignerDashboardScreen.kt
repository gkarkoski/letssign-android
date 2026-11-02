package com.example.challenge_letssign.ui.signer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.usecase.signer.SignerAssignedDocument
import com.example.challenge_letssign.ui.components.ChevronRightIcon
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.ErrorCircleIcon
import com.example.challenge_letssign.ui.components.LetsSignBrandLogo
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSkeletonCard
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.DateTimeFormatters

@Composable
fun SignerDashboardScreen(
    signerId: String,
    signerName: String,
    initialTab: SignerDocumentTab = SignerDocumentTab.PENDING,
    onLogout: () -> Unit,
    onOpenDocumentForSigning: (String) -> Unit = {},
    onOpenSignedDocumentDetails: (String) -> Unit = onOpenDocumentForSigning,
    viewModel: SignerDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(signerId) {
        viewModel.observeDocuments(signerId)
    }

    LaunchedEffect(initialTab) {
        viewModel.onSelectTab(initialTab)
    }

    SignerDashboardContent(
        signerId = signerId,
        signerName = signerName,
        uiState = uiState,
        onSelectTab = viewModel::onSelectTab,
        onRetryLoad = viewModel::onRetryLoad,
        onPendingDocumentClick = onOpenDocumentForSigning,
        onSignedDocumentClick = onOpenSignedDocumentDetails,
        onLogout = onLogout
    )
}

@Composable
fun SignerDashboardContent(
    signerId: String,
    signerName: String,
    uiState: SignerDashboardUiState,
    onSelectTab: (SignerDocumentTab) -> Unit,
    onRetryLoad: () -> Unit = {},
    onPendingDocumentClick: (String) -> Unit,
    onSignedDocumentClick: (String) -> Unit = onPendingDocumentClick,
    onLogout: () -> Unit
) {
    val firstName = signerName.trim().substringBefore(" ").ifBlank { "Rafael" }
    val docs = uiState.visibleDocuments

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LetsSignColors.Mist)
            .navigationBarsPadding()
    ) {
        // Header gradiente da Home do Signatário (Home Signatário Pendentes.png)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                .background(brush = LetsSignColors.HeaderGradient)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LetsSignBrandLogo()
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color.White.copy(alpha = 0.14f))
                            .clickable(onClick = onLogout)
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Sair",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Meus documentos",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontSize = 30.sp,
                        lineHeight = 34.sp
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Olá, $firstName. Veja o que precisa da sua assinatura.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White.copy(alpha = 0.92f)
                    )
                )
            }
        }

        // Abas Pendentes | Assinados (sem barra inferior, conforme fluxo_telas.md)
        SignerStatusTabRow(
            selectedTab = uiState.selectedTab,
            onTabSelected = onSelectTab
        )

        when {
            uiState.isLoading -> {
                // Home Signatário Carregando.png
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    repeat(3) {
                        LetsSignSkeletonCard()
                    }
                }
            }

            uiState.hasError -> {
                // Home Signatário Erro.png
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(LetsSignColors.ErrorSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorCircleIcon(color = LetsSignColors.Error, size = 30.dp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Não foi possível carregar",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Verifique sua conexão e tente novamente.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = LetsSignColors.Muted
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(22.dp))
                    LetsSignPrimaryButton(
                        text = "Tentar novamente",
                        onClick = onRetryLoad,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            docs.isEmpty() -> {
                // Home Signatário Vazio.png
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(LetsSignColors.VioletSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        DocumentFileIcon(color = LetsSignColors.Primary, size = 32.dp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = if (uiState.selectedTab == SignerDocumentTab.PENDING) {
                            "Nenhum documento pendente"
                        } else {
                            "Nenhum documento assinado ainda"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (uiState.selectedTab == SignerDocumentTab.PENDING) {
                            "Quando alguém enviar um documento para você assinar, ele aparece aqui."
                        } else {
                            "Os documentos que você assinar ficarão disponíveis nesta aba."
                        },
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = LetsSignColors.Muted
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        top = 18.dp,
                        bottom = 32.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items = docs, key = { it.document.id }) { item ->
                        SignerDocumentCard(
                            item = item,
                            onClick = {
                                if (item.isPendingForMe) {
                                    onPendingDocumentClick(item.document.id)
                                } else {
                                    onSignedDocumentClick(item.document.id)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignerStatusTabRow(
    selectedTab: SignerDocumentTab,
    onTabSelected: (SignerDocumentTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LetsSignColors.Mist),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SignerDocumentTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(top = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = tab.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Muted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(if (isSelected) LetsSignColors.Primary else LetsSignColors.Line)
                )
            }
        }
    }
}

@Composable
private fun SignerDocumentCard(
    item: SignerAssignedDocument,
    onClick: () -> Unit
) {
    val doc = item.document
    val isSigned = item.isSignedByMe
    val subtitle = if (isSigned && item.mySignedAt != null) {
        "Assinado em ${DateTimeFormatters.formatTimestampWithAs(item.mySignedAt)}"
    } else {
        "Enviado em ${DateTimeFormatters.formatTimestamp(doc.sentAt ?: doc.createdAt)}"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LetsSignColors.Line, RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LetsSignColors.VioletSoft),
                    contentAlignment = Alignment.Center
                ) {
                    DocumentFileIcon(color = LetsSignColors.Primary, size = 24.dp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 14.sp
                        )
                    )
                }
                ChevronRightIcon(color = LetsSignColors.Muted, size = 18.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isSigned) {
                    LetsSignStatusChip(
                        label = "Concluído",
                        style = StatusChipStyle.COMPLETED
                    )
                    Text(
                        text = "Assinado",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Body,
                            fontSize = 14.sp
                        )
                    )
                } else {
                    LetsSignStatusChip(
                        label = "Aguardando você",
                        style = StatusChipStyle.WAITING_YOU
                    )
                    val deadlineText = doc.deadlineAt?.let {
                        DateTimeFormatters.formatDateOnly(it)
                    } ?: "05/11/2026"
                    Text(
                        text = "Prazo: $deadlineText",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Body,
                            fontSize = 14.sp
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignerDashboardScreenPreview() {
    Challenge_letsSignTheme {
        SignerDashboardContent(
            signerId = "signer-1",
            signerName = "Rafael Tavares",
            uiState = SignerDashboardUiState(),
            onSelectTab = {},
            onPendingDocumentClick = {},
            onLogout = {}
        )
    }
}
