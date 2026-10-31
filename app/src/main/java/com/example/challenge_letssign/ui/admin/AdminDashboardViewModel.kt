package com.example.challenge_letssign.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.usecase.document.AdminDocumentOverview
import com.example.challenge_letssign.domain.usecase.document.ObserveAdminDocumentOverviewsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val selectedTab: AdminDocumentTab = AdminDocumentTab.SENT,
    val allDocuments: List<AdminDocumentOverview> = emptyList()
) {
    val draftDocuments: List<AdminDocumentOverview>
        get() = allDocuments.filter { it.isDraft }

    val sentDocuments: List<AdminDocumentOverview>
        get() = allDocuments.filter { it.isSentWaitingAll }

    val partiallySignedDocuments: List<AdminDocumentOverview>
        get() = allDocuments.filter { it.isPartiallySigned }

    val completedDocuments: List<AdminDocumentOverview>
        get() = allDocuments.filter { it.isCompleted }

    val visibleTabDocuments: List<AdminDocumentOverview>
        get() = when (selectedTab) {
            AdminDocumentTab.SENT -> sentDocuments
            AdminDocumentTab.PARTIALLY_SIGNED -> partiallySignedDocuments
            AdminDocumentTab.COMPLETED -> completedDocuments
        }
}

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val observeAdminDocumentOverviewsUseCase: ObserveAdminDocumentOverviewsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    private var loadedAdminId: String? = null

    fun observeDocumentsForAdmin(adminId: String) {
        if (adminId.isBlank() || loadedAdminId == adminId) return
        loadedAdminId = adminId

        viewModelScope.launch {
            observeAdminDocumentOverviewsUseCase(adminId).collect { list ->
                _uiState.update { it.copy(allDocuments = list) }
            }
        }
    }

    fun onSelectTab(tab: AdminDocumentTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
