package com.example.soccertshirts_app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.soccertshirts_app.data.local.dao.JerseyDao
import com.example.soccertshirts_app.data.local.entity.JerseyEntity

@Database(entities = [JerseyEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun jerseyDao(): JerseyDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "soccer_tshirts_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}