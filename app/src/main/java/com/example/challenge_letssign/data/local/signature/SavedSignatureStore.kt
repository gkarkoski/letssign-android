package com.example.challenge_letssign.data.local.signature

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Conjunto salvo de assinatura + rubrica de um signatário.
 */
class SavedSignatureSet(
    val id: String,
    val signaturePng: ByteArray,
    val initialsPng: ByteArray,
    val savedAtMillis: Long
)

/**
 * Banco local de assinaturas salvas.
 *
 * Quando o signatário marca "Salvar assinatura e rubrica" no fim do cadastro, o par de imagens
 * (PNG) é gravado em `filesDir/saved_signatures/<signerId>/`. Nos próximos documentos, ele pode
 * escolher uma assinatura salva ou criar uma nova.
 */
@Singleton
class SavedSignatureStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    /** Lista as assinaturas salvas do signatário, da mais recente para a mais antiga. */
    fun list(signerId: String): List<SavedSignatureSet> = runCatching {
        val dir = signerDirectory(signerId)
        dir.listFiles { file -> file.name.endsWith(SIGNATURE_SUFFIX) }
            .orEmpty()
            .mapNotNull { sigFile ->
                val id = sigFile.name.removeSuffix(SIGNATURE_SUFFIX)
                val initialsFile = File(dir, id + INITIALS_SUFFIX)
                if (sigFile.length() <= 0L || !initialsFile.exists() || initialsFile.length() <= 0L) {
                    null
                } else {
                    SavedSignatureSet(
                        id = id,
                        signaturePng = sigFile.readBytes(),
                        initialsPng = initialsFile.readBytes(),
                        savedAtMillis = id.toLongOrNull() ?: sigFile.lastModified()
                    )
                }
            }
            .sortedByDescending { it.savedAtMillis }
    }.getOrDefault(emptyList())

    /** Salva um novo par assinatura + rubrica. Mantém no máximo [MAX_SAVED_SETS] por signatário. */
    fun save(signerId: String, signaturePng: ByteArray, initialsPng: ByteArray): SavedSignatureSet? {
        if (signaturePng.isEmpty() || initialsPng.isEmpty()) return null
        return runCatching {
            val dir = signerDirectory(signerId)
            val now = System.currentTimeMillis()
            val id = now.toString()
            File(dir, id + SIGNATURE_SUFFIX).writeBytes(signaturePng)
            File(dir, id + INITIALS_SUFFIX).writeBytes(initialsPng)
            list(signerId).drop(MAX_SAVED_SETS).forEach { old -> delete(signerId, old.id) }
            SavedSignatureSet(id, signaturePng, initialsPng, now)
        }.getOrNull()
    }

    fun delete(signerId: String, setId: String) {
        val dir = signerDirectory(signerId)
        runCatching {
            File(dir, setId + SIGNATURE_SUFFIX).delete()
            File(dir, setId + INITIALS_SUFFIX).delete()
        }
    }

    private fun signerDirectory(signerId: String): File {
        val safeId = signerId.replace(Regex("[^A-Za-z0-9_-]"), "_")
        return File(File(context.filesDir, DIRECTORY_NAME), safeId).apply { if (!exists()) mkdirs() }
    }

    companion object {
        const val MAX_SAVED_SETS = 5
        private const val DIRECTORY_NAME = "saved_signatures"
        private const val SIGNATURE_SUFFIX = "_signature.png"
        private const val INITIALS_SUFFIX = "_initials.png"
    }
}