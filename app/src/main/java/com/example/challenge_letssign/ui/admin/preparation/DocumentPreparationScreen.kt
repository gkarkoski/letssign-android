package com.example.challenge_letssign.ui.admin.preparation

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.ui.components.CheckIcon
import com.example.challenge_letssign.ui.components.CloseIcon
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.ErrorCircleIcon
import com.example.challenge_letssign.ui.components.FileAlertIcon
import com.example.challenge_letssign.ui.components.LetsSignAvatar
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignTextField
import com.example.challenge_letssign.ui.components.LetsSignWizardStepper
import com.example.challenge_letssign.ui.components.PlusIcon
import com.example.challenge_letssign.ui.components.SearchIcon
import com.example.challenge_letssign.ui.components.SyncArrowsIcon
import com.example.challenge_letssign.ui.components.TrashIcon
import com.example.challenge_letssign.ui.components.UploadTrayIcon
import com.example.challenge_letssign.ui.components.UserPlusIcon
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors

@Composable
fun DocumentPreparationScreen(
    ownerAdminId: String,
    onCancel: () -> Unit,
    onDraftPrepared: (Document) -> Unit,
    viewModel: DocumentPreparationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // ACTION_OPEN_DOCUMENT com mimetype application/pdf conforme Milestone 4
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.onPdfUriSelected(
                uriString = uri.toString(),
                fallbackName = uri.lastPathSegment
            )
        }
    }

    LaunchedEffect(uiState.completedDraftDocument) {
        val doc = uiState.completedDraftDocument
        if (doc != null) {
            onDraftPrepared(doc)
            viewModel.onDraftSavedHandled()
        }
    }

    DocumentPreparationContent(
        uiState = uiState,
        onCloseOrCancel = onCancel,
        onTitleChanged = viewModel::onTitleChanged,
        onPickPdfClick = {
            pdfPickerLauncher.launch(arrayOf("application/pdf"))
        },
        onContinueStep1 = viewModel::onContinueFromStep1,
        onBackToStep1 = viewModel::onBackToStep1,
        onOpenSignerSelectionSheet = viewModel::onOpenSignerSelectionSheet,
        onDismissSignerSelectionSheet = viewModel::onDismissSignerSelectionSheet,
        onPickerSearchQueryChanged = viewModel::onPickerSearchQueryChanged,
        onTogglePickerSignerSelection = viewModel::onTogglePickerSignerSelection,
        onConfirmPickerSelection = viewModel::onConfirmPickerSelection,
        onSignerRoleChanged = viewModel::onSignerRoleChanged,
        onRemoveAssignedSigner = viewModel::onRemoveAssignedSigner,
        onContinueStep2 = {
            viewModel.onSaveDraftAndContinue(ownerAdminId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentPreparationContent(
    uiState: DocumentPreparationUiState,
    onCloseOrCancel: () -> Unit,
    onTitleChanged: (String) -> Unit,
    onPickPdfClick: () -> Unit,
    onContinueStep1: () -> Unit,
    onBackToStep1: () -> Unit,
    onOpenSignerSelectionSheet: () -> Unit,
    onDismissSignerSelectionSheet: () -> Unit,
    onPickerSearchQueryChanged: (String) -> Unit,
    onTogglePickerSignerSelection: (String) -> Unit,
    onConfirmPickerSelection: () -> Unit,
    onSignerRoleChanged: (String, SignerRoleClass) -> Unit,
    onRemoveAssignedSigner: (String) -> Unit,
    onContinueStep2: () -> Unit,
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = onCloseOrCancel)
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
                LetsSignWizardStepper(currentStep = uiState.currentStep, totalSteps = 5)
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
                    if (uiState.currentStep == 1) {
                        LetsSignSecondaryButton(
                            text = "Cancelar",
                            onClick = onCloseOrCancel,
                            modifier = Modifier.weight(0.42f)
                        )
                        LetsSignPrimaryButton(
                            text = "Continuar",
                            onClick = onContinueStep1,
                            enabled = uiState.canAdvanceFromStep1,
                            modifier = Modifier.weight(0.58f)
                        )
                    } else {
                        LetsSignSecondaryButton(
                            text = "Voltar",
                            onClick = onBackToStep1,
                            modifier = Modifier.weight(0.42f)
                        )
                        LetsSignPrimaryButton(
                            text = if (uiState.isSavingDraft) "Salvando..." else "Continuar",
                            onClick = onContinueStep2,
                            enabled = uiState.canAdvanceFromStep2,
                            isLoading = uiState.isSavingDraft,
                            modifier = Modifier.weight(0.58f)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.currentStep == 1) {
            Step1TitleAndFileContent(
                uiState = uiState,
                onTitleChanged = onTitleChanged,
                onPickPdfClick = onPickPdfClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            Step2AssignSignersContent(
                uiState = uiState,
                onOpenSignerSelectionSheet = onOpenSignerSelectionSheet,
                onSignerRoleChanged = onSignerRoleChanged,
                onRemoveAssignedSigner = onRemoveAssignedSigner,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }

    // Modal Bottom Sheet de Seleção de Signatários (novo_doc_signatarios_selecao_signatarios.png)
    if (uiState.isSignerSelectionSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissSignerSelectionSheet,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            SignerSelectionSheetContent(
                uiState = uiState,
                onDismiss = onDismissSignerSelectionSheet,
                onSearchQueryChanged = onPickerSearchQueryChanged,
                onToggleSigner = onTogglePickerSignerSelection,
                onConfirmSelection = onConfirmPickerSelection
            )
        }
    }
}

@Composable
private fun Step1TitleAndFileContent(
    uiState: DocumentPreparationUiState,
    onTitleChanged: (String) -> Unit,
    onPickPdfClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Título e arquivo",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        LetsSignTextField(
            label = "Título do documento",
            value = uiState.title,
            onValueChange = onTitleChanged,
            placeholder = "Ex.: Contrato de serviço",
            errorMessage = uiState.titleError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Arquivo PDF",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LetsSignColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        when (uiState.pdfStatus) {
            PdfImportStatus.IDLE -> {
                PdfIdleDropzoneCard(onPickPdfClick = onPickPdfClick)
            }
            PdfImportStatus.IMPORTING -> {
                PdfProcessingCard(
                    fileName = uiState.failedFileName ?: "documento.pdf",
                    progress = 0.66f,
                    subtitle = "Processando páginas · 66%"
                )
            }
            PdfImportStatus.READY -> {
                val imported = uiState.importedPdf
                if (imported != null) {
                    PdfReadyCard(
                        importedPdf = imported,
                        onChangePdfClick = onPickPdfClick
                    )
                }
            }
            PdfImportStatus.ERROR -> {
                PdfErrorCard(
                    errorMessage = uiState.pdfErrorMessage
                        ?: "Não foi possível processar o arquivo. Escolha outro arquivo PDF.",
                    onRetryPickPdfClick = onPickPdfClick
                )
            }
        }
    }
}

@Composable
private fun PdfIdleDropzoneCard(
    onPickPdfClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(LetsSignColors.Mist)
            .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(22.dp))
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(LetsSignColors.VioletSoft),
            contentAlignment = Alignment.Center
        ) {
            UploadTrayIcon(color = LetsSignColors.Primary, size = 26.dp)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Selecionar PDF do celular",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "somente arquivos .pdf",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = LetsSignColors.Muted
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(22.dp))
        LetsSignSecondaryButton(
            text = "Escolher arquivo",
            onClick = onPickPdfClick
        )
    }
}

