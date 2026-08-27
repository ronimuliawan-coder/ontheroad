package com.ontheroad.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ontheroad.core.data.local.dao.ShiftDao
import com.ontheroad.core.data.local.dao.TripDao
import com.ontheroad.core.data.local.entity.ExpenseEntity
import com.ontheroad.core.data.local.entity.RoutePointEntity
import com.ontheroad.core.data.local.entity.ShiftEntity
import com.ontheroad.core.data.local.entity.TripEntity

@Database(
    entities = [
        TripEntity::class,
        RoutePointEntity::class,
        ShiftEntity::class,
        ExpenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OnTheRoadDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao
    abstract fun shiftDao(): ShiftDao

    companion object {
        private const val DATABASE_NAME = "ontheroad.db"

        @Volatile
        private var instance: OnTheRoadDatabase? = null

        fun getInstance(context: Context): OnTheRoadDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    OnTheRoadDatabase::class.java,
                    DATABASE_NAME
                ).build().also { instance = it }
            }
        }
    }
}
