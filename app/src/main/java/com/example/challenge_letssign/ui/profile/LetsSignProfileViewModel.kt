package com.example.challenge_letssign.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.PersonType
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val personType: PersonType = PersonType.FISICA,
    val cpfCnpj: String = "123.456.789-09",
    val name: String = "Marina Albuquerque",
    val email: String = "marina.albuquerque@exemplo.com.br",
    val birthDate: String = "14/03/1988",
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
)

@HiltViewModel
class LetsSignProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var loadedUserId: String? = null

    fun loadUserProfile(userId: String, fallbackName: String = "") {
        if (userId.isBlank() || loadedUserId == userId) return
        loadedUserId = userId

        viewModelScope.launch {
            when (val result = userRepository.getUserById(userId)) {
                is AppResult.Success -> {
                    val u = result.data
                    _uiState.update {
                        it.copy(
                            user = u,
                            personType = u.personType,
                            cpfCnpj = u.cpfCnpj.ifBlank { "123.456.789-09" },
                            name = u.name.ifBlank { fallbackName.ifBlank { "Marina Albuquerque" } },
                            email = u.email.ifBlank { "marina.albuquerque@exemplo.com.br" },
                            birthDate = u.birthDate.ifBlank { "14/03/1988" }
                        )
                    }
                }
                is AppResult.Error -> {
                    if (fallbackName.isNotBlank()) {
                        _uiState.update { it.copy(name = fallbackName) }
                    }
                }
            }
        }
    }

    fun onCpfCnpjChanged(newValue: String) {
        _uiState.update { it.copy(cpfCnpj = newValue) }
    }

    fun onNameChanged(newValue: String) {
        _uiState.update { it.copy(name = newValue) }
    }

    fun onEmailChanged(newValue: String) {
        _uiState.update { it.copy(email = newValue) }
    }

    fun onBirthDateChanged(newValue: String) {
        _uiState.update { it.copy(birthDate = newValue) }
    }

    fun onSaveProfile() {
        val current = _uiState.value
        val existingUser = current.user ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val updated = existingUser.copy(
                cpfCnpj = current.cpfCnpj.trim(),
                name = current.name.trim(),
                email = current.email.trim(),
                birthDate = current.birthDate.trim()
            )
            userRepository.updateUser(updated)
            _uiState.update {
                it.copy(
                    user = updated,
                    isSaving = false,
                    snackbarMessage = "Perfil atualizado com sucesso."
                )
            }
        }
    }

    fun onDismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
