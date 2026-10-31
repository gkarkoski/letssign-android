package com.example.challenge_letssign.data.di

import com.example.challenge_letssign.data.repository.local.LocalDocumentRepository
import com.example.challenge_letssign.data.repository.local.LocalUserRepository
import com.example.challenge_letssign.domain.repository.DocumentRepository
import com.example.challenge_letssign.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        localUserRepository: LocalUserRepository
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        localDocumentRepository: LocalDocumentRepository
    ): DocumentRepository
}
