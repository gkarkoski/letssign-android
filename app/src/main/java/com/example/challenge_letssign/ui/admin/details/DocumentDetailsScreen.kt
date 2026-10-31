package com.example.challenge_letssign.ui.admin.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.ui.components.CheckIcon
import com.example.challenge_letssign.ui.components.CloseIcon
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.LetsSignAvatar
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.components.TrashIcon
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.DateTimeFormatters

@Composable
fun DocumentDetailsScreen(
    documentId: String,
    onBack: () -> Unit,
    onOpenPositioningOrViewPdf: (String) -> Unit,
    viewModel: DocumentDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onBack()
        }
    }

    DocumentDetailsContent(
        uiState = uiState,
        onBack = onBack,
        onOpenPositioningOrViewPdf = { onOpenPositioningOrViewPdf(documentId) },
        onDeleteDraft = viewModel::onDeleteDraftDocument
    )
}

@Composable
fun DocumentDetailsContent(
    uiState: DocumentDetailsUiState,
    onBack: () -> Unit,
    onOpenPositioningOrViewPdf: () -> Unit,
    onDeleteDraft: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doc = uiState.document

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LetsSignColors.Mist,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onBack)
                                .padding(6.dp)
                        ) {
                            CloseIcon(color = LetsSignColors.Primary, size = 20.dp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Detalhes do Documento",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }

                    // Regra 4: Só mostra botão de excluir se estiver em DRAFT
                    if (uiState.isDraft) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onDeleteDraft)
                                .padding(8.dp)
                        ) {
                            TrashIcon(color = LetsSignColors.Error, size = 20.dp)
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .navigationBarsPadding()
            ) {
                HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LetsSignSecondaryButton(
                        text = "Voltar",
                        onClick = onBack,
                        modifier = Modifier.weight(0.4f)
                    )
                    // Regra 4: Se DRAFT -> Editar campos; Se PENDING/COMPLETED -> Visualizar PDF
                    LetsSignPrimaryButton(
                        text = if (uiState.isDraft) "Posicionar e enviar" else "Visualizar PDF",
                        onClick = onOpenPositioningOrViewPdf,
                        modifier = Modifier.weight(0.6f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (doc != null) {
                val (chipLabel, chipStyle, barColor) = when {
                    doc.status == DocumentStatus.DRAFT -> Triple(
                        "Rascunho",
                        StatusChipStyle.DRAFT,
                        LetsSignColors.VioletSoft
                    )
                    doc.status == DocumentStatus.COMPLETED -> Triple(
                        "Concluído",
                        StatusChipStyle.COMPLETED,
                        LetsSignColors.Success
                    )
                    uiState.signedCount > 0 -> Triple(
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

                // Card Principal do Documento
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    shadowElevation = 3.dp
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
                                    text = "Criado em ${DateTimeFormatters.formatTimestamp(doc.createdAt)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = LetsSignColors.Muted
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LetsSignStatusChip(label = chipLabel, style = chipStyle)
                            Text(
                                text = "${uiState.signedCount}/${uiState.totalSigners} assinaturas",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LetsSignColors.Body
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val progress = if (uiState.totalSigners <= 0) {
                            0f
                        } else {
                            (uiState.signedCount.toFloat() / uiState.totalSigners.toFloat()).coerceIn(0f, 1f)
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = barColor,
                            trackColor = LetsSignColors.VioletSoft
                        )
                    }
                }

                // Seção: Quem já assinou e quem falta
                Text(
                    text = "Signatários (${uiState.signedCount} de ${uiState.totalSigners} assinaram)",
                    style = MaterialTheme.typography.headlineSmall
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.signers.forEach { item ->
                        val isSigned = item.documentSigner.status == SignatureStatus.SIGNED
                        val roleLabel = when (item.documentSigner.roleClass) {
                            SignerRoleClass.CONTRATANTE -> "Contratante"
                            SignerRoleClass.CONTRATADO -> "Contratado"
                            SignerRoleClass.TESTEMUNHA -> "Testemunha"
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, LetsSignColors.Line, RoundedCornerShape(16.dp))
                                    .padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    LetsSignAvatar(fullName = item.signer.name)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.signer.name,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = item.signer.email,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = LetsSignColors.Muted,
                                                fontSize = 14.sp
                                            )
                                        )
                                    }
                                    LetsSignStatusChip(
                                        label = if (isSigned) "Assinado" else "Pendente",
                                        style = if (isSigned) StatusChipStyle.COMPLETED else StatusChipStyle.SENT
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LetsSignStatusChip(
                                        label = roleLabel,
                                        style = StatusChipStyle.ROLE
                                    )
                                    if (isSigned && item.documentSigner.signedAt != null) {
                                        Text(
                                            text = "Assinado em ${DateTimeFormatters.formatTimestamp(item.documentSigner.signedAt)}" +
                                                (item.documentSigner.signatureIp?.let { " · IP $it" } ?: ""),
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = LetsSignColors.Success,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = "Aguardando assinatura",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = LetsSignColors.Muted
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Seção: Histórico de Eventos
                Text(
                    text = "Histórico de eventos",
                    style = MaterialTheme.typography.headlineSmall
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, LetsSignColors.Line, RoundedCornerShape(18.dp))
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        uiState.auditEvents.forEach { event ->
                            Row(verticalAlignment = Alignment.Top) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 3.dp)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (event.isSuccess) LetsSignColors.SuccessSoft else LetsSignColors.VioletSoft
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (event.isSuccess) {
                                        CheckIcon(color = LetsSignColors.Success, size = 12.dp)
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(LetsSignColors.Primary)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp)
                                    )
                                    Text(
                                        text = event.description,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = LetsSignColors.Body,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Text(
                                        text = DateTimeFormatters.formatTimestamp(event.timestampMillis),
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = LetsSignColors.Muted
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
