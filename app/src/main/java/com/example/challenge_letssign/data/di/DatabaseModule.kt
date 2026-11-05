package com.example.challenge_letssign.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.challenge_letssign.data.local.LetsSignDatabase
import com.example.challenge_letssign.data.local.dao.DocumentDao
import com.example.challenge_letssign.data.local.dao.SignatureFieldDao
import com.example.challenge_letssign.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.challenge_letssign.data.local.dao.ContactGroupDao

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLetsSignDatabase(
        @ApplicationContext context: Context
    ): LetsSignDatabase {
        val migration4To5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE documents ADD COLUMN deletedByUserId TEXT")
                database.execSQL("ALTER TABLE documents ADD COLUMN deletedByUserName TEXT")
                database.execSQL("ALTER TABLE documents ADD COLUMN deletedAt INTEGER")
                database.execSQL("ALTER TABLE documents ADD COLUMN hiddenByUserIds TEXT NOT NULL DEFAULT ''")
            }
        }
        return Room.databaseBuilder(
            context,
            LetsSignDatabase::class.java,
            LetsSignDatabase.DATABASE_NAME
        )
            .addMigrations(migration4To5)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: LetsSignDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideDocumentDao(database: LetsSignDatabase): DocumentDao = database.documentDao()

    @Provides
    @Singleton
    fun provideSignatureFieldDao(database: LetsSignDatabase): SignatureFieldDao =
        database.signatureFieldDao()

    @Provides
    @Singleton
    fun provideContactGroupDao(database: LetsSignDatabase): ContactGroupDao =
        database.contactGroupDao()
}
