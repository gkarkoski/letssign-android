package com.example.challenge_letssign.ui.signer.signing

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import android.graphics.Path as AndroidPath
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.domain.model.SignatureStatus
import com.example.challenge_letssign.ui.components.CloseIcon
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignSnackbar
import com.example.challenge_letssign.ui.components.LetsSignSnackbarType
import com.example.challenge_letssign.ui.components.LetsSignTextButton
import com.example.challenge_letssign.ui.components.SignaturePenIcon
import com.example.challenge_letssign.ui.theme.LetsSignColors
import com.example.challenge_letssign.utils.PdfCoordinateConverter
import kotlinx.coroutines.delay
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

@Composable
fun SignerDocumentSignScreen(
    documentId: String,
    signerId: String,
    onBack: () -> Unit,
    viewModel: SignerDocumentSignViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        onSelectPage = viewModel::onSelectPage,
        onFocusMyNextField = viewModel::onFocusMyNextField,
        onOpenDrawingModal = viewModel::onOpenDrawingModal,
        onDismissDrawingModal = viewModel::onDismissDrawingModal,
        onConfirmDrawnSignature = viewModel::onConfirmDrawnSignature,
        onDismissSnackbar = viewModel::onDismissSnackbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignerDocumentSignContent(
    uiState: SignerDocumentSignUiState,
    onBack: () -> Unit,
    onSelectPage: (Int) -> Unit,
    onFocusMyNextField: () -> Unit,
    onOpenDrawingModal: () -> Unit,
    onDismissDrawingModal: () -> Unit,
    onConfirmDrawnSignature: (ByteArray) -> Unit,
    onDismissSnackbar: () -> Unit,
    modifier: Modifier = Modifier
) {
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.document?.title ?: "Assinar Documento",
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        val pagesWithFields = uiState.myFields.map { it.pageNumber + 1 }.distinct().sorted()
                        val subtitle = if (uiState.mySignatureStatus == SignatureStatus.SIGNED) {
                            "Você já assinou este documento"
                        } else if (pagesWithFields.isNotEmpty()) {
                            "Sua assinatura é solicitada na(s) pág. ${pagesWithFields.joinToString(", ")}"
                        } else {
                            "Visualize o documento abaixo"
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (uiState.mySignatureStatus == SignatureStatus.SIGNED) {
                                    LetsSignColors.Success
                                } else {
                                    LetsSignColors.Primary
                                },
                                fontWeight = FontWeight.SemiBold
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LetsSignSecondaryButton(
                        text = "Voltar",
                        onClick = onBack,
                        modifier = Modifier.weight(0.38f)
                    )
                    if (uiState.mySignatureStatus == SignatureStatus.PENDING) {
                        LetsSignPrimaryButton(
                            text = "Assinar documento",
                            onClick = onOpenDrawingModal,
                            enabled = uiState.canSign,
                            leadingIcon = {
                                SignaturePenIcon(color = Color.White, size = 20.dp)
                            },
                            modifier = Modifier.weight(0.62f)
                        )
                    } else {
                        LetsSignPrimaryButton(
                            text = "Documento assinado",
                            onClick = onBack,
                            enabled = false,
                            modifier = Modifier.weight(0.62f)
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
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Paginação + botão rápido para ir direto à página onde está o campo do signatário
                if (uiState.totalPages > 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                            .border(1.dp, LetsSignColors.Line, RoundedCornerShape(14.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Anterior",
                            color = if (uiState.currentPageIndex > 0) LetsSignColors.Primary else LetsSignColors.Muted,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = uiState.currentPageIndex > 0) {
                                    onSelectPage(uiState.currentPageIndex - 1)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Text(
                            text = "Página ${uiState.currentPageIndex + 1} de ${uiState.totalPages}",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            modifier = Modifier.clickable(onClick = onFocusMyNextField)
                        )
                        Text(
                            text = "Próxima",
                            color = if (uiState.currentPageIndex < uiState.totalPages - 1) LetsSignColors.Primary else LetsSignColors.Muted,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = uiState.currentPageIndex < uiState.totalPages - 1) {
                                    onSelectPage(uiState.currentPageIndex + 1)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                val rendered = uiState.renderedPage
                val aspectRatio = rendered?.aspectRatio ?: (1f / 1.414f)
                var canvasSize by remember { mutableStateOf(IntSize.Zero) }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspectRatio),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White,
                    shadowElevation = 6.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.5.dp, LetsSignColors.Line, RoundedCornerShape(14.dp))
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

                            // Se ainda está pendente, destaca a caixa designada ao usuário logado
                            // Ao tocar na caixa, também abre o modal de assinatura
                            if (uiState.mySignatureStatus == SignatureStatus.PENDING && viewW > 0f && viewH > 0f) {
                                uiState.currentPageMyFields.forEach { field ->
                                    SignerTargetFieldOverlay(
                                        field = field,
                                        viewWidthPx = viewW,
                                        viewHeightPx = viewH,
                                        onClick = onOpenDrawingModal
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
                        },
                        actionLabel = "Fechar",
                        onActionClick = onDismissSnackbar
                    )
                }
            }
        }
    }

    // Modal de Canvas para desenhar a assinatura (gerar Bitmap / PNG bytes)
    if (uiState.isDrawingModalOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissDrawingModal,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            SignatureDrawingSheetContent(
                isSubmitting = uiState.isSubmittingSignature,
                onCancel = onDismissDrawingModal,
                onConfirmSignatureBytes = onConfirmDrawnSignature
            )
        }
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
            .background(LetsSignColors.VioletSoft.copy(alpha = 0.92f))
            .border(2.dp, LetsSignColors.Primary, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Toque para assinar aqui",
            color = LetsSignColors.Primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SignatureDrawingSheetContent(
    isSubmitting: Boolean,
    onCancel: () -> Unit,
    onConfirmSignatureBytes: (ByteArray) -> Unit
) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    var currentStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val hasDrawn = strokes.isNotEmpty() || currentStroke.size > 1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Desenhe sua assinatura",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Use o dedo ou caneta para assinar no quadro abaixo.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LetsSignColors.Muted
                    )
                )
            }
            LetsSignTextButton(
                text = "Limpar",
                onClick = {
                    strokes.clear()
                    currentStroke = emptyList()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(LetsSignColors.Mist)
                .border(1.5.dp, LetsSignColors.Primary, RoundedCornerShape(18.dp))
                .onSizeChanged { canvasSize = it }
                .pointerInput(Unit) {
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
            if (!hasDrawn) {
                Text(
                    text = "Assine aqui",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = LetsSignColors.Muted.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // Linha guia pontilhada
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 32.dp, vertical = 42.dp)
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
                                width = 3.5.dp.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LetsSignSecondaryButton(
                text = "Cancelar",
                onClick = onCancel,
                enabled = !isSubmitting,
                modifier = Modifier.weight(0.4f)
            )
            LetsSignPrimaryButton(
                text = if (isSubmitting) "Assinando..." else "Confirmar assinatura",
                onClick = {
                    val allStrokes = if (currentStroke.size > 1) strokes + listOf(currentStroke) else strokes
                    val pngBytes = renderSignatureStrokesToPngBytes(
                        strokes = allStrokes,
                        widthPx = canvasSize.width.coerceAtLeast(600),
                        heightPx = canvasSize.height.coerceAtLeast(240)
                    )
                    onConfirmSignatureBytes(pngBytes)
                },
                enabled = hasDrawn && !isSubmitting,
                isLoading = isSubmitting,
                modifier = Modifier.weight(0.6f)
            )
        }
    }
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