@Composable
private fun PdfProcessingCard(
    fileName: String,
    progress: Float,
    subtitle: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp
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
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LetsSignColors.VioletSoft),
                    contentAlignment = Alignment.Center
                ) {
                    SyncArrowsIcon(color = LetsSignColors.Primary, size = 24.dp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Muted
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { progress },
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

@Composable
private fun PdfReadyCard(
    importedPdf: ImportedPdfFile,
    onChangePdfClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 4.dp
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
                        text = importedPdf.originalDisplayName,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val pageLabel = if (importedPdf.pageCount == 1) "1 página" else "${importedPdf.pageCount} páginas"
                    Text(
                        text = "$pageLabel · Pronto para configurar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Success
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = LetsSignColors.Primary,
                trackColor = LetsSignColors.VioletSoft
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                LetsSignSecondaryButton(
                    text = "Trocar arquivo",
                    onClick = onChangePdfClick
                )
            }
        }
    }
}

@Composable
private fun PdfErrorCard(
    errorMessage: String,
    onRetryPickPdfClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(LetsSignColors.ErrorSoft)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = LetsSignColors.Error,
                cornerRadius = CornerRadius(22.dp.toPx(), 22.dp.toPx()),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                )
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(LetsSignColors.Error.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                FileAlertIcon(color = LetsSignColors.Error, size = 26.dp)
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Arquivo inválido ou corrompido",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = LetsSignColors.Error
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = LetsSignColors.Muted
                ),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(22.dp))
            LetsSignSecondaryButton(
                text = "Escolher outro arquivo",
                onClick = onRetryPickPdfClick
            )
        }
    }
}

