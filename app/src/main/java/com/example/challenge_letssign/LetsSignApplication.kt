package com.example.challenge_letssign

import android.app.Application
import com.example.challenge_letssign.data.di.IoDispatcher
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
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    private val applicationScope by lazy {
        CoroutineScope(SupervisorJob() + ioDispatcher)
    }

    override fun onCreate() {
        super.onCreate()
        // Obrigatório inicializar o motor PDFBox na classe base Application
        PDFBoxResourceLoader.init(applicationContext)

        // Garante a inserção dos dados mockados (1 Admin, 2 Signatários) em Dispatchers.IO
        applicationScope.launch {
            DatabaseSeeder.seedIfNeeded(userDao)
        }
    }
}
