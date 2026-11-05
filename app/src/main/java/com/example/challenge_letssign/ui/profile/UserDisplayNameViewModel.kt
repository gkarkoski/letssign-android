package com.example.challenge_letssign.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.repository.UserRepository
import com.example.challenge_letssign.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Nome atual do usuário logado, lido do banco.
 * As Homes usavam só o nome recebido no login; ao editar o Perfil, a saudação ("Olá, ...")
 * ficava desatualizada. Este ViewModel relê o nome sempre que a Home volta a ser exibida.
 */
@HiltViewModel
class UserDisplayNameViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _name = MutableStateFlow<String?>(null)
    val name: StateFlow<String?> = _name.asStateFlow()

    fun refresh(userId: String) {
        if (userId.isBlank()) return
        viewModelScope.launch {
            val result = userRepository.getUserById(userId)
            if (result is AppResult.Success) {
                _name.value = result.data.name
            }
        }
    }
}
