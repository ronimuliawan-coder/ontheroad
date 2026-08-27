package com.ontheroad.core.data.repository

import com.ontheroad.core.data.local.dao.ShiftDao
import com.ontheroad.core.data.local.entity.ExpenseEntity
import com.ontheroad.core.data.local.entity.ShiftEntity
import com.ontheroad.core.domain.repository.ShiftRepository
import com.ontheroad.core.model.Expense
import com.ontheroad.core.model.Shift
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ShiftRepositoryImpl(
    private val shiftDao: ShiftDao
) : ShiftRepository {

    override suspend fun insertShift(shift: Shift) {
        shiftDao.insertShift(ShiftEntity.fromDomain(shift))
    }

    override suspend fun updateShift(shift: Shift) {
        shiftDao.updateShift(ShiftEntity.fromDomain(shift))
    }

    override suspend fun getShiftById(id: String): Shift? {
        return shiftDao.getShiftById(id)?.toDomain()
    }

    override fun getActiveShift(): Flow<Shift?> {
        return shiftDao.getActiveShift().map { it?.toDomain() }
    }

    override fun getAllShifts(): Flow<List<Shift>> {
        return shiftDao.getAllShifts().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertExpense(expense: Expense) {
        shiftDao.insertExpense(ExpenseEntity.fromDomain(expense))
    }

    override fun getExpensesForShift(shiftId: String): Flow<List<Expense>> {
        return shiftDao.getExpensesForShift(shiftId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
