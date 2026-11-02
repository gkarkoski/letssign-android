package com.example.challenge_letssign.ui.admin.preparation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.challenge_letssign.domain.model.AdditionalAuthOption
import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.ImportedPdfFile
import com.example.challenge_letssign.domain.model.SignerAuthMethod
import com.example.challenge_letssign.domain.model.SignerDeliveryChannel
import com.example.challenge_letssign.domain.model.SignerRoleClass
import com.example.challenge_letssign.domain.model.User
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.usecase.document.ImportPdfUseCase
import com.example.challenge_letssign.domain.usecase.document.PrepareDraftDocumentUseCase
import com.example.challenge_letssign.domain.usecase.document.SelectedSignerAssignment
import com.example.challenge_letssign.domain.usecase.signer.ObserveSignersUseCase
import com.example.challenge_letssign.utils.AppResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val roleClass: SignerRoleClass = SignerRoleClass.CONTRATANTE,
    val deliveryChannel: SignerDeliveryChannel = SignerDeliveryChannel.EMAIL,
    val authMethod: SignerAuthMethod = SignerAuthMethod.EMAIL,
    val additionalAuths: List<AdditionalAuthOption> = emptyList(),
    val language: String = "Português"
)

data class AiSuggestedSignerItem(
    val user: User,
    val suggestedRole: SignerRoleClass,
    val isSelected: Boolean = true
)

data class ContactGroupSummary(
    val groupName: String,
    val members: List<User>,
    val isFullySelected: Boolean
)

