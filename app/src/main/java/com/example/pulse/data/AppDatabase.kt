package com.example.pulse.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FavoriteTrackEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile //сообщение о создании БД
        private var INSTANCE: AppDatabase? = null

        // База создаётся один раз на всё приложение
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pulse.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}