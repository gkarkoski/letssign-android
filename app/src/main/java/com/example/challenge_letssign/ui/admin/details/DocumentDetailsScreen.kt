package com.example.challenge_letssign.ui.admin.details

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.ui.components.CheckIcon
import com.example.challenge_letssign.ui.components.ClockIcon
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.ErrorCircleIcon
import com.example.challenge_letssign.ui.components.LetsSignAvatar
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignSkeletonCard
import com.example.challenge_letssign.ui.components.LetsSignSnackbar
import com.example.challenge_letssign.ui.components.LetsSignSnackbarType
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.components.TrashIcon
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.DateTimeFormatters
import kotlinx.coroutines.delay

@Composable
fun DocumentDetailsScreen(
    documentId: String,
    onBack: () -> Unit,
    onEditDraftSignersAndFile: (String) -> Unit = {},
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

    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null && uiState.snackbarActionLabel == null) {
            delay(3500)
            viewModel.onDismissSnackbar()
        }
    }

    DocumentDetailsContent(
        uiState = uiState,
        onBack = onBack,
        onEditDraftSignersAndFile = { onEditDraftSignersAndFile(documentId) },
        onOpenPositioningOrViewPdf = { onOpenPositioningOrViewPdf(documentId) },
        onResendReminder = viewModel::onResendReminder,
        onDownloadSignedDocument = viewModel::onDownloadSignedDocument,
        onRetryLoad = viewModel::onRetryLoad,
        onDeleteDraft = viewModel::onDeleteDraftDocument,
        onDismissSnackbar = viewModel::onDismissSnackbar
    )
}

