package com.example.challenge_letssign.ui.admin.preparation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.usecase.document.ImportPdfUseCase
import com.example.challenge_letssign.domain.usecase.document.PrepareDraftDocumentUseCase
import com.example.challenge_letssign.domain.usecase.document.SelectedSignerAssignment
import com.example.challenge_letssign.domain.usecase.signer.ObserveSignersUseCase
import com.example.challenge_letssign.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PdfImportStatus {
    IDLE,
    IMPORTING,
    READY,
    ERROR
}

data class AssignedSignerUiModel(
    val user: User,
    val roleClass: SignerRoleClass
)

data class DocumentPreparationUiState(
    val currentStep: Int = 1,
    val documentId: String? = null,
    val title: String = "",
    val titleError: String? = null,
    val pdfStatus: PdfImportStatus = PdfImportStatus.IDLE,
    val importedPdf: ImportedPdfFile? = null,
    val failedFileName: String? = null,
    val pdfErrorMessage: String? = null,
    val availableSigners: List<User> = emptyList(),
    val assignedSigners: List<AssignedSignerUiModel> = emptyList(),
    val isSignerSelectionSheetOpen: Boolean = false,
    val pickerSearchQuery: String = "",
    val pickerSelectedIds: Set<String> = emptySet(),
    val isSavingDraft: Boolean = false,
    val generalError: String? = null,
    val completedDraftDocument: Document? = null
) {
    val canAdvanceFromStep1: Boolean
        get() = title.trim().isNotBlank() &&
            pdfStatus == PdfImportStatus.READY &&
            importedPdf != null

    val canAdvanceFromStep2: Boolean
        get() = assignedSigners.isNotEmpty() && !isSavingDraft

    val filteredPickerSigners: List<User>
        get() {
            val q = pickerSearchQuery.trim().lowercase()
            if (q.isEmpty()) return availableSigners
            return availableSigners.filter {
                it.name.lowercase().contains(q) || it.email.lowercase().contains(q)
            }
        }
}

