package com.ontheroad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ontheroad.core.domain.repository.ShiftRepository
import com.ontheroad.core.domain.usecase.GetShiftSummaryUseCase
import com.ontheroad.core.model.Expense
import com.ontheroad.core.model.ExpenseCategory
import com.ontheroad.core.model.Shift
import com.ontheroad.core.model.ShiftStatus
import com.ontheroad.core.model.ShiftSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ShiftUiState(
    val activeShift: Shift? = null,
    val summary: ShiftSummary? = null,
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ShiftViewModel(
    private val shiftRepository: ShiftRepository,
    private val getShiftSummaryUseCase: GetShiftSummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShiftUiState())
    val uiState: StateFlow<ShiftUiState> = _uiState.asStateFlow()

    init {
        observeActiveShift()
    }

    private fun observeActiveShift() {
        shiftRepository.getActiveShift()
            .onEach { shift ->
                _uiState.update { it.copy(activeShift = shift) }
                if (shift != null) {
                    refreshShiftSummary(shift.id)
                    observeExpenses(shift.id)
                } else {
                    _uiState.update { it.copy(summary = null, expenses = emptyList()) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeExpenses(shiftId: String) {
        shiftRepository.getExpensesForShift(shiftId)
            .onEach { expenses ->
                _uiState.update { it.copy(expenses = expenses) }
                refreshShiftSummary(shiftId)
            }
            .launchIn(viewModelScope)
    }

    fun refreshShiftSummary(shiftId: String) {
        viewModelScope.launch {
            val summary = getShiftSummaryUseCase(shiftId)
            _uiState.update { it.copy(summary = summary) }
        }
    }

    fun startShift(dailyTargetCents: Long = 0) {
        viewModelScope.launch {
            val shift = Shift(
                id = UUID.randomUUID().toString(),
                startTimeMillis = System.currentTimeMillis(),
                dailyTargetCents = dailyTargetCents,
                status = ShiftStatus.ACTIVE
            )
            shiftRepository.insertShift(shift)
        }
    }

    fun endShift() {
        val active = _uiState.value.activeShift ?: return
        viewModelScope.launch {
            val completed = active.copy(
                endTimeMillis = System.currentTimeMillis(),
                status = ShiftStatus.COMPLETED
            )
            shiftRepository.updateShift(completed)
        }
    }

    fun logExpense(category: ExpenseCategory, amountCents: Long, notes: String = "") {
        val shiftId = _uiState.value.activeShift?.id
        viewModelScope.launch {
            val expense = Expense(
                id = UUID.randomUUID().toString(),
                shiftId = shiftId,
                category = category,
                amountCents = amountCents,
                timestampMillis = System.currentTimeMillis(),
                notes = notes
            )
            shiftRepository.insertExpense(expense)
        }
    }
}
