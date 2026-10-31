package com.example.challenge_letssign.domain.repository

import com.example.challenge_letssign.domain.model.Document
import com.example.challenge_letssign.domain.model.DocumentSigner
import com.example.challenge_letssign.domain.model.DocumentSignerWithUser
import com.example.challenge_letssign.domain.model.DocumentStatus
import com.example.challenge_letssign.domain.model.SignatureField
import com.example.challenge_letssign.utils.AppResult
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeDocumentsByAdmin(adminId: String): Flow<List<Document>>
    fun observeDocumentsBySigner(signerId: String): Flow<List<Document>>
    fun observeDocumentById(documentId: String): Flow<Document?>
    suspend fun getDocumentById(documentId: String): AppResult<Document>
    suspend fun createDocument(document: Document): AppResult<Document>
    suspend fun updateDocumentStatus(documentId: String, status: DocumentStatus): AppResult<Unit>
    suspend fun deleteDocument(documentId: String): AppResult<Unit>

    // Document Signers (N:N Association)
    fun observeDocumentSigners(documentId: String): Flow<List<DocumentSignerWithUser>>
    suspend fun getDocumentSigners(documentId: String): AppResult<List<DocumentSigner>>
    suspend fun assignSignerToDocument(documentSigner: DocumentSigner): AppResult<Unit>
    suspend fun removeSignerFromDocument(documentId: String, signerId: String): AppResult<Unit>
    suspend fun markDocumentAsSignedBySigner(
        documentId: String,
        signerId: String,
        signedAt: Long,
        signatureIp: String
    ): AppResult<Unit>

    // Signature Fields
    fun observeSignatureFields(documentId: String): Flow<List<SignatureField>>
    suspend fun getSignatureFields(documentId: String): AppResult<List<SignatureField>>
    suspend fun getSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<List<SignatureField>>
    suspend fun saveSignatureField(field: SignatureField): AppResult<SignatureField>
    suspend fun deleteSignatureField(fieldId: String): AppResult<Unit>
    suspend fun deleteSignatureFieldsBySigner(documentId: String, signerId: String): AppResult<Unit>
}
