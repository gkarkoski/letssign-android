package com.example.challenge_letssign.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.challenge_letssign.data.local.converter.RoomConverters
import com.example.challenge_letssign.data.local.dao.DocumentDao
import com.example.challenge_letssign.data.local.dao.SignatureFieldDao
import com.example.challenge_letssign.data.local.dao.UserDao
import com.example.challenge_letssign.data.local.entity.DocumentEntity
import com.example.challenge_letssign.data.local.entity.DocumentSignerCrossRef
import com.example.challenge_letssign.data.local.entity.SignatureFieldEntity
import com.example.challenge_letssign.data.local.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DocumentEntity::class,
        DocumentSignerCrossRef::class,
        SignatureFieldEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class LetsSignDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun documentDao(): DocumentDao
    abstract fun signatureFieldDao(): SignatureFieldDao

    companion object {
        const val DATABASE_NAME = "letssign_database.db"
    }
}
