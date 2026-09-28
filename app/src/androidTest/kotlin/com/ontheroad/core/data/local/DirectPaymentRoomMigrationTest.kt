package com.ontheroad.core.data.local

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ontheroad.core.data.local.entity.TripEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DirectPaymentRoomMigrationTest {

    @Test
    fun versionTwoCompletedDirectRowsReceiveTheirRecordedPaymentTotal() = runBlocking(Dispatchers.IO) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "direct-payment-migration-test.db"
        context.deleteDatabase(databaseName)

        val versionThree = Room.databaseBuilder(context, OnTheRoadDatabase::class.java, databaseName)
            .addMigrations(OnTheRoadDatabase.MIGRATION_1_2, OnTheRoadDatabase.MIGRATION_2_3)
            .build()
        try {
            val completedDirectTrip = TripEntity(
                id = "direct-completed",
                shiftId = null,
                platformId = "direct",
                categoryId = "passenger",
                startTimeMillis = 1_000L,
                endTimeMillis = 2_000L,
                startAddress = "Pickup",
                endAddress = "Drop-off",
                startLatitude = 1.0,
                startLongitude = 2.0,
                endLatitude = 3.0,
                endLongitude = 4.0,
                actualDistanceMeters = 4_000.0,
                quotedDistanceMeters = 3_500.0,
                quotedFareAmountCents = 15_000L,
                customerPaidTotalAmountCents = null,
                durationSeconds = 1L,
                platformFeeAmountCents = 7_000L,
                cashCollectedAmountCents = 2_000L,
                tipAmountCents = 500L,
                notes = "",
                status = "COMPLETED"
            )
            versionThree.tripDao().insertTrip(completedDirectTrip)
            versionThree.tripDao().insertTrip(
                completedDirectTrip.copy(id = "non-direct-completed", platformId = "grab")
            )
            versionThree.tripDao().insertTrip(
                completedDirectTrip.copy(
                    id = "direct-in-progress",
                    endTimeMillis = null,
                    status = "IN_PROGRESS"
                )
            )
        } finally {
            versionThree.close()
        }

        val databaseFile = context.getDatabasePath(databaseName)
        val sqlite = SQLiteDatabase.openDatabase(databaseFile.path, null, SQLiteDatabase.OPEN_READWRITE)
        try {
            sqlite.execSQL("ALTER TABLE trips DROP COLUMN customerPaidTotalAmountCents")
            sqlite.version = 2
        } finally {
            sqlite.close()
        }

        val migrated = Room.databaseBuilder(context, OnTheRoadDatabase::class.java, databaseName)
            .addMigrations(OnTheRoadDatabase.MIGRATION_1_2, OnTheRoadDatabase.MIGRATION_2_3)
            .build()
        try {
            val trip = migrated.tripDao().getTripById("direct-completed")
            assertEquals(9_500L, trip?.customerPaidTotalAmountCents)
            assertEquals(15_000L, trip?.quotedFareAmountCents)
            assertEquals(null, migrated.tripDao().getTripById("non-direct-completed")?.customerPaidTotalAmountCents)
            assertEquals(null, migrated.tripDao().getTripById("direct-in-progress")?.customerPaidTotalAmountCents)
        } finally {
            migrated.close()
            context.deleteDatabase(databaseName)
        }
    }
}
