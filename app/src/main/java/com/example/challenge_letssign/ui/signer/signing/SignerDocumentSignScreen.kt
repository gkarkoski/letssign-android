package com.example.challenge_letssign.ui.signer.signing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Typeface
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.res.ResourcesCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.R
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureFieldType
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.ui.components.CheckIcon
import com.example.challenge_letssign.ui.components.DocumentFileIcon
import com.example.challenge_letssign.ui.components.EditPenIcon
import com.example.challenge_letssign.ui.components.ErrorCircleIcon
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignSnackbar
import com.example.challenge_letssign.ui.components.LetsSignSnackbarType
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.LetsSignTextButton
import com.example.challenge_letssign.ui.components.LetsSignTextField
import com.example.challenge_letssign.ui.components.ShieldCheckIcon
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.theme.CaveatFamily
import com.example.challenge_letssign.ui.theme.DancingScriptFamily
import com.example.challenge_letssign.ui.theme.HomemadeAppleFamily
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.DateTimeFormatters
import com.example.challenge_letssign.utils.PdfCoordinateConverter
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

@Composable
fun SignerDocumentSignScreen(
    documentId: String,
    signerId: String,
    onBack: () -> Unit,
    onViewSignedDocuments: () -> Unit = onBack,
    viewModel: SignerDocumentSignViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.wizardStep != SignWizardStep.STEP_1_READ_DOCUMENT) {
        when (uiState.wizardStep) {
            SignWizardStep.STEP_2_CAPTURE_SIGNATURE,
            SignWizardStep.STEP_2_CAPTURE_INITIALS -> viewModel.onDismissDrawingModal()
            SignWizardStep.STEP_3_CONFIRM -> viewModel.onRedoFromConfirm()
            SignWizardStep.FAILURE -> viewModel.onReturnToDocumentFromFailure()
            else -> onBack()
        }
    }

    LaunchedEffect(documentId, signerId) {
        viewModel.loadForSigning(documentId, signerId)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            delay(3500)
            viewModel.onDismissSnackbar()
        }
    }

    SignerDocumentSignContent(
        uiState = uiState,
        onBack = onBack,
        onViewSignedDocuments = onViewSignedDocuments,
        onSelectPage = viewModel::onSelectPage,
        onFocusMyNextField = viewModel::onFocusMyNextField,
        onStartSignClick = viewModel::onOpenDrawingModal,
        onCancelCapture = viewModel::onDismissDrawingModal,
        onSignatureCaptureModeChanged = viewModel::onSignatureCaptureModeChanged,
        onTypedSignatureTextChanged = viewModel::onTypedSignatureTextChanged,
        onTypedSignatureFontChanged = viewModel::onTypedSignatureFontChanged,
        onSignatureCapturedAdvanceToInitials = viewModel::onSignatureCapturedAdvanceToInitials,
        onInitialsCaptureModeChanged = viewModel::onInitialsCaptureModeChanged,
        onTypedInitialsTextChanged = viewModel::onTypedInitialsTextChanged,
        onTypedInitialsFontChanged = viewModel::onTypedInitialsFontChanged,
        onInitialsCapturedAdvanceToConfirm = viewModel::onInitialsCapturedAdvanceToConfirm,
        onRedoFromConfirm = viewModel::onRedoFromConfirm,
        onConfirmAndSaveSignature = viewModel::onConfirmAndSaveSignature,
        onRetrySaveAfterFailure = viewModel::onRetrySaveAfterFailure,
        onReturnToDocumentFromFailure = viewModel::onReturnToDocumentFromFailure
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignerDocumentSignContent(
    uiState: SignerDocumentSignUiState,
    onBack: () -> Unit,
    onViewSignedDocuments: () -> Unit = onBack,
    onSelectPage: (Int) -> Unit,
    onFocusMyNextField: () -> Unit,
    onStartSignClick: () -> Unit,
    onCancelCapture: () -> Unit,
    onSignatureCaptureModeChanged: (SignatureCaptureMode) -> Unit = {},
    onTypedSignatureTextChanged: (String) -> Unit = {},
    onTypedSignatureFontChanged: (CursiveFontStyle) -> Unit = {},
    onSignatureCapturedAdvanceToInitials: (ByteArray) -> Unit = {},
    onInitialsCaptureModeChanged: (SignatureCaptureMode) -> Unit = {},
    onTypedInitialsTextChanged: (String) -> Unit = {},
    onTypedInitialsFontChanged: (CursiveFontStyle) -> Unit = {},
    onInitialsCapturedAdvanceToConfirm: (ByteArray) -> Unit = {},
    onRedoFromConfirm: () -> Unit = {},
    onConfirmAndSaveSignature: () -> Unit = {},
    onRetrySaveAfterFailure: () -> Unit = {},
    onReturnToDocumentFromFailure: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Tela 9: Assinar Concluída.png
    if (uiState.wizardStep == SignWizardStep.COMPLETED) {
        SignCompletedSuccessScreenContent(
            uiState = uiState,
            onViewSignedDocumentsClick = onViewSignedDocuments,
            onBackToHomeClick = onBack,
            modifier = modifier
        )
        return
    }

    // Tela 10: Assinar Falha.png
    if (uiState.wizardStep == SignWizardStep.FAILURE) {
        SignFailureScreenContent(
            onRetryClick = onRetrySaveAfterFailure,
            onBackToSignatureClick = onReturnToDocumentFromFailure,
            modifier = modifier
        )
        return
    }

    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    var pdfCanvasHeightPx by remember { mutableStateOf(0) }

    // Ao abrir ou trocar de página, rola automaticamente para focar na posição vertical onde está a caixa do signatário
    val firstFieldOnPage = uiState.currentPageMyFields.firstOrNull()
    LaunchedEffect(uiState.currentPageIndex, firstFieldOnPage?.id, pdfCanvasHeightPx) {
        if (firstFieldOnPage != null && pdfCanvasHeightPx > 0) {
            val targetPixelY = (firstFieldOnPage.relativeY * pdfCanvasHeightPx).roundToInt()
            val offsetScroll = (targetPixelY - with(density) { 120.dp.toPx() }.roundToInt()).coerceAtLeast(0)
            scrollState.animateScrollTo(offsetScroll)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = LetsSignColors.Mist,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.document?.title ?: "Contrato de serviços",
                            style = MaterialTheme.typography.headlineMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = uiState.topBarStepSubtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LetsSignColors.Muted,
                                fontSize = 15.sp
                            )
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

                // Barra de informações da página e campos ("Página 2 de 6 · 2 campos seus: assinatura e rubrica")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (uiState.totalPages > 1 && uiState.currentPageIndex > 0) {
                            Text(
                                text = "‹",
                                fontSize = 20.sp,
                                color = LetsSignColors.Primary,
                                modifier = Modifier.clickable { onSelectPage(uiState.currentPageIndex - 1) }
                            )
                        }
                        Text(
                            text = "Página ${uiState.currentPageIndex + 1} de ${uiState.totalPages}",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            modifier = Modifier.clickable(onClick = onFocusMyNextField)
                        )
                        if (uiState.totalPages > 1 && uiState.currentPageIndex < uiState.totalPages - 1) {
                            Text(
                                text = "›",
                                fontSize = 20.sp,
                                color = LetsSignColors.Primary,
                                modifier = Modifier.clickable { onSelectPage(uiState.currentPageIndex + 1) }
                            )
                        }
                    }

                    Text(
                        text = uiState.footerFieldsDescription,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 15.sp
                        )
                    )
                }

                HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LetsSignSecondaryButton(
                        text = "Depois",
                        onClick = onBack,
                        modifier = Modifier.weight(0.42f)
                    )
                    if (uiState.mySignatureStatus == SignatureStatus.PENDING) {
                        LetsSignPrimaryButton(
                            text = "Assinar",
                            onClick = onStartSignClick,
                            enabled = uiState.canSign,
                            modifier = Modifier.weight(0.58f)
                        )
                    } else {
                        LetsSignPrimaryButton(
                            text = "Assinado",
                            onClick = onBack,
                            enabled = false,
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
                    .verticalScroll(scrollState)
                    .padding(horizontal = 22.dp, vertical = 18.dp)
            ) {
                val rendered = uiState.renderedPage
                val aspectRatio = rendered?.aspectRatio ?: (1f / 1.414f)
                var canvasSize by remember { mutableStateOf(IntSize.Zero) }

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
                            .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(12.dp))
                            .onSizeChanged {
                                canvasSize = it
                                pdfCanvasHeightPx = it.height
                            }
                    ) {
                        if (uiState.isLoadingPage || rendered == null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = LetsSignColors.Primary)
                            }
                        } else {
                            Image(
                                bitmap = rendered.bitmap.asImageBitmap(),
                                contentDescription = "Página do PDF",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )

                            val viewW = canvasSize.width.toFloat()
                            val viewH = canvasSize.height.toFloat()

                            if (uiState.mySignatureStatus == SignatureStatus.PENDING && viewW > 0f && viewH > 0f) {
                                uiState.currentPageMyFields.forEach { field ->
                                    SignerTargetFieldOverlay(
                                        field = field,
                                        viewWidthPx = viewW,
                                        viewHeightPx = viewH,
                                        onClick = onStartSignClick
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (!uiState.snackbarMessage.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 24.dp, vertical = 16.dp)
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

    // Etapa 2 de 3: Modal Bottom Sheet de Captura de Assinatura ou Rubrica
    if (uiState.wizardStep == SignWizardStep.STEP_2_CAPTURE_SIGNATURE) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onCancelCapture,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            SignatureOrInitialsCaptureSheetContent(
                title = "Sua assinatura",
                subtitle = "Escolha como quer assinar.",
                drawPlaceholder = "Desenhe sua assinatura aqui",
                typeFieldLabel = "Seu nome",
                confirmButtonLabel = "Usar assinatura",
                mode = uiState.signatureMode,
                typedText = uiState.typedSignatureText,
                selectedFont = uiState.typedSignatureFont,
                onModeChanged = onSignatureCaptureModeChanged,
                onTypedTextChanged = onTypedSignatureTextChanged,
                onFontChanged = onTypedSignatureFontChanged,
                onCancel = onCancelCapture,
                onConfirmBytes = onSignatureCapturedAdvanceToInitials
            )
        }
    } else if (uiState.wizardStep == SignWizardStep.STEP_2_CAPTURE_INITIALS) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onCancelCapture,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            SignatureOrInitialsCaptureSheetContent(
                title = "Sua rubrica",
                subtitle = "As iniciais do seu nome. Usada nas páginas do documento.",
                drawPlaceholder = "Desenhe sua rubrica aqui",
                typeFieldLabel = "Suas iniciais",
                confirmButtonLabel = "Usar rubrica",
                mode = uiState.initialsMode,
                typedText = uiState.typedInitialsText,
                selectedFont = uiState.typedInitialsFont,
                onModeChanged = onInitialsCaptureModeChanged,
                onTypedTextChanged = onTypedInitialsTextChanged,
                onFontChanged = onTypedInitialsFontChanged,
                onCancel = onCancelCapture,
                onConfirmBytes = onInitialsCapturedAdvanceToConfirm
            )
        }
    }

    // Etapa 3 de 3: Diálogo Confirmar assinatura e rubrica? (Assinar Confirmar.png e Assinar Salvando.png)
    if (uiState.wizardStep == SignWizardStep.STEP_3_CONFIRM) {
        ConfirmSignatureAndInitialsDialog(
            documentTitle = uiState.document?.title ?: "Contrato de serviços",
            signatureBytes = uiState.capturedSignatureBytes,
            initialsBytes = uiState.capturedInitialsBytes,
            isSaving = uiState.isSubmittingSignature,
            onRedoClick = onRedoFromConfirm,
            onConfirmClick = onConfirmAndSaveSignature
        )
    }
}

