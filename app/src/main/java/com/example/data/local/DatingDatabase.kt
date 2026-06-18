package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.DateProposal
import com.example.data.model.UserProfile

@Database(
    entities = [UserProfile::class, DateProposal::class, ChatMessage::class],
    version = 1,
    exportSchema = false
)
abstract class DatingDatabase : RoomDatabase() {
    abstract fun datingDao(): DatingDao

    companion object {
        @Volatile
        private var INSTANCE: DatingDatabase? = null

        fun getDatabase(context: Context): DatingDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DatingDatabase::class.java,
                    "dating_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
