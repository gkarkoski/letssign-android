package com.example.challenge_letssign.ui.auth

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.ui.components.LetsSignErrorBanner
import com.example.challenge_letssign.ui.components.LetsSignGradientHeader
import com.example.challenge_letssign.ui.components.LetsSignOtpInput
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignTextField
import com.example.challenge_letssign.ui.components.MailIcon
import com.example.challenge_letssign.ui.components.ShieldCheckIcon
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors

@Composable
fun LoginScreen(
    onLoginSuccess: (User) -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.step == LoginStep.CODE) {
        viewModel.onUseAnotherEmailClick()
    }

    LaunchedEffect(uiState.authenticatedUser) {
        val user = uiState.authenticatedUser
        if (user != null) {
            onLoginSuccess(user)
            viewModel.onNavigationHandled()
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onEmailChanged = viewModel::onEmailChanged,
        onSendCodeClick = viewModel::onSendCodeClick,
        onOtpCodeChanged = viewModel::onOtpCodeChanged,
        onVerifyCodeClick = viewModel::onVerifyCodeClick,
        onResendCodeClick = viewModel::onResendCodeClick,
        onUseAnotherEmailClick = viewModel::onUseAnotherEmailClick
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onOtpCodeChanged: (String) -> Unit,
    onVerifyCodeClick: () -> Unit,
    onResendCodeClick: () -> Unit,
    onUseAnotherEmailClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = {
                fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(200))
            },
            label = "login_step_transition"
        ) { step ->
            when (step) {
                LoginStep.EMAIL -> {
                    LoginEmailStepContent(
                        uiState = uiState,
                        onEmailChanged = onEmailChanged,
                        onSendCodeClick = {
                            focusManager.clearFocus()
                            onSendCodeClick()
                        }
                    )
                }
                LoginStep.CODE -> {
                    LoginCodeStepContent(
                        uiState = uiState,
                        onOtpCodeChanged = onOtpCodeChanged,
                        onVerifyCodeClick = {
                            focusManager.clearFocus()
                            onVerifyCodeClick()
                        },
                        onResendCodeClick = onResendCodeClick,
                        onUseAnotherEmailClick = {
                            focusManager.clearFocus()
                            onUseAnotherEmailClick()
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShieldCheckIcon(color = LetsSignColors.Muted, size = 18.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Assinatura eletrônica com validade jurídica",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LetsSignColors.Muted,
                    fontSize = 15.sp
                )
            )
        }
    }
}

@Composable
private fun LoginEmailStepContent(
    uiState: LoginUiState,
    onEmailChanged: (String) -> Unit,
    onSendCodeClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        LetsSignGradientHeader(
            title = "Bem-vindo de volta",
            subtitle = "Entre para enviar e assinar documentos com validade jurídica."
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            if (!uiState.emailError.isNullOrBlank()) {
                LetsSignErrorBanner(text = uiState.emailError)
                Spacer(modifier = Modifier.height(20.dp))
            }

            LetsSignTextField(
                label = "E-mail",
                value = uiState.email,
                onValueChange = onEmailChanged,
                placeholder = "admin@exemplo.com.br",
                errorMessage = uiState.emailError,
                showInlineErrorText = false,
                enabled = !uiState.isSendingCode,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onSendCodeClick() }
                ),
                leadingIcon = {
                    MailIcon(color = LetsSignColors.Muted)
                }
            )

            if (uiState.emailError.isNullOrBlank() && !uiState.isSendingCode) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MailIcon(color = LetsSignColors.Muted, size = 18.dp)
                    Text(
                        text = "Enviaremos um código de 6 dígitos para este e-mail.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = LetsSignColors.Muted,
                            fontSize = 15.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            LetsSignPrimaryButton(
                text = if (uiState.isSendingCode) "Enviando código..." else "Enviar código",
                onClick = onSendCodeClick,
                isLoading = uiState.isSendingCode,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun LoginCodeStepContent(
    uiState: LoginUiState,
    onOtpCodeChanged: (String) -> Unit,
    onVerifyCodeClick: () -> Unit,
    onResendCodeClick: () -> Unit,
    onUseAnotherEmailClick: () -> Unit
) {
    val hasError = !uiState.codeError.isNullOrBlank()

    Column(modifier = Modifier.fillMaxWidth()) {
        LetsSignGradientHeader(
            title = "Confirme seu acesso",
            subtitle = "Digite o código de 6 dígitos enviado para\n${uiState.email.ifBlank { "admin@exemplo.com.br" }}."
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            if (hasError) {
                LetsSignErrorBanner(text = uiState.codeError.orEmpty())
                Spacer(modifier = Modifier.height(20.dp))
            }

            LetsSignOtpInput(
                code = uiState.otpCode,
                onCodeChanged = onOtpCodeChanged,
                hasError = hasError,
                onDone = onVerifyCodeClick
            )

            Spacer(modifier = Modifier.height(18.dp))

            if (hasError || uiState.resendCountdownSeconds <= 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onResendCodeClick)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Reenviar código",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = LetsSignColors.Primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Text(
                    text = "Não recebeu? Você pode pedir um novo código\nem ${uiState.formattedCountdown}.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.Muted,
                        fontSize = 16.sp,
                        lineHeight = 24.sp
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // No estado Login Código (enquanto menos de 6 dígitos e sem erro), exibe o visual
            // suave cinza-lavanda do Figma mas permanece clicável; com 6 dígitos ou em Login Código inválido, fica roxo primário.
            val showPrimaryFilled = uiState.isCodeComplete || hasError
            Button(
                onClick = onVerifyCodeClick,
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (showPrimaryFilled) LetsSignColors.Primary else LetsSignColors.Line,
                    contentColor = if (showPrimaryFilled) Color.White else LetsSignColors.Body
                ),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
            ) {
                Text(
                    text = "Entrar",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = if (showPrimaryFilled) Color.White else LetsSignColors.Body
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onUseAnotherEmailClick)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Usar outro e-mail",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = LetsSignColors.Primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 17.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginEmailPreview() {
    Challenge_letsSignTheme {
        LoginScreenContent(
            uiState = LoginUiState(email = "admin@exemplo.com.br"),
            onEmailChanged = {},
            onSendCodeClick = {},
            onOtpCodeChanged = {},
            onVerifyCodeClick = {},
            onResendCodeClick = {},
            onUseAnotherEmailClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginCodePreview() {
    Challenge_letsSignTheme {
        LoginScreenContent(
            uiState = LoginUiState(
                step = LoginStep.CODE,
                email = "admin@exemplo.com.br",
                otpCode = "482"
            ),
            onEmailChanged = {},
            onSendCodeClick = {},
            onOtpCodeChanged = {},
            onVerifyCodeClick = {},
            onResendCodeClick = {},
            onUseAnotherEmailClick = {}
        )
    }
}
