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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.usecase.signer.SignerAssignedDocument
import com.example.challenge_letssign.ui.components.ChevronRightIcon
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.LetsSignBottomNavTab
import com.example.challenge_letssign.ui.components.LetsSignBottomNavigationBar
import com.example.challenge_letssign.ui.components.LetsSignGradientHeader
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.DateTimeFormatters

@Composable
fun SignerDashboardScreen(
    signerId: String,
    signerName: String,
    onLogout: () -> Unit,
    onOpenDocumentForSigning: (String) -> Unit = {},
    viewModel: SignerDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(signerId) {
        viewModel.observeDocuments(signerId)
    }

    SignerDashboardContent(
        signerId = signerId,
        signerName = signerName,
        uiState = uiState,
        onSelectTab = viewModel::onSelectTab,
        onDocumentClick = onOpenDocumentForSigning,
        onLogout = onLogout
    )
}

@Composable
fun SignerDashboardContent(
    signerId: String,
    signerName: String,
    uiState: SignerDashboardUiState,
    onSelectTab: (SignerDocumentTab) -> Unit,
    onDocumentClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedBottomTab by rememberSaveable { mutableStateOf(LetsSignBottomNavTab.SIGNATURES) }

    Scaffold(
        containerColor = LetsSignColors.Mist,
        bottomBar = {
            LetsSignBottomNavigationBar(
                selectedTab = selectedBottomTab,
                onTabSelected = { selectedBottomTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedBottomTab) {
                LetsSignBottomNavTab.DOCUMENTS,
                LetsSignBottomNavTab.SIGNATURES -> {
                    LetsSignGradientHeader(
                        title = "Assinaturas",
                        subtitle = "Olá, ${signerName.substringBefore(" ")}. Confira os documentos enviados para você."
                    )

                    SignerStatusTabRow(
                        selectedTab = uiState.selectedTab,
                        pendingCount = uiState.pendingDocuments.size,
                        signedCount = uiState.signedDocuments.size,
                        onTabSelected = onSelectTab
                    )

                    val docs = uiState.visibleDocuments
                    if (docs.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(22.dp))
                                        .background(brush = LetsSignColors.IconGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    DocumentFileIcon(color = Color.White, size = 34.dp)
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
                                        "Quando um documento for enviado para você assinar, ele aparecerá aqui."
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
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 24.dp,
                                end = 24.dp,
                                top = 20.dp,
                                bottom = 32.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(items = docs, key = { it.document.id }) { item ->
                                SignerDocumentCard(
                                    item = item,
                                    onClick = { onDocumentClick(item.document.id) }
                                )
                            }
                        }
                    }
                }

                LetsSignBottomNavTab.PROFILE -> {
                    LetsSignGradientHeader(
                        title = "Perfil",
                        subtitle = "Você está autenticado como Signatário."
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = signerName,
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "ID: $signerId",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LetsSignSecondaryButton(
                            text = "Sair da conta",
                            onClick = onLogout
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
    pendingCount: Int,
    signedCount: Int,
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
            val count = if (tab == SignerDocumentTab.PENDING) pendingCount else signedCount
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onTabSelected(tab) }
                    .padding(top = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${tab.title} ($count)",
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Body
                )
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
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
    val roleLabel = when (item.myRoleClass) {
        SignerRoleClass.CONTRATANTE -> "Contratante"
        SignerRoleClass.CONTRATADO -> "Contratado"
        SignerRoleClass.TESTEMUNHA -> "Testemunha"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LetsSignColors.Line, RoundedCornerShape(22.dp))
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LetsSignColors.VioletSoft),
                    contentAlignment = Alignment.Center
                ) {
                    DocumentFileIcon(color = LetsSignColors.Primary, size = 26.dp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doc.title,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Enviado em ${DateTimeFormatters.formatTimestamp(doc.createdAt)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Muted
                        )
                    )
                }
                ChevronRightIcon(color = LetsSignColors.Body, size = 18.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    LetsSignStatusChip(
                        label = if (item.isSignedByMe) "Assinado por você" else "Aguardando sua assinatura",
                        style = if (item.isSignedByMe) StatusChipStyle.COMPLETED else StatusChipStyle.PARTIAL
                    )
                    LetsSignStatusChip(
                        label = roleLabel,
                        style = StatusChipStyle.ROLE
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { item.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = if (item.isSignedByMe) LetsSignColors.Success else LetsSignColors.Primary,
                trackColor = LetsSignColors.VioletSoft
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignerDashboardScreenPreview() {
    Challenge_letsSignTheme {
        SignerDashboardContent(
            signerId = "signer-1",
            signerName = "Carlos Silva",
            uiState = SignerDashboardUiState(),
            onSelectTab = {},
            onDocumentClick = {},
            onLogout = {}
        )
    }
}
