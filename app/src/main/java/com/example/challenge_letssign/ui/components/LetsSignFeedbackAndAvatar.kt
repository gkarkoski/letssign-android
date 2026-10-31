package com.example.challenge_letssign.ui.components

import androidx.compose.foundation.background
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
    size: Dp = 44.dp
) {
    val initials = extractInitials(fullName)
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(LetsSignColors.VioletSoft),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium.copy(
                color = LetsSignColors.VioletDeep,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        )
    }
}

fun extractInitials(fullName: String): String {
    val parts = fullName.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts.first().take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
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
