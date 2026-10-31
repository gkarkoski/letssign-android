package com.example.challenge_letssign.ui.admin.signers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.ui.components.CloseIcon
import com.example.challenge_letssign.ui.components.DeleteSignerConfirmDialog
import com.example.challenge_letssign.ui.components.EditPenIcon
import com.example.challenge_letssign.ui.components.LetsSignAvatar
import com.example.challenge_letssign.ui.components.LetsSignGradientHeader
import com.example.challenge_letssign.ui.components.LetsSignPasswordField
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignSnackbar
import com.example.challenge_letssign.ui.components.LetsSignSnackbarType
import com.example.challenge_letssign.ui.components.LetsSignTextField
import com.example.challenge_letssign.ui.components.MailIcon
import com.example.challenge_letssign.ui.components.PlusIcon
import com.example.challenge_letssign.ui.components.SearchIcon
import com.example.challenge_letssign.ui.components.TrashIcon
import com.example.challenge_letssign.ui.components.UserPlusIcon
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors
import kotlinx.coroutines.delay

@Composable
fun SignerManagementScreen(
    viewModel: SignerManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            delay(3500)
            viewModel.onDismissSnackbar()
        }
    }

    SignerManagementContent(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onOpenCreateForm = viewModel::onOpenCreateForm,
        onOpenEditForm = viewModel::onOpenEditForm,
        onDismissForm = viewModel::onDismissForm,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onPhoneChanged = viewModel::onPhoneChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onSaveSigner = viewModel::onSaveSigner,
        onRequestDeleteSigner = viewModel::onRequestDeleteSigner,
        onCancelDeleteSigner = viewModel::onCancelDeleteSigner,
        onConfirmDeleteSigner = viewModel::onConfirmDeleteSigner,
        onDismissSnackbar = viewModel::onDismissSnackbar
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignerManagementContent(
    uiState: SignerManagementUiState,
    onSearchQueryChanged: (String) -> Unit,
    onOpenCreateForm: () -> Unit,
    onOpenEditForm: (User) -> Unit,
    onDismissForm: () -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSaveSigner: () -> Unit,
    onRequestDeleteSigner: (User) -> Unit,
    onCancelDeleteSigner: () -> Unit,
    onConfirmDeleteSigner: () -> Unit,
    onDismissSnackbar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val signers = uiState.filteredSigners

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LetsSignColors.Mist)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            LetsSignGradientHeader(
                title = "Signatários",
                subtitle = "Cadastre e gerencie quem vai assinar os documentos."
            )

            // Barra de busca igual ao Figma (novo_doc_signatarios_selecao_signatarios.png)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                SignerSearchBar(
                    query = uiState.searchQuery,
                    onQueryChanged = onSearchQueryChanged
                )
            }

            if (signers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EmptySignersCard(
                        isSearching = uiState.searchQuery.isNotBlank(),
                        onAddSignerClick = onOpenCreateForm
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        top = 4.dp,
                        bottom = 96.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = signers, key = { it.id }) { signer ->
                        SignerListItemCard(
                            signer = signer,
                            onEditClick = { onOpenEditForm(signer) },
                            onDeleteClick = { onRequestDeleteSigner(signer) }
                        )
                    }
                }
            }
        }

        // FAB pílula "+ Novo signatário" (components2.png)
        if (signers.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 20.dp)
            ) {
                LetsSignPrimaryButton(
                    text = "Novo signatário",
                    onClick = onOpenCreateForm,
                    leadingIcon = {
                        PlusIcon(color = Color.White, size = 18.dp)
                    }
                )
            }
        }

        // Snackbar do Figma (components2.png)
        if (!uiState.snackbarMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 20.dp)
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

    // Diálogo de confirmação de exclusão do Figma (components2.png)
    val pendingDelete = uiState.signerPendingDeletion
    if (pendingDelete != null) {
        DeleteSignerConfirmDialog(
            signerName = pendingDelete.name,
            onDismiss = onCancelDeleteSigner,
            onConfirmDelete = onConfirmDeleteSigner
        )
    }

    // Modal Bottom Sheet de Cadastro / Edição
    if (uiState.isFormVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissForm,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            SignerFormSheetContent(
                uiState = uiState,
                onDismiss = onDismissForm,
                onNameChanged = onNameChanged,
                onEmailChanged = onEmailChanged,
                onPhoneChanged = onPhoneChanged,
                onPasswordChanged = onPasswordChanged,
                onTogglePasswordVisibility = onTogglePasswordVisibility,
                onSave = onSaveSigner
            )
        }
    }
}

