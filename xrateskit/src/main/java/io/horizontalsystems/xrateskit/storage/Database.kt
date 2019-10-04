package io.horizontalsystems.xrateskit.storage

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

@androidx.room.Database(version = 1, exportSchema = false, entities = [
    Rate::class
])

abstract class Database : RoomDatabase() {
    abstract val rateDao: RateDao

    companion object {
        fun create(context: Context): Database {
            return Room.databaseBuilder(context, Database::class.java, "x-rate-database").build()
        }
    }
}
