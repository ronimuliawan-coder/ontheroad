package com.ontheroad.core.domain.repository

import com.ontheroad.core.model.Expense
import com.ontheroad.core.model.Shift
import kotlinx.coroutines.flow.Flow

/**
 * Interface defining operations on Shifts and Expenses.
 * Pure Kotlin interface conforming to Clean Architecture and ARCH-001.
 */
interface ShiftRepository {
    suspend fun insertShift(shift: Shift)
    suspend fun updateShift(shift: Shift)
    suspend fun getShiftById(id: String): Shift?
    fun getActiveShift(): Flow<Shift?>
    fun getAllShifts(): Flow<List<Shift>>
    suspend fun insertExpense(expense: Expense)
    fun getExpensesForShift(shiftId: String): Flow<List<Expense>>
}