@HiltViewModel
class DocumentPreparationViewModel @Inject constructor(
    observeSignersUseCase: ObserveSignersUseCase,
    private val importPdfUseCase: ImportPdfUseCase,
    private val prepareDraftDocumentUseCase: PrepareDraftDocumentUseCase,
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentPreparationUiState())
    val uiState: StateFlow<DocumentPreparationUiState> = _uiState.asStateFlow()

    private var loadedDraftId: String? = null

    init {
        viewModelScope.launch {
            observeSignersUseCase().collect { signers ->
                _uiState.update { state ->
                    // Mantém os signatários atribuídos atualizados caso algum nome/email mude
                    val signersById = signers.associateBy { it.id }
                    val updatedAssigned = state.assignedSigners.mapNotNull { assigned ->
                        val updatedUser = signersById[assigned.user.id]
                        updatedUser?.let { assigned.copy(user = it) }
                    }
                    state.copy(
                        availableSigners = signers,
                        assignedSigners = updatedAssigned
                    )
                }
            }
        }
    }

    /**
     * Slide 17 (Permitir remover ou substituir assinantes antes de ser enviado):
     * Carrega um documento DRAFT existente para edição de título, arquivo ou signatários.
     */
    fun loadExistingDraftIfNeeded(existingDocumentId: String?) {
        if (existingDocumentId.isNullOrBlank() || loadedDraftId == existingDocumentId) return
        loadedDraftId = existingDocumentId

        viewModelScope.launch {
            val docRes = documentRepository.getDocumentById(existingDocumentId)
            val doc = docRes.getOrNull() ?: return@launch
            if (doc.status != DocumentStatus.DRAFT) return@launch

            val signersWithUsers = documentRepository.observeDocumentSigners(existingDocumentId).first()
            val assignedModels = signersWithUsers.map {
                AssignedSignerUiModel(
                    user = it.signer,
                    roleClass = it.documentSigner.roleClass
                )
            }

            _uiState.update { state ->
                state.copy(
                    currentStep = 2,
                    documentId = doc.id,
                    title = doc.title,
                    pdfStatus = PdfImportStatus.READY,
                    importedPdf = ImportedPdfFile(
                        internalFileName = doc.fileName,
                        originalDisplayName = "${doc.title}.pdf",
                        pageCount = 1
                    ),
                    assignedSigners = assignedModels
                )
            }
        }
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.update {
            it.copy(
                title = newTitle,
                titleError = null,
                generalError = null
            )
        }
    }

    fun onPdfUriSelected(uriString: String, fallbackName: String? = null) {
        if (uriString.isBlank()) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    pdfStatus = PdfImportStatus.IMPORTING,
                    failedFileName = fallbackName ?: "documento.pdf",
                    pdfErrorMessage = null,
                    generalError = null
                )
            }

            when (val result = importPdfUseCase(uriString, fallbackName)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            pdfStatus = PdfImportStatus.READY,
                            importedPdf = result.data,
                            failedFileName = null,
                            pdfErrorMessage = null
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            pdfStatus = PdfImportStatus.ERROR,
                            importedPdf = null,
                            pdfErrorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun onContinueFromStep1() {
        val state = _uiState.value
        if (state.title.trim().isBlank()) {
            _uiState.update { it.copy(titleError = "Informe o título do documento.") }
            return
        }
        if (state.pdfStatus != PdfImportStatus.READY || state.importedPdf == null) {
            _uiState.update { it.copy(pdfErrorMessage = "Selecione um arquivo PDF válido.") }
            return
        }
        _uiState.update { it.copy(currentStep = 2, generalError = null) }
    }

    fun onBackToStep1() {
        _uiState.update { it.copy(currentStep = 1, generalError = null) }
    }

    fun onOpenSignerSelectionSheet() {
        _uiState.update { state ->
            state.copy(
                isSignerSelectionSheetOpen = true,
                pickerSearchQuery = "",
                pickerSelectedIds = state.assignedSigners.map { it.user.id }.toSet()
            )
        }
    }

    fun onDismissSignerSelectionSheet() {
        _uiState.update {
            it.copy(
                isSignerSelectionSheetOpen = false,
                pickerSearchQuery = ""
            )
        }
    }

    fun onPickerSearchQueryChanged(query: String) {
        _uiState.update { it.copy(pickerSearchQuery = query) }
    }

    fun onTogglePickerSignerSelection(signerId: String) {
        _uiState.update { state ->
            val currentSet = state.pickerSelectedIds
            val updatedSet = if (signerId in currentSet) {
                currentSet - signerId
            } else {
                currentSet + signerId
            }
            state.copy(pickerSelectedIds = updatedSet)
        }
    }

    fun onConfirmPickerSelection() {
        _uiState.update { state ->
            val existingRolesById = state.assignedSigners.associate { it.user.id to it.roleClass }
            val newAssigned = state.availableSigners
                .filter { it.id in state.pickerSelectedIds }
                .map { user ->
                    AssignedSignerUiModel(
                        user = user,
                        roleClass = existingRolesById[user.id] ?: SignerRoleClass.CONTRATANTE
                    )
                }
            state.copy(
                assignedSigners = newAssigned,
                isSignerSelectionSheetOpen = false,
                pickerSearchQuery = "",
                generalError = null
            )
        }
    }

    fun onSignerRoleChanged(signerId: String, newRole: SignerRoleClass) {
        _uiState.update { state ->
            state.copy(
                assignedSigners = state.assignedSigners.map { item ->
                    if (item.user.id == signerId) item.copy(roleClass = newRole) else item
                }
            )
        }
    }

    fun onRemoveAssignedSigner(signerId: String) {
        _uiState.update { state ->
            state.copy(
                assignedSigners = state.assignedSigners.filterNot { it.user.id == signerId }
            )
        }
    }

    fun onSaveDraftAndContinue(ownerAdminId: String) {
        val state = _uiState.value
        if (state.isSavingDraft) return

        val imported = state.importedPdf
        if (imported == null) {
            _uiState.update {
                it.copy(
                    currentStep = 1,
                    pdfErrorMessage = "Selecione um arquivo PDF válido."
                )
            }
            return
        }

        if (state.assignedSigners.isEmpty()) {
            _uiState.update {
                it.copy(generalError = "Selecione ao menos 1 signatário para continuar.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingDraft = true, generalError = null) }

            val assignments = state.assignedSigners.map {
                SelectedSignerAssignment(
                    signerId = it.user.id,
                    roleClass = it.roleClass
                )
            }

            when (
                val result = prepareDraftDocumentUseCase(
                    existingDocumentId = state.documentId,
                    title = state.title,
                    internalFileName = imported.internalFileName,
                    ownerAdminId = ownerAdminId,
                    signerAssignments = assignments
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSavingDraft = false,
                            documentId = result.data.id,
                            completedDraftDocument = result.data
                        )
                    }
                }
                is AppResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isSavingDraft = false,
                            generalError = result.message
                        )
                    }
                }
            }
        }
    }

    fun onDraftSavedHandled() {
        _uiState.update { it.copy(completedDraftDocument = null) }
    }
}
