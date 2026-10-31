package com.example.challenge_letssign.ui.signer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.usecase.signer.ObserveSignerDocumentsUseCase
import com.example.challenge_letssign.domain.usecase.signer.SignerAssignedDocument
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SignerDocumentTab(val title: String) {
    PENDING("Pendentes"),
    SIGNED("Assinados")
}

data class SignerDashboardUiState(
    val selectedTab: SignerDocumentTab = SignerDocumentTab.PENDING,
    val allAssignedDocuments: List<SignerAssignedDocument> = emptyList()
) {
    val pendingDocuments: List<SignerAssignedDocument>
        get() = allAssignedDocuments.filter { it.isPendingForMe }

    val signedDocuments: List<SignerAssignedDocument>
        get() = allAssignedDocuments.filter { it.isSignedByMe }

    val visibleDocuments: List<SignerAssignedDocument>
        get() = when (selectedTab) {
            SignerDocumentTab.PENDING -> pendingDocuments
            SignerDocumentTab.SIGNED -> signedDocuments
        }
}

@HiltViewModel
class SignerDashboardViewModel @Inject constructor(
    private val observeSignerDocumentsUseCase: ObserveSignerDocumentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignerDashboardUiState())
    val uiState: StateFlow<SignerDashboardUiState> = _uiState.asStateFlow()

    private var loadedSignerId: String? = null

    fun observeDocuments(signerId: String) {
        if (signerId.isBlank() || loadedSignerId == signerId) return
        loadedSignerId = signerId

        viewModelScope.launch {
            observeSignerDocumentsUseCase(signerId).collect { docs ->
                _uiState.update { it.copy(allAssignedDocuments = docs) }
            }
        }
    }

    fun onSelectTab(tab: SignerDocumentTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
