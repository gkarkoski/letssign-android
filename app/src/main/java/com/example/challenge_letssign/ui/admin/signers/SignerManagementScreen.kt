package com.example.challenge_letssign.ui.admin.signers

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.model.UserRole
import com.example.challenge_letssign.ui.components.ArrowLeftIcon
import com.example.challenge_letssign.ui.components.CalendarIcon
import com.example.challenge_letssign.ui.components.CheckIcon
import com.example.challenge_letssign.ui.components.CloseIcon
import com.example.challenge_letssign.ui.components.DeleteSignerConfirmDialog
import com.example.challenge_letssign.ui.components.ErrorCircleIcon
import com.example.challenge_letssign.ui.components.LetsSignAvatar
import com.example.challenge_letssign.ui.components.LetsSignErrorBanner
import com.example.challenge_letssign.ui.components.LetsSignPrimaryButton
import com.example.challenge_letssign.ui.components.LetsSignSearchBar
import com.example.challenge_letssign.ui.components.LetsSignSecondaryButton
import com.example.challenge_letssign.ui.components.LetsSignSnackbar
import com.example.challenge_letssign.ui.components.LetsSignSnackbarType
import com.example.challenge_letssign.ui.components.LetsSignStatusChip
import com.example.challenge_letssign.ui.components.PlusIcon
import com.example.challenge_letssign.ui.components.SearchIcon
import com.example.challenge_letssign.ui.components.StatusChipStyle
import com.example.challenge_letssign.ui.components.UserPlusIcon
import com.example.challenge_letssign.ui.theme.Challenge_letsSignTheme
import com.example.challenge_letssign.ui.theme.LetsSignColors
import kotlinx.coroutines.delay