@Composable
fun DocumentDetailsContent(
    uiState: DocumentDetailsUiState,
    onBack: () -> Unit,
    onEditDraftSignersAndFile: () -> Unit = {},
    onOpenPositioningOrViewPdf: () -> Unit,
    onResendReminder: () -> Unit = {},
    onDownloadSignedDocument: () -> Unit = {},
    onRetryLoad: () -> Unit = {},
    onDeleteDraft: () -> Unit,
    onDismissSnackbar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val doc = uiState.document
    val isCompleted = doc?.status == DocumentStatus.COMPLETED ||
        (uiState.totalSigners > 0 && uiState.signedCount == uiState.totalSigners)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LetsSignColors.Mist,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LetsSignColors.Mist)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
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
                        Text(
                            text = "‹",
                            fontSize = 26.sp,
                            color = LetsSignColors.Ink
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Detalhe do documento",
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
        },
        bottomBar = {
            if (!uiState.isLoading && !uiState.hasError && doc != null) {
                when {
                    uiState.isDraft -> {
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
                                    text = "Signatários",
                                    onClick = onEditDraftSignersAndFile,
                                    modifier = Modifier.weight(0.45f)
                                )
                                LetsSignPrimaryButton(
                                    text = "Posicionar",
                                    onClick = onOpenPositioningOrViewPdf,
                                    modifier = Modifier.weight(0.55f)
                                )
                            }
                        }
                    }

                    !isCompleted -> {
                        // Detalhe Enviado.png e Detalhe Parcial.png: "Reenviar lembrete" / "Reenviar lembrete para Beatriz"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .navigationBarsPadding()
                        ) {
                            HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                LetsSignSecondaryButton(
                                    text = uiState.reminderButtonLabel,
                                    onClick = onResendReminder,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    isCompleted && !uiState.isDownloaded -> {
                        // Detalhe Concluído.png: "Baixar documento assinado"
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .navigationBarsPadding()
                        ) {
                            HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 16.dp)
                            ) {
                                LetsSignPrimaryButton(
                                    text = "Baixar documento assinado",
                                    onClick = onDownloadSignedDocument,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    // Detalhe Carregando.png
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        LetsSignSkeletonCard()
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(14.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(LetsSignColors.Line.copy(alpha = 0.7f))
                        )
                        repeat(3) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(LetsSignColors.Line.copy(alpha = 0.7f))
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.6f)
                                            .height(14.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(LetsSignColors.Line.copy(alpha = 0.8f))
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.45f)
                                            .height(11.dp)
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(LetsSignColors.Line.copy(alpha = 0.6f))
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(88.dp)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(999.dp))
                                        .background(LetsSignColors.Line.copy(alpha = 0.6f))
                                )
                            }
                        }
                    }
                }

                uiState.hasError -> {
                    // Detalhe Erro.png
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
                            text = "Verifique sua conexão e tente novamente. Nada foi alterado no documento.",
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

                doc != null -> {
                    val (chipLabel, chipStyle, barColor) = when {
                        doc.status == DocumentStatus.DRAFT -> Triple(
                            "Rascunho",
                            StatusChipStyle.DRAFT,
                            LetsSignColors.VioletSoft
                        )
                        isCompleted -> Triple(
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
                            LetsSignColors.VioletSoft
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = 24.dp,
                                end = 24.dp,
                                top = 8.dp,
                                bottom = if (uiState.isDownloaded) 96.dp else 28.dp
                            )
                    ) {
                        // Card Principal do Documento (clicável para abrir e visualizar o PDF!)
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(22.dp))
                                .clickable(onClick = onOpenPositioningOrViewPdf),
                            shape = RoundedCornerShape(22.dp),
                            color = Color.White,
                            shadowElevation = 2.dp
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
                                            text = "${doc.effectiveDisplayFileName} · ${doc.pageCount} páginas",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = LetsSignColors.Muted,
                                                fontSize = 15.sp
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LetsSignStatusChip(label = chipLabel, style = chipStyle)
                                    Text(
                                        text = "${uiState.signedCount} de ${uiState.totalSigners} assinaturas",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = LetsSignColors.Ink,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp
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
                                    progress = { if (isCompleted) 1f else progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(999.dp)),
                                    color = barColor,
                                    trackColor = LetsSignColors.VioletSoft
                                )
                            }
                        }

                        // Banner Verde de Download (Detalhe Baixado.png)
                        if (uiState.isDownloaded) {
                            Spacer(modifier = Modifier.height(18.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(LetsSignColors.SuccessSoft)
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, LetsSignColors.SuccessText, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CheckIcon(color = LetsSignColors.SuccessText, size = 12.dp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Documento assinado salvo em Downloads: ${uiState.downloadedFileName ?: "documento-assinado.pdf"}",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = LetsSignColors.SuccessText,
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp
                                    )
                                )
                            }
                        } else {
                            // Seção Signatários (Detalhe Enviado, Parcial, Concluído)
                            Spacer(modifier = Modifier.height(22.dp))
                            Text(
                                text = "Signatários (${uiState.totalSigners})",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            uiState.signers.forEachIndexed { index, item ->
                                val isSigned = item.documentSigner.status == SignatureStatus.SIGNED
                                val roleLabel = item.documentSigner.roleClass.displayLabel
                                val subtitle = if (isSigned && item.documentSigner.signedAt != null) {
                                    "$roleLabel · Assinou em\n${DateTimeFormatters.formatTimestampWithAs(item.documentSigner.signedAt)}"
                                } else {
                                    val sentDate = DateTimeFormatters.formatDateOnly(doc.sentAt ?: doc.createdAt)
                                    "$roleLabel · Convite\nenviado em $sentDate"
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LetsSignAvatar(fullName = item.signer.name, size = 44.dp)
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.signer.name,
                                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = LetsSignColors.Muted,
                                                fontSize = 15.sp,
                                                lineHeight = 22.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    LetsSignStatusChip(
                                        label = if (isSigned) "Assinou" else "Aguardando",
                                        style = if (isSigned) StatusChipStyle.COMPLETED else StatusChipStyle.WAITING
                                    )
                                }

                                if (index < uiState.signers.lastIndex) {
                                    HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                                }
                            }
                        }

                        // Seção Acompanhamento (Linha do Tempo Vertical)
                        Spacer(modifier = Modifier.height(22.dp))
                        Text(
                            text = "Acompanhamento",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        val steps = uiState.timelineSteps
                        steps.forEachIndexed { idx, step ->
                            val isLast = idx == steps.lastIndex
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.width(28.dp)
                                ) {
                                    when (step.visualState) {
                                        TimelineStepVisualState.COMPLETED_CHECK -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(LetsSignColors.SuccessSoft),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CheckIcon(color = LetsSignColors.Success, size = 13.dp)
                                            }
                                        }
                                        TimelineStepVisualState.WAITING_CLOCK -> {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(LetsSignColors.VioletSoft),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                ClockIcon(color = LetsSignColors.Primary, size = 14.dp)
                                            }
                                        }
                                        TimelineStepVisualState.FUTURE_DASHED -> {
                                            Box(
                                                modifier = Modifier.size(26.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Canvas(modifier = Modifier.size(24.dp)) {
                                                    drawCircle(
                                                        color = LetsSignColors.Line,
                                                        style = Stroke(
                                                            width = 1.5.dp.toPx(),
                                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    if (!isLast) {
                                        Box(
                                            modifier = Modifier
                                                .width(1.5.dp)
                                                .height(34.dp)
                                                .background(LetsSignColors.Line)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 14.dp)) {
                                    Text(
                                        text = step.title,
                                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = step.subtitle,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = LetsSignColors.Muted,
                                            fontSize = 14.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Snackbar ("Download concluído · Abrir" em Detalhe Baixado.png ou "Lembrete enviado")
            if (!uiState.snackbarMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 18.dp)
                ) {
                    LetsSignSnackbar(
                        message = uiState.snackbarMessage,
                        type = LetsSignSnackbarType.SUCCESS_DARK,
                        actionLabel = uiState.snackbarActionLabel,
                        onActionClick = {
                            if (uiState.snackbarActionLabel == "Abrir") {
                                onOpenPositioningOrViewPdf()
                            }
                            onDismissSnackbar()
                        }
                    )
                }
            }
        }
    }
}
