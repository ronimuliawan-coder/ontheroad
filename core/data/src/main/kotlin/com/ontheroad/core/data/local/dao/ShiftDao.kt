package com.ontheroad.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ontheroad.core.data.local.entity.ExpenseEntity
import com.ontheroad.core.data.local.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity)

    @Update
    suspend fun updateShift(shift: ShiftEntity)

    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun getShiftById(id: String): ShiftEntity?

    @Query("SELECT * FROM shifts WHERE status = 'ACTIVE' ORDER BY startTimeMillis DESC LIMIT 1")
    fun getActiveShift(): Flow<ShiftEntity?>

    @Query("SELECT * FROM shifts ORDER BY startTimeMillis DESC")
    fun getAllShifts(): Flow<List<ShiftEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Query("SELECT * FROM expenses WHERE shiftId = :shiftId ORDER BY timestampMillis DESC")
    fun getExpensesForShift(shiftId: String): Flow<List<ExpenseEntity>>
}
