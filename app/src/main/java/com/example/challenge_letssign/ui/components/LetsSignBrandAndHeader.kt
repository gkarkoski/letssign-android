package com.example.challenge_letssign.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.challenge_letssign.ui.theme.LetsSignColors

@Composable
fun LetsSignBrandLogo(
    modifier: Modifier = Modifier,
    textColor: Color = Color.White
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LetsSign",
                color = textColor,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Canvas(modifier = Modifier.size(22.dp, 18.dp)) {
                val path = Path().apply {
                    moveTo(size.width * 0.1f, size.height * 0.55f)
                    lineTo(size.width * 0.42f, size.height * 0.85f)
                    lineTo(size.width * 0.92f, size.height * 0.12f)
                }
                drawPath(
                    path = path,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF8654FF),
                            LetsSignColors.CyanGlow
                        ),
                        start = Offset(0f, size.height),
                        end = Offset(size.width, 0f)
                    ),
                    style = Stroke(
                        width = 3.8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
        Text(
            text = "é simples assinar!",
            color = textColor.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp
        )
    }
}

@Composable
fun LetsSignGradientHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
            .background(brush = LetsSignColors.HeaderGradient)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Column {
            LetsSignBrandLogo()
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontSize = 30.sp,
                    lineHeight = 34.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White.copy(alpha = 0.92f)
                )
            )
        }
    }
}

enum class StatusChipStyle {
    DRAFT,
    SENT,
    WAITING_YOU,
    WAITING,
    PARTIAL,
    COMPLETED,
    ROLE
}

@Composable
fun LetsSignStatusChip(
    label: String,
    style: StatusChipStyle,
    modifier: Modifier = Modifier
) {
    val pillShape = RoundedCornerShape(999.dp)
    val (bgColor, textColor, borderColor) = when (style) {
        StatusChipStyle.DRAFT -> Triple(LetsSignColors.Mist, LetsSignColors.Body, LetsSignColors.Line)
        StatusChipStyle.SENT -> Triple(LetsSignColors.VioletSoft, LetsSignColors.VioletDeep, Color.Transparent)
        StatusChipStyle.WAITING_YOU -> Triple(LetsSignColors.VioletSoft, LetsSignColors.VioletDeep, Color.Transparent)
        StatusChipStyle.WAITING -> Triple(LetsSignColors.VioletSoft, LetsSignColors.VioletDeep, Color.Transparent)
        StatusChipStyle.PARTIAL -> Triple(Color.White, LetsSignColors.Primary, LetsSignColors.Primary)
        StatusChipStyle.COMPLETED -> Triple(LetsSignColors.SuccessSoft, LetsSignColors.Success, Color.Transparent)
        StatusChipStyle.ROLE -> Triple(Color.White, LetsSignColors.Ink, LetsSignColors.Line)
    }

    Row(
        modifier = modifier
            .clip(pillShape)
            .background(bgColor)
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.25.dp, borderColor, pillShape)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 12.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (style) {
            StatusChipStyle.SENT -> SendPlaneIcon(color = textColor, size = 13.dp)
            StatusChipStyle.WAITING_YOU -> EditPenIcon(color = textColor, size = 13.dp)
            StatusChipStyle.WAITING -> ClockIcon(color = textColor, size = 13.dp)
            StatusChipStyle.PARTIAL -> PartialCircleIcon(color = textColor, size = 13.dp)
            StatusChipStyle.COMPLETED -> CheckIcon(color = textColor, size = 13.dp)
            else -> Unit
        }
        Text(
            text = label,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
