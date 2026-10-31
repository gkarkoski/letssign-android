package com.example.challenge_letssign.ui.admin.signers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "123456",
    val isPasswordVisible: Boolean = false,
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    val isSaving: Boolean = false,
    val signerPendingDeletion: User? = null,
    val snackbarMessage: String? = null,
    val isErrorSnackbar: Boolean = false
) {
    val filteredSigners: List<User>
        get() {
            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) return allSigners
            return allSigners.filter {
                it.name.lowercase().contains(q) || it.email.lowercase().contains(q)
            }
        }

    val isEmailValidFormat: Boolean
        get() = email.trim().isNotBlank() && SaveSignerUseCase.EMAIL_REGEX.matches(email.trim())
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

    fun onOpenCreateForm() {
        _uiState.update {
            it.copy(
                isFormVisible = true,
                editingSignerId = null,
                name = "",
                email = "",
                phone = "",
                password = "123456",
                isPasswordVisible = false,
                nameError = null,
                emailError = null,
                phoneError = null,
                passwordError = null
            )
        }
    }

    fun onOpenEditForm(signer: User) {
        _uiState.update {
            it.copy(
                isFormVisible = true,
                editingSignerId = signer.id,
                name = signer.name,
                email = signer.email,
                phone = signer.phone,
                password = signer.password,
                isPasswordVisible = false,
                nameError = null,
                emailError = null,
                phoneError = null,
                passwordError = null
            )
        }
    }

    fun onDismissForm() {
        _uiState.update {
            it.copy(
                isFormVisible = false,
                nameError = null,
                emailError = null,
                phoneError = null,
                passwordError = null
            )
        }
    }

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = null) }
    }

    fun onEmailChanged(newEmail: String) {
        _uiState.update { it.copy(email = newEmail, emailError = null) }
    }

    fun onPhoneChanged(newPhone: String) {
        _uiState.update { it.copy(phone = newPhone, phoneError = null) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(password = newPassword, passwordError = null) }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onSaveSigner() {
        val current = _uiState.value
        if (current.isSaving) return

        val trimmedName = current.name.trim()
        val trimmedEmail = current.email.trim()
        val trimmedPhone = current.phone.trim()
        val trimmedPassword = current.password.trim()

        var nameError: String? = null
        var emailError: String? = null
        var phoneError: String? = null
        var passwordError: String? = null
        var hasError = false

        if (trimmedName.isBlank()) {
            nameError = "Informe o nome completo do signatário."
            hasError = true
        }
        if (trimmedEmail.isBlank() || !SaveSignerUseCase.EMAIL_REGEX.matches(trimmedEmail)) {
            emailError = "E-mail inválido. Confira o formato."
            hasError = true
        }
        if (trimmedPhone.isBlank()) {
            phoneError = "Informe o telefone do signatário."
            hasError = true
        }
        if (trimmedPassword.isBlank()) {
            passwordError = "Informe uma senha de acesso para o signatário."
            hasError = true
        }

        if (hasError) {
            _uiState.update {
                it.copy(
                    nameError = nameError,
                    emailError = emailError,
                    phoneError = phoneError,
                    passwordError = passwordError
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }

            when (
                val result = saveSignerUseCase(
                    signerId = current.editingSignerId,
                    name = trimmedName,
                    email = trimmedEmail,
                    phone = trimmedPhone,
                    password = trimmedPassword
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            isFormVisible = false,
                            editingSignerId = null,
                            snackbarMessage = "Signatário salvo",
                            isErrorSnackbar = false
                        )
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
                            snackbarMessage = "Signatário excluído",
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
