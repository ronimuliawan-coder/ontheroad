package com.ontheroad.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ontheroad.core.model.Expense
import com.ontheroad.core.model.ExpenseCategory
import com.ontheroad.core.model.Shift
import com.ontheroad.core.model.ShiftStatus

@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey val id: String,
    val startTimeMillis: Long,
    val endTimeMillis: Long?,
    val dailyTargetCents: Long,
    val status: String
) {
    fun toDomain(): Shift = Shift(
        id = id,
        startTimeMillis = startTimeMillis,
        endTimeMillis = endTimeMillis,
        dailyTargetCents = dailyTargetCents,
        status = runCatching { ShiftStatus.valueOf(status) }.getOrDefault(ShiftStatus.COMPLETED)
    )

    companion object {
        fun fromDomain(shift: Shift): ShiftEntity = ShiftEntity(
            id = shift.id,
            startTimeMillis = shift.startTimeMillis,
            endTimeMillis = shift.endTimeMillis,
            dailyTargetCents = shift.dailyTargetCents,
            status = shift.status.name
        )
    }
}

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val shiftId: String?,
    val category: String,
    val amountCents: Long,
    val timestampMillis: Long,
    val notes: String
) {
    fun toDomain(): Expense = Expense(
        id = id,
        shiftId = shiftId,
        category = runCatching { ExpenseCategory.valueOf(category) }.getOrDefault(ExpenseCategory.OTHER),
        amountCents = amountCents,
        timestampMillis = timestampMillis,
        notes = notes
    )

    companion object {
        fun fromDomain(expense: Expense): ExpenseEntity = ExpenseEntity(
            id = expense.id,
            shiftId = expense.shiftId,
            category = expense.category.name,
            amountCents = expense.amountCents,
            timestampMillis = expense.timestampMillis,
            notes = expense.notes
        )
    }
}
