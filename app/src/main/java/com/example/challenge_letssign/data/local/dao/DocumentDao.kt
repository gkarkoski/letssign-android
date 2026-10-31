package com.example.challenge_letssign.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.challenge_letssign.data.local.entity.DocumentEntity
import com.example.challenge_letssign.data.local.entity.DocumentSignerCrossRef
import com.example.challenge_letssign.data.local.entity.DocumentSignerWithUserEntity
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Query("SELECT * FROM documents WHERE ownerAdminId = :adminId ORDER BY createdAt DESC")
    fun observeDocumentsByAdmin(adminId: String): Flow<List<DocumentEntity>>

    @Query(
        """
        SELECT d.* FROM documents d
        INNER JOIN document_signer_cross_ref ref ON d.id = ref.documentId
        WHERE ref.signerId = :signerId AND d.status != 'DRAFT'
        ORDER BY d.createdAt DESC
        """
    )
    fun observeDocumentsBySigner(signerId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    fun observeDocumentById(documentId: String): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :documentId LIMIT 1")
    suspend fun getDocumentById(documentId: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Query("UPDATE documents SET status = :status WHERE id = :documentId")
    suspend fun updateDocumentStatus(documentId: String, status: DocumentStatus)

    @Query("DELETE FROM documents WHERE id = :documentId")
    suspend fun deleteDocumentById(documentId: String)

    // CrossRef Operations (N:N)
    @Transaction
    @Query("SELECT * FROM document_signer_cross_ref WHERE documentId = :documentId")
    fun observeDocumentSignersWithUsers(documentId: String): Flow<List<DocumentSignerWithUserEntity>>

    @Query("SELECT * FROM document_signer_cross_ref WHERE documentId = :documentId")
    suspend fun getDocumentSigners(documentId: String): List<DocumentSignerCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDocumentSigner(crossRef: DocumentSignerCrossRef)

    @Query("DELETE FROM document_signer_cross_ref WHERE documentId = :documentId AND signerId = :signerId")
    suspend fun deleteDocumentSigner(documentId: String, signerId: String)

    @Query(
        """
        UPDATE document_signer_cross_ref
        SET status = :status, signedAt = :signedAt, signatureIp = :signatureIp
        WHERE documentId = :documentId AND signerId = :signerId
        """
    )
    suspend fun updateSignerSignatureStatus(
        documentId: String,
        signerId: String,
        status: SignatureStatus,
        signedAt: Long,
        signatureIp: String
    )

    @Query(
        """
        SELECT COUNT(*) FROM document_signer_cross_ref
        WHERE documentId = :documentId AND status = 'PENDING'
        """
    )
    suspend fun countPendingSignersForDocument(documentId: String): Int
    // Regra 3: Reatividade de Exclusão - Excluir em cascata os SignatureFieldEntity associados
    @Transaction
    suspend fun removeSignerWithCascadeFields(documentId: String, signerId: String) {
        deleteSignatureFieldsForSigner(documentId, signerId)
        deleteDocumentSigner(documentId, signerId)
    }

    @Query("DELETE FROM signature_fields WHERE documentId = :documentId AND signerId = :signerId")
    suspend fun deleteSignatureFieldsForSigner(documentId: String, signerId: String)

    // Regra 5: Conclusão Automática - Atualizar status para SIGNED e verificar se não há mais PENDING
    @Transaction
    suspend fun signAndAutoCompleteIfAllSigned(
        documentId: String,
        signerId: String,
        signedAt: Long,
        signatureIp: String
    ) {
        updateSignerSignatureStatus(
            documentId = documentId,
            signerId = signerId,
            status = SignatureStatus.SIGNED,
            signedAt = signedAt,
            signatureIp = signatureIp
        )
        val remainingPending = countPendingSignersForDocument(documentId)
        if (remainingPending == 0) {
            updateDocumentStatus(documentId, DocumentStatus.COMPLETED)
        }
    }
}
