package com.example.challenge_letssign.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.challenge_letssign.ui.theme.LetsSignColors

@Composable
fun LetsSignAvatar(
    fullName: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    backgroundColor: Color = LetsSignColors.VioletSoft,
    textColor: Color = LetsSignColors.VioletDeep
) {
    val initials = extractInitials(fullName)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        )
    }
}

fun extractInitials(fullName: String): String {
    val ignoredSuffixes = setOf("ltda", "ltda.", "s/a", "s.a.", "sa", "me", "eireli", "epp")
    val rawParts = fullName.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    val parts = rawParts.filterNot { it.lowercase() in ignoredSuffixes }
        .ifEmpty { rawParts }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
    }
}

/**
 * Banner de erro usado no topo das telas de erro (Login Erro, Login Código inválido, Contato Novo Erros, Revisão com Pendência).
 */
@Composable
fun LetsSignErrorBanner(
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(LetsSignColors.ErrorSoft)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.padding(top = 2.dp)) {
                ErrorCircleIcon(color = LetsSignColors.Error, size = 20.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.ErrorText,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                )
                if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = LetsSignColors.ErrorText,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable(onClick = onActionClick)
                    )
                }
            }
        }
    }
}

/**
 * Card esqueleto para telas de Carregando (Home Admin Carregando, Home Signatário Carregando, Detalhe Carregando).
 */
@Composable
fun LetsSignSkeletonCard(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LetsSignColors.Line, RoundedCornerShape(18.dp))
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(LetsSignColors.Line.copy(alpha = 0.7f))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(LetsSignColors.Line.copy(alpha = 0.85f))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.45f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(LetsSignColors.Line.copy(alpha = 0.6f))
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(110.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(LetsSignColors.Line.copy(alpha = 0.65f))
                )
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(LetsSignColors.Line.copy(alpha = 0.6f))
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(LetsSignColors.Line.copy(alpha = 0.5f))
            )
        }
    }
}

enum class LetsSignSnackbarType {
    SUCCESS_DARK,
    ERROR
}

@Composable
fun LetsSignSnackbar(
    message: String,
    type: LetsSignSnackbarType = LetsSignSnackbarType.SUCCESS_DARK,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val bgColor = when (type) {
        LetsSignSnackbarType.SUCCESS_DARK -> LetsSignColors.Ink
        LetsSignSnackbarType.ERROR -> LetsSignColors.Error
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                when (type) {
                    LetsSignSnackbarType.SUCCESS_DARK -> CheckIcon(color = Color.White, size = 16.dp)
                    LetsSignSnackbarType.ERROR -> ErrorCircleIcon(color = Color.White, size = 18.dp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (type == LetsSignSnackbarType.SUCCESS_DARK) {
                            LetsSignColors.CyanGlow
                        } else {
                            Color.White
                        },
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(onClick = onActionClick)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun DeleteSignerConfirmDialog(
    signerName: String,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Excluir signatário?",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$signerName deixará de receber documentos.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.Muted
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LetsSignSecondaryButton(
                        text = "Cancelar",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    LetsSignPrimaryButton(
                        text = "Excluir",
                        onClick = onConfirmDelete,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
