package com.example.challenge_letssign.ui.admin.signers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.usecase.signer.DeleteSignerUseCase
import com.example.challenge_letssign.domain.usecase.signer.ObserveSignersUseCase
import com.example.challenge_letssign.domain.usecase.signer.SaveSignerUseCase
import com.example.challenge_letssign.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignerManagementUiState(
    val allSigners: List<User> = emptyList(),
    val searchQuery: String = "",
    val isFormVisible: Boolean = false,
    val editingSignerId: String? = null,
    val personType: PersonType = PersonType.FISICA,
    val cpfCnpj: String = "",
    val name: String = "",
    val email: String = "",
    val birthDate: String = "",
    val cpfCnpjError: String? = null,
    val nameError: String? = null,
    val emailError: String? = null,
    val birthDateError: String? = null,
    val isSaving: Boolean = false,
    val signerPendingDeletion: User? = null,
    val snackbarMessage: String? = null,
    val isErrorSnackbar: Boolean = false,
    // Fluxo Novo Grupo (novo_grupo.png, novo_grupo_selecionar_contatos.png, novo_grupo_finalizar.png)
    val isNewGroupVisible: Boolean = false,
    val isGroupContactPickerSheetOpen: Boolean = false,
    val isCreatingContactFromGroupPicker: Boolean = false,
    val groupNameInput: String = "",
    val groupConfirmedMemberIds: Set<String> = emptySet(),
    val groupPickerSelectedIds: Set<String> = emptySet(),
    val groupPickerSearchQuery: String = ""
) {
    val filteredSigners: List<User>
        get() {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) return allSigners
            val qDigits = q.filter { it.isDigit() }
            return allSigners.filter { user ->
                user.name.lowercase().contains(q) ||
                    user.email.lowercase().contains(q) ||
                    user.cpfCnpj.lowercase().contains(q) ||
                    (qDigits.isNotEmpty() && user.cpfCnpj.filter { it.isDigit() }.contains(qDigits)) ||
                    user.groupName.lowercase().contains(q)
            }
        }

    val filteredGroupPickerSigners: List<User>
        get() {
            val q = groupPickerSearchQuery.trim().lowercase()
            val sorted = allSigners.sortedBy { preferredGroupContactOrder(it.name) }
            if (q.isEmpty()) return sorted
            return sorted.filter { user ->
                user.name.lowercase().contains(q) ||
                    user.email.lowercase().contains(q) ||
                    user.groupName.lowercase().contains(q)
            }
        }

    val groupConfirmedMembers: List<User>
        get() = allSigners
            .filter { it.id in groupConfirmedMemberIds }
            .sortedBy { preferredGroupContactOrder(it.name) }

    val canCreateGroup: Boolean
        get() = groupNameInput.trim().isNotBlank() && groupConfirmedMemberIds.isNotEmpty()

    val validationErrorCount: Int
        get() = listOfNotNull(cpfCnpjError, nameError, emailError, birthDateError).size

    val formSummaryErrorBanner: String?
        get() = when {
            validationErrorCount <= 0 -> null
            validationErrorCount == 1 -> "Corrija 1 campo para salvar o contato."
            else -> "Corrija $validationErrorCount campos para salvar o contato."
        }

    private fun preferredGroupContactOrder(name: String): Int = when {
        name.startsWith("Marina", ignoreCase = true) -> 0
        name.startsWith("Rafael", ignoreCase = true) -> 1
        name.startsWith("Beatriz", ignoreCase = true) -> 2
        name.startsWith("Camila", ignoreCase = true) -> 3
        name.startsWith("Lucas", ignoreCase = true) -> 4
        else -> 10
    }
}

