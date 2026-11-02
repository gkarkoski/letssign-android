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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.challenge_letssign.ui.components.ErrorCircleIcon
import com.example.challenge_letssign.ui.components.LetsSignBottomNavTab
import com.example.challenge_letssign.ui.components.LetsSignBottomNavigationBar
import com.example.challenge_letssign.ui.components.LetsSignBrandLogo
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSearchBar
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignSkeletonCard
import com.example.challenge_letssign.ui.components.LetsSignSnackbar
import com.example.challenge_letssign.ui.components.LetsSignSnackbarType
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.PlusIcon
import com.example.challenge_letssign.ui.components.SearchIcon
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.DateTimeFormatters

enum class AdminDocumentTab(val title: String) {
    SENT("Enviados"),
    PARTIALLY_SIGNED("Parcialmente Assinados"),
    COMPLETED("Concluídos")
}

@Composable
fun AdminDashboardScreen(
    adminId: String,
    adminName: String,
    openMySignedTabSignal: Boolean = false,
    onLogout: () -> Unit,
    onNewDocumentClick: () -> Unit = {},
    onDocumentClick: (String) -> Unit = {},
    onSignDocumentClick: (String) -> Unit = onDocumentClick,
    viewModel: AdminDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(adminId) {
        viewModel.observeDocumentsForAdmin(adminId)
    }

    LaunchedEffect(openMySignedTabSignal) {
        if (openMySignedTabSignal) {
            viewModel.onSelectMySignaturesTab(MySignaturesTab.SIGNED)
        }
    }

    AdminDashboardContent(
        adminId = adminId,
        adminName = adminName,
        uiState = uiState,
        onSelectDocTab = viewModel::onSelectTab,
        onSelectMySignaturesTab = viewModel::onSelectMySignaturesTab,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onClearSearch = viewModel::onClearSearch,
        onWaitingOthersSearchQueryChanged = viewModel::onWaitingOthersSearchQueryChanged,
        onRetryLoad = viewModel::onRetryLoad,
        onNewDocumentClick = onNewDocumentClick,
        onDocumentClick = onDocumentClick,
        onSignDocumentClick = onSignDocumentClick,
        onLogout = onLogout
    )
}

@Composable
fun AdminDashboardContent(
    adminId: String,
    adminName: String,
    uiState: AdminDashboardUiState,
    onSelectDocTab: (AdminDocumentTab) -> Unit,
    onSelectMySignaturesTab: (MySignaturesTab) -> Unit = {},
    onSearchQueryChanged: (String) -> Unit = {},
    onClearSearch: () -> Unit = {},
    onWaitingOthersSearchQueryChanged: (String) -> Unit = {},
    onRetryLoad: () -> Unit = {},
    onNewDocumentClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onSignDocumentClick: (String) -> Unit = onDocumentClick,
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
            when (selectedBottomTab) {
                LetsSignBottomNavTab.DOCUMENTS -> {
                    AdminDocumentsTabScreen(
                        uiState = uiState,
                        onSelectDocTab = onSelectDocTab,
                        onSearchQueryChanged = onSearchQueryChanged,
                        onClearSearch = onClearSearch,
                        onRetryLoad = onRetryLoad,
                        onNewDocumentClick = onNewDocumentClick,
                        onDocumentClick = onDocumentClick,
                        onLogout = onLogout
                    )
                }

                LetsSignBottomNavTab.MY_SIGNATURES -> {
                    AdminMySignaturesTabScreen(
                        uiState = uiState,
                        onSelectTab = onSelectMySignaturesTab,
                        onPendingDocumentClick = onSignDocumentClick,
                        onSignedDocumentClick = onDocumentClick,
                        onLogout = onLogout
                    )
                }

                LetsSignBottomNavTab.WAITING_OTHERS -> {
                    AdminWaitingOthersTabScreen(
                        uiState = uiState,
                        onSearchQueryChanged = onWaitingOthersSearchQueryChanged,
                        onDocumentClick = onDocumentClick,
                        onLogout = onLogout
                    )
                }

                LetsSignBottomNavTab.CONTACTS,
                LetsSignBottomNavTab.SIGNATURES,
                LetsSignBottomNavTab.PROFILE -> {
                    SignerManagementScreen()
                }
            }
        }
    }
}

@Composable
private fun AdminCompactGradientHeader(
    title: String,
    onLogout: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .background(brush = LetsSignColors.HeaderGradient)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 22.dp)
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
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontSize = 30.sp,
                    lineHeight = 34.sp
                )
            )
        }
    }
}

