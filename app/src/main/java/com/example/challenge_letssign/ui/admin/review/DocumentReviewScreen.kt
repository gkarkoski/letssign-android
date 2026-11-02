package com.example.challenge_letssign.ui.admin.review

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.ui.components.CheckIcon
import com.example.challenge_letssign.ui.components.ClockIcon
import com.example.challenge_letssign.ui.components.CloseIcon
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.ErrorCircleIcon
import com.example.challenge_letssign.ui.components.LetsSignErrorBanner
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.LetsSignTextButton
import com.example.challenge_letssign.ui.components.LetsSignTextField
import com.example.challenge_letssign.ui.components.LetsSignWizardStepper
import com.example.challenge_letssign.ui.components.SendPlaneIcon
import com.example.challenge_letssign.ui.components.ShieldCheckIcon
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.components.UserProfileIcon
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.DateTimeFormatters

@Composable
fun DocumentReviewScreen(
    documentId: String,
    onBack: () -> Unit,
    onPreviewDocumentPdf: (String) -> Unit = {},
    onTrackSentDocument: (String) -> Unit = {},
    onDocumentSentSuccess: (String) -> Unit,
    viewModel: DocumentReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    DocumentReviewContent(
        uiState = uiState,
        onBack = onBack,
        onAddMissingFieldsClick = onBack,
        onInviteMessageChanged = viewModel::onInviteMessageChanged,
        onConfirmSend = viewModel::onSendDocument,
        onRetrySend = viewModel::onSendDocument,
        onReturnToReviewFromError = viewModel::onReturnToReviewFromError,
        onTrackSentDocument = {
            viewModel.onSentNavigationHandled()
            onTrackSentDocument(documentId)
        },
        onBackToHomeAfterSend = {
            viewModel.onSentNavigationHandled()
            onDocumentSentSuccess(documentId)
        }
    )
}