@HiltViewModel
class SignerManagementViewModel @Inject constructor(
    observeSignersUseCase: ObserveSignersUseCase,
    private val saveSignerUseCase: SaveSignerUseCase,
    private val deleteSignerUseCase: DeleteSignerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignerManagementUiState())
    val uiState: StateFlow<SignerManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeSignersUseCase().collect { signers ->
                _uiState.update { it.copy(allSigners = signers) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onClearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
    }

    fun onOpenCreateForm() {
        _uiState.update {
            it.copy(
                isFormVisible = true,
                isCreatingContactFromGroupPicker = false,
                editingSignerId = null,
                personType = PersonType.FISICA,
                cpfCnpj = "",
                name = "",
                email = "",
                birthDate = "",
                cpfCnpjError = null,
                nameError = null,
                emailError = null,
                birthDateError = null
            )
        }
    }

    fun onOpenCreateFormFromGroupPicker() {
        _uiState.update {
            it.copy(
                isGroupContactPickerSheetOpen = false,
                isFormVisible = true,
                isCreatingContactFromGroupPicker = true,
                editingSignerId = null,
                personType = PersonType.FISICA,
                cpfCnpj = "",
                name = "",
                email = "",
                birthDate = "",
                cpfCnpjError = null,
                nameError = null,
                emailError = null,
                birthDateError = null
            )
        }
    }

    fun onOpenEditForm(signer: User) {
        _uiState.update {
            it.copy(
                isFormVisible = true,
                isCreatingContactFromGroupPicker = false,
                editingSignerId = signer.id,
                personType = signer.personType,
                cpfCnpj = signer.cpfCnpj,
                name = signer.name,
                email = signer.email,
                birthDate = signer.birthDate,
                cpfCnpjError = null,
                nameError = null,
                emailError = null,
                birthDateError = null
            )
        }
    }

    fun onDismissForm() {
        _uiState.update { state ->
            if (state.isCreatingContactFromGroupPicker) {
                state.copy(
                    isFormVisible = false,
                    isCreatingContactFromGroupPicker = false,
                    isNewGroupVisible = true,
                    isGroupContactPickerSheetOpen = true,
                    editingSignerId = null,
                    cpfCnpjError = null,
                    nameError = null,
                    emailError = null,
                    birthDateError = null
                )
            } else {
                state.copy(
                    isFormVisible = false,
                    editingSignerId = null,
                    cpfCnpjError = null,
                    nameError = null,
                    emailError = null,
                    birthDateError = null
                )
            }
        }
    }

    // --- Fluxo Novo Grupo ---

    fun onOpenNewGroupFlow() {
        _uiState.update {
            it.copy(
                isNewGroupVisible = true,
                isGroupContactPickerSheetOpen = false,
                groupNameInput = "",
                groupConfirmedMemberIds = emptySet(),
                groupPickerSelectedIds = emptySet(),
                groupPickerSearchQuery = ""
            )
        }
    }

    fun onDismissNewGroupFlow() {
        _uiState.update {
            it.copy(
                isNewGroupVisible = false,
                isGroupContactPickerSheetOpen = false,
                groupNameInput = "",
                groupConfirmedMemberIds = emptySet(),
                groupPickerSelectedIds = emptySet(),
                groupPickerSearchQuery = ""
            )
        }
    }

    fun onGroupNameChanged(newGroupName: String) {
        _uiState.update { it.copy(groupNameInput = newGroupName) }
    }

    fun onOpenGroupContactPickerSheet() {
        _uiState.update { state ->
            state.copy(
                isGroupContactPickerSheetOpen = true,
                groupPickerSelectedIds = state.groupConfirmedMemberIds,
                groupPickerSearchQuery = ""
            )
        }
    }

    fun onDismissGroupContactPickerSheet() {
        _uiState.update { it.copy(isGroupContactPickerSheetOpen = false) }
    }

    fun onGroupPickerSearchQueryChanged(query: String) {
        _uiState.update { it.copy(groupPickerSearchQuery = query) }
    }

    fun onToggleGroupPickerContact(signerId: String) {
        _uiState.update { state ->
            val current = state.groupPickerSelectedIds
            val updated = if (signerId in current) current - signerId else current + signerId
            state.copy(groupPickerSelectedIds = updated)
        }
    }

    fun onConfirmGroupPickerSelection() {
        _uiState.update { state ->
            state.copy(
                groupConfirmedMemberIds = state.groupPickerSelectedIds,
                isGroupContactPickerSheetOpen = false,
                groupPickerSearchQuery = ""
            )
        }
    }

    fun onToggleConfirmedGroupMember(signerId: String) {
        _uiState.update { state ->
            val current = state.groupConfirmedMemberIds
            val updated = if (signerId in current) current - signerId else current + signerId
            state.copy(groupConfirmedMemberIds = updated)
        }
    }

    fun onCreateGroup() {
        val state = _uiState.value
        val trimmedGroupName = state.groupNameInput.trim()
        if (trimmedGroupName.isBlank() || state.groupConfirmedMemberIds.isEmpty()) return

        viewModelScope.launch {
            when (val res = saveSignerUseCase.createGroup(trimmedGroupName, state.groupConfirmedMemberIds)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isNewGroupVisible = false,
                            isGroupContactPickerSheetOpen = false,
                            groupNameInput = "",
                            groupConfirmedMemberIds = emptySet(),
                            groupPickerSelectedIds = emptySet(),
                            snackbarMessage = "Grupo \"$trimmedGroupName\" criado com sucesso.",
                            isErrorSnackbar = false
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            snackbarMessage = res.message,
                            isErrorSnackbar = true
                        )
                    }
                }
            }
        }
    }

    fun onPersonTypeChanged(newType: PersonType) {
        // Na edição o Tipo não muda (conforme Contato Editar.png)
        if (_uiState.value.editingSignerId != null) return
        _uiState.update {
            it.copy(
                personType = newType,
                cpfCnpjError = null,
                nameError = null,
                emailError = null,
                birthDateError = null
            )
        }
    }

    fun onCpfCnpjChanged(newValue: String) {
        _uiState.update { it.copy(cpfCnpj = newValue, cpfCnpjError = null) }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = null) }
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, emailError = null) }
    }

    fun onBirthDateChanged(newDate: String) {
        _uiState.update { it.copy(birthDate = newDate, birthDateError = null) }
    }

    fun onSaveSigner() {
        val current = _uiState.value
        if (current.isSaving) return

        val trimmedName = current.name.trim()
        val trimmedEmail = current.email.trim()
        val digitsCpfCnpj = current.cpfCnpj.filter { it.isDigit() }
        val trimmedBirthDate = current.birthDate.trim()

        var cpfCnpjError: String? = null
        var nameError: String? = null
        var emailError: String? = null
        var birthDateError: String? = null

        if (current.personType == PersonType.FISICA) {
            if (digitsCpfCnpj.length != 11) {
                cpfCnpjError = SaveSignerUseCase.CPF_ERROR_MESSAGE
            }
        } else {
            if (digitsCpfCnpj.length != 14) {
                cpfCnpjError = SaveSignerUseCase.CNPJ_ERROR_MESSAGE
            }
        }

        if (trimmedName.isBlank()) {
            nameError = SaveSignerUseCase.NAME_ERROR_MESSAGE
        }

        if (trimmedEmail.isBlank() || !SaveSignerUseCase.EMAIL_REGEX.matches(trimmedEmail)) {
            emailError = SaveSignerUseCase.EMAIL_ERROR_MESSAGE
        }

        if (current.personType == PersonType.FISICA && !SaveSignerUseCase.isValidBirthDate(trimmedBirthDate)) {
            birthDateError = SaveSignerUseCase.BIRTH_DATE_ERROR_MESSAGE
        }

        if (cpfCnpjError != null || nameError != null || emailError != null || birthDateError != null) {
            _uiState.update {
                it.copy(
                    cpfCnpjError = cpfCnpjError,
                    nameError = nameError,
                    emailError = emailError,
                    birthDateError = birthDateError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            when (
                val result = saveSignerUseCase(
                    signerId = current.editingSignerId,
                    personType = current.personType,
                    cpfCnpj = current.cpfCnpj,
                    name = trimmedName,
                    email = trimmedEmail,
                    birthDate = trimmedBirthDate
                )
            ) {
                is AppResult.Success -> {
                    val createdUser = result.data
                    _uiState.update { state ->
                        if (state.isCreatingContactFromGroupPicker) {
                            state.copy(
                                isSaving = false,
                                isFormVisible = false,
                                isCreatingContactFromGroupPicker = false,
                                isNewGroupVisible = true,
                                isGroupContactPickerSheetOpen = true,
                                groupPickerSelectedIds = state.groupPickerSelectedIds + createdUser.id,
                                editingSignerId = null,
                                snackbarMessage = "Contato salvo com sucesso.",
                                isErrorSnackbar = false
                            )
                        } else {
                            state.copy(
                                isSaving = false,
                                isFormVisible = false,
                                editingSignerId = null,
                                snackbarMessage = "Contato salvo com sucesso.",
                                isErrorSnackbar = false
                            )
                        }
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            emailError = result.message
                        )
                    }
                }
            }
        }
    }

    fun onDeleteEditingSigner() {
        val editingId = _uiState.value.editingSignerId ?: return
        val target = _uiState.value.allSigners.firstOrNull { it.id == editingId }
        if (target != null) {
            _uiState.update { it.copy(signerPendingDeletion = target) }
        } else {
            viewModelScope.launch {
                deleteSignerUseCase(editingId)
                _uiState.update {
                    it.copy(
                        isFormVisible = false,
                        editingSignerId = null,
                        snackbarMessage = "Contato excluído.",
                        isErrorSnackbar = false
                    )
                }
            }
        }
    }

    fun onRequestDeleteSigner(signer: User) {
        _uiState.update { it.copy(signerPendingDeletion = signer) }
    }

    fun onCancelDeleteSigner() {
        _uiState.update { it.copy(signerPendingDeletion = null) }
    }

    fun onConfirmDeleteSigner() {
        val target = _uiState.value.signerPendingDeletion ?: return
        viewModelScope.launch {
            when (val result = deleteSignerUseCase(target.id)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            signerPendingDeletion = null,
                            isFormVisible = false,
                            editingSignerId = null,
                            snackbarMessage = "Contato excluído.",
                            isErrorSnackbar = false
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            signerPendingDeletion = null,
                            snackbarMessage = result.message,
                            isErrorSnackbar = true
                        )
                    }
                }
            }
        }
    }

    fun onDismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