@Composable
fun SignerManagementScreen(
    viewModel: SignerManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(enabled = uiState.isFormVisible || uiState.isNewGroupVisible) {
        when {
            uiState.isFormVisible -> viewModel.onDismissForm()
            uiState.isGroupContactPickerSheetOpen -> viewModel.onDismissGroupContactPickerSheet()
            uiState.isNewGroupVisible -> viewModel.onDismissNewGroupFlow()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        if (uiState.snackbarMessage != null) {
            delay(3500)
            viewModel.onDismissSnackbar()
        }
    }

    SignerManagementContent(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onClearSearch = viewModel::onClearSearch,
        onOpenCreateForm = viewModel::onOpenCreateForm,
        onOpenEditForm = viewModel::onOpenEditForm,
        onDismissForm = viewModel::onDismissForm,
        onPersonTypeChanged = viewModel::onPersonTypeChanged,
        onCpfCnpjChanged = viewModel::onCpfCnpjChanged,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onBirthDateChanged = viewModel::onBirthDateChanged,
        onSaveSigner = viewModel::onSaveSigner,
        onDeleteEditingSigner = viewModel::onDeleteEditingSigner,
        onCancelDeleteSigner = viewModel::onCancelDeleteSigner,
        onConfirmDeleteSigner = viewModel::onConfirmDeleteSigner,
        onOpenNewGroupFlow = viewModel::onOpenNewGroupFlow,
        onDismissNewGroupFlow = viewModel::onDismissNewGroupFlow,
        onGroupNameChanged = viewModel::onGroupNameChanged,
        onOpenGroupContactPickerSheet = viewModel::onOpenGroupContactPickerSheet,
        onDismissGroupContactPickerSheet = viewModel::onDismissGroupContactPickerSheet,
        onGroupPickerSearchQueryChanged = viewModel::onGroupPickerSearchQueryChanged,
        onToggleGroupPickerContact = viewModel::onToggleGroupPickerContact,
        onConfirmGroupPickerSelection = viewModel::onConfirmGroupPickerSelection,
        onToggleConfirmedGroupMember = viewModel::onToggleConfirmedGroupMember,
        onCreateGroup = viewModel::onCreateGroup,
        onOpenCreateFormFromGroupPicker = viewModel::onOpenCreateFormFromGroupPicker
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignerManagementContent(
    uiState: SignerManagementUiState,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onOpenCreateForm: () -> Unit,
    onOpenEditForm: (User) -> Unit,
    onDismissForm: () -> Unit,
    onPersonTypeChanged: (PersonType) -> Unit,
    onCpfCnpjChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onBirthDateChanged: (String) -> Unit,
    onSaveSigner: () -> Unit,
    onDeleteEditingSigner: () -> Unit,
    onCancelDeleteSigner: () -> Unit,
    onConfirmDeleteSigner: () -> Unit,
    onOpenNewGroupFlow: () -> Unit = {},
    onDismissNewGroupFlow: () -> Unit = {},
    onGroupNameChanged: (String) -> Unit = {},
    onOpenGroupContactPickerSheet: () -> Unit = {},
    onDismissGroupContactPickerSheet: () -> Unit = {},
    onGroupPickerSearchQueryChanged: (String) -> Unit = {},
    onToggleGroupPickerContact: (String) -> Unit = {},
    onConfirmGroupPickerSelection: () -> Unit = {},
    onToggleConfirmedGroupMember: (String) -> Unit = {},
    onCreateGroup: () -> Unit = {},
    onOpenCreateFormFromGroupPicker: () -> Unit = onOpenCreateForm,
    modifier: Modifier = Modifier
) {
    val activeSubScreen = when {
        uiState.isFormVisible -> "FORM"
        uiState.isNewGroupVisible -> "GROUP"
        else -> "LIST"
    }

    AnimatedContent(
        targetState = activeSubScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(220))
        },
        label = "contacts_list_form_or_group",
        modifier = modifier.fillMaxSize()
    ) { mode ->
        when (mode) {
            "FORM" -> {
                ContactFormScreenContent(
                    uiState = uiState,
                    onBack = onDismissForm,
                    onPersonTypeChanged = onPersonTypeChanged,
                    onCpfCnpjChanged = onCpfCnpjChanged,
                    onNameChanged = onNameChanged,
                    onEmailChanged = onEmailChanged,
                    onBirthDateChanged = onBirthDateChanged,
                    onDeleteClick = onDeleteEditingSigner,
                    onSaveClick = onSaveSigner
                )
            }
            "GROUP" -> {
                NewGroupScreenContent(
                    uiState = uiState,
                    onCloseOrCancel = onDismissNewGroupFlow,
                    onGroupNameChanged = onGroupNameChanged,
                    onOpenAddContactsSheet = onOpenGroupContactPickerSheet,
                    onToggleMember = onToggleConfirmedGroupMember,
                    onCreateGroupClick = onCreateGroup
                )
            }
            else -> {
                ContactsListScreenContent(
                    uiState = uiState,
                    onSearchQueryChanged = onSearchQueryChanged,
                    onClearSearch = onClearSearch,
                    onNewContactClick = onOpenCreateForm,
                    onNewGroupClick = onOpenNewGroupFlow,
                    onContactClick = onOpenEditForm
                )
            }
        }
    }

    if (uiState.isNewGroupVisible && uiState.isGroupContactPickerSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissGroupContactPickerSheet,
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
        ) {
            GroupContactSelectionSheetContent(
                uiState = uiState,
                onDismiss = onDismissGroupContactPickerSheet,
                onCreateNewContactClick = onOpenCreateFormFromGroupPicker,
                onSearchQueryChanged = onGroupPickerSearchQueryChanged,
                onToggleContact = onToggleGroupPickerContact,
                onConfirmSelection = onConfirmGroupPickerSelection
            )
        }
    }

    val pendingDelete = uiState.signerPendingDeletion
    if (pendingDelete != null) {
        DeleteSignerConfirmDialog(
            signerName = pendingDelete.name,
            onDismiss = onCancelDeleteSigner,
            onConfirmDelete = onConfirmDeleteSigner
        )
    }
}

/**
 * Fluxo de Criar Grupo de Signatários:
 * - novo_grupo.png (Estado inicial vazio: "Nenhum contato selecionado" + "+ Adicionar contato" + "Continuar" desabilitado)
 * - novo_grupo_finalizar.png (Estado com contatos selecionados: checkboxes marcados + botão "Criar grupo")
 */
