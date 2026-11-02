package com.example.challenge_letssign.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.ui.components.CalendarIcon
import com.example.challenge_letssign.ui.components.LetsSignBrandLogo
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignSnackbar
import com.example.challenge_letssign.ui.components.LetsSignSnackbarType
import com.example.challenge_letssign.ui.theme.LetsSignColors
import kotlinx.coroutines.delay

@Composable
fun LetsSignProfileScreen(
    userId: String,
    userName: String = "",
    onLogout: () -> Unit = {},
    viewModel: LetsSignProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(userId, userName) {
        viewModel.loadUserProfile(userId, userName)
    }

    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            delay(3000)
            viewModel.onDismissSnackbar()
        }
    }

    LetsSignProfileContent(
        uiState = uiState,
        onCpfCnpjChanged = viewModel::onCpfCnpjChanged,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onBirthDateChanged = viewModel::onBirthDateChanged,
        onSaveClick = viewModel::onSaveProfile,
        onLogout = onLogout
    )
}

@Composable
fun LetsSignProfileContent(
    uiState: ProfileUiState,
    onCpfCnpjChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onBirthDateChanged: (String) -> Unit,
    onSaveClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LetsSignColors.Mist)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header roxo gradiente (perfil_admin.png / perfil_signatario.png)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                    .background(brush = LetsSignColors.HeaderGradient)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LetsSignBrandLogo()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(Color.White.copy(alpha = 0.14f))
                                .clickable(onClick = onLogout)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "Sair",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "Perfil",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontSize = 30.sp,
                            lineHeight = 34.sp
                        )
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Dados do perfil",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Pessoa Tipo (desabilitado)
                ProfileInputField(
                    label = "Pessoa Tipo",
                    isRequired = false,
                    value = uiState.personType.displayLabel,
                    onValueChange = {},
                    enabled = false
                )

                Spacer(modifier = Modifier.height(18.dp))

                // CPF * / CNPJ *
                ProfileInputField(
                    label = if (uiState.personType == PersonType.JURIDICA) "CNPJ" else "CPF",
                    isRequired = true,
                    value = uiState.cpfCnpj,
                    onValueChange = onCpfCnpjChanged,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Nome * / Razão social *
                ProfileInputField(
                    label = if (uiState.personType == PersonType.JURIDICA) "Razão social" else "Nome",
                    isRequired = true,
                    value = uiState.name,
                    onValueChange = onNameChanged,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // E-mail
                ProfileInputField(
                    label = "E-mail",
                    isRequired = false,
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                if (uiState.personType == PersonType.FISICA) {
                    Spacer(modifier = Modifier.height(18.dp))

                    // Data de nascimento
                    ProfileInputField(
                        label = "Data de nascimento",
                        isRequired = false,
                        value = uiState.birthDate,
                        onValueChange = onBirthDateChanged,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        trailingIcon = {
                            CalendarIcon(color = LetsSignColors.Muted, size = 20.dp)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LetsSignSecondaryButton(
                        text = "Sair da conta",
                        onClick = onLogout,
                        modifier = Modifier.weight(0.45f)
                    )
                    LetsSignPrimaryButton(
                        text = "Salvar",
                        onClick = onSaveClick,
                        isLoading = uiState.isSaving,
                        modifier = Modifier.weight(0.55f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (!uiState.snackbarMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 18.dp)
            ) {
                LetsSignSnackbar(
                    message = uiState.snackbarMessage,
                    type = LetsSignSnackbarType.SUCCESS_DARK
                )
            }
        }
    }
}

@Composable
private fun ProfileInputField(
    label: String,
    isRequired: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val borderColor = if (isFocused) LetsSignColors.Primary else LetsSignColors.Line
    val borderWidth = if (isFocused) 1.5.dp else 1.25.dp
    val bgColor = if (enabled) Color.White else LetsSignColors.Mist

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        color = LetsSignColors.Ink,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                ) {
                    append(label)
                }
                if (isRequired) {
                    withStyle(
                        SpanStyle(
                            color = LetsSignColors.Error,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    ) {
                        append(" *")
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = keyboardOptions,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = if (enabled) LetsSignColors.Ink else LetsSignColors.Muted,
                fontSize = 17.sp
            ),
            cursorBrush = SolidColor(LetsSignColors.Primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        trailingIcon()
                    }
                }
            }
        )
    }
}
