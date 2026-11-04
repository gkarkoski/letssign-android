package com.example.challenge_letssign.data.di

import com.example.challenge_letssign.data.repository.local.LocalContactGroupRepository
import com.example.challenge_letssign.domain.repository.ContactGroupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ContactGroupModule {

    @Binds
    @Singleton
    abstract fun bindContactGroupRepository(
        implementation: LocalContactGroupRepository
    ): ContactGroupRepository
}