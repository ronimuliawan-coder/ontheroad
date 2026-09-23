package com.ontheroad.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ontheroad.core.data.local.dao.CategoryDao
import com.ontheroad.core.data.local.dao.PlatformDao
import com.ontheroad.core.data.local.dao.ShiftDao
import com.ontheroad.core.data.local.dao.TripDao
import com.ontheroad.core.data.local.entity.CategoryEntity
import com.ontheroad.core.data.local.entity.ExpenseEntity
import com.ontheroad.core.data.local.entity.PlatformEntity
import com.ontheroad.core.data.local.entity.RoutePointEntity
import com.ontheroad.core.data.local.entity.ShiftEntity
import com.ontheroad.core.data.local.entity.TripEntity

@Database(
    entities = [
        TripEntity::class,
        RoutePointEntity::class,
        ShiftEntity::class,
        ExpenseEntity::class,
        PlatformEntity::class,
        CategoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OnTheRoadDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao
    abstract fun shiftDao(): ShiftDao
    abstract fun platformDao(): PlatformDao
    abstract fun categoryDao(): CategoryDao

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
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(PrepopulateDataCallback())
                    .build()
                    .also { instance = it }
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trips ADD COLUMN quotedFareAmountCents INTEGER")
            }
        }
    }
}
