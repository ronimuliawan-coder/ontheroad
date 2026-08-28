package com.ontheroad.viewmodel

import com.ontheroad.core.domain.repository.ShiftRepository
import com.ontheroad.core.domain.repository.TripRepository
import com.ontheroad.core.domain.usecase.GetShiftSummaryUseCase
import com.ontheroad.core.model.Expense
import com.ontheroad.core.model.ExpenseCategory
import com.ontheroad.core.model.RoutePoint
import com.ontheroad.core.model.Shift
import com.ontheroad.core.model.ShiftStatus
import com.ontheroad.core.model.Trip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

private class TestFakeShiftRepository : ShiftRepository {
    val shiftsFlow = MutableStateFlow<Map<String, Shift>>(emptyMap())
    val expensesFlow = MutableStateFlow<List<Expense>>(emptyList())

    override suspend fun insertShift(shift: Shift) {
        shiftsFlow.value = shiftsFlow.value + (shift.id to shift)
    }

    override suspend fun updateShift(shift: Shift) {
        shiftsFlow.value = shiftsFlow.value + (shift.id to shift)
    }

    override suspend fun getShiftById(id: String): Shift? = shiftsFlow.value[id]

    override fun getActiveShift(): Flow<Shift?> = shiftsFlow.map { map ->
        map.values.firstOrNull { it.status == ShiftStatus.ACTIVE }
    }

    override fun getAllShifts(): Flow<List<Shift>> = shiftsFlow.map { it.values.toList() }

    override suspend fun insertExpense(expense: Expense) {
        expensesFlow.value = expensesFlow.value + expense
    }

    override fun getExpensesForShift(shiftId: String): Flow<List<Expense>> = expensesFlow.map { list ->
        list.filter { it.shiftId == shiftId }
    }
}

private class TestStubTripRepository : TripRepository {
    override suspend fun insertTrip(trip: Trip) {}
    override suspend fun updateTrip(trip: Trip) {}
    override suspend fun getTripById(id: String): Trip? = null
    override fun getActiveTrip(): Flow<Trip?> = MutableStateFlow(null)
    override fun getAllTrips(): Flow<List<Trip>> = MutableStateFlow(emptyList())
    override fun getTripsByShiftId(shiftId: String): Flow<List<Trip>> = MutableStateFlow(emptyList())
    override suspend fun insertRoutePoint(routePoint: RoutePoint) {}
    override fun getRoutePointsForTrip(tripId: String): Flow<List<RoutePoint>> = MutableStateFlow(emptyList())
    override suspend fun deleteTrip(id: String) {}
}

@OptIn(ExperimentalCoroutinesApi::class)
class ShiftViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var shiftRepository: TestFakeShiftRepository
    private lateinit var tripRepository: TestStubTripRepository
    private lateinit var getShiftSummaryUseCase: GetShiftSummaryUseCase
    private lateinit var viewModel: ShiftViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        shiftRepository = TestFakeShiftRepository()
        tripRepository = TestStubTripRepository()
        getShiftSummaryUseCase = GetShiftSummaryUseCase(tripRepository, shiftRepository)
        viewModel = ShiftViewModel(shiftRepository, getShiftSummaryUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun startsShiftAndUpdatesUiState() = runTest(testDispatcher) {
        viewModel.startShift(dailyTargetCents = 10000)
        testDispatcher.scheduler.runCurrent()

        val activeShift = viewModel.uiState.value.activeShift
        assertNotNull(activeShift)
        assertEquals(ShiftStatus.ACTIVE, activeShift?.status)
        assertEquals(10000L, activeShift?.dailyTargetCents)
    }

    @Test
    fun logsExpenseForActiveShift() = runTest(testDispatcher) {
        viewModel.startShift()
        testDispatcher.scheduler.runCurrent()

        viewModel.logExpense(
            category = ExpenseCategory.FUEL,
            amountCents = 2500,
            notes = "Gasoline"
        )
        testDispatcher.scheduler.runCurrent()

        val expenses = viewModel.uiState.value.expenses
        assertEquals(1, expenses.size)
        assertEquals(ExpenseCategory.FUEL, expenses[0].category)
        assertEquals(2500L, expenses[0].amountCents)
    }

    @Test
    fun endsShiftAndClearsActiveShift() = runTest(testDispatcher) {
        viewModel.startShift()
        testDispatcher.scheduler.runCurrent()

        viewModel.endShift()
        testDispatcher.scheduler.runCurrent()

        assertNull(viewModel.uiState.value.activeShift)
    }
}
