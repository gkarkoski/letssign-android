package com.example.challenge_letssign.ui.admin.positioning

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureFieldType
import com.example.challenge_letssign.ui.components.ChevronRightIcon
import com.example.challenge_letssign.ui.components.CloseIcon
import com.example.challenge_letssign.ui.components.EditPenIcon
import com.example.challenge_letssign.ui.components.ErrorCircleIcon
import com.example.challenge_letssign.ui.components.LetsSignAiButton
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignSnackbar
import com.example.challenge_letssign.ui.components.LetsSignSnackbarType
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.LetsSignTextButton
import com.example.challenge_letssign.ui.components.LetsSignWizardStepper
import com.example.challenge_letssign.ui.components.SparklesIcon
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.components.TrashIcon
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.PdfCoordinateConverter
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun PdfPositioningScreen(
    documentId: String,
    onBack: () -> Unit,
    onContinueToReview: (String) -> Unit,
    viewModel: PdfPositioningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(documentId) {
        viewModel.loadDocument(documentId)
    }

    LaunchedEffect(uiState.isReadyForReview) {
        if (uiState.isReadyForReview) {
            onContinueToReview(documentId)
            viewModel.onReviewNavigationHandled()
        }
    }

    LaunchedEffect(uiState.snackbarMessage, uiState.isDraggingOutOfBounds) {
        if (uiState.snackbarMessage != null && !uiState.isDraggingOutOfBounds) {
            delay(3000)
            viewModel.onDismissSnackbar()
        }
    }

    PdfPositioningContent(
        uiState = uiState,
        onBack = onBack,
        onSelectPage = viewModel::onSelectPage,
        onSelectSigner = viewModel::onSelectSigner,
        onSelectField = viewModel::onSelectField,
        onOpenAddFieldSheet = viewModel::onOpenAddFieldSheet,
        onDismissAddFieldSheet = viewModel::onDismissAddFieldSheet,
        onSelectAddFieldPickerSigner = viewModel::onSelectAddFieldPickerSigner,
        onConfirmAddFieldFromSheet = viewModel::onConfirmAddFieldFromSheet,
        onSuggestFieldsWithAiClick = { viewModel.onSuggestFieldsWithAiClick() },
        onCancelAiAnalysis = viewModel::onCancelAiAnalysis,
        onOpenReviewSingleAiSuggestion = viewModel::onOpenReviewSingleAiSuggestion,
        onChangeReviewingSigner = viewModel::onChangeReviewingSigner,
        onChangeReviewingFieldType = viewModel::onChangeReviewingFieldType,
        onAcceptSingleReviewedAiSuggestion = viewModel::onAcceptSingleReviewedAiSuggestion,
        onDiscardSingleReviewedAiSuggestion = viewModel::onDiscardSingleReviewedAiSuggestion,
        onAcceptAllAiSuggestions = viewModel::onAcceptAllAiSuggestions,
        onUpdateDraggingOutOfBounds = viewModel::onUpdateDraggingOutOfBounds,
        onDropFieldInPixels = viewModel::onDropSignatureFieldInPixels,
        onDeleteField = viewModel::onDeleteSignatureField,
        onContinue = {
            // Na navegação do Figma (fluxo_telas.md), Continuar de Novo Doc Campos leva a Novo Doc Revisão
            // (que exibe Revisão normal ou Revisão com Pendência caso falte campo de algum signatário).
            viewModel.onContinueToReview(allowWithPendingFields = true)
        },
        onDismissSnackbar = viewModel::onDismissSnackbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfPositioningContent(
    uiState: PdfPositioningUiState,
    onBack: () -> Unit,
    onSelectPage: (Int) -> Unit,
    onSelectSigner: (String) -> Unit,
    onSelectField: (String?) -> Unit = {},
    onOpenAddFieldSheet: () -> Unit = {},
    onDismissAddFieldSheet: () -> Unit = {},
    onSelectAddFieldPickerSigner: (String) -> Unit = {},
    onConfirmAddFieldFromSheet: () -> Unit = {},
    onSuggestFieldsWithAiClick: () -> Unit = {},
    onCancelAiAnalysis: () -> Unit = {},
    onOpenReviewSingleAiSuggestion: (String) -> Unit = {},
    onChangeReviewingSigner: (String) -> Unit = {},
    onChangeReviewingFieldType: (SignatureFieldType) -> Unit = {},
    onAcceptSingleReviewedAiSuggestion: () -> Unit = {},
    onDiscardSingleReviewedAiSuggestion: () -> Unit = {},
    onAcceptAllAiSuggestions: () -> Unit = {},
    onUpdateDraggingOutOfBounds: (Boolean) -> Unit = {},
    onDropFieldInPixels: (SignatureField, Float, Float, Float, Float, Float, Float) -> Boolean,
    onDeleteField: (String) -> Unit,
    onContinue: () -> Unit,
    onDismissSnackbar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LetsSignColors.Mist,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp)
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
                            text = if (uiState.isEditable) "Campos de assinatura" else (uiState.document?.title ?: "Visualizar PDF"),
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (uiState.isEditable) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LetsSignWizardStepper(currentStep = 4, totalSteps = 6)
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)

                // Barra 1: < Página X de Y > e botão "+ Campo" (Novo Doc Campos.png)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    enabled = !uiState.isLoadingPage && uiState.currentPageIndex > 0,
                                    onClick = { onSelectPage(uiState.currentPageIndex - 1) }
                                )
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "‹",
                                fontSize = 22.sp,
                                color = if (!uiState.isLoadingPage && uiState.currentPageIndex > 0) {
                                    LetsSignColors.Ink
                                } else {
                                    LetsSignColors.Muted
                                }
                            )
                        }
                        if (!uiState.isLoadingPage) {
                            Text(
                                text = "Página ${uiState.currentPageIndex + 1} de ${uiState.totalPages}",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(
                                        enabled = uiState.currentPageIndex < uiState.totalPages - 1,
                                        onClick = { onSelectPage(uiState.currentPageIndex + 1) }
                                    )
                                    .padding(6.dp)
                            ) {
                                Text(
                                    text = "›",
                                    fontSize = 22.sp,
                                    color = if (uiState.currentPageIndex < uiState.totalPages - 1) {
                                        LetsSignColors.Ink
                                    } else {
                                        LetsSignColors.Muted
                                    }
                                )
                            }
                        }
                    }

                    if (uiState.isEditable) {
                        Button(
                            onClick = onOpenAddFieldSheet,
                            enabled = !uiState.isLoadingPage,
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LetsSignColors.Primary,
                                contentColor = Color.White,
                                disabledContainerColor = LetsSignColors.Primary.copy(alpha = 0.45f),
                                disabledContentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
                            modifier = Modifier.heightIn(min = 42.dp)
                        ) {
                            Text(
                                text = "+ Campo",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)

                // Barra 2: Pílulas dos Signatários (● Marina 2 | ● Rafael 1 | ● Beatriz 1)
                if (uiState.signers.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.signers.forEachIndexed { index, signerWithUser ->
                            val signer = signerWithUser.signer
                            val isSelected = signer.id == uiState.selectedSignerId
                            val palette = SignerPalette.styleForIndex(index)
                            val count = uiState.allFields.count { it.signerId == signer.id }

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(
                                        if (isSelected) palette.backgroundColor else Color.White
                                    )
                                    .border(
                                        width = if (isSelected) 1.75.dp else 1.25.dp,
                                        color = if (isSelected) palette.borderColor else LetsSignColors.Line,
                                        shape = RoundedCornerShape(999.dp)
                                    )
                                    .clickable { onSelectSigner(signer.id) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(palette.borderColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = signer.name.substringBefore(" "),
                                    color = LetsSignColors.Ink,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (!uiState.isLoadingPage && count > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = count.toString(),
                                        color = LetsSignColors.Muted,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                }

                // Barra 3: Botão centralizado "✨ Sugerir posições com IA"
                if (uiState.isEditable && !uiState.isLoadingPage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LetsSignAiButton(
                            text = "Sugerir posições com IA",
                            onClick = onSuggestFieldsWithAiClick
                        )
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
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LetsSignSecondaryButton(
                        text = "Voltar",
                        onClick = onBack,
                        modifier = Modifier.weight(0.42f)
                    )
                    if (uiState.isEditable) {
                        LetsSignPrimaryButton(
                            text = "Continuar",
                            onClick = onContinue,
                            enabled = !uiState.isLoadingPage,
                            modifier = Modifier.weight(0.58f)
                        )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                PdfInteractiveCanvasBox(
                    uiState = uiState,
                    onSelectField = onSelectField,
                    onUpdateDraggingOutOfBounds = onUpdateDraggingOutOfBounds,
                    onDropFieldInPixels = onDropFieldInPixels,
                    onDeleteField = onDeleteField
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Snackbar ("O campo precisa ficar dentro da página." em Novo Doc Campo Fora dos Limites.png)
            if (!uiState.snackbarMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    LetsSignSnackbar(
                        message = uiState.snackbarMessage,
                        type = if (uiState.isErrorSnackbar) {
                            LetsSignSnackbarType.ERROR
                        } else {
                            LetsSignSnackbarType.SUCCESS_DARK
                        }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet: Novo Doc Adicionar Campo.png
    if (uiState.isAddFieldSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissAddFieldSheet,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            AddSignatureFieldSheetContent(
                signers = uiState.signers,
                selectedSignerId = uiState.addFieldPickerSignerId ?: uiState.selectedSignerId,
                currentPageNumberOneBased = uiState.currentPageIndex + 1,
                onSelectSigner = onSelectAddFieldPickerSigner,
                onConfirmAdd = onConfirmAddFieldFromSheet
            )
        }
    }

    // Diálogo Modal: Novo Doc IA Analisando.png
    if (uiState.aiModalState == AiFieldsModalState.ANALYZING) {
        AiAnalyzingFieldsDialog(
            totalPages = uiState.totalPages,
            onCancel = onCancelAiAnalysis
        )
    }

    // Modal Bottom Sheets da IA nos Campos (Sugestões, Revisar Campo, Nada Encontrado, Falha)
    val showAiSheet = uiState.aiModalState in setOf(
        AiFieldsModalState.SUGGESTIONS,
        AiFieldsModalState.REVIEWING_SINGLE,
        AiFieldsModalState.NOTHING_FOUND,
        AiFieldsModalState.ERROR
    )
    if (showAiSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onCancelAiAnalysis,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            when (uiState.aiModalState) {
                AiFieldsModalState.SUGGESTIONS -> {
                    AiSuggestionsSheetContent(
                        suggestions = uiState.aiSuggestedFields,
                        distinctPagesCount = uiState.distinctAiSuggestedPagesCount,
                        signers = uiState.signers,
                        onReviewSingleClick = onOpenReviewSingleAiSuggestion,
                        onDiscardAll = onCancelAiAnalysis,
                        onAcceptAll = onAcceptAllAiSuggestions
                    )
                }
                AiFieldsModalState.REVIEWING_SINGLE -> {
                    val reviewing = uiState.reviewingAiSuggestion
                    if (reviewing != null) {
                        AiReviewSingleFieldSheetContent(
                            suggestion = reviewing,
                            signers = uiState.signers,
                            selectedSignerId = uiState.reviewingSignerId ?: reviewing.signerId,
                            selectedFieldType = uiState.reviewingFieldType,
                            onChangeSigner = onChangeReviewingSigner,
                            onChangeFieldType = onChangeReviewingFieldType,
                            onDiscard = onDiscardSingleReviewedAiSuggestion,
                            onAccept = onAcceptSingleReviewedAiSuggestion
                        )
                    }
                }
                AiFieldsModalState.NOTHING_FOUND -> {
                    AiNothingFoundSheetContent(
                        onAddManuallyClick = onOpenAddFieldSheet,
                        onRetryClick = onSuggestFieldsWithAiClick
                    )
                }
                AiFieldsModalState.ERROR -> {
                    AiFailureSheetContent(
                        onRetryClick = onSuggestFieldsWithAiClick,
                        onPositionManuallyClick = onOpenAddFieldSheet
                    )
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun PdfInteractiveCanvasBox(
    uiState: PdfPositioningUiState,
    onSelectField: (String?) -> Unit,
    onUpdateDraggingOutOfBounds: (Boolean) -> Unit,
    onDropFieldInPixels: (SignatureField, Float, Float, Float, Float, Float, Float) -> Boolean,
    onDeleteField: (String) -> Unit
) {
    val rendered = uiState.renderedPage
    val aspectRatio = rendered?.aspectRatio ?: (1f / 1.414f)
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val signerIndexMap = remember(uiState.signers) {
        uiState.signers.mapIndexed { index, item -> item.signer.id to index }.toMap()
    }
    val signerNameMap = remember(uiState.signers) {
        uiState.signers.associate { it.signer.id to it.signer.name }
    }

    val pageBorderColor = if (uiState.isDraggingOutOfBounds) {
        LetsSignColors.Error
    } else {
        LetsSignColors.Line
    }
    val pageBorderWidth = if (uiState.isDraggingOutOfBounds) 2.dp else 1.25.dp

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(pageBorderWidth, pageBorderColor, RoundedCornerShape(12.dp))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null
                ) {
                    onSelectField(null)
                }
                .onSizeChanged { canvasSize = it }
        ) {
            if (uiState.isLoadingPage || rendered == null) {
                // Novo Doc Carregando Páginas.png
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Linhas em esqueleto no topo da folha
                    repeat(6) { idx ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (idx == 0) 0.65f else if (idx == 5) 0.35f else 0.92f)
                                .height(if (idx == 0) 10.dp else 7.dp)
                                .align(Alignment.Start)
                                .clip(RoundedCornerShape(999.dp))
                                .background(LetsSignColors.Line.copy(alpha = 0.7f))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    repeat(5) { idx ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(if (idx == 0) 0.45f else if (idx == 4) 0.55f else 0.90f)
                                .height(if (idx == 0) 10.dp else 7.dp)
                                .align(Alignment.Start)
                                .clip(RoundedCornerShape(999.dp))
                                .background(LetsSignColors.Line.copy(alpha = 0.7f))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    CircularProgressIndicator(
                        color = LetsSignColors.Primary,
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Carregando páginas...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }
            } else {
                Image(
                    bitmap = rendered.bitmap.asImageBitmap(),
                    contentDescription = "Página ${uiState.currentPageIndex + 1} do PDF",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                val viewWidthPx = canvasSize.width.toFloat()
                val viewHeightPx = canvasSize.height.toFloat()

                if (viewWidthPx > 0f && viewHeightPx > 0f) {
                    uiState.currentPageFields.forEach { field ->
                        val signerIndex = signerIndexMap[field.signerId] ?: 0
                        val palette = SignerPalette.styleForIndex(signerIndex)
                        val signerName = signerNameMap[field.signerId] ?: "Assinatura"
                        val isSelected = uiState.selectedFieldId == field.id

                        DraggableSignatureFieldBox(
                            field = field,
                            signerName = signerName,
                            palette = palette,
                            isSelected = isSelected,
                            isEditable = uiState.isEditable,
                            viewWidthPx = viewWidthPx,
                            viewHeightPx = viewHeightPx,
                            onSelectField = { onSelectField(field.id) },
                            onUpdateDraggingOutOfBounds = onUpdateDraggingOutOfBounds,
                            onDropInPixels = { dropX, dropY, wPx, hPx ->
                                onDropFieldInPixels(
                                    field,
                                    dropX,
                                    dropY,
                                    wPx,
                                    hPx,
                                    viewWidthPx,
                                    viewHeightPx
                                )
                            },
                            onDeleteClick = { onDeleteField(field.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableSignatureFieldBox(
    field: SignatureField,
    signerName: String,
    palette: SignerColorStyle,
    isSelected: Boolean,
    isEditable: Boolean,
    viewWidthPx: Float,
    viewHeightPx: Float,
    onSelectField: () -> Unit,
    onUpdateDraggingOutOfBounds: (Boolean) -> Unit,
    onDropInPixels: (Float, Float, Float, Float) -> Boolean,
    onDeleteClick: () -> Unit
) {
    val density = LocalDensity.current
    val initialBounds = PdfCoordinateConverter.relativeToPixels(
        relativeX = field.relativeX,
        relativeY = field.relativeY,
        relativeWidth = field.relativeWidth,
        relativeHeight = field.relativeHeight,
        viewWidthPx = viewWidthPx,
        viewHeightPx = viewHeightPx
    )

    var offsetXPx by remember(field.id, field.relativeX, field.relativeY, viewWidthPx, viewHeightPx) {
        mutableFloatStateOf(initialBounds.xPx)
    }
    var offsetYPx by remember(field.id, field.relativeX, field.relativeY, viewWidthPx, viewHeightPx) {
        mutableFloatStateOf(initialBounds.yPx)
    }
    var isDragging by remember(field.id) { mutableStateOf(false) }

    val widthPx = initialBounds.widthPx
    val heightPx = initialBounds.heightPx

    val currentRelCoords = PdfCoordinateConverter.pixelsToRelative(
        dropXPx = offsetXPx,
        dropYPx = offsetYPx,
        widthPx = widthPx,
        heightPx = heightPx,
        viewWidthPx = viewWidthPx,
        viewHeightPx = viewHeightPx
    )
    val isOutOfBounds = !PdfCoordinateConverter.isWithinPageBounds(currentRelCoords)

    LaunchedEffect(isDragging, isOutOfBounds) {
        if (isDragging) {
            onUpdateDraggingOutOfBounds(isOutOfBounds)
        }
    }

    val boxWidthDp = with(density) { widthPx.toDp() }
    val boxHeightDp = with(density) { heightPx.toDp() }

    val firstName = signerName.substringBefore(" ")
    val labelText = if (field.fieldType == SignatureFieldType.INITIALS) {
        "Rubrica · $firstName"
    } else {
        firstName
    }

    val activeBgColor = if (isOutOfBounds) LetsSignColors.ErrorSoft else palette.backgroundColor
    val activeBorderColor = if (isOutOfBounds) LetsSignColors.Error else palette.borderColor
    val activeTextColor = if (isOutOfBounds) LetsSignColors.Error else palette.textColor

    // Quando está sendo arrastado para fora dos limites (Novo Doc Campo Fora dos Limites.png),
    // mostra a caixa "fantasma" tracejada na posição original válida
    if (isDragging && isOutOfBounds) {
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = initialBounds.xPx.roundToInt(),
                        y = initialBounds.yPx.roundToInt()
                    )
                }
                .size(width = boxWidthDp, height = boxHeightDp)
                .clip(RoundedCornerShape(10.dp))
                .background(palette.backgroundColor.copy(alpha = 0.55f)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawRoundRect(
                    color = palette.borderColor.copy(alpha = 0.65f),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                EditPenIcon(color = palette.textColor.copy(alpha = 0.6f), size = 14.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = labelText,
                    color = palette.textColor.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .zIndex(if (isSelected || isDragging) 2f else 1f)
            .offset {
                IntOffset(
                    x = offsetXPx.roundToInt(),
                    y = offsetYPx.roundToInt()
                )
            }
    ) {
        // Barra flutuante superior escura quando selecionado (Novo Doc Campo Selecionado.png)
        if (isEditable && isSelected && !isOutOfBounds) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-44).dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(LetsSignColors.Ink)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                MoveFourWayIcon(color = Color.White, size = 18.dp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onDeleteClick)
                        .padding(2.dp)
                ) {
                    TrashIcon(color = Color.White, size = 18.dp)
                }
            }
        }

        // Caixa do campo
        Box(
            modifier = Modifier
                .size(width = boxWidthDp, height = boxHeightDp)
                .clip(RoundedCornerShape(10.dp))
                .background(activeBgColor.copy(alpha = 0.92f))
                .clickable { onSelectField() }
                .then(
                    if (isEditable) {
                        Modifier.pointerInput(field.id, viewWidthPx, viewHeightPx) {
                            detectDragGestures(
                                onDragStart = {
                                    isDragging = true
                                    onSelectField()
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetXPx += dragAmount.x
                                    offsetYPx += dragAmount.y
                                },
                                onDragEnd = {
                                    isDragging = false
                                    val accepted = onDropInPixels(
                                        offsetXPx,
                                        offsetYPx,
                                        widthPx,
                                        heightPx
                                    )
                                    if (!accepted) {
                                        offsetXPx = initialBounds.xPx
                                        offsetYPx = initialBounds.yPx
                                    }
                                },
                                onDragCancel = {
                                    isDragging = false
                                    onUpdateDraggingOutOfBounds(false)
                                    offsetXPx = initialBounds.xPx
                                    offsetYPx = initialBounds.yPx
                                }
                            )
                        }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeStyle = if (isSelected || isOutOfBounds) {
                    Stroke(width = 1.75.dp.toPx())
                } else {
                    Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                }
                drawRoundRect(
                    color = activeBorderColor,
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = strokeStyle
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                EditPenIcon(color = activeTextColor, size = 14.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = labelText,
                    color = activeTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Tooltip inferior "Arraste para posicionar" quando selecionado (Novo Doc Campo Selecionado.png)
        if (isEditable && isSelected && !isOutOfBounds) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(LetsSignColors.Ink)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Arraste para posicionar",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// --- Modal Bottom Sheet: Novo Doc Adicionar Campo.png ---

@Composable
private fun AddSignatureFieldSheetContent(
    signers: List<DocumentSignerWithUser>,
    selectedSignerId: String?,
    currentPageNumberOneBased: Int,
    onSelectSigner: (String) -> Unit,
    onConfirmAdd: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
    ) {
        Text(
            text = "Adicionar campo de assinatura",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Escolha quem vai assinar neste campo. Ele será colocado na página $currentPageNumberOneBased.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LetsSignColors.Muted,
                fontSize = 15.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        signers.forEachIndexed { index, signerWithUser ->
            val signer = signerWithUser.signer
            val isSelected = signer.id == selectedSignerId
            val colorDot = SignerPalette.styleForIndex(index).borderColor

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSigner(signer.id) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelectSigner(signer.id) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = LetsSignColors.Primary,
                        unselectedColor = LetsSignColors.Muted
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colorDot)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = signer.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                LetsSignStatusChip(
                    label = signerWithUser.documentSigner.roleClass.displayLabel,
                    style = StatusChipStyle.ROLE
                )
            }
            if (index < signers.lastIndex) {
                HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LetsSignPrimaryButton(
            text = "Adicionar na página $currentPageNumberOneBased",
            onClick = onConfirmAdd,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// --- Diálogo e Bottom Sheets da IA nos Campos ---

@Composable
private fun AiAnalyzingFieldsDialog(
    totalPages: Int,
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
                    text = "Procurando onde assinar",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Lendo as ${totalPages.coerceAtLeast(1)} páginas para achar assinaturas e rubricas. Só esta análise usa o documento.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = LetsSignColors.Muted,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                LinearProgressIndicator(
                    progress = { 0.55f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp)),
                    color = LetsSignColors.Primary,
                    trackColor = LetsSignColors.VioletSoft
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Página ${4.coerceAtMost(totalPages.coerceAtLeast(1))} de ${totalPages.coerceAtLeast(1)}",
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
private fun AiSuggestionsSheetContent(
    suggestions: List<AiFieldSuggestionItem>,
    distinctPagesCount: Int,
    signers: List<DocumentSignerWithUser>,
    onReviewSingleClick: (String) -> Unit,
    onDiscardAll: () -> Unit,
    onAcceptAll: () -> Unit
) {
    val signerIndexMap = remember(signers) {
        signers.mapIndexed { idx, s -> s.signer.id to idx }.toMap()
    }
    val signerNameMap = remember(signers) {
        signers.associate { it.signer.id to it.signer.name.substringBefore(" ") }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SparklesIcon(color = LetsSignColors.Primary, size = 22.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${suggestions.size} campos sugeridos",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Em $distinctPagesCount páginas. Nada é salvo até você aceitar.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LetsSignColors.Muted,
                fontSize = 15.sp
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        suggestions.take(3).forEachIndexed { idx, item ->
            val signerIdx = signerIndexMap[item.signerId] ?: 0
            val colorDot = SignerPalette.styleForIndex(signerIdx).borderColor
            val firstName = signerNameMap[item.signerId] ?: "Signatário"

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onReviewSingleClick(item.id) }
                    .padding(vertical = 12.dp),
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
                        text = "${item.fieldType.displayLabel} · $firstName",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Pág. ${item.pageNumber + 1} · ${item.contextHint}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 14.sp
                        )
                    )
                }
                ChevronRightIcon(color = LetsSignColors.Muted, size = 18.dp)
            }
            if (idx < suggestions.take(3).lastIndex) {
                HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LetsSignSecondaryButton(
                text = "Descartar",
                onClick = onDiscardAll,
                modifier = Modifier.weight(0.42f)
            )
            LetsSignPrimaryButton(
                text = "Aceitar ${suggestions.size} campos",
                onClick = onAcceptAll,
                modifier = Modifier.weight(0.58f)
            )
        }
    }
}

@Composable
private fun AiReviewSingleFieldSheetContent(
    suggestion: AiFieldSuggestionItem,
    signers: List<DocumentSignerWithUser>,
    selectedSignerId: String,
    selectedFieldType: SignatureFieldType,
    onChangeSigner: (String) -> Unit,
    onChangeFieldType: (SignatureFieldType) -> Unit,
    onDiscard: () -> Unit,
    onAccept: () -> Unit
) {
    var signerMenuExpanded by remember { mutableStateOf(false) }
    val selectedSignerIdx = signers.indexOfFirst { it.signer.id == selectedSignerId }.coerceAtLeast(0)
    val selectedSignerName = signers.getOrNull(selectedSignerIdx)?.signer?.name ?: "Signatário"
    val selectedDotColor = SignerPalette.styleForIndex(selectedSignerIdx).borderColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
    ) {
        Text(
            text = "Revisar campo",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Encontrado ${suggestion.contextHint}, página ${suggestion.pageNumber + 1}.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LetsSignColors.Muted,
                fontSize = 15.sp
            )
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Signatário",
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
                    .height(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(10.dp))
                    .clickable { signerMenuExpanded = true }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(selectedDotColor)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = selectedSignerName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = LetsSignColors.Ink,
                        fontSize = 17.sp
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(text = "⌄", color = LetsSignColors.Muted, fontSize = 18.sp)
            }

            DropdownMenu(
                expanded = signerMenuExpanded,
                onDismissRequest = { signerMenuExpanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                signers.forEach { item ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = item.signer.name,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = LetsSignColors.Ink
                                )
                            )
                        },
                        onClick = {
                            signerMenuExpanded = false
                            onChangeSigner(item.signer.id)
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Tipo de campo",
            style = MaterialTheme.typography.bodySmall.copy(
                color = LetsSignColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Seletor segmentado Assinatura vs Rubrica (Novo Doc IA Revisar Campo.png)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(LetsSignColors.Mist)
                .border(1.dp, LetsSignColors.Line, RoundedCornerShape(999.dp))
                .padding(4.dp)
        ) {
            SignatureFieldType.entries.forEach { type ->
                val isSelected = type == selectedFieldType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { onChangeFieldType(type) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = type.displayLabel,
                        color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Body,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LetsSignSecondaryButton(
                text = "Descartar",
                onClick = onDiscard,
                modifier = Modifier.weight(0.42f)
            )
            LetsSignPrimaryButton(
                text = "Aceitar campo",
                onClick = onAccept,
                modifier = Modifier.weight(0.58f)
            )
        }
    }
}

@Composable
private fun AiNothingFoundSheetContent(
    onAddManuallyClick: () -> Unit,
    onRetryClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LetsSignColors.VioletSoft),
            contentAlignment = Alignment.Center
        ) {
            SparklesIcon(color = LetsSignColors.Primary, size = 28.dp)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Nenhum campo encontrado",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Não achei linhas de assinatura neste documento. Isso é comum em PDFs escaneados ou sem texto.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = LetsSignColors.Muted,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(22.dp))
        LetsSignPrimaryButton(
            text = "Adicionar campo manualmente",
            onClick = onAddManuallyClick,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        LetsSignTextButton(
            text = "Tentar de novo",
            onClick = onRetryClick
        )
    }
}

@Composable
private fun AiFailureSheetContent(
    onRetryClick: () -> Unit,
    onPositionManuallyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(LetsSignColors.ErrorSoft),
            contentAlignment = Alignment.Center
        ) {
            ErrorCircleIcon(color = LetsSignColors.Error, size = 28.dp)
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "Não foi possível analisar",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Houve um problema de conexão. Seu documento e os campos que você já criou foram mantidos.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = LetsSignColors.Muted,
                fontSize = 16.sp
            ),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(22.dp))
        LetsSignPrimaryButton(
            text = "Tentar novamente",
            onClick = onRetryClick,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        LetsSignTextButton(
            text = "Posicionar manualmente",
            onClick = onPositionManuallyClick
        )
    }
}

@Composable
private fun MoveFourWayIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.7.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.12f),
            end = Offset(w * 0.5f, h * 0.88f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.12f, h * 0.5f),
            end = Offset(w * 0.88f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        val arrows = Path().apply {
            // Top
            moveTo(w * 0.36f, h * 0.26f)
            lineTo(w * 0.5f, h * 0.12f)
            lineTo(w * 0.64f, h * 0.26f)
            // Bottom
            moveTo(w * 0.36f, h * 0.74f)
            lineTo(w * 0.5f, h * 0.88f)
            lineTo(w * 0.64f, h * 0.74f)
            // Left
            moveTo(w * 0.26f, h * 0.36f)
            lineTo(w * 0.12f, h * 0.5f)
            lineTo(w * 0.26f, h * 0.64f)
            // Right
            moveTo(w * 0.74f, h * 0.36f)
            lineTo(w * 0.88f, h * 0.5f)
            lineTo(w * 0.74f, h * 0.64f)
        }
        drawPath(
            path = arrows,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}
