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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
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
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.ui.components.CheckIcon
import com.example.challenge_letssign.ui.components.CloseIcon
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignSnackbar
import com.example.challenge_letssign.ui.components.LetsSignSnackbarType
import com.example.challenge_letssign.ui.components.LetsSignWizardStepper
import com.example.challenge_letssign.ui.components.PlusIcon
import com.example.challenge_letssign.ui.components.extractInitials
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

    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            delay(3000)
            viewModel.onDismissSnackbar()
        }
    }

    PdfPositioningContent(
        uiState = uiState,
        onBack = onBack,
        onSelectPage = viewModel::onSelectPage,
        onSelectSigner = viewModel::onSelectSigner,
        onAddFieldForSelectedSigner = viewModel::onAddSignatureFieldForSelectedSigner,
        onDropFieldInPixels = viewModel::onDropSignatureFieldInPixels,
        onDeleteField = viewModel::onDeleteSignatureField,
        onContinue = viewModel::onContinueToReview,
        onDismissSnackbar = viewModel::onDismissSnackbar
    )
}

@Composable
fun PdfPositioningContent(
    uiState: PdfPositioningUiState,
    onBack: () -> Unit,
    onSelectPage: (Int) -> Unit,
    onSelectSigner: (String) -> Unit,
    onAddFieldForSelectedSigner: () -> Unit,
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
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.document?.title ?: "Posicionar assinaturas",
                            style = MaterialTheme.typography.headlineSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Arraste os campos de assinatura sobre a página do PDF",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = LetsSignColors.Muted
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                LetsSignWizardStepper(currentStep = 3, totalSteps = 5)
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
                    LetsSignPrimaryButton(
                        text = "Continuar",
                        onClick = onContinue,
                        enabled = uiState.canContinueToReview,
                        modifier = Modifier.weight(0.58f)
                    )
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
                // Regra 4: Aviso de modo somente leitura se não for DRAFT
                if (uiState.document != null && !uiState.isEditable) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(LetsSignColors.VioletSoft)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Modo visualização: documentos enviados ou concluídos não podem ser editados.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = LetsSignColors.VioletDeep,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Seletor de Signatários (s1, s2, s3) + Botão de adicionar campo (oculto se não editável - Regra 4)
                if (uiState.signers.isNotEmpty()) {
                    SignerSelectorToolbar(
                        signers = uiState.signers,
                        allFields = uiState.allFields,
                        selectedSignerId = uiState.selectedSignerId,
                        isEditable = uiState.isEditable,
                        onSelectSigner = onSelectSigner,
                        onAddFieldClick = onAddFieldForSelectedSigner
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Controles de paginação do PDF
                if (uiState.totalPages > 1) {
                    PdfPagePaginationBar(
                        currentPageIndex = uiState.currentPageIndex,
                        totalPages = uiState.totalPages,
                        onPreviousPage = { onSelectPage(uiState.currentPageIndex - 1) },
                        onNextPage = { onSelectPage(uiState.currentPageIndex + 1) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Motor Visual do PDF + Canvas Interativo de Drag & Drop
                PdfInteractiveCanvasBox(
                    uiState = uiState,
                    onDropFieldInPixels = onDropFieldInPixels,
                    onDeleteField = onDeleteField
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Snackbar ("Campo fora dos limites da página" conforme components2.png)
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
}

@Composable
private fun SignerSelectorToolbar(
    signers: List<DocumentSignerWithUser>,
    allFields: List<SignatureField>,
    selectedSignerId: String?,
    isEditable: Boolean,
    onSelectSigner: (String) -> Unit,
    onAddFieldClick: () -> Unit
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
                .border(1.dp, LetsSignColors.Line, RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "Signatários do documento (1 cor por signatário):",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LetsSignColors.Body,
                    fontWeight = FontWeight.SemiBold
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                signers.forEachIndexed { index, signerWithUser ->
                    val signer = signerWithUser.signer
                    val isSelected = signer.id == selectedSignerId
                    val palette = SignerPalette.styleForIndex(index)
                    val fieldCount = allFields.count { it.signerId == signer.id }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (isSelected) palette.backgroundColor else LetsSignColors.Mist
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) palette.borderColor else LetsSignColors.Line,
                                shape = RoundedCornerShape(999.dp)
                            )
                            .clickable { onSelectSigner(signer.id) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(palette.borderColor),
                            contentAlignment = Alignment.Center
                        ) {
                            if (fieldCount > 0) {
                                CheckIcon(color = Color.White, size = 12.dp)
                            } else {
                                Text(
                                    text = extractInitials(signer.name),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${signer.name.substringBefore(" ")} ($fieldCount)",
                            color = if (isSelected) palette.textColor else LetsSignColors.Ink,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            if (isEditable) {
                Spacer(modifier = Modifier.height(12.dp))
                LetsSignPrimaryButton(
                    text = "Inserir assinatura nesta página",
                    onClick = onAddFieldClick,
                    leadingIcon = {
                        PlusIcon(color = Color.White, size = 18.dp)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PdfPagePaginationBar(
    currentPageIndex: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit
) {
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
            color = if (currentPageIndex > 0) LetsSignColors.Primary else LetsSignColors.Muted,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = currentPageIndex > 0, onClick = onPreviousPage)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
        Text(
            text = "Página ${currentPageIndex + 1} de $totalPages",
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp)
        )
        Text(
            text = "Próxima",
            color = if (currentPageIndex < totalPages - 1) LetsSignColors.Primary else LetsSignColors.Muted,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = currentPageIndex < totalPages - 1, onClick = onNextPage)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun PdfInteractiveCanvasBox(
    uiState: PdfPositioningUiState,
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
                .onSizeChanged { canvasSize = it }
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

                        DraggableSignatureFieldBox(
                            field = field,
                            signerName = signerName,
                            palette = palette,
                            isEditable = uiState.isEditable,
                            viewWidthPx = viewWidthPx,
                            viewHeightPx = viewHeightPx,
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
    isEditable: Boolean,
    viewWidthPx: Float,
    viewHeightPx: Float,
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

    // Regra 2: Verifica em tempo real se a posição atual sai da página para exibir o estado "Fora do limite" do Figma
    val currentRelCoords = PdfCoordinateConverter.pixelsToRelative(
        dropXPx = offsetXPx,
        dropYPx = offsetYPx,
        widthPx = widthPx,
        heightPx = heightPx,
        viewWidthPx = viewWidthPx,
        viewHeightPx = viewHeightPx
    )
    val isOutOfBounds = !PdfCoordinateConverter.isWithinPageBounds(currentRelCoords)

    val boxWidthDp = with(density) { widthPx.toDp() }
    val boxHeightDp = with(density) { heightPx.toDp() }

    val activeBgColor = if (isOutOfBounds) LetsSignColors.ErrorSoft else palette.backgroundColor
    val activeBorderColor = if (isOutOfBounds) LetsSignColors.Error else palette.borderColor
    val activeTextColor = if (isOutOfBounds) LetsSignColors.Error else palette.textColor

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = offsetXPx.roundToInt(),
                    y = offsetYPx.roundToInt()
                )
            }
            .size(width = boxWidthDp, height = boxHeightDp)
            .clip(RoundedCornerShape(10.dp))
            .background(activeBgColor.copy(alpha = 0.92f))
            .then(
                if (isEditable) {
                    Modifier.pointerInput(field.id, viewWidthPx, viewHeightPx) {
                        detectDragGestures(
                            onDragStart = {
                                isDragging = true
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
                                    // Bloqueia o drop fora da página voltando para a última posição válida
                                    offsetXPx = initialBounds.xPx
                                    offsetYPx = initialBounds.yPx
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                offsetXPx = initialBounds.xPx
                                offsetYPx = initialBounds.yPx
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
    ) {
        // Borda tracejada durante o drag ou sólida quando posicionada (conforme "Campos de assinatura" no components2.png)
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeStyle = if (isDragging && !isOutOfBounds) {
                Stroke(
                    width = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                )
            } else {
                Stroke(width = 1.75.dp.toPx())
            }
            drawRoundRect(
                color = activeBorderColor,
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                style = strokeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (isOutOfBounds) {
                    "Fora do limite"
                } else {
                    "Assinatura · ${signerName.substringBefore(" ")}"
                },
                color = activeTextColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            if (isEditable && !isOutOfBounds) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(activeBorderColor.copy(alpha = 0.15f))
                        .clickable(onClick = onDeleteClick),
                    contentAlignment = Alignment.Center
                ) {
                    CloseIcon(color = activeTextColor, size = 12.dp)
                }
            }
        }
    }
}