@Composable
private fun SignerSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.text.BasicTextField(
        value = query,
        onValueChange = onQueryChanged,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = LetsSignColors.Ink),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White)
                    .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchIcon(color = LetsSignColors.Primary, size = 20.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Buscar por nome ou e-mail",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = LetsSignColors.Muted
                            )
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
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
private fun SignerListItemCard(
    signer: User,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onEditClick),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, LetsSignColors.Line, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LetsSignAvatar(fullName = signer.name)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = signer.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = signer.email,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = LetsSignColors.Muted
                    )
                )
                if (signer.phone.isNotBlank()) {
                    Text(
                        text = signer.phone,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = LetsSignColors.Muted
                        )
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onEditClick)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EditPenIcon(color = LetsSignColors.Primary, size = 18.dp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onDeleteClick)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TrashIcon(color = LetsSignColors.Error, size = 18.dp)
                }
            }
        }
    }
}

@Composable
private fun EmptySignersCard(
    isSearching: Boolean,
    onAddSignerClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White)
            .border(1.dp, LetsSignColors.Line, RoundedCornerShape(22.dp))
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(LetsSignColors.VioletSoft),
            contentAlignment = Alignment.Center
        ) {
            UserPlusIcon(color = LetsSignColors.Primary, size = 28.dp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSearching) {
                "Nenhum signatário encontrado"
            } else {
                "Nenhum signatário cadastrado"
            },
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isSearching) {
                "Tente buscar por outro nome ou e-mail."
            } else {
                "Adicione signatários para incluí-los nos seus documentos PDF."
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LetsSignColors.Muted
            ),
            textAlign = TextAlign.Center
        )
        if (!isSearching) {
            Spacer(modifier = Modifier.height(20.dp))
            LetsSignPrimaryButton(
                text = "Adicionar signatário",
                onClick = onAddSignerClick,
                leadingIcon = {
                    PlusIcon(color = Color.White, size = 18.dp)
                }
            )
        }
    }
}

@Composable
private fun SignerFormSheetContent(
    uiState: SignerManagementUiState,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPhoneChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSave: () -> Unit
) {
    val isEditing = uiState.editingSignerId != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEditing) "Editar signatário" else "Novo signatário",
                style = MaterialTheme.typography.headlineMedium
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onDismiss)
                    .padding(6.dp)
            ) {
                CloseIcon(color = LetsSignColors.Primary, size = 20.dp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LetsSignTextField(
            label = "Nome completo",
            value = uiState.name,
            onValueChange = onNameChanged,
            placeholder = "Ex.: Marina Albuquerque",
            errorMessage = uiState.nameError,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LetsSignTextField(
            label = "E-mail",
            value = uiState.email,
            onValueChange = onEmailChanged,
            placeholder = "marina@empresa.com.br",
            helperText = "Usado para enviar o convite.",
            errorMessage = uiState.emailError,
            isSuccess = uiState.isEmailValidFormat && uiState.emailError == null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            leadingIcon = {
                MailIcon(color = LetsSignColors.Muted)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        LetsSignTextField(
            label = "Telefone",
            value = uiState.phone,
            onValueChange = onPhoneChanged,
            placeholder = "(11) 98888-7777",
            errorMessage = uiState.phoneError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LetsSignPasswordField(
            label = "Senha de acesso do signatário",
            value = uiState.password,
            onValueChange = onPasswordChanged,
            isPasswordVisible = uiState.isPasswordVisible,
            onTogglePasswordVisibility = onTogglePasswordVisibility,
            errorMessage = uiState.passwordError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

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
                text = if (isEditing) "Salvar" else "Adicionar",
                onClick = onSave,
                isLoading = uiState.isSaving,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignerManagementPreview() {
    Challenge_letsSignTheme {
        SignerManagementContent(
            uiState = SignerManagementUiState(
                allSigners = listOf(
                    User(
                        id = "1",
                        name = "Marina Albuquerque",
                        email = "marina.albuquerque@exemplo.com.br",
                        phone = "(11) 98888-1111",
                        password = "123",
                        role = UserRole.SIGNER
                    ),
                    User(
                        id = "2",
                        name = "Rafael Tavares",
                        email = "rafael.tavares@exemplo.com.br",
                        phone = "(11) 97777-2222",
                        password = "123",
                        role = UserRole.SIGNER
                    )
                )
            ),
            onSearchQueryChanged = {},
            onOpenCreateForm = {},
            onOpenEditForm = {},
            onDismissForm = {},
            onNameChanged = {},
            onEmailChanged = {},
            onPhoneChanged = {},
            onPasswordChanged = {},
            onTogglePasswordVisibility = {},
            onSaveSigner = {},
            onRequestDeleteSigner = {},
            onCancelDeleteSigner = {},
            onConfirmDeleteSigner = {},
            onDismissSnackbar = {}
        )
    }
}
