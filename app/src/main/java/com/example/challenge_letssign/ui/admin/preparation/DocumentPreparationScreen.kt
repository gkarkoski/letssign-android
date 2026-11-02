package com.example.challenge_letssign.ui.admin.preparation

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.model.AdditionalAuthOption
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.model.SignerAuthMethod
import com.example.challenge_letssign.domain.model.SignerDeliveryChannel
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.ui.components.CheckIcon
import com.example.challenge_letssign.ui.components.CloseIcon
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.ErrorCircleIcon
import com.example.challenge_letssign.ui.components.FileAlertIcon
import com.example.challenge_letssign.ui.components.LetsSignAiButton
import com.example.challenge_letssign.ui.components.LetsSignAvatar
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSearchBar
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignTextField
import com.example.challenge_letssign.ui.components.LetsSignWizardStepper
import com.example.challenge_letssign.ui.components.LockIcon
import com.example.challenge_letssign.ui.components.MailIcon
import com.example.challenge_letssign.ui.components.PlusIcon
import com.example.challenge_letssign.ui.components.SparklesIcon
import com.example.challenge_letssign.ui.components.SyncArrowsIcon
import com.example.challenge_letssign.ui.components.TrashIcon
import com.example.challenge_letssign.ui.components.UploadTrayIcon
import com.example.challenge_letssign.ui.components.UserPlusIcon
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors

