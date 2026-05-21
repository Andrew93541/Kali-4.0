package com.kali.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [AlertEntity::class, LocationEntity::class, MediaEntity::class], version = 1)
abstract class KaliDatabase : RoomDatabase() {
    abstract fun alertDao(): AlertDao
    abstract fun locationDao(): LocationDao
    abstract fun mediaDao(): MediaDao

    companion object {
        @Volatile private var INSTANCE: KaliDatabase? = null
        fun get(context: Context): KaliDatabase = INSTANCE ?: synchronized(this) {
            Room.databaseBuilder(context.applicationContext, KaliDatabase::class.java, "kali_db")
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
        }
    }
}