@Composable
private fun NewGroupScreenContent(
    uiState: SignerManagementUiState,
    onCloseOrCancel: () -> Unit,
    onGroupNameChanged: (String) -> Unit,
    onOpenAddContactsSheet: () -> Unit,
    onToggleMember: (String) -> Unit,
    onCreateGroupClick: () -> Unit
) {
    val selectedMembers = uiState.groupConfirmedMembers
    val hasSelectedMembers = selectedMembers.isNotEmpty()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onCloseOrCancel)
                        .padding(6.dp)
                ) {
                    CloseIcon(color = LetsSignColors.Primary, size = 20.dp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Novo Grupo",
                    style = MaterialTheme.typography.headlineMedium
                )
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
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LetsSignSecondaryButton(
                        text = "Cancelar",
                        onClick = onCloseOrCancel,
                        modifier = Modifier.weight(0.38f)
                    )
                    LetsSignPrimaryButton(
                        text = if (hasSelectedMembers) "Criar grupo" else "Continuar",
                        onClick = onCreateGroupClick,
                        enabled = uiState.canCreateGroup,
                        modifier = Modifier.weight(0.62f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Nome do grupo",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Input com halo/borda roxa (novo_grupo.png e novo_grupo_finalizar.png)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(LetsSignColors.VioletSoft.copy(alpha = 0.45f))
                    .padding(3.dp)
            ) {
                BasicTextField(
                    value = uiState.groupNameInput,
                    onValueChange = onGroupNameChanged,
                    singleLine = true,
                    interactionSource = interactionSource,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = LetsSignColors.Ink,
                        fontSize = 17.sp
                    ),
                    cursorBrush = SolidColor(LetsSignColors.Primary),
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color.White)
                                .border(
                                    width = if (isFocused || uiState.groupNameInput.isNotBlank()) 1.75.dp else 1.5.dp,
                                    color = LetsSignColors.Primary,
                                    shape = RoundedCornerShape(11.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                if (uiState.groupNameInput.isEmpty()) {
                                    Text(
                                        text = "Ex.: Turma 15 Calculo II AGO - 2027",
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            color = LetsSignColors.Body,
                                            fontSize = 17.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (!hasSelectedMembers) {
                // novo_grupo.png: Card central vazio ("Nenhum contato selecionado" + "+ Adicionar contato")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(LetsSignColors.Mist)
                        .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(22.dp))
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
                        text = "Nenhum contato selecionado",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    LetsSignPrimaryButton(
                        text = "Adicionar contato",
                        onClick = onOpenAddContactsSheet,
                        leadingIcon = {
                            PlusIcon(color = Color.White, size = 18.dp)
                        }
                    )
                }
            } else {
                // novo_grupo_finalizar.png: Lista de contatos selecionados com checkboxes marcados
                selectedMembers.forEach { member ->
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleMember(member.id) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GroupCheckbox(
                                checked = true,
                                onCheckedChange = { onToggleMember(member.id) }
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            LetsSignAvatar(fullName = member.name, size = 46.dp)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = member.name,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = 17.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = member.email,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = LetsSignColors.Muted
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                LetsSignSecondaryButton(
                    text = "+ Adicionar contato",
                    onClick = onOpenAddContactsSheet,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Bottom Sheet "Selecionar contatos" (novo_grupo_selecionar_contatos.png)
 */
@Composable
private fun GroupContactSelectionSheetContent(
    uiState: SignerManagementUiState,
    onDismiss: () -> Unit,
    onCreateNewContactClick: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onToggleContact: (String) -> Unit,
    onConfirmSelection: () -> Unit
) {
    val contacts = uiState.filteredGroupPickerSigners
    val selectedCount = uiState.groupPickerSelectedIds.size
    val buttonLabel = when (selectedCount) {
        0 -> "Confirmar seleção"
        1 -> "Adicionar 1 selecionado"
        else -> "Adicionar $selectedCount selecionados"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Selecionar contatos",
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

        Spacer(modifier = Modifier.height(18.dp))

        // Botão "Cadastrar novo contato" em destaque no topo (novo_grupo_selecionar_contatos.png)
        LetsSignPrimaryButton(
            text = "Cadastrar novo contato",
            onClick = onCreateNewContactClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        LetsSignSearchBar(
            query = uiState.groupPickerSearchQuery,
            onQueryChanged = onSearchQueryChanged,
            placeholder = "Buscar por grupo, nome ou e-mail"
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 350.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(items = contacts, key = { it.id }) { contact ->
                val isChecked = contact.id in uiState.groupPickerSelectedIds
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleContact(contact.id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GroupCheckbox(
                            checked = isChecked,
                            onCheckedChange = { onToggleContact(contact.id) }
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        LetsSignAvatar(fullName = contact.name, size = 46.dp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact.name,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 17.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = contact.email,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LetsSignColors.Muted
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = LetsSignColors.Line)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LetsSignPrimaryButton(
            text = buttonLabel,
            onClick = onConfirmSelection,
            enabled = selectedCount > 0,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun GroupCheckbox(
    checked: Boolean,
    onCheckedChange: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) LetsSignColors.Primary else Color.White)
            .border(
                width = if (checked) 0.dp else 1.5.dp,
                color = if (checked) LetsSignColors.Primary else LetsSignColors.Line,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onCheckedChange),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            CheckIcon(color = Color.White, size = 14.dp)
        }
    }
}

@Composable
private fun ContactsListScreenContent(
    uiState: SignerManagementUiState,
    onSearchQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onNewContactClick: () -> Unit,
    onNewGroupClick: () -> Unit,
    onContactClick: (User) -> Unit
) {
    val signers = uiState.filteredSigners
    val isSearching = uiState.searchQuery.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header gradiente com busca embutida (Contatos Lista.png / Contatos Busca.png)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                    .background(brush = LetsSignColors.HeaderGradient)
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Column {
                    Text(
                        text = "Contatos",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = Color.White,
                            fontSize = 30.sp,
                            lineHeight = 34.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Pessoas e empresas que podem assinar seus documentos.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    LetsSignSearchBar(
                        query = uiState.searchQuery,
                        onQueryChanged = onSearchQueryChanged,
                        placeholder = "Buscar por nome, e-mail ou CPF/CNPJ"
                    )
                }
            }

            if (signers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LetsSignColors.Mist)
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSearching) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 64.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(LetsSignColors.VioletSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                SearchIcon(color = LetsSignColors.Primary, size = 28.dp)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Nenhum contato encontrado",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Confira o nome, o e-mail ou o CPF/CNPJ, ou limpe a busca para ver todos os contatos.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LetsSignColors.Muted
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            LetsSignSecondaryButton(
                                text = "Limpar busca",
                                onClick = onClearSearch
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(bottom = 64.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(LetsSignColors.VioletSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                UserPlusIcon(color = LetsSignColors.Primary, size = 28.dp)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Nenhum contato ainda",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Cadastre quem vai assinar seus documentos. Você pode adicionar pessoas físicas ou empresas.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = LetsSignColors.Muted
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                val countLabel = if (isSearching) {
                    val suffix = if (signers.size == 1) "resultado" else "resultados"
                    "${signers.size} $suffix para \"${uiState.searchQuery.trim()}\""
                } else {
                    "${signers.size} contatos cadastrados"
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 24.dp,
                        end = 24.dp,
                        top = 18.dp,
                        bottom = 116.dp
                    )
                ) {
                    item {
                        Text(
                            text = countLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = LetsSignColors.Muted,
                                fontSize = 14.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    itemsIndexed(items = signers, key = { _, item -> item.id }) { index, signer ->
                        ContactListRow(
                            signer = signer,
                            highlightQuery = uiState.searchQuery.trim(),
                            onClick = { onContactClick(signer) }
                        )
                        if (index < signers.lastIndex) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = LetsSignColors.Line
                            )
                        }
                    }
                }
            }
        }

        // Snackbar ("Contato salvo com sucesso." ou "Grupo criado com sucesso.")
        if (!uiState.snackbarMessage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 92.dp)
            ) {
                LetsSignSnackbar(
                    message = uiState.snackbarMessage,
                    type = if (uiState.isErrorSnackbar) {
                        LetsSignSnackbarType.ERROR
                    } else {
                        LetsSignSnackbarType.SUCCESS_DARK
                    }
                )
            }
        }

        // Botões flutuantes na base: "+ Novo grupo" e "+ Novo contato"
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 18.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LetsSignSecondaryButton(
                text = "+ Novo grupo",
                onClick = onNewGroupClick,
                modifier = Modifier.weight(0.45f)
            )
            LetsSignPrimaryButton(
                text = "Novo contato",
                onClick = onNewContactClick,
                leadingIcon = {
                    PlusIcon(color = Color.White, size = 18.dp)
                },
                modifier = Modifier.weight(0.55f)
            )
        }
    }
}

@Composable
private fun ContactListRow(
    signer: User,
    highlightQuery: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LetsSignAvatar(fullName = signer.name, size = 44.dp)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildHighlightedName(signer.name, highlightQuery),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = LetsSignColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = signer.email,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LetsSignColors.Muted,
                    fontSize = 15.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        LetsSignStatusChip(
            label = signer.personType.displayLabel,
            style = StatusChipStyle.ROLE
        )
    }
}

private fun buildHighlightedName(fullName: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(fullName)
    val startIdx = fullName.lowercase().indexOf(query.lowercase())
    if (startIdx < 0) return AnnotatedString(fullName)
    val endIdx = (startIdx + query.length).coerceAtMost(fullName.length)
    return buildAnnotatedString {
        append(fullName.substring(0, startIdx))
        withStyle(SpanStyle(color = LetsSignColors.Primary, fontWeight = FontWeight.Bold)) {
            append(fullName.substring(startIdx, endIdx))
        }
        append(fullName.substring(endIdx))
    }
}

@Composable
private fun ContactFormScreenContent(
    uiState: SignerManagementUiState,
    onBack: () -> Unit,
    onPersonTypeChanged: (PersonType) -> Unit,
    onCpfCnpjChanged: (String) -> Unit,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onBirthDateChanged: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val isEditing = uiState.editingSignerId != null
    var isTypeMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
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
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Editar contato" else "Novo signatário",
                    style = MaterialTheme.typography.headlineMedium
                )
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
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LetsSignSecondaryButton(
                        text = "Cancelar",
                        onClick = onBack,
                        modifier = Modifier.weight(0.38f)
                    )
                    LetsSignPrimaryButton(
                        text = "Salvar",
                        onClick = onSaveClick,
                        isLoading = uiState.isSaving,
                        modifier = Modifier.weight(0.62f)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            val summaryBanner = uiState.formSummaryErrorBanner
            if (!summaryBanner.isNullOrBlank()) {
                LetsSignErrorBanner(text = summaryBanner)
                Spacer(modifier = Modifier.height(20.dp))
            }

            Text(
                text = "Dados gerais",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Tipo",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = LetsSignColors.Ink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isEditing) LetsSignColors.Mist else Color.White)
                        .border(1.25.dp, LetsSignColors.Line, RoundedCornerShape(10.dp))
                        .clickable(enabled = !isEditing) { isTypeMenuExpanded = true }
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.personType.displayLabel,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = if (isEditing) LetsSignColors.Muted else LetsSignColors.Ink,
                            fontSize = 17.sp
                        )
                    )
                    Text(
                        text = "⌄",
                        color = LetsSignColors.Muted,
                        fontSize = 18.sp
                    )
                }

                DropdownMenu(
                    expanded = isTypeMenuExpanded,
                    onDismissRequest = { isTypeMenuExpanded = false },
                    modifier = Modifier.background(Color.White)
                ) {
                    PersonType.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option.displayLabel,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        color = LetsSignColors.Ink
                                    )
                                )
                            },
                            onClick = {
                                isTypeMenuExpanded = false
                                onPersonTypeChanged(option)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (uiState.personType == PersonType.FISICA) {
                ContactFormInputField(
                    label = "CPF",
                    isRequired = true,
                    value = uiState.cpfCnpj,
                    onValueChange = onCpfCnpjChanged,
                    placeholder = "000.000.000-00",
                    errorMessage = uiState.cpfCnpjError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                ContactFormInputField(
                    label = "Nome",
                    isRequired = true,
                    value = uiState.name,
                    onValueChange = onNameChanged,
                    placeholder = "",
                    errorMessage = uiState.nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                ContactFormInputField(
                    label = "E-mail",
                    isRequired = !isEditing,
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    placeholder = "nome@empresa.com.br",
                    errorMessage = uiState.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                ContactFormInputField(
                    label = "Data de nascimento",
                    isRequired = !isEditing,
                    value = uiState.birthDate,
                    onValueChange = onBirthDateChanged,
                    placeholder = "dd/mm/aaaa",
                    errorMessage = uiState.birthDateError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    trailingIcon = {
                        CalendarIcon(color = LetsSignColors.Muted, size = 20.dp)
                    }
                )
            } else {
                ContactFormInputField(
                    label = "CNPJ",
                    isRequired = true,
                    value = uiState.cpfCnpj,
                    onValueChange = onCpfCnpjChanged,
                    placeholder = "00.000.000/0000-00",
                    errorMessage = uiState.cpfCnpjError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        SearchIcon(color = LetsSignColors.Muted, size = 19.dp)
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                ContactFormInputField(
                    label = "Razão social",
                    isRequired = true,
                    value = uiState.name,
                    onValueChange = onNameChanged,
                    placeholder = "",
                    errorMessage = uiState.nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                ContactFormInputField(
                    label = "E-mail",
                    isRequired = !isEditing,
                    value = uiState.email,
                    onValueChange = onEmailChanged,
                    placeholder = "nome@empresa.com.br",
                    errorMessage = uiState.emailError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    )
                )
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Excluir contato",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = LetsSignColors.ErrorText,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onDeleteClick)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ContactFormInputField(
    label: String,
    isRequired: Boolean,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val hasError = !errorMessage.isNullOrBlank()

    val borderColor = when {
        hasError -> LetsSignColors.Error
        isFocused -> LetsSignColors.Primary
        else -> LetsSignColors.Line
    }
    val borderWidth = if (hasError || isFocused) 1.5.dp else 1.25.dp

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
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = keyboardOptions,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = LetsSignColors.Ink,
                fontSize = 17.sp
            ),
            cursorBrush = SolidColor(LetsSignColors.Primary),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                        .border(borderWidth, borderColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    if (hasError) {
                        Spacer(modifier = Modifier.width(10.dp))
                        ErrorCircleIcon(color = LetsSignColors.Error, size = 20.dp)
                    } else if (trailingIcon != null) {
                        Spacer(modifier = Modifier.width(10.dp))
                        trailingIcon()
                    }
                }
            }
        )

        if (hasError) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(modifier = Modifier.padding(top = 2.dp)) {
                    ErrorCircleIcon(color = LetsSignColors.Error, size = 16.dp)
                }
                Text(
                    text = errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = LetsSignColors.ErrorText,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                )
            }
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
                        role = UserRole.SIGNER,
                        personType = PersonType.FISICA
                    ),
                    User(
                        id = "2",
                        name = "Nova Soluções Ltda.",
                        email = "contato@novasolucoes.com.br",
                        role = UserRole.SIGNER,
                        personType = PersonType.JURIDICA
                    )
                )
            ),
            onSearchQueryChanged = {},
            onClearSearch = {},
            onOpenCreateForm = {},
            onOpenEditForm = {},
            onDismissForm = {},
            onPersonTypeChanged = {},
            onCpfCnpjChanged = {},
            onNameChanged = {},
            onEmailChanged = {},
            onBirthDateChanged = {},
            onSaveSigner = {},
            onDeleteEditingSigner = {},
            onCancelDeleteSigner = {},
            onConfirmDeleteSigner = {}
        )
    }
}
