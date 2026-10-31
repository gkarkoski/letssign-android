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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
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
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.ui.components.CloseIcon
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.ErrorCircleIcon
import com.example.challenge_letssign.ui.components.LetsSignAvatar
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.LetsSignWizardStepper
import com.example.challenge_letssign.ui.components.SendPlaneIcon
import com.example.challenge_letssign.ui.components.ShieldCheckIcon
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.theme.LetsSignColors

@Composable
fun DocumentReviewScreen(
    documentId: String,
    onBack: () -> Unit,
    onDocumentSentSuccess: (String) -> Unit,
    viewModel: DocumentReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    LaunchedEffect(uiState.isSentSuccess) {
        if (uiState.isSentSuccess) {
            onDocumentSentSuccess(documentId)
            viewModel.onSentNavigationHandled()
        }
    }

    DocumentReviewContent(
        uiState = uiState,
        onBack = onBack,
        onConfirmSend = viewModel::onSendDocument
    )
}

@Composable
fun DocumentReviewContent(
    uiState: DocumentReviewUiState,
    onBack: () -> Unit,
    onConfirmSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stepIndex = if (uiState.isSending) 5 else 4

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
                        text = "Novo Documento",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                LetsSignWizardStepper(currentStep = stepIndex, totalSteps = 5)
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
                        leadingIcon = {
                            SendPlaneIcon(color = Color.White, size = 16.dp)
                        },
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
            Text(
                text = "Revisar e enviar",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Confira o resumo das configurações antes de disparar o documento para os signatários.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = LetsSignColors.Muted
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Resumo do Documento
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = LetsSignColors.Mist
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(22.dp))
                        .padding(20.dp),
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
                            text = uiState.document?.title ?: "Documento PDF",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${uiState.signers.size} signatário(s) · ${uiState.fields.size} campo(s) posicionado(s)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LetsSignColors.Muted
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Signatários e campos atribuídos",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.signers.forEach { signerItem ->
                    val signerFields = uiState.fields.filter { it.signerId == signerItem.signer.id }
                    val pagesList = signerFields.map { it.pageNumber + 1 }.distinct().sorted()
                    val pagesText = if (pagesList.isEmpty()) {
                        "Nenhum campo posicionado"
                    } else {
                        "Pág. ${pagesList.joinToString(", ")}"
                    }
                    val roleLabel = when (signerItem.documentSigner.roleClass) {
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, LetsSignColors.Line, RoundedCornerShape(16.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LetsSignAvatar(fullName = signerItem.signer.name)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = signerItem.signer.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = signerItem.signer.email,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = LetsSignColors.Muted,
                                        fontSize = 14.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "${signerFields.size} campo(s) · $pagesText",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = LetsSignColors.Primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                            LetsSignStatusChip(
                                label = roleLabel,
                                style = StatusChipStyle.ROLE
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LetsSignColors.VioletSoft)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShieldCheckIcon(color = LetsSignColors.Primary, size = 20.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Após o envio, o documento ficará pendente para os signatários e seus campos não poderão mais ser alterados.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.VioletDeep,
                        fontSize = 14.sp
                    )
                )
            }

            if (!uiState.errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(LetsSignColors.ErrorSoft)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ErrorCircleIcon(color = LetsSignColors.Error, size = 18.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.errorMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Error
                        )
                    )
                }
            }
        }
    }
}