@Composable
fun DocumentReviewContent(
    uiState: DocumentReviewUiState,
    onBack: () -> Unit,
    onAddMissingFieldsClick: () -> Unit = onBack,
    onInviteMessageChanged: (String) -> Unit,
    onConfirmSend: () -> Unit,
    onRetrySend: () -> Unit = onConfirmSend,
    onReturnToReviewFromError: () -> Unit = {},
    onTrackSentDocument: () -> Unit = {},
    onBackToHomeAfterSend: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Etapa 6: Novo Doc Enviado.png
    if (uiState.isSentSuccess) {
        DocumentSentSuccessScreenContent(
            uiState = uiState,
            onTrackDocumentClick = onTrackSentDocument,
            onBackToHomeClick = onBackToHomeAfterSend,
            modifier = modifier
        )
        return
    }

    // Etapa 6 (Falha): Novo Doc Falha no Envio.png
    if (!uiState.errorMessage.isNullOrBlank()) {
        DocumentSendFailureScreenContent(
            onRetryClick = onRetrySend,
            onReturnToReviewClick = onReturnToReviewFromError,
            modifier = modifier
        )
        return
    }

    // Etapa 5: Novo Doc Revisão.png / Novo Doc Revisão com Pendência.png (+ Diálogo Novo Doc Enviando.png)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!uiState.isSending) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onBack)
                                .padding(6.dp)
                        ) {
                            CloseIcon(color = LetsSignColors.Primary, size = 20.dp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = "Novo Documento",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                LetsSignWizardStepper(currentStep = 5, totalSteps = 6)
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
                        enabled = !uiState.isSending,
                        modifier = Modifier.weight(0.38f)
                    )
                    LetsSignPrimaryButton(
                        text = if (uiState.isSending) "Enviando..." else "Confirmar envio",
                        onClick = onConfirmSend,
                        enabled = uiState.canSend,
                        isLoading = uiState.isSending,
                        modifier = Modifier.weight(0.62f)
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
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Novo Doc Revisão com Pendência.png
            val firstMissingSigner = uiState.signersWithoutFields.firstOrNull()
            if (firstMissingSigner != null) {
                LetsSignErrorBanner(
                    text = "${firstMissingSigner.signer.name} ainda não tem campo de assinatura. Adicione ao menos um campo para poder enviar.",
                    actionLabel = "Adicionar campo",
                    onActionClick = onAddMissingFieldsClick
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = "Revisar e enviar",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Card do Documento PDF
            val doc = uiState.document
            val pageCount = doc?.pageCount ?: 6
            val displayFileName = doc?.effectiveDisplayFileName ?: "contrato-servicos.pdf"
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(18.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            text = doc?.title ?: "Contrato de serviços",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$displayFileName · $pageCount páginas",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LetsSignColors.Muted
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cabeçalho: Signatários e campos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Signatários e campos",
                    style = MaterialTheme.typography.titleMedium
                )
                val pagesLabel = if (uiState.distinctPagesCount == 1) "1 página" else "${uiState.distinctPagesCount} páginas"
                Text(
                    text = "${uiState.fields.size} campos · $pagesLabel",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LetsSignColors.Muted,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            uiState.signers.forEachIndexed { index, signerItem ->
                val signerFields = uiState.fields.filter { it.signerId == signerItem.signer.id }
                val pagesList = signerFields.map { it.pageNumber + 1 }.distinct().sorted()
                val colorDot = LetsSignColors.signerColor(index)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(colorDot)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = signerItem.signer.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 17.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (signerFields.isEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ErrorCircleIcon(color = LetsSignColors.Error, size = 15.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sem campo de assinatura",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = LetsSignColors.ErrorText,
                                        fontSize = 15.sp
                                    )
                                )
                            }
                        } else {
                            val fieldsCountLabel = if (signerFields.size == 1) "1 campo" else "${signerFields.size} campos"
                            val pagesText = if (pagesList.size == 1) {
                                "pág. ${pagesList.first()}"
                            } else {
                                "págs. ${pagesList.dropLast(1).joinToString(", ")} e ${pagesList.last()}"
                            }
                            Text(
                                text = "$fieldsCountLabel · $pagesText",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LetsSignColors.Muted,
                                    fontSize = 15.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            val channelLabel = signerItem.documentSigner.deliveryChannel.displayLabel.lowercase()
                            val authLabel = signerItem.documentSigner.authMethod.displayLabel.lowercase()
                            Text(
                                text = "Link por $channelLabel · Autenticação por $authLabel",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LetsSignColors.Muted,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    LetsSignStatusChip(
                        label = signerItem.documentSigner.roleClass.displayLabel,
                        style = StatusChipStyle.ROLE
                    )
                }

                if (index < uiState.signers.lastIndex) {
                    HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Mensagem do convite (Novo Doc Revisão.png)
            LetsSignTextField(
                label = "Mensagem do convite",
                value = uiState.inviteMessage,
                onValueChange = onInviteMessageChanged,
                placeholder = "Escreva uma mensagem para os signatários...",
                helperText = "Enviada junto com o convite a todos os signatários.",
                singleLine = false,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Diálogo Modal: Novo Doc Enviando.png
    if (uiState.isSending) {
        Dialog(onDismissRequest = {}) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(LetsSignColors.VioletSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        SendPlaneIcon(color = LetsSignColors.Primary, size = 26.dp)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Enviando solicitações",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Estamos registrando o documento e enviando o convite para ${uiState.signers.size} signatários. Não feche o app.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 16.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    LinearProgressIndicator(
                        progress = { 0.45f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = LetsSignColors.Primary,
                        trackColor = LetsSignColors.VioletSoft
                    )
                }
            }
        }
    }
}

/**
 * Etapa 6 de 6: Novo Doc Enviado.png
 */
@Composable
private fun DocumentSentSuccessScreenContent(
    uiState: DocumentReviewUiState,
    onTrackDocumentClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doc = uiState.document

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Novo Documento",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                LetsSignWizardStepper(currentStep = 6, totalSteps = 6)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LetsSignPrimaryButton(
                        text = "Acompanhar documento",
                        onClick = onTrackDocumentClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LetsSignTextButton(
                        text = "Voltar ao início",
                        onClick = onBackToHomeClick
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Card Escuro Gradiente ("Documento enviado!")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(brush = LetsSignColors.DarkSectionGradient)
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(brush = LetsSignColors.IconGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CheckIcon(color = Color.White, size = 14.dp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(22.dp))
                    Text(
                        text = "Documento enviado!",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontSize = 28.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Os convites já estão a caminho. Agora é só acompanhar as assinaturas.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.85f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Card Branco de Resumo
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(22.dp))
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
                        Text(
                            text = doc?.title ?: "Contrato de serviços",
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        LetsSignStatusChip(
                            label = "Enviado",
                            style = StatusChipStyle.SENT
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ClockIcon(color = LetsSignColors.Muted, size = 18.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Enviado em ${DateTimeFormatters.formatTimestamp(doc?.sentAt ?: uiState.sentTimestampMillis).replace(" · ", " às ")}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LetsSignColors.Body,
                                fontSize = 15.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserProfileIcon(color = LetsSignColors.Muted, size = 18.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "${uiState.signers.size} signatários convidados",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LetsSignColors.Body,
                                fontSize = 15.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Etapa 6 (Falha): Novo Doc Falha no Envio.png
 */
@Composable
private fun DocumentSendFailureScreenContent(
    onRetryClick: () -> Unit,
    onReturnToReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Novo Documento",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(20.dp))
                LetsSignWizardStepper(currentStep = 6, totalSteps = 6)
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LetsSignPrimaryButton(
                        text = "Tentar novamente",
                        onClick = onRetryClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LetsSignSecondaryButton(
                        text = "Voltar à revisão",
                        onClick = onReturnToReviewClick,
                        modifier = Modifier.fillMaxWidth()
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
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                text = "Não foi possível enviar",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Houve um problema de conexão durante o envio. Seu documento, os signatários e os campos foram mantidos.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = LetsSignColors.Muted
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(18.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "O que fazer agora",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(LetsSignColors.VioletSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1",
                                color = LetsSignColors.Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Verifique sua conexão com a internet.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(LetsSignColors.VioletSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "2",
                                color = LetsSignColors.Primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Toque em Tentar novamente.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LetsSignColors.VioletSoft)
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                ShieldCheckIcon(color = LetsSignColors.Primary, size = 20.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Nenhum convite será duplicado. Se algum já tiver saído, ele não será reenviado.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.Primary,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}
