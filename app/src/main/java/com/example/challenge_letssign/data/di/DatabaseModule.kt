package com.example.challenge_letssign.data.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.challenge_letssign.data.local.LetsSignDatabase
import com.example.challenge_letssign.data.local.dao.DocumentDao
import com.example.challenge_letssign.data.local.dao.SignatureFieldDao
import com.example.challenge_letssign.data.local.dao.UserDao
import com.example.challenge_letssign.data.local.seed.DatabaseSeeder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideLetsSignDatabase(
        @ApplicationContext context: Context
    ): LetsSignDatabase {
        return Room.databaseBuilder(
            context,
            LetsSignDatabase::class.java,
            LetsSignDatabase.DATABASE_NAME
        )
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    seedInitialUsers(db)
                }

                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    seedInitialUsers(db)
                }

                private fun seedInitialUsers(db: SupportSQLiteDatabase) {
                    DatabaseSeeder.initialUsers.forEach { user ->
                        db.execSQL(
                            """
                            INSERT OR IGNORE INTO users (id, name, email, phone, password, role)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """.trimIndent(),
                            arrayOf(
                                user.id,
                                user.name,
                                user.email,
                                user.phone,
                                user.password,
                                user.role.name
                            )
                        )
                    }
                }
            })
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
}
