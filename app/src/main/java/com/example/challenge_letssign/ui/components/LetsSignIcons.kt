package com.example.challenge_letssign.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MailIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.75.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.08f, h * 0.18f),
            size = Size(w * 0.84f, h * 0.64f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        val flap = Path().apply {
            moveTo(w * 0.1f, h * 0.22f)
            lineTo(w * 0.5f, h * 0.54f)
            lineTo(w * 0.9f, h * 0.22f)
        }
        drawPath(
            path = flap,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun LockIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.75.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        // Body
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.16f, h * 0.45f),
            size = Size(w * 0.68f, h * 0.44f),
            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        // Shackle
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.28f, h * 0.14f),
            size = Size(w * 0.44f, h * 0.52f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun EyeIcon(
    color: Color,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.75.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        val eyePath = Path().apply {
            moveTo(w * 0.08f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.15f, w * 0.92f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.85f, w * 0.08f, h * 0.5f)
            close()
        }
        drawPath(
            path = eyePath,
            color = color,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )
        drawCircle(
            color = color,
            radius = w * 0.13f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )
        if (!isVisible) {
            drawLine(
                color = color,
                start = Offset(w * 0.18f, h * 0.82f),
                end = Offset(w * 0.82f, h * 0.18f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun ShieldCheckIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.6.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        val shield = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.86f, h * 0.22f)
            lineTo(w * 0.86f, h * 0.52f)
            quadraticTo(w * 0.86f, h * 0.82f, w * 0.5f, h * 0.94f)
            quadraticTo(w * 0.14f, h * 0.82f, w * 0.14f, h * 0.52f)
            lineTo(w * 0.14f, h * 0.22f)
            close()
        }
        drawPath(
            path = shield,
            color = color,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )

        val check = Path().apply {
            moveTo(w * 0.34f, h * 0.52f)
            lineTo(w * 0.46f, h * 0.64f)
            lineTo(w * 0.68f, h * 0.38f)
        }
        drawPath(
            path = check,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ErrorCircleIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.5.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawCircle(
            color = color,
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.28f),
            end = Offset(w * 0.5f, h * 0.55f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = color,
            radius = strokeWidth * 0.6f,
            center = Offset(w * 0.5f, h * 0.7f)
        )
    }
}

@Composable
fun DocumentFileIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.75.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        val docPath = Path().apply {
            moveTo(w * 0.22f, h * 0.1f)
            lineTo(w * 0.6f, h * 0.1f)
            lineTo(w * 0.78f, h * 0.28f)
            lineTo(w * 0.78f, h * 0.9f)
            lineTo(w * 0.22f, h * 0.9f)
            close()
        }
        drawPath(
            path = docPath,
            color = color,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )
        // Folded corner
        val foldPath = Path().apply {
            moveTo(w * 0.58f, h * 0.1f)
            lineTo(w * 0.58f, h * 0.3f)
            lineTo(w * 0.78f, h * 0.3f)
        }
        drawPath(
            path = foldPath,
            color = color,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )
        // Text lines inside
        drawLine(
            color = color,
            start = Offset(w * 0.34f, h * 0.48f),
            end = Offset(w * 0.66f, h * 0.48f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.34f, h * 0.62f),
            end = Offset(w * 0.66f, h * 0.62f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.34f, h * 0.76f),
            end = Offset(w * 0.52f, h * 0.76f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun SignaturePenIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.75.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        // Cursive loop
        val loop = Path().apply {
            moveTo(w * 0.28f, h * 0.62f)
            quadraticTo(w * 0.48f, h * 0.12f, w * 0.62f, h * 0.25f)
            quadraticTo(w * 0.7f, h * 0.35f, w * 0.46f, h * 0.62f)
            quadraticTo(w * 0.62f, h * 0.65f, w * 0.8f, h * 0.58f)
        }
        drawPath(
            path = loop,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Dotted baseline
        for (i in 0..4) {
            val x = w * (0.2f + i * 0.15f)
            drawCircle(
                color = color,
                radius = strokeWidth * 0.55f,
                center = Offset(x, h * 0.82f)
            )
        }
    }
}

@Composable
fun UserProfileIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.75.dp.toPx()
        val w = this.size.width
        val h = this.size.height

        drawCircle(
            color = color,
            radius = w * 0.18f,
            center = Offset(w * 0.5f, h * 0.32f),
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.2f, h * 0.58f),
            size = Size(w * 0.6f, h * 0.44f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun PlusIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 2.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.18f),
            end = Offset(w * 0.5f, h * 0.82f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.18f, h * 0.5f),
            end = Offset(w * 0.82f, h * 0.5f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun SendPlaneIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.5.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        val plane = Path().apply {
            moveTo(w * 0.1f, h * 0.45f)
            lineTo(w * 0.9f, h * 0.12f)
            lineTo(w * 0.58f, h * 0.9f)
            lineTo(w * 0.45f, h * 0.55f)
            close()
        }
        drawPath(
            path = plane,
            color = color,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun PartialCircleIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.5.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawCircle(
            color = color,
            radius = w * 0.42f,
            center = Offset(w * 0.5f, h * 0.5f),
            style = Stroke(width = strokeWidth)
        )
        drawArc(
            color = color,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(w * 0.08f, h * 0.08f),
            size = Size(w * 0.84f, h * 0.84f)
        )
    }
}

@Composable
fun CheckIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 14.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.8.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.78f)
            lineTo(w * 0.85f, h * 0.22f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun ChevronRightIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.8.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.35f, h * 0.22f)
            lineTo(w * 0.65f, h * 0.5f)
            lineTo(w * 0.35f, h * 0.78f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun SearchIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.8.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawCircle(
            color = color,
            radius = w * 0.32f,
            center = Offset(w * 0.44f, h * 0.44f),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = color,
            start = Offset(w * 0.68f, h * 0.68f),
            end = Offset(w * 0.88f, h * 0.88f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun CloseIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.8.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawLine(
            color = color,
            start = Offset(w * 0.22f, h * 0.22f),
            end = Offset(w * 0.78f, h * 0.78f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.78f, h * 0.22f),
            end = Offset(w * 0.22f, h * 0.78f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun UserPlusIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.75.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        // Head
        drawCircle(
            color = color,
            radius = w * 0.16f,
            center = Offset(w * 0.38f, h * 0.34f),
            style = Stroke(width = strokeWidth)
        )
        // Shoulders
        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.12f, h * 0.56f),
            size = Size(w * 0.52f, h * 0.4f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        // Plus sign
        drawLine(
            color = color,
            start = Offset(w * 0.78f, h * 0.32f),
            end = Offset(w * 0.78f, h * 0.56f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.66f, h * 0.44f),
            end = Offset(w * 0.9f, h * 0.44f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun TrashIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.7.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        // Top lid
        drawLine(
            color = color,
            start = Offset(w * 0.18f, h * 0.25f),
            end = Offset(w * 0.82f, h * 0.25f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Handle
        drawLine(
            color = color,
            start = Offset(w * 0.38f, h * 0.16f),
            end = Offset(w * 0.62f, h * 0.16f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Body
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.26f, h * 0.25f),
            size = Size(w * 0.48f, h * 0.62f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
    }
}

@Composable
fun EditPenIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.7.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.2f, h * 0.8f)
            lineTo(w * 0.26f, h * 0.6f)
            lineTo(w * 0.68f, h * 0.18f)
            lineTo(w * 0.82f, h * 0.32f)
            lineTo(w * 0.4f, h * 0.74f)
            close()
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun UploadTrayIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.8.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        // Tray
        val tray = Path().apply {
            moveTo(w * 0.16f, h * 0.62f)
            lineTo(w * 0.16f, h * 0.82f)
            lineTo(w * 0.84f, h * 0.82f)
            lineTo(w * 0.84f, h * 0.62f)
        }
        drawPath(
            path = tray,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        // Arrow shaft
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.64f),
            end = Offset(w * 0.5f, h * 0.2f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Arrow head
        val arrow = Path().apply {
            moveTo(w * 0.32f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.2f)
            lineTo(w * 0.68f, h * 0.38f)
        }
        drawPath(
            path = arrow,
            color = color,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun FileAlertIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.75.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        val docPath = Path().apply {
            moveTo(w * 0.22f, h * 0.1f)
            lineTo(w * 0.6f, h * 0.1f)
            lineTo(w * 0.78f, h * 0.28f)
            lineTo(w * 0.78f, h * 0.9f)
            lineTo(w * 0.22f, h * 0.9f)
            close()
        }
        drawPath(
            path = docPath,
            color = color,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )
        val foldPath = Path().apply {
            moveTo(w * 0.58f, h * 0.1f)
            lineTo(w * 0.58f, h * 0.3f)
            lineTo(w * 0.78f, h * 0.3f)
        }
        drawPath(
            path = foldPath,
            color = color,
            style = Stroke(width = strokeWidth, join = StrokeJoin.Round)
        )
        drawLine(
            color = color,
            start = Offset(w * 0.5f, h * 0.42f),
            end = Offset(w * 0.5f, h * 0.64f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawCircle(
            color = color,
            radius = strokeWidth * 0.65f,
            center = Offset(w * 0.5f, h * 0.76f)
        )
    }
}

@Composable
fun SyncArrowsIcon(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.8.dp.toPx()
        val w = this.size.width
        val h = this.size.height
        drawArc(
            color = color,
            startAngle = 200f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(w * 0.18f, h * 0.18f),
            size = Size(w * 0.64f, h * 0.64f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
        drawArc(
            color = color,
            startAngle = 20f,
            sweepAngle = 130f,
            useCenter = false,
            topLeft = Offset(w * 0.18f, h * 0.18f),
            size = Size(w * 0.64f, h * 0.64f),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