@Composable
fun DocumentPreparationScreen(
    ownerAdminId: String,
    existingDocumentId: String? = null,
    onCancel: () -> Unit,
    onDraftPrepared: (Document) -> Unit,
    viewModel: DocumentPreparationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(existingDocumentId) {
        viewModel.loadExistingDraftIfNeeded(existingDocumentId)
    }

    BackHandler {
        when {
            uiState.activeStep3Sheet != null -> viewModel.onDismissStep3Sheet()
            uiState.isSignerSelectionSheetOpen -> viewModel.onDismissSignerSelectionSheet()
            uiState.isAiDetectingSigners || uiState.isReviewingAiSigners -> viewModel.onCancelAiSignerDetection()
            uiState.currentStep == 3 -> viewModel.onBackFromStep3()
            uiState.currentStep == 2 -> viewModel.onBackToStep1()
            else -> onCancel()
        }
    }

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
        onClearPdfClick = viewModel::onClearPdfFile,
        onContinueStep1 = viewModel::onContinueFromStep1,
        onBackToStep1 = viewModel::onBackToStep1,
        onOpenSignerSelectionSheet = viewModel::onOpenSignerSelectionSheet,
        onRequestReplaceSigner = viewModel::onRequestReplaceSigner,
        onDismissSignerSelectionSheet = viewModel::onDismissSignerSelectionSheet,
        onPickerSearchQueryChanged = viewModel::onPickerSearchQueryChanged,
        onTogglePickerSignerSelection = viewModel::onTogglePickerSignerSelection,
        onTogglePickerGroupSelection = viewModel::onTogglePickerGroupSelection,
        onConfirmPickerSelection = viewModel::onConfirmPickerSelection,
        onSuggestSignersWithAiClick = viewModel::onSuggestSignersWithAiClick,
        onCancelAiSignerDetection = viewModel::onCancelAiSignerDetection,
        onToggleAiSuggestedSigner = viewModel::onToggleAiSuggestedSigner,
        onConfirmAiSuggestedSigners = viewModel::onConfirmAiSuggestedSigners,
        onSignerRoleChanged = viewModel::onSignerRoleChanged,
        onRemoveAssignedSigner = viewModel::onRemoveAssignedSigner,
        onContinueStep2 = viewModel::onContinueFromStep2,
        onBackFromStep3 = viewModel::onBackFromStep3,
        onToggleStep3SignerExpanded = viewModel::onToggleStep3SignerExpanded,
        onOpenDeliveryLinkSheet = viewModel::onOpenDeliveryLinkSheet,
        onSelectDeliveryChannel = viewModel::onSelectDeliveryChannel,
        onOpenAuthMethodSheet = viewModel::onOpenAuthMethodSheet,
        onSelectAuthMethod = viewModel::onSelectAuthMethod,
        onOpenAdditionalAuthsSheet = viewModel::onOpenAdditionalAuthsSheet,
        onTogglePendingAdditionalAuth = viewModel::onTogglePendingAdditionalAuth,
        onApplyAdditionalAuths = viewModel::onApplyAdditionalAuths,
        onSignerLanguageChanged = viewModel::onSignerLanguageChanged,
        onDismissStep3Sheet = viewModel::onDismissStep3Sheet,
        onContinueStep3 = {
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
    onClearPdfClick: () -> Unit = {},
    onContinueStep1: () -> Unit,
    onBackToStep1: () -> Unit,
    onOpenSignerSelectionSheet: () -> Unit,
    onRequestReplaceSigner: (String) -> Unit = {},
    onDismissSignerSelectionSheet: () -> Unit,
    onPickerSearchQueryChanged: (String) -> Unit,
    onTogglePickerSignerSelection: (String) -> Unit,
    onTogglePickerGroupSelection: (String) -> Unit = {},
    onConfirmPickerSelection: () -> Unit,
    onSuggestSignersWithAiClick: () -> Unit = {},
    onCancelAiSignerDetection: () -> Unit = {},
    onToggleAiSuggestedSigner: (String) -> Unit = {},
    onConfirmAiSuggestedSigners: () -> Unit = {},
    onSignerRoleChanged: (String, SignerRoleClass) -> Unit,
    onRemoveAssignedSigner: (String) -> Unit,
    onContinueStep2: () -> Unit,
    onBackFromStep3: () -> Unit = {},
    onToggleStep3SignerExpanded: (String) -> Unit = {},
    onOpenDeliveryLinkSheet: (String) -> Unit = {},
    onSelectDeliveryChannel: (String, SignerDeliveryChannel) -> Unit = { _, _ -> },
    onOpenAuthMethodSheet: (String) -> Unit = {},
    onSelectAuthMethod: (String, SignerAuthMethod) -> Unit = { _, _ -> },
    onOpenAdditionalAuthsSheet: (String) -> Unit = {},
    onTogglePendingAdditionalAuth: (AdditionalAuthOption) -> Unit = {},
    onApplyAdditionalAuths: () -> Unit = {},
    onSignerLanguageChanged: (String, String) -> Unit = { _, _ -> },
    onDismissStep3Sheet: () -> Unit = {},
    onContinueStep3: () -> Unit = {},
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
                LetsSignWizardStepper(currentStep = uiState.currentStep, totalSteps = 6)
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
                    when {
                        uiState.currentStep == 1 -> {
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
                        }
                        uiState.currentStep == 2 && uiState.isReviewingAiSigners -> {
                            LetsSignSecondaryButton(
                                text = "Voltar",
                                onClick = onCancelAiSignerDetection,
                                modifier = Modifier.weight(0.42f)
                            )
                            LetsSignPrimaryButton(
                                text = "Adicionar ${uiState.selectedAiSignersCount}",
                                onClick = onConfirmAiSuggestedSigners,
                                enabled = uiState.selectedAiSignersCount > 0,
                                modifier = Modifier.weight(0.58f)
                            )
                        }
                        uiState.currentStep == 2 -> {
                            LetsSignSecondaryButton(
                                text = "Voltar",
                                onClick = onBackToStep1,
                                modifier = Modifier.weight(0.42f)
                            )
                            LetsSignPrimaryButton(
                                text = "Continuar",
                                onClick = onContinueStep2,
                                enabled = uiState.canAdvanceFromStep2,
                                modifier = Modifier.weight(0.58f)
                            )
                        }
                        else -> {
                            // Etapa 3: Envio e Autenticação
                            LetsSignSecondaryButton(
                                text = "Voltar",
                                onClick = onBackFromStep3,
                                modifier = Modifier.weight(0.42f)
                            )
                            LetsSignPrimaryButton(
                                text = if (uiState.isSavingDraft) "Salvando..." else "Continuar",
                                onClick = onContinueStep3,
                                enabled = uiState.canAdvanceFromStep2,
                                isLoading = uiState.isSavingDraft,
                                modifier = Modifier.weight(0.58f)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        when (uiState.currentStep) {
            1 -> {
                Step1TitleAndFileContent(
                    uiState = uiState,
                    onTitleChanged = onTitleChanged,
                    onPickPdfClick = onPickPdfClick,
                    onClearPdfClick = onClearPdfClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            2 -> {
                Step2AssignSignersContent(
                    uiState = uiState,
                    onOpenSignerSelectionSheet = onOpenSignerSelectionSheet,
                    onRequestReplaceSigner = onRequestReplaceSigner,
                    onSuggestSignersWithAiClick = onSuggestSignersWithAiClick,
                    onToggleAiSuggestedSigner = onToggleAiSuggestedSigner,
                    onSignerRoleChanged = onSignerRoleChanged,
                    onRemoveAssignedSigner = onRemoveAssignedSigner,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            else -> {
                Step3DeliveryAndAuthContent(
                    uiState = uiState,
                    onToggleSignerExpanded = onToggleStep3SignerExpanded,
                    onOpenDeliveryLinkSheet = onOpenDeliveryLinkSheet,
                    onOpenAuthMethodSheet = onOpenAuthMethodSheet,
                    onOpenAdditionalAuthsSheet = onOpenAdditionalAuthsSheet,
                    onSignerLanguageChanged = onSignerLanguageChanged,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }

    // Diálogo Novo Doc IA Detectando.png
    if (uiState.isAiDetectingSigners) {
        AiDetectingSignersDialog(
            pageCount = uiState.importedPdf?.pageCount ?: 6,
            onCancel = onCancelAiSignerDetection
        )
    }

    // Modal Bottom Sheet de Seleção de Signatários (Novo Doc Selecionar Signatários / Busca)
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
                onToggleGroup = onTogglePickerGroupSelection,
                onConfirmSelection = onConfirmPickerSelection
            )
        }
    }

    // Modal Bottom Sheets da Etapa 3 (Envio Link, Envio Método, Envio Adicionais)
    val step3Sheet = uiState.activeStep3Sheet
    if (step3Sheet != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissStep3Sheet,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            when (step3Sheet) {
                is Step3ModalSheet.DeliveryLinkSheet -> {
                    val currentChannel = uiState.assignedSigners
                        .firstOrNull { it.user.id == step3Sheet.signerId }
                        ?.deliveryChannel
                        ?: SignerDeliveryChannel.EMAIL
                    DeliveryChannelSheetContent(
                        selectedChannel = currentChannel,
                        onDismiss = onDismissStep3Sheet,
                        onSelectChannel = { channel ->
                            onSelectDeliveryChannel(step3Sheet.signerId, channel)
                        }
                    )
                }
                is Step3ModalSheet.AuthMethodSheet -> {
                    val currentMethod = uiState.assignedSigners
                        .firstOrNull { it.user.id == step3Sheet.signerId }
                        ?.authMethod
                        ?: SignerAuthMethod.EMAIL
                    AuthMethodSheetContent(
                        selectedMethod = currentMethod,
                        onDismiss = onDismissStep3Sheet,
                        onSelectMethod = { method ->
                            onSelectAuthMethod(step3Sheet.signerId, method)
                        }
                    )
                }
                is Step3ModalSheet.AdditionalAuthsSheet -> {
                    AdditionalAuthsSheetContent(
                        selectedOptions = step3Sheet.pendingSelections,
                        onDismiss = onDismissStep3Sheet,
                        onToggleOption = onTogglePendingAdditionalAuth,
                        onApply = onApplyAdditionalAuths
                    )
                }
            }
        }
    }
}

@Composable
private fun Step1TitleAndFileContent(
    uiState: DocumentPreparationUiState,
    onTitleChanged: (String) -> Unit,
    onPickPdfClick: () -> Unit,
    onClearPdfClick: () -> Unit,
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
                    fileName = uiState.failedFileName ?: "contrato-servicos.pdf",
                    progress = 0.66f,
                    subtitle = "Processando páginas · 66%"
                )
            }
            PdfImportStatus.READY -> {
                val imported = uiState.importedPdf
                if (imported != null) {
                    PdfReadyCard(
                        importedPdf = imported,
                        onClearPdfClick = onClearPdfClick,
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
    onClearPdfClick: () -> Unit,
    onChangePdfClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                            text = "$pageLabel · ${importedPdf.formattedFileSize}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LetsSignColors.Muted
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onClearPdfClick)
                            .padding(6.dp)
                    ) {
                        CloseIcon(color = LetsSignColors.Primary, size = 18.dp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(LetsSignColors.SuccessSoft)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, LetsSignColors.Success, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        CheckIcon(color = LetsSignColors.Success, size = 12.dp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Arquivo válido e pronto para uso.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.SuccessText,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Trocar arquivo",
            style = MaterialTheme.typography.titleMedium.copy(
                color = LetsSignColors.Primary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onChangePdfClick)
                .padding(horizontal = 4.dp, vertical = 6.dp)
        )
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

// --- Etapa 2: Quem vai assinar? ---

@Composable
private fun Step2AssignSignersContent(
    uiState: DocumentPreparationUiState,
    onOpenSignerSelectionSheet: () -> Unit,
    onRequestReplaceSigner: (String) -> Unit,
    onSuggestSignersWithAiClick: () -> Unit,
    onToggleAiSuggestedSigner: (String) -> Unit,
    onSignerRoleChanged: (String, SignerRoleClass) -> Unit,
    onRemoveAssignedSigner: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        if (uiState.isReviewingAiSigners) {
            // Novo Doc Signatários detectados pela IA.png
            Text(
                text = "Quem vai assinar?",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Encontramos ${uiState.aiSuggestedSigners.size} possíveis signatários no documento. Desmarque quem não deve assinar.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = LetsSignColors.Muted
                )
            )
            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.aiSuggestedSigners.forEachIndexed { index, item ->
                    AiSuggestedSignerCard(
                        item = item,
                        colorDot = LetsSignColors.signerColor(index),
                        onToggle = { onToggleAiSuggestedSigner(item.user.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LetsSignColors.Mist)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.Top
            ) {
                ErrorCircleIcon(color = LetsSignColors.Primary, size = 20.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Confira os nomes e e-mails antes de adicionar. A IA pode errar.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.Body,
                        fontSize = 15.sp
                    )
                )
            }
            return
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Quem vai assinar?",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.weight(1f)
            )
            if (uiState.assignedSigners.isEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                LetsSignAiButton(
                    text = "Sugerir com IA",
                    onClick = onSuggestSignersWithAiClick
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Escolha os signatários e classifique cada um como Contratante, Contratado ou Testemunha.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = LetsSignColors.Muted
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.assignedSigners.isEmpty()) {
            // Novo Doc Adicionar Signatários.png
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
                    text = "Nenhum signatário selecionado",
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
            // Novo Doc Classificados.png
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                uiState.assignedSigners.forEachIndexed { index, assigned ->
                    AssignedSignerClassifiedCard(
                        assignedSigner = assigned,
                        colorDot = LetsSignColors.signerColor(index),
                        onRoleSelected = { role ->
                            onSignerRoleChanged(assigned.user.id, role)
                        },
                        onReplaceClick = {
                            onRequestReplaceSigner(assigned.user.id)
                        },
                        onRemoveClick = {
                            onRemoveAssignedSigner(assigned.user.id)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LetsSignSecondaryButton(
                text = "+ Adicionar signatários",
                onClick = onOpenSignerSelectionSheet,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AiSuggestedSignerCard(
    item: AiSuggestedSignerItem,
    colorDot: Color,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onToggle),
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
            LetsSignCheckbox(
                checked = item.isSelected,
                onCheckedChange = onToggle
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(colorDot)
            )
            Spacer(modifier = Modifier.width(10.dp))
            LetsSignAvatar(fullName = item.user.name, size = 44.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.user.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.user.email,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LetsSignColors.Muted,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(LetsSignColors.VioletSoft)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.suggestedRole.displayLabel,
                        color = LetsSignColors.Primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AssignedSignerClassifiedCard(
    assignedSigner: AssignedSignerUiModel,
    colorDot: Color,
    onRoleSelected: (SignerRoleClass) -> Unit,
    onReplaceClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    var roleMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
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
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colorDot)
                )
                Spacer(modifier = Modifier.width(10.dp))
                LetsSignAvatar(fullName = assignedSigner.user.name, size = 44.dp)
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
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                // Botão Substituir (⇄)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onReplaceClick)
                        .padding(8.dp)
                ) {
                    SwapHorizontalIcon(color = LetsSignColors.Primary, size = 20.dp)
                }
                // Botão Remover (Lixeira)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onRemoveClick)
                        .padding(8.dp)
                ) {
                    TrashIcon(color = LetsSignColors.Error, size = 20.dp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Classificação",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LetsSignColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(10.dp))
                        .clickable { roleMenuExpanded = true }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = assignedSigner.roleClass.displayLabel,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = LetsSignColors.Ink,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = "⌄",
                        color = LetsSignColors.Muted,
                        fontSize = 18.sp
                    )
                }

                DropdownMenu(
                    expanded = roleMenuExpanded,
                    onDismissRequest = { roleMenuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    SignerRoleClass.entries.forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = role.displayLabel,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = LetsSignColors.Ink
                                    )
                                )
                            },
                            onClick = {
                                roleMenuExpanded = false
                                onRoleSelected(role)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiDetectingSignersDialog(
    pageCount: Int,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
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
                    SparklesIcon(color = LetsSignColors.Primary, size = 28.dp)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Procurando signatários",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Lendo o documento para achar nomes e e-mails de quem vai assinar. Só esta análise usa o documento.",
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
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Página ${3.coerceAtMost(pageCount.coerceAtLeast(1))} de ${pageCount.coerceAtLeast(1)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LetsSignColors.Muted,
                        fontSize = 14.sp
                    )
                )
                Spacer(modifier = Modifier.height(22.dp))
                LetsSignSecondaryButton(
                    text = "Cancelar",
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth()
                )
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
    onToggleGroup: (String) -> Unit,
    onConfirmSelection: () -> Unit
) {
    val filteredSigners = uiState.filteredPickerSigners
    val matchingGroups = uiState.matchingPickerGroups
    val selectedCount = uiState.pickerSelectedIds.size
    val buttonLabel = when {
        uiState.replacingSignerId != null -> "Substituir signatário"
        selectedCount == 0 -> "Confirmar seleção"
        selectedCount == 1 -> "Adicionar 1 selecionado"
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

        LetsSignSearchBar(
            query = uiState.pickerSearchQuery,
            onQueryChanged = onSearchQueryChanged,
            placeholder = "Buscar por grupo, nome ou e-mail"
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            // Se houver grupos correspondentes (ex.: "Jurídico · 2 signatários" em Novo Doc Selecionar Signatários Busca.png)
            items(items = matchingGroups, key = { "group_${it.groupName}" }) { group ->
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleGroup(group.groupName) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LetsSignCheckbox(
                            checked = group.isFullySelected,
                            onCheckedChange = { onToggleGroup(group.groupName) }
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(LetsSignColors.VioletSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            UserPlusIcon(color = LetsSignColors.Primary, size = 22.dp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.groupName,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Grupo · ${group.members.size} signatários",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LetsSignColors.Muted
                                )
                            )
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                }
            }

            items(items = filteredSigners, key = { it.id }) { signer ->
                val isChecked = signer.id in uiState.pickerSelectedIds
                val isIndentedUnderGroup = matchingGroups.isNotEmpty() &&
                    matchingGroups.any { it.groupName.equals(signer.groupName, ignoreCase = true) }

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleSigner(signer.id) }
                            .padding(
                                start = if (isIndentedUnderGroup) 24.dp else 0.dp,
                                top = 12.dp,
                                bottom = 12.dp
                            ),
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
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
            enabled = selectedCount > 0,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- Etapa 3: Envio e Autenticação (Novo Doc Envio e Autenticação / Link / Método / Adicionais) ---

@Composable
private fun Step3DeliveryAndAuthContent(
    uiState: DocumentPreparationUiState,
    onToggleSignerExpanded: (String) -> Unit,
    onOpenDeliveryLinkSheet: (String) -> Unit,
    onOpenAuthMethodSheet: (String) -> Unit,
    onOpenAdditionalAuthsSheet: (String) -> Unit,
    onSignerLanguageChanged: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Envio e autenticação",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Defina como cada signatário recebe o link de assinatura e como confirma a identidade.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = LetsSignColors.Muted
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            uiState.assignedSigners.forEachIndexed { index, assigned ->
                val isExpanded = uiState.expandedStep3SignerId == assigned.user.id
                Step3SignerConfigCard(
                    assignedSigner = assigned,
                    colorDot = LetsSignColors.signerColor(index),
                    isExpanded = isExpanded,
                    onHeaderClick = { onToggleSignerExpanded(assigned.user.id) },
                    onOpenDeliveryLinkSheet = { onOpenDeliveryLinkSheet(assigned.user.id) },
                    onOpenAuthMethodSheet = { onOpenAuthMethodSheet(assigned.user.id) },
                    onOpenAdditionalAuthsSheet = { onOpenAdditionalAuthsSheet(assigned.user.id) },
                    onLanguageSelected = { lang -> onSignerLanguageChanged(assigned.user.id, lang) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun Step3SignerConfigCard(
    assignedSigner: AssignedSignerUiModel,
    colorDot: Color,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    onOpenDeliveryLinkSheet: () -> Unit,
    onOpenAuthMethodSheet: () -> Unit,
    onOpenAdditionalAuthsSheet: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    var languageMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(18.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onHeaderClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colorDot)
                )
                Spacer(modifier = Modifier.width(10.dp))
                LetsSignAvatar(fullName = assignedSigner.user.name, size = 44.dp)
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
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = if (isExpanded) "⌃" else "⌄",
                    color = LetsSignColors.Body,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(16.dp))

                // 1. Enviar link de assinatura por *
                Step3SelectField(
                    label = "Enviar link de assinatura por",
                    isRequired = true,
                    selectedText = assignedSigner.deliveryChannel.displayLabel,
                    leadingIcon = { MailIcon(color = LetsSignColors.Ink, size = 20.dp) },
                    onClick = onOpenDeliveryLinkSheet
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Método de autenticação *
                Step3SelectField(
                    label = "Método de autenticação",
                    isRequired = true,
                    selectedText = assignedSigner.authMethod.displayLabel,
                    leadingIcon = { MailIcon(color = LetsSignColors.Ink, size = 20.dp) },
                    onClick = onOpenAuthMethodSheet
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Autenticações adicionais
                val additionalText = when (val count = assignedSigner.additionalAuths.size) {
                    0 -> "Nenhuma"
                    1 -> assignedSigner.additionalAuths.first().displayLabel
                    else -> "$count opções selecionadas"
                }
                Step3SelectField(
                    label = "Autenticações adicionais",
                    isRequired = false,
                    selectedText = additionalText,
                    textColor = if (assignedSigner.additionalAuths.isEmpty()) {
                        LetsSignColors.Muted
                    } else {
                        LetsSignColors.Ink
                    },
                    leadingIcon = null,
                    onClick = onOpenAdditionalAuthsSheet
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Idioma
                Box(modifier = Modifier.fillMaxWidth()) {
                    Step3SelectField(
                        label = "Idioma",
                        isRequired = false,
                        selectedText = assignedSigner.language,
                        leadingIcon = null,
                        onClick = { languageMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = languageMenuExpanded,
                        onDismissRequest = { languageMenuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        listOf("Português", "Inglês", "Espanhol").forEach { lang ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = lang,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = LetsSignColors.Ink
                                        )
                                    )
                                },
                                onClick = {
                                    languageMenuExpanded = false
                                    onLanguageSelected(lang)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Step3SelectField(
    label: String,
    isRequired: Boolean,
    selectedText: String,
    textColor: Color = LetsSignColors.Ink,
    leadingIcon: (@Composable () -> Unit)?,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = LetsSignColors.Ink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                ) {
                    append(label)
                }
                if (isRequired) {
                    withStyle(
                        SpanStyle(
                            color = LetsSignColors.Ink,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    ) {
                        append(" *")
                    }
                }
            }
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(10.dp))
                .clickable(onClick = onClick)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = selectedText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = textColor,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "⌄",
                color = LetsSignColors.Muted,
                fontSize = 18.sp
            )
        }
    }
}

// --- Bottom Sheets da Etapa 3 ---

@Composable
private fun DeliveryChannelSheetContent(
    selectedChannel: SignerDeliveryChannel,
    onDismiss: () -> Unit,
    onSelectChannel: (SignerDeliveryChannel) -> Unit
) {
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
                text = "Enviar link de assinatura por",
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

        SignerDeliveryChannel.entries.forEach { option ->
            val isSelected = option == selectedChannel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) LetsSignColors.VioletSoft else Color.White)
                    .clickable { onSelectChannel(option) }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MailIcon(
                    color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Muted,
                    size = 22.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.displayLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 14.sp
                        )
                    )
                }
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelectChannel(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = LetsSignColors.Primary,
                        unselectedColor = LetsSignColors.Line
                    )
                )
            }
        }
    }
}

@Composable
private fun AuthMethodSheetContent(
    selectedMethod: SignerAuthMethod,
    onDismiss: () -> Unit,
    onSelectMethod: (SignerAuthMethod) -> Unit
) {
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
                text = "Método de autenticação",
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

        SignerAuthMethod.entries.forEach { option ->
            val isSelected = option == selectedMethod
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) LetsSignColors.VioletSoft else Color.White)
                    .clickable { onSelectMethod(option) }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (option == SignerAuthMethod.NO_AUTH) {
                    LockIcon(
                        color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Muted,
                        size = 22.dp
                    )
                } else {
                    MailIcon(
                        color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Muted,
                        size = 22.dp
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.displayLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 14.sp
                        )
                    )
                }
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelectMethod(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = LetsSignColors.Primary,
                        unselectedColor = LetsSignColors.Line
                    )
                )
            }
        }
    }
}

@Composable
private fun AdditionalAuthsSheetContent(
    selectedOptions: Set<AdditionalAuthOption>,
    onDismiss: () -> Unit,
    onToggleOption: (AdditionalAuthOption) -> Unit,
    onApply: () -> Unit
) {
    val count = selectedOptions.size
    val buttonLabel = when (count) {
        0 -> "Nenhuma opção"
        1 -> "Aplicar 1 opção"
        else -> "Aplicar $count opções"
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
                text = "Autenticações adicionais",
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

        AdditionalAuthOption.entries.forEach { option ->
            val isChecked = option in selectedOptions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isChecked) LetsSignColors.VioletSoft else Color.White)
                    .clickable { onToggleOption(option) }
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = option.displayLabel,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = option.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 14.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                LetsSignCheckbox(
                    checked = isChecked,
                    onCheckedChange = { onToggleOption(option) }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        LetsSignPrimaryButton(
            text = buttonLabel,
            onClick = onApply,
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

@Composable
private fun SwapHorizontalIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.75.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        // Top arrow pointing right
        drawLine(
            color = color,
            start = Offset(w * 0.18f, h * 0.34f),
            end = Offset(w * 0.82f, h * 0.34f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        val rightHead = Path().apply {
            moveTo(w * 0.64f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.34f)
            lineTo(w * 0.64f, h * 0.50f)
        }
        drawPath(
            path = rightHead,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Bottom arrow pointing left
        drawLine(
            color = color,
            start = Offset(w * 0.82f, h * 0.66f),
            end = Offset(w * 0.18f, h * 0.66f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        val leftHead = Path().apply {
            moveTo(w * 0.36f, h * 0.50f)
            lineTo(w * 0.18f, h * 0.66f)
            lineTo(w * 0.36f, h * 0.82f)
        }
        drawPath(
            path = leftHead,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
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