/**
 * Menu 1: Documentos (Home Admin Aguardando Assinaturas, Concluídos, Busca, Sem Resultado, Vazio, Carregando, Erro).
 */
@Composable
private fun AdminDocumentsTabScreen(
    uiState: AdminDashboardUiState,
    onSelectDocTab: (AdminDocumentTab) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onRetryLoad: () -> Unit,
    onNewDocumentClick: () -> Unit,
    onDocumentClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val tabDocs = uiState.visibleTabDocuments
    val draftDocs = uiState.draftDocuments
    val showDraftSection = uiState.selectedTab == AdminDocumentTab.SENT &&
        uiState.searchQuery.isBlank() &&
        draftDocs.isNotEmpty()
    val isSearching = uiState.searchQuery.isNotBlank()
    val hasAnyDocsInCurrentTab = if (uiState.selectedTab == AdminDocumentTab.SENT) {
        uiState.allDocuments.any { it.isSentWaitingAll || it.isPartiallySigned || it.isDraft }
    } else {
        uiState.completedDocuments.isNotEmpty()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            AdminCompactGradientHeader(
                title = "Documentos",
                onLogout = onLogout
            )

            // Abas Enviados | Concluídos (Componentes.png)
            TwoTabSelectorBar(
                leftTitle = "Enviados",
                rightTitle = "Concluídos",
                isLeftSelected = uiState.selectedTab != AdminDocumentTab.COMPLETED,
                onSelectLeft = { onSelectDocTab(AdminDocumentTab.SENT) },
                onSelectRight = { onSelectDocTab(AdminDocumentTab.COMPLETED) }
            )

            when {
                uiState.isLoading -> {
                    // Home Admin Carregando.png
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
                    // Home Admin Erro.png
                    Box(modifier = Modifier.fillMaxSize()) {
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
                                text = "Verifique sua conexão com a internet e tente novamente.",
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

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 24.dp, vertical = 18.dp)
                        ) {
                            LetsSignSnackbar(
                                message = "Sem conexão. Seus dados estão seguros.",
                                type = LetsSignSnackbarType.ERROR
                            )
                        }
                    }
                }

                !hasAnyDocsInCurrentTab && !isSearching -> {
                    // Home Admin Vazio.png
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(brush = LetsSignColors.IconGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            DocumentFileIcon(color = Color.White, size = 34.dp)
                        }
                        Spacer(modifier = Modifier.height(22.dp))
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
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                else -> {
                    // Campo de busca ("Buscar por título ou signatário")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 14.dp)
                    ) {
                        LetsSignSearchBar(
                            query = uiState.searchQuery,
                            onQueryChanged = onSearchQueryChanged,
                            placeholder = "Buscar por título ou signatário"
                        )
                    }

                    if (tabDocs.isEmpty() && !showDraftSection) {
                        // Home Admin Concluídos Sem Resultado.png
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 32.dp, vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(LetsSignColors.VioletSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                SearchIcon(color = LetsSignColors.Primary, size = 28.dp)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Nenhum documento encontrado",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Confira o título ou o nome do signatário, ou limpe a busca para ver todos os concluídos.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LetsSignColors.Muted
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            LetsSignSecondaryButton(
                                text = "Limpar busca",
                                onClick = onClearSearch
                            )
                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                start = 24.dp,
                                end = 24.dp,
                                top = 2.dp,
                                bottom = 104.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (isSearching) {
                                item {
                                    val resultSuffix = if (tabDocs.size == 1) "resultado" else "resultados"
                                    Text(
                                        text = "${tabDocs.size} $resultSuffix",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = LetsSignColors.Muted,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }

                            if (showDraftSection) {
                                item {
                                    Text(
                                        text = "Em preparação (${draftDocs.size})",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = LetsSignColors.Muted,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                                items(items = draftDocs, key = { "draft_${it.document.id}" }) { overview ->
                                    AdminDocumentCard(
                                        overview = overview,
                                        onClick = { onDocumentClick(overview.document.id) }
                                    )
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
            }
        }

        // FAB flutuante "+ Novo documento" (Home Admin Aguardando Assinaturas / Concluídos)
        if (!uiState.isLoading && !uiState.hasError && (hasAnyDocsInCurrentTab || isSearching)) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 40.dp, vertical = 18.dp)
                    .fillMaxWidth()
            ) {
                LetsSignPrimaryButton(
                    text = "Novo documento",
                    onClick = onNewDocumentClick,
                    leadingIcon = {
                        PlusIcon(color = Color.White, size = 18.dp)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Menu 2: Minhas Assinaturas (Home Admin Pendentes.png e Home Admin Assinados.png).
 */
@Composable
private fun AdminMySignaturesTabScreen(
    uiState: AdminDashboardUiState,
    onSelectTab: (MySignaturesTab) -> Unit,
    onPendingDocumentClick: (String) -> Unit,
    onSignedDocumentClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val isPendingTab = uiState.mySignaturesTab == MySignaturesTab.PENDING
    val docs = if (isPendingTab) uiState.myPendingSignatures else uiState.mySignedDocuments

    Column(modifier = Modifier.fillMaxSize()) {
        AdminCompactGradientHeader(
            title = "Minhas Assinaturas",
            onLogout = onLogout
        )

        TwoTabSelectorBar(
            leftTitle = "Pendentes",
            rightTitle = "Assinados",
            isLeftSelected = isPendingTab,
            onSelectLeft = { onSelectTab(MySignaturesTab.PENDING) },
            onSelectRight = { onSelectTab(MySignaturesTab.SIGNED) }
        )

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
            items(items = docs, key = { "mysig_${it.document.id}" }) { overview ->
                MySignatureDocumentCard(
                    overview = overview,
                    isPending = isPendingTab,
                    onClick = {
                        if (isPendingTab) {
                            onPendingDocumentClick(overview.document.id)
                        } else {
                            onSignedDocumentClick(overview.document.id)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Menu 3: Aguardando Terceiros (Home Aguardando Terceiros.png).
 */
@Composable
private fun AdminWaitingOthersTabScreen(
    uiState: AdminDashboardUiState,
    onSearchQueryChanged: (String) -> Unit,
    onDocumentClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val docs = uiState.filteredWaitingOthersDocuments

    Column(modifier = Modifier.fillMaxSize()) {
        AdminCompactGradientHeader(
            title = "Aguardando Terceiros",
            onLogout = onLogout
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            LetsSignSearchBar(
                query = uiState.waitingOthersSearchQuery,
                onQueryChanged = onSearchQueryChanged,
                placeholder = "Buscar por título ou signatário"
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 4.dp,
                bottom = 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = docs, key = { "wait_${it.document.id}" }) { overview ->
                AdminDocumentCard(
                    overview = overview,
                    onClick = { onDocumentClick(overview.document.id) }
                )
            }
        }
    }
}

@Composable
private fun TwoTabSelectorBar(
    leftTitle: String,
    rightTitle: String,
    isLeftSelected: Boolean,
    onSelectLeft: () -> Unit,
    onSelectRight: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LetsSignColors.Mist)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf(
                Triple(leftTitle, isLeftSelected, onSelectLeft),
                Triple(rightTitle, !isLeftSelected, onSelectRight)
            ).forEach { (title, isSelected, onClick) ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onClick),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = title,
                        color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Muted,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .background(
                                if (isSelected) LetsSignColors.Primary else LetsSignColors.Line
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDocumentCard(
    overview: AdminDocumentOverview,
    onClick: () -> Unit
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
            LetsSignColors.VioletSoft
        )
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
                        text = subtitleText,
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
                LetsSignStatusChip(
                    label = chipLabel,
                    style = chipStyle
                )
                Text(
                    text = "${overview.signedCount}/${overview.totalSigners} assinaturas",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.Body,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = {
                    if (overview.isCompleted) 1f else overview.progressFraction
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = progressColor,
                trackColor = LetsSignColors.VioletSoft
            )
        }
    }
}

@Composable
private fun MySignatureDocumentCard(
    overview: AdminDocumentOverview,
    isPending: Boolean,
    onClick: () -> Unit
) {
    val doc = overview.document
    val subtitleText = if (isPending) {
        "Enviado em ${DateTimeFormatters.formatTimestamp(doc.sentAt ?: doc.createdAt)}"
    } else {
        "Assinado em ${DateTimeFormatters.formatTimestamp(doc.sentAt ?: doc.createdAt)}"
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
                        text = subtitleText,
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
                if (isPending) {
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
                } else {
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
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AdminDashboardPreview() {
    Challenge_letsSignTheme {
        AdminDashboardContent(
            adminId = "1",
            adminName = "Administrador",
            uiState = AdminDashboardUiState(),
            onSelectDocTab = {},
            onNewDocumentClick = {},
            onDocumentClick = {},
            onLogout = {}
        )
    }
}
