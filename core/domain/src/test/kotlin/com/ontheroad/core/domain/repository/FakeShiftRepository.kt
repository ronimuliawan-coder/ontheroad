package com.ontheroad.core.domain.repository

import com.ontheroad.core.model.Expense
import com.ontheroad.core.model.Shift
import com.ontheroad.core.model.ShiftStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeShiftRepository : ShiftRepository {

    private val shiftsFlow = MutableStateFlow<Map<String, Shift>>(emptyMap())
    private val expensesFlow = MutableStateFlow<List<Expense>>(emptyList())

    override suspend fun insertShift(shift: Shift) {
        shiftsFlow.value = shiftsFlow.value + (shift.id to shift)
    }

    override suspend fun updateShift(shift: Shift) {
        shiftsFlow.value = shiftsFlow.value + (shift.id to shift)
    }

    override suspend fun getShiftById(id: String): Shift? {
        return shiftsFlow.value[id]
    }

    override fun getActiveShift(): Flow<Shift?> {
        return shiftsFlow.map { map ->
            map.values.firstOrNull { it.status == ShiftStatus.ACTIVE }
        }
    }

    override fun getAllShifts(): Flow<List<Shift>> {
        return shiftsFlow.map { it.values.toList() }
    }

    override suspend fun insertExpense(expense: Expense) {
        expensesFlow.value = expensesFlow.value + expense
    }

    override fun getExpensesForShift(shiftId: String): Flow<List<Expense>> {
        return expensesFlow.map { list ->
            list.filter { it.shiftId == shiftId }
        }
    }
}
