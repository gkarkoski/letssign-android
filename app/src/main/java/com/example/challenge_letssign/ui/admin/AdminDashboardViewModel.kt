package com.example.challenge_letssign.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.usecase.document.AdminDocumentOverview
import com.example.challenge_letssign.domain.usecase.document.ObserveAdminDocumentOverviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MySignaturesTab(val title: String) {
    PENDING("Pendentes"),
    SIGNED("Assinados")
}

data class AdminDashboardUiState(
    val selectedTab: AdminDocumentTab = AdminDocumentTab.SENT,
    val mySignaturesTab: MySignaturesTab = MySignaturesTab.PENDING,
    val allDocuments: List<AdminDocumentOverview> = emptyList(),
    val searchQuery: String = "",
    val waitingOthersSearchQuery: String = "",
    val isLoading: Boolean = false,
    val hasError: Boolean = false
) {
    val draftDocuments: List<AdminDocumentOverview>
        get() = allDocuments.filter { it.isDraft }

    val sentDocuments: List<AdminDocumentOverview>
        get() = allDocuments.filter { it.isSentWaitingAll }

    val partiallySignedDocuments: List<AdminDocumentOverview>
        get() = allDocuments.filter { it.isPartiallySigned }

    val completedDocuments: List<AdminDocumentOverview>
        get() = allDocuments.filter { it.isCompleted }

    /**
     * Na aba "Enviados" do novo Figma (Home Admin Aguardando Assinaturas.png),
     * aparecem tanto os documentos Enviados (0/3) quanto os Parcialmente assinados (2/3).
     * Mantemos `visibleTabDocuments` compatível tanto com 2 abas (SENT, COMPLETED)
     * quanto caso PARTIALLY_SIGNED seja consultada diretamente.
     */
    val visibleTabDocuments: List<AdminDocumentOverview>
        get() {
            val baseList = when (selectedTab) {
                AdminDocumentTab.SENT -> allDocuments.filter { it.isSentWaitingAll || it.isPartiallySigned }
                AdminDocumentTab.PARTIALLY_SIGNED -> partiallySignedDocuments
                AdminDocumentTab.COMPLETED -> completedDocuments
            }
            return filterByQuery(baseList, searchQuery)
        }

    val filteredWaitingOthersDocuments: List<AdminDocumentOverview>
        get() = filterByQuery(partiallySignedDocuments, waitingOthersSearchQuery)

    val myPendingSignatures: List<AdminDocumentOverview>
        get() = allDocuments.filter { it.isSentWaitingAll || it.isPartiallySigned }

    val mySignedDocuments: List<AdminDocumentOverview>
        get() = completedDocuments

    private fun filterByQuery(
        list: List<AdminDocumentOverview>,
        rawQuery: String
    ): List<AdminDocumentOverview> {
        val q = rawQuery.trim().lowercase()
        if (q.isEmpty()) return list
        return list.filter { item ->
            item.document.title.lowercase().contains(q) ||
                item.signerNames.any { name -> name.lowercase().contains(q) }
        }
    }
}

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val observeAdminDocumentOverviewsUseCase: ObserveAdminDocumentOverviewsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    var reloadDelayMillis: Long = 1500L

    private var loadedAdminId: String? = null

    fun observeDocumentsForAdmin(adminId: String) {
        if (adminId.isBlank() || loadedAdminId == adminId) return
        loadedAdminId = adminId

        viewModelScope.launch {
            observeAdminDocumentOverviewsUseCase(adminId).collect { list ->
                _uiState.update {
                    it.copy(
                        allDocuments = list,
                        isLoading = false,
                        hasError = false
                    )
                }
            }
        }
    }

    fun onSelectTab(tab: AdminDocumentTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                searchQuery = ""
            )
        }
    }

    fun onSelectMySignaturesTab(tab: MySignaturesTab) {
        _uiState.update { it.copy(mySignaturesTab = tab) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun onClearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
    }

    fun onWaitingOthersSearchQueryChanged(query: String) {
        _uiState.update { it.copy(waitingOthersSearchQuery = query) }
    }

    fun onRetryLoad() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    hasError = false
                )
            }
            if (reloadDelayMillis > 0L) {
                delay(reloadDelayMillis)
            }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    hasError = false
                )
            }
        }
    }
}
