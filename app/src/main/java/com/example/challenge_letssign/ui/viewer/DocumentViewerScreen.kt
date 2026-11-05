package com.example.challenge_letssign.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.ui.components.ArrowLeftIcon
import com.example.challenge_letssign.ui.theme.LetsSignColors

/**
 * Tela "Ver documento": leitura do PDF página a página, sem edição.
 */
@Composable
fun DocumentViewerScreen(
    documentId: String,
    onBack: () -> Unit,
    viewModel: DocumentViewerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(documentId) {
        viewModel.load(documentId)
    }

    Scaffold(
        containerColor = LetsSignColors.Mist,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onBack)
                        .padding(6.dp)
                ) {
                    ArrowLeftIcon(color = LetsSignColors.Primary, size = 22.dp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = uiState.title.ifBlank { "Documento" },
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Visualização do documento",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 14.sp
                        )
                    )
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
                val canGoBack = uiState.currentPageIndex > 0
                val canGoForward = uiState.currentPageIndex < uiState.totalPages - 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PageArrow(
                        symbol = "‹",
                        enabled = canGoBack,
                        onClick = { viewModel.onSelectPage(uiState.currentPageIndex - 1) }
                    )
                    Text(
                        text = "Página ${uiState.currentPageIndex + 1} de ${uiState.totalPages}",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp)
                    )
                    PageArrow(
                        symbol = "›",
                        enabled = canGoForward,
                        onClick = { viewModel.onSelectPage(uiState.currentPageIndex + 1) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            val page = uiState.renderedPage
            when {
                uiState.errorMessage != null && page == null -> {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge.copy(color = LetsSignColors.ErrorText),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
                page == null -> CircularProgressIndicator(color = LetsSignColors.Primary)
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Image(
                                bitmap = page.bitmap.asImageBitmap(),
                                contentDescription = "Página ${page.pageIndex + 1} do documento",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(page.aspectRatio)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .border(1.dp, LetsSignColors.Line, RoundedCornerShape(14.dp))
                            )
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = LetsSignColors.Primary)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PageArrow(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        text = symbol,
        fontSize = 24.sp,
        color = if (enabled) LetsSignColors.Primary else LetsSignColors.Line,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 2.dp)
    )
}
