package com.example.challenge_letssign

import android.app.Application
import com.example.challenge_letssign.data.di.IoDispatcher
import com.example.challenge_letssign.data.local.dao.DocumentDao
import com.example.challenge_letssign.data.local.dao.SignatureFieldDao
import com.example.challenge_letssign.data.local.dao.UserDao
import com.example.challenge_letssign.data.local.seed.DatabaseSeeder
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class LetsSignApplication : Application() {

    @Inject
    lateinit var userDao: UserDao

    @Inject
    lateinit var documentDao: DocumentDao

    @Inject
    lateinit var signatureFieldDao: SignatureFieldDao

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    private val applicationScope by lazy {
        CoroutineScope(SupervisorJob() + ioDispatcher)
    }

    override fun onCreate() {
        super.onCreate()
        // Obrigatório inicializar o motor PDFBox na classe base Application
        PDFBoxResourceLoader.init(applicationContext)

        // Garante a inserção dos dados mockados (Admin, 6 Signatários do Figma e Documentos exemplo) em Dispatchers.IO
        applicationScope.launch {
            DatabaseSeeder.seedIfNeeded(
                userDao = userDao,
                documentDao = documentDao,
                signatureFieldDao = signatureFieldDao,
                filesDir = applicationContext.filesDir
            )
        }
    }
}