sealed interface Step3ModalSheet {
    data class DeliveryLinkSheet(val signerId: String) : Step3ModalSheet
    data class AuthMethodSheet(val signerId: String) : Step3ModalSheet
    data class AdditionalAuthsSheet(
        val signerId: String,
        val pendingSelections: Set<AdditionalAuthOption>
    ) : Step3ModalSheet
}

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
    // Seleção e Substituição de Signatários (Etapa 2)
    val isSignerSelectionSheetOpen: Boolean = false,
    val replacingSignerId: String? = null,
    val pickerSearchQuery: String = "",
    val pickerSelectedIds: Set<String> = emptySet(),
    // Detecção de Signatários com IA (Etapa 2)
    val isAiDetectingSigners: Boolean = false,
    val isReviewingAiSigners: Boolean = false,
    val aiSuggestedSigners: List<AiSuggestedSignerItem> = emptyList(),
    // Envio e Autenticação (Etapa 3)
    val expandedStep3SignerId: String? = null,
    val activeStep3Sheet: Step3ModalSheet? = null,
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
                it.name.lowercase().contains(q) ||
                    it.email.lowercase().contains(q) ||
                    it.groupName.lowercase().contains(q)
            }
        }

    /**
     * Quando o usuário digita o nome de um grupo na busca (ex.: "Jurídico"),
     * retorna o cabeçalho de grupo como em `Novo Doc Selecionar Signatários Busca.png`.
     */
    val matchingPickerGroups: List<ContactGroupSummary>
        get() {
            val q = pickerSearchQuery.trim().lowercase()
            if (q.isEmpty()) return emptyList()
            return availableSigners
                .filter { it.groupName.isNotBlank() && it.groupName.lowercase().contains(q) }
                .groupBy { it.groupName }
                .map { (groupName, members) ->
                    ContactGroupSummary(
                        groupName = groupName,
                        members = members,
                        isFullySelected = members.isNotEmpty() && members.all { it.id in pickerSelectedIds }
                    )
                }
        }

    val selectedAiSignersCount: Int
        get() = aiSuggestedSigners.count { it.isSelected }
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

    var pdfProcessingDelayMillis: Long = 1500L
    var aiDetectionDelayMillis: Long = 2500L

    private var loadedDraftId: String? = null
    private var pdfImportJob: Job? = null
    private var aiDetectionJob: Job? = null

    init {
        viewModelScope.launch {
            observeSignersUseCase().collect { signers ->
                _uiState.update { state ->
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
                    roleClass = it.documentSigner.roleClass,
                    deliveryChannel = it.documentSigner.deliveryChannel,
                    authMethod = it.documentSigner.authMethod,
                    additionalAuths = it.documentSigner.additionalAuths,
                    language = it.documentSigner.language
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
                        originalDisplayName = doc.effectiveDisplayFileName,
                        pageCount = doc.pageCount,
                        fileSizeBytes = doc.fileSizeBytes
                    ),
                    assignedSigners = assignedModels,
                    expandedStep3SignerId = assignedModels.firstOrNull()?.user?.id
                )
            }
        }
    }

    // --- Etapa 1: Título e arquivo ---

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
        pdfImportJob?.cancel()
        pdfImportJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    pdfStatus = PdfImportStatus.IMPORTING,
                    failedFileName = fallbackName ?: "documento.pdf",
                    pdfErrorMessage = null,
                    generalError = null
                )
            }

            if (pdfProcessingDelayMillis > 0L) {
                delay(pdfProcessingDelayMillis)
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

    fun onClearPdfFile() {
        pdfImportJob?.cancel()
        _uiState.update {
            it.copy(
                pdfStatus = PdfImportStatus.IDLE,
                importedPdf = null,
                failedFileName = null,
                pdfErrorMessage = null
            )
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
        _uiState.update {
            it.copy(
                currentStep = 2,
                isReviewingAiSigners = false,
                generalError = null
            )
        }
    }

    fun onBackToStep1() {
        aiDetectionJob?.cancel()
        _uiState.update {
            it.copy(
                currentStep = 1,
                isAiDetectingSigners = false,
                isReviewingAiSigners = false,
                generalError = null
            )
        }
    }

    // --- Etapa 2: Quem vai assinar? (Seleção manual, Busca por Grupo, Substituição e IA) ---

    fun onOpenSignerSelectionSheet() {
        _uiState.update { state ->
            state.copy(
                isSignerSelectionSheetOpen = true,
                replacingSignerId = null,
                pickerSearchQuery = "",
                pickerSelectedIds = state.assignedSigners.map { it.user.id }.toSet()
            )
        }
    }

    fun onRequestReplaceSigner(signerIdToReplace: String) {
        _uiState.update { state ->
            state.copy(
                isSignerSelectionSheetOpen = true,
                replacingSignerId = signerIdToReplace,
                pickerSearchQuery = "",
                pickerSelectedIds = setOf(signerIdToReplace)
            )
        }
    }

    fun onDismissSignerSelectionSheet() {
        _uiState.update {
            it.copy(
                isSignerSelectionSheetOpen = false,
                replacingSignerId = null,
                pickerSearchQuery = ""
            )
        }
    }

    fun onPickerSearchQueryChanged(query: String) {
        _uiState.update { it.copy(pickerSearchQuery = query) }
    }

    fun onTogglePickerSignerSelection(signerId: String) {
        _uiState.update { state ->
            if (state.replacingSignerId != null) {
                state.copy(pickerSelectedIds = setOf(signerId))
            } else {
                val currentSet = state.pickerSelectedIds
                val updatedSet = if (signerId in currentSet) {
                    currentSet - signerId
                } else {
                    currentSet + signerId
                }
                state.copy(pickerSelectedIds = updatedSet)
            }
        }
    }

    fun onTogglePickerGroupSelection(groupName: String) {
        _uiState.update { state ->
            val groupMemberIds = state.availableSigners
                .filter { it.groupName.equals(groupName, ignoreCase = true) }
                .map { it.id }
                .toSet()
            if (groupMemberIds.isEmpty()) return@update state

            val allAlreadySelected = groupMemberIds.all { it in state.pickerSelectedIds }
            val updatedSet = if (allAlreadySelected) {
                state.pickerSelectedIds - groupMemberIds
            } else {
                state.pickerSelectedIds + groupMemberIds
            }
            state.copy(pickerSelectedIds = updatedSet)
        }
    }

    fun onConfirmPickerSelection() {
        _uiState.update { state ->
            val replacingId = state.replacingSignerId
            if (replacingId != null) {
                val chosenId = state.pickerSelectedIds.firstOrNull() ?: return@update state.copy(
                    isSignerSelectionSheetOpen = false,
                    replacingSignerId = null
                )
                val newUser = state.availableSigners.firstOrNull { it.id == chosenId }
                    ?: return@update state
                val updatedList = state.assignedSigners
                    .map { item ->
                        if (item.user.id == replacingId) item.copy(user = newUser) else item
                    }
                    .distinctBy { it.user.id }
                return@update state.copy(
                    assignedSigners = updatedList,
                    isSignerSelectionSheetOpen = false,
                    replacingSignerId = null,
                    pickerSearchQuery = "",
                    expandedStep3SignerId = updatedList.firstOrNull()?.user?.id,
                    generalError = null
                )
            }

            val existingById = state.assignedSigners.associateBy { it.user.id }
            val defaultRoles = listOf(
                SignerRoleClass.CONTRATANTE,
                SignerRoleClass.CONTRATADO,
                SignerRoleClass.TESTEMUNHA
            )
            // Ordena colocando Marina, Rafael, Beatriz na ordem do Figma quando selecionados juntos
            val orderedSelection = state.availableSigners
                .filter { it.id in state.pickerSelectedIds }
                .sortedBy { preferredFigmaSignerOrder(it.name) }

            val newAssigned = orderedSelection.mapIndexed { idx, user ->
                existingById[user.id] ?: AssignedSignerUiModel(
                    user = user,
                    roleClass = defaultRoles[idx.coerceAtMost(defaultRoles.lastIndex)]
                )
            }
            state.copy(
                assignedSigners = newAssigned,
                isSignerSelectionSheetOpen = false,
                replacingSignerId = null,
                pickerSearchQuery = "",
                expandedStep3SignerId = newAssigned.firstOrNull()?.user?.id,
                generalError = null
            )
        }
    }

    // --- Sugerir com IA (Etapa 2: Novo Doc IA Detectando -> Signatários detectados pela IA) ---

    fun onSuggestSignersWithAiClick() {
        if (_uiState.value.isAiDetectingSigners) return
        aiDetectionJob?.cancel()
        aiDetectionJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAiDetectingSigners = true,
                    generalError = null
                )
            }

            if (aiDetectionDelayMillis > 0L) {
                delay(aiDetectionDelayMillis)
            }

            _uiState.update { state ->
                val sortedCandidates = state.availableSigners
                    .sortedBy { preferredFigmaSignerOrder(it.name) }
                    .take(3)
                val roles = listOf(
                    SignerRoleClass.CONTRATANTE,
                    SignerRoleClass.CONTRATADO,
                    SignerRoleClass.TESTEMUNHA
                )
                val suggestions = sortedCandidates.mapIndexed { index, user ->
                    AiSuggestedSignerItem(
                        user = user,
                        suggestedRole = roles[index.coerceAtMost(roles.lastIndex)],
                        isSelected = true
                    )
                }
                state.copy(
                    isAiDetectingSigners = false,
                    isReviewingAiSigners = true,
                    aiSuggestedSigners = suggestions
                )
            }
        }
    }

    fun onCancelAiSignerDetection() {
        aiDetectionJob?.cancel()
        _uiState.update {
            it.copy(
                isAiDetectingSigners = false,
                isReviewingAiSigners = false
            )
        }
    }

    fun onToggleAiSuggestedSigner(signerId: String) {
        _uiState.update { state ->
            state.copy(
                aiSuggestedSigners = state.aiSuggestedSigners.map { item ->
                    if (item.user.id == signerId) item.copy(isSelected = !item.isSelected) else item
                }
            )
        }
    }

    fun onConfirmAiSuggestedSigners() {
        _uiState.update { state ->
            val selectedItems = state.aiSuggestedSigners.filter { it.isSelected }
            val assigned = selectedItems.map { item ->
                AssignedSignerUiModel(
                    user = item.user,
                    roleClass = item.suggestedRole
                )
            }
            state.copy(
                assignedSigners = assigned,
                isReviewingAiSigners = false,
                expandedStep3SignerId = assigned.firstOrNull()?.user?.id,
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
            val updated = state.assignedSigners.filterNot { it.user.id == signerId }
            state.copy(
                assignedSigners = updated,
                expandedStep3SignerId = if (state.expandedStep3SignerId == signerId) {
                    updated.firstOrNull()?.user?.id
                } else {
                    state.expandedStep3SignerId
                }
            )
        }
    }

    fun onContinueFromStep2() {
        val state = _uiState.value
        if (state.assignedSigners.isEmpty()) {
            _uiState.update {
                it.copy(generalError = "Selecione ao menos 1 signatário para continuar.")
            }
            return
        }
        _uiState.update {
            it.copy(
                currentStep = 3,
                expandedStep3SignerId = it.expandedStep3SignerId ?: it.assignedSigners.firstOrNull()?.user?.id,
                generalError = null
            )
        }
    }

    fun onBackFromStep3() {
        _uiState.update {
            it.copy(
                currentStep = 2,
                activeStep3Sheet = null,
                generalError = null
            )
        }
    }

    // --- Etapa 3: Envio e Autenticação ---

    fun onToggleStep3SignerExpanded(signerId: String) {
        _uiState.update { state ->
            val nextId = if (state.expandedStep3SignerId == signerId) null else signerId
            state.copy(expandedStep3SignerId = nextId)
        }
    }

    fun onOpenDeliveryLinkSheet(signerId: String) {
        _uiState.update {
            it.copy(activeStep3Sheet = Step3ModalSheet.DeliveryLinkSheet(signerId))
        }
    }

    fun onSelectDeliveryChannel(signerId: String, channel: SignerDeliveryChannel) {
        _uiState.update { state ->
            state.copy(
                assignedSigners = state.assignedSigners.map { item ->
                    if (item.user.id == signerId) item.copy(deliveryChannel = channel) else item
                },
                activeStep3Sheet = null
            )
        }
    }

    fun onOpenAuthMethodSheet(signerId: String) {
        _uiState.update {
            it.copy(activeStep3Sheet = Step3ModalSheet.AuthMethodSheet(signerId))
        }
    }

    fun onSelectAuthMethod(signerId: String, method: SignerAuthMethod) {
        _uiState.update { state ->
            state.copy(
                assignedSigners = state.assignedSigners.map { item ->
                    if (item.user.id == signerId) item.copy(authMethod = method) else item
                },
                activeStep3Sheet = null
            )
        }
    }

    fun onOpenAdditionalAuthsSheet(signerId: String) {
        _uiState.update { state ->
            val currentOptions = state.assignedSigners
                .firstOrNull { it.user.id == signerId }
                ?.additionalAuths
                ?.toSet()
                .orEmpty()
            state.copy(
                activeStep3Sheet = Step3ModalSheet.AdditionalAuthsSheet(
                    signerId = signerId,
                    pendingSelections = currentOptions
                )
            )
        }
    }

    fun onTogglePendingAdditionalAuth(option: AdditionalAuthOption) {
        _uiState.update { state ->
            val sheet = state.activeStep3Sheet as? Step3ModalSheet.AdditionalAuthsSheet
                ?: return@update state
            val updated = if (option in sheet.pendingSelections) {
                sheet.pendingSelections - option
            } else {
                sheet.pendingSelections + option
            }
            state.copy(activeStep3Sheet = sheet.copy(pendingSelections = updated))
        }
    }

    fun onApplyAdditionalAuths() {
        _uiState.update { state ->
            val sheet = state.activeStep3Sheet as? Step3ModalSheet.AdditionalAuthsSheet
                ?: return@update state
            val orderedSelected = AdditionalAuthOption.entries.filter { it in sheet.pendingSelections }
            state.copy(
                assignedSigners = state.assignedSigners.map { item ->
                    if (item.user.id == sheet.signerId) {
                        item.copy(additionalAuths = orderedSelected)
                    } else {
                        item
                    }
                },
                activeStep3Sheet = null
            )
        }
    }

    fun onSignerLanguageChanged(signerId: String, language: String) {
        _uiState.update { state ->
            state.copy(
                assignedSigners = state.assignedSigners.map { item ->
                    if (item.user.id == signerId) item.copy(language = language) else item
                }
            )
        }
    }

    fun onDismissStep3Sheet() {
        _uiState.update { it.copy(activeStep3Sheet = null) }
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
                it.copy(
                    currentStep = 2,
                    generalError = "Selecione ao menos 1 signatário para continuar."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingDraft = true, generalError = null) }

            val assignments = state.assignedSigners.mapIndexed { idx, item ->
                SelectedSignerAssignment(
                    signerId = item.user.id,
                    roleClass = item.roleClass,
                    deliveryChannel = item.deliveryChannel,
                    authMethod = item.authMethod,
                    additionalAuths = item.additionalAuths,
                    language = item.language,
                    colorIndex = idx % 3
                )
            }

            when (
                val result = prepareDraftDocumentUseCase(
                    existingDocumentId = state.documentId,
                    title = state.title,
                    internalFileName = imported.internalFileName,
                    originalDisplayName = imported.originalDisplayName,
                    pageCount = imported.pageCount,
                    fileSizeBytes = imported.fileSizeBytes,
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

    override fun onCleared() {
        pdfImportJob?.cancel()
        aiDetectionJob?.cancel()
        super.onCleared()
    }

    private fun preferredFigmaSignerOrder(name: String): Int = when {
        name.startsWith("Marina", ignoreCase = true) -> 0
        name.startsWith("Rafael", ignoreCase = true) -> 1
        name.startsWith("Beatriz", ignoreCase = true) -> 2
        else -> 10
    }
}
