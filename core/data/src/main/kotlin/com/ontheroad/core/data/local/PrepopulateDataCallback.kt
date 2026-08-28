package com.ontheroad.core.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Pre-populates the Room database with standard gig platforms and trip categories.
 */
class PrepopulateDataCallback : RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            db.beginTransaction()
            try {
                // Pre-seed platforms
                db.execSQL("INSERT OR IGNORE INTO platforms (id, name, colorHex, isCustom) VALUES ('grab', 'Grab', '#00B14F', 0)")
                db.execSQL("INSERT OR IGNORE INTO platforms (id, name, colorHex, isCustom) VALUES ('gojek', 'Gojek', '#00AA13', 0)")
                db.execSQL("INSERT OR IGNORE INTO platforms (id, name, colorHex, isCustom) VALUES ('uber', 'Uber', '#000000', 0)")
                db.execSQL("INSERT OR IGNORE INTO platforms (id, name, colorHex, isCustom) VALUES ('lyft', 'Lyft', '#FF00BF', 0)")
                db.execSQL("INSERT OR IGNORE INTO platforms (id, name, colorHex, isCustom) VALUES ('shopeefood', 'ShopeeFood', '#EE4D2D', 0)")
                db.execSQL("INSERT OR IGNORE INTO platforms (id, name, colorHex, isCustom) VALUES ('maxim', 'Maxim', '#FFDE00', 0)")
                db.execSQL("INSERT OR IGNORE INTO platforms (id, name, colorHex, isCustom) VALUES ('indrive', 'inDrive', '#91D347', 0)")
                db.execSQL("INSERT OR IGNORE INTO platforms (id, name, colorHex, isCustom) VALUES ('lalamove', 'Lalamove', '#F68B1F', 0)")
                db.execSQL("INSERT OR IGNORE INTO platforms (id, name, colorHex, isCustom) VALUES ('direct', 'Direct Client', '#3B82F6', 0)")

                // Pre-seed categories
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName) VALUES ('passenger', 'Passenger Rideshare', 'directions_car')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName) VALUES ('food', 'Food Delivery', 'restaurant')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName) VALUES ('package', 'Package Courier', 'inventory_2')")
                db.execSQL("INSERT OR IGNORE INTO categories (id, name, iconName) VALUES ('errand', 'Errands / Other', 'task_alt')")

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
        }
    }
}
