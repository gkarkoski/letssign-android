package com.example.challenge_letssign.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.challenge_letssign.ui.theme.LetsSignColors

private val PillShape = RoundedCornerShape(999.dp)
private val TextFieldShape = RoundedCornerShape(14.dp)
private val OtpBoxShape = RoundedCornerShape(12.dp)

@Composable
fun LetsSignPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = { if (!isLoading) onClick() },
        enabled = enabled,
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isLoading) LetsSignColors.Primary.copy(alpha = 0.85f) else LetsSignColors.Primary,
            contentColor = Color.White,
            disabledContainerColor = LetsSignColors.Line,
            disabledContentColor = LetsSignColors.Muted
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier.heightIn(min = 54.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
        } else if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun LetsSignSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = PillShape,
        border = BorderStroke(1.25.dp, LetsSignColors.Line),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = LetsSignColors.Primary
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier.heightIn(min = 50.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(color = LetsSignColors.Primary)
        )
    }
}

@Composable
fun LetsSignTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        shape = PillShape,
        modifier = modifier.heightIn(min = 48.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(color = LetsSignColors.Primary)
        )
    }
}

@Composable
fun LetsSignAiButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(PillShape)
            .background(LetsSignColors.VioletSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SparklesIcon(color = LetsSignColors.Primary, size = 16.dp)
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                color = LetsSignColors.Primary,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
fun LetsSignTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String? = null,
    errorMessage: String? = null,
    showInlineErrorText: Boolean = true,
    isSuccess: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val hasError = !errorMessage.isNullOrBlank()

    val borderColor = when {
        hasError -> LetsSignColors.Error
        isSuccess -> LetsSignColors.Success
        isFocused -> LetsSignColors.Primary
        else -> LetsSignColors.Line
    }
    val borderWidth = if (isFocused || hasError || isSuccess) 1.75.dp else 1.25.dp
    val containerColor = if (enabled) LetsSignColors.Mist else LetsSignColors.Mist.copy(alpha = 0.6f)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LetsSignColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            interactionSource = interactionSource,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (enabled) LetsSignColors.Ink else LetsSignColors.Muted,
                fontSize = 17.sp
            ),
            cursorBrush = SolidColor(LetsSignColors.Primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clip(TextFieldShape)
                        .background(containerColor)
                        .border(borderWidth, borderColor, TextFieldShape)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        leadingIcon()
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty() && placeholder.isNotEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    color = LetsSignColors.Muted,
                                    fontSize = 17.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(12.dp))
                        trailingIcon()
                    }
                }
            }
        )

        if (hasError && showInlineErrorText) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ErrorCircleIcon(color = LetsSignColors.Error, size = 16.dp)
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LetsSignColors.Error,
                        fontSize = 14.sp
                    )
                )
            }
        } else if (!helperText.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = LetsSignColors.Muted,
                    fontSize = 14.sp
                )
            )
        }
    }
}

/**
 * Componente de 6 dígitos para o Código de acesso (Login Código / Login Código inválido).
 */
@Composable
fun LetsSignOtpInput(
    code: String,
    onCodeChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Código de acesso",
    hasError: Boolean = false,
    onDone: () -> Unit = {}
) {
    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val sanitizedCode = code.filter { it.isDigit() }.take(6)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LetsSignColors.Ink,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp
            )
        )
        Spacer(modifier = Modifier.height(10.dp))

        BasicTextField(
            value = sanitizedCode,
            onValueChange = { raw ->
                val digits = raw.filter { it.isDigit() }.take(6)
                onCodeChanged(digits)
            },
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val activeIndex = sanitizedCode.length.coerceAtMost(5)
                    for (index in 0 until 6) {
                        val char = sanitizedCode.getOrNull(index)?.toString().orEmpty()
                        val isActiveBox = (isFocused || sanitizedCode.isNotEmpty()) &&
                            index == activeIndex &&
                            sanitizedCode.length < 6 &&
                            !hasError

                        val borderColor = when {
                            hasError -> LetsSignColors.Error
                            isActiveBox -> LetsSignColors.Primary
                            else -> LetsSignColors.Line
                        }
                        val borderWidth = if (hasError || isActiveBox) 1.75.dp else 1.25.dp

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp)
                                .clip(OtpBoxShape)
                                .background(Color.White)
                                .border(borderWidth, borderColor, OtpBoxShape)
                                .clickable { focusRequester.requestFocus() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = LetsSignColors.Ink,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 22.sp,
                                    textAlign = TextAlign.Center
                                )
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun LetsSignSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isHighlighted = isFocused || query.isNotBlank()
    val borderColor = if (isHighlighted) LetsSignColors.Primary else LetsSignColors.Line
    val borderWidth = if (isHighlighted) 1.5.dp else 1.25.dp
    val iconColor = if (isHighlighted) LetsSignColors.Primary else LetsSignColors.Muted

    BasicTextField(
        value = query,
        onValueChange = onQueryChanged,
        singleLine = true,
        interactionSource = interactionSource,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = LetsSignColors.Ink,
            fontSize = 16.sp
        ),
        cursorBrush = SolidColor(LetsSignColors.Primary),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(containerColor)
                    .border(borderWidth, borderColor, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchIcon(color = iconColor, size = 19.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = LetsSignColors.Muted,
                                fontSize = 16.sp
                            )
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onQueryChanged("") }
                            .padding(4.dp)
                    ) {
                        CloseIcon(color = LetsSignColors.Muted, size = 16.dp)
                    }
                }
            }
        }
    )
}

@Composable
fun LetsSignPasswordField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "••••••••",
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    LetsSignTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        errorMessage = errorMessage,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = if (isPasswordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation(mask = '•')
        },
        leadingIcon = {
            LockIcon(color = LetsSignColors.Muted)
        },
        trailingIcon = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onTogglePasswordVisibility)
                    .padding(4.dp)
            ) {
                EyeIcon(
                    color = LetsSignColors.Primary,
                    isVisible = isPasswordVisible
                )
            }
        }
    )
}