@Composable
private fun SignerTargetFieldOverlay(
    field: SignatureField,
    viewWidthPx: Float,
    viewHeightPx: Float,
    onClick: () -> Unit
) {
    val density = LocalDensity.current
    val bounds = PdfCoordinateConverter.relativeToPixels(
        relativeX = field.relativeX,
        relativeY = field.relativeY,
        relativeWidth = field.relativeWidth,
        relativeHeight = field.relativeHeight,
        viewWidthPx = viewWidthPx,
        viewHeightPx = viewHeightPx
    )

    val widthDp = with(density) { bounds.widthPx.toDp() }
    val heightDp = with(density) { bounds.heightPx.toDp() }

    val isInitials = field.fieldType == SignatureFieldType.INITIALS
    val bgColor = if (isInitials) LetsSignColors.Signer1Soft else LetsSignColors.Signer2Soft
    val borderColor = if (isInitials) LetsSignColors.Signer1 else LetsSignColors.Signer2
    val textColor = if (isInitials) LetsSignColors.Signer1 else LetsSignColors.Signer2

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = bounds.xPx.roundToInt(),
                    y = bounds.yPx.roundToInt()
                )
            }
            .size(width = widthDp, height = heightDp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor.copy(alpha = 0.92f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRoundRect(
                color = borderColor,
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                style = Stroke(
                    width = 1.6.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            if (!isInitials) {
                EditPenIcon(color = textColor, size = 14.dp)
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = if (isInitials) "Rubrica" else "Assine aqui",
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Modal Bottom Sheet reutilizado para:
 * - Assinar Captura.png / Assinar Desenhada.png / Assinar Digitar.png ("Sua assinatura")
 * - Assinar Rubrica.png / Assinar Rubrica Digitar.png ("Sua rubrica")
 */
@Composable
private fun SignatureOrInitialsCaptureSheetContent(
    title: String,
    subtitle: String,
    drawPlaceholder: String,
    typeFieldLabel: String,
    confirmButtonLabel: String,
    mode: SignatureCaptureMode,
    typedText: String,
    selectedFont: CursiveFontStyle,
    onModeChanged: (SignatureCaptureMode) -> Unit,
    onTypedTextChanged: (String) -> Unit,
    onFontChanged: (CursiveFontStyle) -> Unit,
    onCancel: () -> Unit,
    onConfirmBytes: (ByteArray) -> Unit
) {
    val context = LocalContext.current
    val strokes = remember(title) { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember(title) { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember(title) { mutableStateOf(IntSize.Zero) }
    val hasDrawn = strokes.isNotEmpty() || currentStroke.size > 1
    val canConfirm = when (mode) {
        SignatureCaptureMode.DRAW -> hasDrawn
        SignatureCaptureMode.TYPE -> typedText.trim().isNotBlank()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LetsSignColors.Muted,
                fontSize = 15.sp
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Seletor segmentado: ✎ Desenhar | T Digitar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(999.dp))
                .background(LetsSignColors.Mist)
                .border(1.dp, LetsSignColors.Line, RoundedCornerShape(999.dp))
                .padding(4.dp)
        ) {
            listOf(
                SignatureCaptureMode.DRAW to "Desenhar",
                SignatureCaptureMode.TYPE to "Digitar"
            ).forEach { (tabMode, label) ->
                val isSelected = mode == tabMode
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSelected) Color.White else Color.Transparent)
                        .clickable { onModeChanged(tabMode) },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (tabMode == SignatureCaptureMode.DRAW) {
                        EditPenIcon(
                            color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Body,
                            size = 15.dp
                        )
                    } else {
                        Text(
                            text = "T",
                            color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Body,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = label,
                        color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Body,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        if (mode == SignatureCaptureMode.DRAW) {
            // Quadro de desenho pontilhado (Assinar Captura / Desenhada / Rubrica)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(title) {
                        detectDragGestures(
                            onDragStart = { startOffset ->
                                currentStroke = listOf(startOffset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                currentStroke = currentStroke + change.position
                            },
                            onDragEnd = {
                                if (currentStroke.size > 1) {
                                    strokes.add(currentStroke)
                                }
                                currentStroke = emptyList()
                            },
                            onDragCancel = {
                                currentStroke = emptyList()
                            }
                        )
                    }
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawRoundRect(
                        color = LetsSignColors.Line,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                        )
                    )
                }

                if (!hasDrawn) {
                    Text(
                        text = drawPlaceholder,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                // Linha base horizontal
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 28.dp, vertical = 36.dp)
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(LetsSignColors.Line)
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val allStrokes = if (currentStroke.size > 1) strokes + listOf(currentStroke) else strokes
                    allStrokes.forEach { points ->
                        if (points.size > 1) {
                            val path = Path().apply {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    lineTo(points[i].x, points[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = LetsSignColors.Ink,
                                style = Stroke(
                                    width = 3.2.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        } else {
            // Aba Digitar (Assinar Digitar.png / Assinar Rubrica Digitar.png)
            LetsSignTextField(
                label = typeFieldLabel,
                value = typedText,
                onValueChange = onTypedTextChanged,
                placeholder = if (typeFieldLabel == "Suas iniciais") "RT" else "Rafael Tavares"
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Estilo da letra",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LetsSignColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CursiveFontStyle.entries.forEach { fontStyle ->
                    val isSelected = fontStyle == selectedFont
                    val composeFontFamily = fontStyle.toComposeFontFamily()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) LetsSignColors.VioletSoft else Color.White)
                            .border(
                                width = if (isSelected) 1.75.dp else 1.25.dp,
                                color = if (isSelected) LetsSignColors.Primary else LetsSignColors.Line,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onFontChanged(fontStyle) }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onFontChanged(fontStyle) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = LetsSignColors.Primary,
                                unselectedColor = LetsSignColors.Line
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = typedText.ifBlank { "Assinatura" },
                            fontFamily = composeFontFamily,
                            fontSize = if (fontStyle == CursiveFontStyle.HOMEMADE_APPLE) 22.sp else 28.sp,
                            color = LetsSignColors.Ink,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LetsSignSecondaryButton(
                text = "Cancelar",
                onClick = onCancel,
                modifier = Modifier.weight(0.42f)
            )
            LetsSignPrimaryButton(
                text = confirmButtonLabel,
                onClick = {
                    val bytes = if (mode == SignatureCaptureMode.DRAW) {
                        val allStrokes = if (currentStroke.size > 1) strokes + listOf(currentStroke) else strokes
                        renderSignatureStrokesToPngBytes(
                            strokes = allStrokes,
                            widthPx = canvasSize.width.coerceAtLeast(600),
                            heightPx = canvasSize.height.coerceAtLeast(240)
                        )
                    } else {
                        renderTypedSignatureToPngBytes(
                            context = context,
                            text = typedText.trim(),
                            fontStyle = selectedFont
                        )
                    }
                    onConfirmBytes(bytes)
                },
                enabled = canConfirm,
                modifier = Modifier.weight(0.58f)
            )
        }
    }
}

/**
 * Etapa 3 de 3: Diálogo Confirmar assinatura e rubrica? (Assinar Confirmar.png e Assinar Salvando.png)
 */
@Composable
private fun ConfirmSignatureAndInitialsDialog(
    documentTitle: String,
    signatureBytes: ByteArray?,
    initialsBytes: ByteArray?,
    isSaving: Boolean,
    onRedoClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    val sigBitmap = remember(signatureBytes) {
        signatureBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }
    val initBitmap = remember(initialsBytes) {
        initialsBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    Dialog(onDismissRequest = { if (!isSaving) onRedoClick() }) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Confirmar assinatura e rubrica?",
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Elas serão aplicadas ao $documentTitle e não poderão ser alteradas.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = LetsSignColors.Muted,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Box Assinatura
                    Column(
                        modifier = Modifier
                            .weight(0.65f)
                            .height(92.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Assinatura",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = LetsSignColors.Muted,
                                fontSize = 13.sp
                            )
                        )
                        if (sigBitmap != null) {
                            Image(
                                bitmap = sigBitmap.asImageBitmap(),
                                contentDescription = "Assinatura",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            )
                        }
                    }

                    // Box Rubrica
                    Column(
                        modifier = Modifier
                            .weight(0.35f)
                            .height(92.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(14.dp))
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rubrica",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = LetsSignColors.Muted,
                                fontSize = 13.sp
                            )
                        )
                        if (initBitmap != null) {
                            Image(
                                bitmap = initBitmap.asImageBitmap(),
                                contentDescription = "Rubrica",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
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
                        text = if (isSaving) "Cancelar" else "Refazer",
                        onClick = onRedoClick,
                        enabled = !isSaving,
                        modifier = Modifier.weight(0.42f)
                    )
                    LetsSignPrimaryButton(
                        text = if (isSaving) "Salvando..." else "Confirmar",
                        onClick = onConfirmClick,
                        isLoading = isSaving,
                        modifier = Modifier.weight(0.58f)
                    )
                }
            }
        }
    }
}

/**
 * Tela 9: Assinar Concluída.png
 */
@Composable
private fun SignCompletedSuccessScreenContent(
    uiState: SignerDocumentSignUiState,
    onViewSignedDocumentsClick: () -> Unit,
    onBackToHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val doc = uiState.document

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
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
                        text = "Ver documentos assinados",
                        onClick = onViewSignedDocumentsClick,
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
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            // Card Escuro Gradiente ("Documento assinado!")
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
                        text = "Documento assinado!",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontSize = 28.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Sua assinatura foi registrada com sucesso.",
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(22.dp))
                        .padding(20.dp),
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
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Assinado em\n${DateTimeFormatters.formatTimestampWithAs(uiState.signedTimestampMillis)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = LetsSignColors.Muted,
                                fontSize = 15.sp,
                                lineHeight = 22.sp
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    LetsSignStatusChip(
                        label = "Concluído",
                        style = StatusChipStyle.COMPLETED
                    )
                }
            }
        }
    }
}

/**
 * Tela 10: Assinar Falha.png
 */
@Composable
private fun SignFailureScreenContent(
    onRetryClick: () -> Unit,
    onBackToSignatureClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
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
                        text = "Voltar à assinatura",
                        onClick = onBackToSignatureClick,
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
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(LetsSignColors.ErrorSoft),
                contentAlignment = Alignment.Center
            ) {
                ErrorCircleIcon(color = LetsSignColors.Error, size = 32.dp)
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text(
                text = "Não foi possível assinar",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Houve um problema de conexão ao salvar sua assinatura. Ela ainda não foi registrada.",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = LetsSignColors.Muted
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                    text = "Tentar de novo não duplica a assinatura. Seu desenho foi mantido.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.Primary,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

private fun CursiveFontStyle.toComposeFontFamily(): FontFamily = when (this) {
    CursiveFontStyle.DANCING_SCRIPT -> DancingScriptFamily
    CursiveFontStyle.CAVEAT -> CaveatFamily
    CursiveFontStyle.HOMEMADE_APPLE -> HomemadeAppleFamily
}

private fun CursiveFontStyle.toFontResId(): Int = when (this) {
    CursiveFontStyle.DANCING_SCRIPT -> R.font.dancing_script
    CursiveFontStyle.CAVEAT -> R.font.caveat
    CursiveFontStyle.HOMEMADE_APPLE -> R.font.homemade_apple
}

private fun renderSignatureStrokesToPngBytes(
    strokes: List<List<Offset>>,
    widthPx: Int,
    heightPx: Int
): ByteArray {
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val androidCanvas = AndroidCanvas(bitmap)
    val paint = Paint().apply {
        color = android.graphics.Color.rgb(23, 18, 58) // LetsSignColors.Ink (#17123a)
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 9f
    }

    strokes.forEach { points ->
        if (points.size > 1) {
            val path = AndroidPath().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            androidCanvas.drawPath(path, paint)
        }
    }

    return ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        bitmap.recycle()
        out.toByteArray()
    }
}

private fun renderTypedSignatureToPngBytes(
    context: Context,
    text: String,
    fontStyle: CursiveFontStyle,
    widthPx: Int = 600,
    heightPx: Int = 220
): ByteArray {
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = AndroidCanvas(bitmap)
    val typeface = runCatching {
        ResourcesCompat.getFont(context, fontStyle.toFontResId())
    }.getOrNull() ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)

    val paint = Paint().apply {
        color = android.graphics.Color.rgb(23, 18, 58)
        isAntiAlias = true
        this.typeface = typeface
        textAlign = Paint.Align.CENTER
        textSize = if (text.length <= 4) 86f else 64f
    }

    val centerY = (heightPx / 2f) - ((paint.descent() + paint.ascent()) / 2f)
    canvas.drawText(text.ifBlank { "Assinatura" }, widthPx / 2f, centerY, paint)

    return ByteArrayOutputStream().use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        bitmap.recycle()
        out.toByteArray()
    }
}