@Composable
private fun Step2AssignSignersContent(
    uiState: DocumentPreparationUiState,
    onOpenSignerSelectionSheet: () -> Unit,
    onSignerRoleChanged: (String, SignerRoleClass) -> Unit,
    onRemoveAssignedSigner: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Quem vai assinar?",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Escolha os signatários e classifique cada um como Contratante, Contratado ou Testemunha.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = LetsSignColors.Muted
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.assignedSigners.isEmpty()) {
            // Estado vazio idêntico a novo_doc_signatarios.png
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(LetsSignColors.Mist)
                    .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(22.dp))
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(LetsSignColors.VioletSoft),
                    contentAlignment = Alignment.Center
                ) {
                    UserPlusIcon(color = LetsSignColors.Primary, size = 28.dp)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nenhuma signatário selecionado",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                LetsSignPrimaryButton(
                    text = "Adicionar signatários",
                    onClick = onOpenSignerSelectionSheet,
                    leadingIcon = {
                        PlusIcon(color = Color.White, size = 18.dp)
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Banner violeta-soft: "Selecione ao menos 1 signatário para continuar."
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LetsSignColors.VioletSoft)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.Top
            ) {
                ErrorCircleIcon(color = LetsSignColors.Primary, size = 20.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Selecione ao menos 1 signatário para continuar.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = LetsSignColors.Primary
                    )
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                uiState.assignedSigners.forEach { assigned ->
                    AssignedSignerRoleCard(
                        assignedSigner = assigned,
                        onRoleSelected = { role ->
                            onSignerRoleChanged(assigned.user.id, role)
                        },
                        onRemoveClick = {
                            onRemoveAssignedSigner(assigned.user.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            LetsSignSecondaryButton(
                text = "+ Adicionar ou editar signatários",
                onClick = onOpenSignerSelectionSheet,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (!uiState.generalError.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(LetsSignColors.ErrorSoft)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ErrorCircleIcon(color = LetsSignColors.Error, size = 18.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = uiState.generalError,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.Error
                    )
                )
            }
        }
    }
}

@Composable
private fun AssignedSignerRoleCard(
    assignedSigner: AssignedSignerUiModel,
    onRoleSelected: (SignerRoleClass) -> Unit,
    onRemoveClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LetsSignAvatar(fullName = assignedSigner.user.name)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assignedSigner.user.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = assignedSigner.user.email,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 14.sp
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onRemoveClick)
                        .padding(8.dp)
                ) {
                    TrashIcon(color = LetsSignColors.Error, size = 18.dp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Papel no documento:",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LetsSignColors.Body,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SignerRoleClass.entries.forEach { role ->
                    val isSelected = assignedSigner.roleClass == role
                    val roleLabel = when (role) {
                        SignerRoleClass.CONTRATANTE -> "Contratante"
                        SignerRoleClass.CONTRATADO -> "Contratado"
                        SignerRoleClass.TESTEMUNHA -> "Testemunha"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (isSelected) LetsSignColors.VioletSoft else Color.White
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Line,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .clickable { onRoleSelected(role) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = roleLabel,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) LetsSignColors.VioletDeep else LetsSignColors.Ink
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SignerSelectionSheetContent(
    uiState: DocumentPreparationUiState,
    onDismiss: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleSigner: (String) -> Unit,
    onConfirmSelection: () -> Unit
) {
    val filteredSigners = uiState.filteredPickerSigners
    val selectedCount = uiState.pickerSelectedIds.size
    val buttonLabel = when (selectedCount) {
        0 -> "Confirmar seleção"
        1 -> "Adicionar 1 selecionado"
        else -> "Adicionar $selectedCount selecionados"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Selecionar signatários",
                style = MaterialTheme.typography.headlineMedium
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onDismiss)
                    .padding(6.dp)
            ) {
                CloseIcon(color = LetsSignColors.Primary, size = 20.dp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search input idêntico ao novo_doc_signatarios_selecao_signatarios.png
        BasicTextField(
            value = uiState.pickerSearchQuery,
            onValueChange = onSearchQueryChanged,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = LetsSignColors.Ink),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchIcon(color = LetsSignColors.Primary, size = 20.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (uiState.pickerSearchQuery.isEmpty()) {
                            Text(
                                text = "Buscar por nome ou e-mail",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = LetsSignColors.Muted
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items = filteredSigners, key = { it.id }) { signer ->
                val isChecked = signer.id in uiState.pickerSelectedIds
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleSigner(signer.id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LetsSignCheckbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleSigner(signer.id) }
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        LetsSignAvatar(fullName = signer.name, size = 46.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = signer.name,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = signer.email,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LetsSignColors.Muted
                                )
                            )
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LetsSignPrimaryButton(
            text = buttonLabel,
            onClick = onConfirmSelection,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun LetsSignCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) LetsSignColors.Primary else Color.White)
            .border(
                width = 1.5.dp,
                color = if (checked) LetsSignColors.Primary else LetsSignColors.Line,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            CheckIcon(color = Color.White, size = 15.dp)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Step1Preview() {
    Challenge_letsSignTheme {
        DocumentPreparationContent(
            uiState = DocumentPreparationUiState(
                currentStep = 1,
                title = "Contrato de serviço"
            ),
            onCloseOrCancel = {},
            onTitleChanged = {},
            onPickPdfClick = {},
            onContinueStep1 = {},
            onBackToStep1 = {},
            onOpenSignerSelectionSheet = {},
            onDismissSignerSelectionSheet = {},
            onPickerSearchQueryChanged = {},
            onTogglePickerSignerSelection = {},
            onConfirmPickerSelection = {},
            onSignerRoleChanged = { _, _ -> },
            onRemoveAssignedSigner = {},
            onContinueStep2 = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Step2EmptyPreview() {
    Challenge_letsSignTheme {
        DocumentPreparationContent(
            uiState = DocumentPreparationUiState(
                currentStep = 2,
                title = "Contrato de serviço",
                pdfStatus = PdfImportStatus.READY,
                importedPdf = ImportedPdfFile("doc_1.pdf", "contrato-servicos.pdf", 3)
            ),
            onCloseOrCancel = {},
            onTitleChanged = {},
            onPickPdfClick = {},
            onContinueStep1 = {},
            onBackToStep1 = {},
            onOpenSignerSelectionSheet = {},
            onDismissSignerSelectionSheet = {},
            onPickerSearchQueryChanged = {},
            onTogglePickerSignerSelection = {},
            onConfirmPickerSelection = {},
            onSignerRoleChanged = { _, _ -> },
            onRemoveAssignedSigner = {},
            onContinueStep2 = {}
        )
    }
}
