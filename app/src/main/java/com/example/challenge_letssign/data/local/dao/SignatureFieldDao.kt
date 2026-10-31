package com.example.challenge_letssign.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.challenge_letssign.data.local.entity.SignatureFieldEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SignatureFieldDao {

    @Query("SELECT * FROM signature_fields WHERE documentId = :documentId ORDER BY pageNumber ASC")
    fun observeSignatureFieldsByDocument(documentId: String): Flow<List<SignatureFieldEntity>>

    @Query("SELECT * FROM signature_fields WHERE documentId = :documentId ORDER BY pageNumber ASC")
    suspend fun getSignatureFieldsByDocument(documentId: String): List<SignatureFieldEntity>

    @Query(
        """
        SELECT * FROM signature_fields
        WHERE documentId = :documentId AND signerId = :signerId
        ORDER BY pageNumber ASC
        """
    )
    suspend fun getSignatureFieldsBySigner(documentId: String, signerId: String): List<SignatureFieldEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSignatureField(field: SignatureFieldEntity)

    @Query("DELETE FROM signature_fields WHERE id = :fieldId")
    suspend fun deleteSignatureFieldById(fieldId: String)

    @Query("DELETE FROM signature_fields WHERE documentId = :documentId AND signerId = :signerId")
    suspend fun deleteSignatureFieldsBySigner(documentId: String, signerId: String)
}
