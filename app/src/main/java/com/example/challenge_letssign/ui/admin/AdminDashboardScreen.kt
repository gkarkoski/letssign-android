package com.example.challenge_letssign.ui.admin

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
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.usecase.document.AdminDocumentOverview
import com.example.challenge_letssign.ui.admin.signers.SignerManagementScreen
import com.example.challenge_letssign.ui.components.ChevronRightIcon
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.LetsSignBottomNavTab
import com.example.challenge_letssign.ui.components.LetsSignBottomNavigationBar
import com.example.challenge_letssign.ui.components.LetsSignGradientHeader
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.PlusIcon
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.DateTimeFormatters

enum class AdminDocumentTab(val title: String) {
    SENT("Enviados"),
    PARTIALLY_SIGNED("Parcialmente\nAssinados"),
    COMPLETED("Concluídos")
}

@Composable
fun AdminDashboardScreen(
    adminId: String,
    adminName: String,
    onLogout: () -> Unit,
    onNewDocumentClick: () -> Unit = {},
    onDocumentClick: (String) -> Unit = {},
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(adminId) {
        viewModel.observeDocumentsForAdmin(adminId)
    }

    AdminDashboardContent(
        adminId = adminId,
        adminName = adminName,
        uiState = uiState,
        onSelectDocTab = viewModel::onSelectTab,
        onNewDocumentClick = onNewDocumentClick,
        onDocumentClick = onDocumentClick,
        onLogout = onLogout
    )
}

@Composable
fun AdminDashboardContent(
    adminId: String,
    adminName: String,
    uiState: AdminDashboardUiState,
    onSelectDocTab: (AdminDocumentTab) -> Unit,
    onNewDocumentClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedBottomTab by rememberSaveable { mutableStateOf(LetsSignBottomNavTab.DOCUMENTS) }

    Scaffold(
        containerColor = LetsSignColors.Mist,
        bottomBar = {
            LetsSignBottomNavigationBar(
                selectedTab = selectedBottomTab,
                onTabSelected = { selectedBottomTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (selectedBottomTab) {
                    LetsSignBottomNavTab.DOCUMENTS -> {
                        LetsSignGradientHeader(
                            title = "Documentos",
                            subtitle = "Acompanhe o que você enviou para assinatura."
                        )

                        AdminStatusTabRow(
                            selectedTab = uiState.selectedTab,
                            onTabSelected = onSelectDocTab
                        )

                        val tabDocs = uiState.visibleTabDocuments
                        val draftDocs = uiState.draftDocuments
                        val showDraftSection = uiState.selectedTab == AdminDocumentTab.SENT && draftDocs.isNotEmpty()

                        if (tabDocs.isEmpty() && !showDraftSection) {
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
                                        text = "Nenhum documento enviado ainda",
                                        style = MaterialTheme.typography.headlineMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Envie seu primeiro PDF para assinatura e acompanhe cada etapa por aqui.",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = LetsSignColors.Muted
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    LetsSignPrimaryButton(
                                        text = "Novo documento",
                                        onClick = onNewDocumentClick,
                                        leadingIcon = {
                                            PlusIcon(color = Color.White, size = 18.dp)
                                        }
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
                                    bottom = 96.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (showDraftSection) {
                                    item {
                                        Text(
                                            text = "Em preparação (${draftDocs.size})",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color = LetsSignColors.Muted
                                            )
                                        )
                                    }
                                    items(items = draftDocs, key = { "draft_${it.document.id}" }) { overview ->
                                        AdminDocumentCard(
                                            overview = overview,
                                            onClick = { onDocumentClick(overview.document.id) }
                                        )
                                    }
                                    if (tabDocs.isNotEmpty()) {
                                        item {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Enviados (${tabDocs.size})",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    color = LetsSignColors.Muted
                                                )
                                            )
                                        }
                                    }
                                }

                                items(items = tabDocs, key = { it.document.id }) { overview ->
                                    AdminDocumentCard(
                                        overview = overview,
                                        onClick = { onDocumentClick(overview.document.id) }
                                    )
                                }
                            }
                        }
                    }

                    LetsSignBottomNavTab.SIGNATURES -> {
                        SignerManagementScreen()
                    }

                    LetsSignBottomNavTab.PROFILE -> {
                        LetsSignGradientHeader(
                            title = "Perfil",
                            subtitle = "Você está autenticado como Administrador."
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = adminName,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "ID: $adminId",
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

            // FAB "+ Novo documento" quando já há itens na lista (home_admin_enviados.png)
            val showFloatingButton = selectedBottomTab == LetsSignBottomNavTab.DOCUMENTS &&
                (uiState.visibleTabDocuments.isNotEmpty() ||
                    (uiState.selectedTab == AdminDocumentTab.SENT && uiState.draftDocuments.isNotEmpty()))

            if (showFloatingButton) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 24.dp, bottom = 20.dp)
                ) {
                    LetsSignPrimaryButton(
                        text = "Novo documento",
                        onClick = onNewDocumentClick,
                        leadingIcon = {
                            PlusIcon(color = Color.White, size = 18.dp)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDocumentCard(
    overview: AdminDocumentOverview,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doc = overview.document
    val subtitleText = if (doc.status == DocumentStatus.DRAFT) {
        "Criado em ${DateTimeFormatters.formatTimestamp(doc.createdAt)}"
    } else {
        "Enviado em ${DateTimeFormatters.formatTimestamp(doc.sentAt ?: doc.createdAt)}"
    }

    val (chipLabel, chipStyle, progressColor) = when {
        overview.isDraft -> Triple(
            "Rascunho",
            StatusChipStyle.DRAFT,
            LetsSignColors.VioletSoft
        )
        overview.isCompleted -> Triple(
            "Concluído",
            StatusChipStyle.COMPLETED,
            LetsSignColors.Success
        )
        overview.isPartiallySigned -> Triple(
            "Parcialmente assinado",
            StatusChipStyle.PARTIAL,
            LetsSignColors.Primary
        )
        else -> Triple(
            "Enviado",
            StatusChipStyle.SENT,
            LetsSignColors.Primary
        )
    }

    Surface(
        modifier = modifier
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        text = subtitleText,
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
                LetsSignStatusChip(
                    label = chipLabel,
                    style = chipStyle
                )
                Text(
                    text = "${overview.signedCount}/${overview.totalSigners} assinaturas",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.Body
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            LinearProgressIndicator(
                progress = { overview.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = progressColor,
                trackColor = LetsSignColors.VioletSoft
            )
        }
    }
}

@Composable
fun AdminStatusTabRow(
    selectedTab: AdminDocumentTab,
    onTabSelected: (AdminDocumentTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LetsSignColors.Mist)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AdminDocumentTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(tab) }
                        .padding(top = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.height(42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.title,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            lineHeight = 18.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Body
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(
                                if (isSelected) LetsSignColors.Primary else LetsSignColors.Line
                            )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminDashboardScreenPreview() {
    Challenge_letsSignTheme {
        AdminDashboardContent(
            adminId = "admin-1",
            adminName = "Administrador LetsSign",
            uiState = AdminDashboardUiState(),
            onSelectDocTab = {},
            onNewDocumentClick = {},
            onDocumentClick = {},
            onLogout = {}
        )
    }
}
