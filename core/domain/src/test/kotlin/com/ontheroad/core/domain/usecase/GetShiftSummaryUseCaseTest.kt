package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.FakeShiftRepository
import com.ontheroad.core.domain.repository.FakeTripRepository
import com.ontheroad.core.model.Expense
import com.ontheroad.core.model.ExpenseCategory
import com.ontheroad.core.model.Trip
import com.ontheroad.core.model.TripStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetShiftSummaryUseCaseTest {

    private lateinit var tripRepository: FakeTripRepository
    private lateinit var shiftRepository: FakeShiftRepository
    private lateinit var getShiftSummaryUseCase: GetShiftSummaryUseCase

    @Before
    fun setUp() {
        tripRepository = FakeTripRepository()
        shiftRepository = FakeShiftRepository()
        getShiftSummaryUseCase = GetShiftSummaryUseCase(tripRepository, shiftRepository)
    }

    @Test
    fun computesShiftFinancialAndOdometerReconciliationAccurately() = runTest {
        val shiftId = "shift-today"

        // Trip 1: 5.0 km actual, 4.5 km quoted (+500m uncompensated). $15 platform, $5 cash, $2 tip = $22. 1800s.
        val trip1 = Trip(
            id = "trip-1",
            shiftId = shiftId,
            platformId = "grab",
            categoryId = "passenger",
            startTimeMillis = 1000,
            endTimeMillis = 1801000,
            startAddress = "A",
            endAddress = "B",
            startLatitude = 0.0,
            startLongitude = 0.0,
            actualDistanceMeters = 5000.0,
            quotedDistanceMeters = 4500.0,
            durationSeconds = 1800,
            platformFeeAmountCents = 1500,
            cashCollectedAmountCents = 500,
            tipAmountCents = 200,
            status = TripStatus.COMPLETED
        )

        // Trip 2: 3.0 km actual, 3.0 km quoted (0 discrepancy). $10 platform, $0 cash = $10. 1200s.
        val trip2 = Trip(
            id = "trip-2",
            shiftId = shiftId,
            platformId = "gojek",
            categoryId = "food",
            startTimeMillis = 2000000,
            endTimeMillis = 3200000,
            startAddress = "C",
            endAddress = "D",
            startLatitude = 0.0,
            startLongitude = 0.0,
            actualDistanceMeters = 3000.0,
            quotedDistanceMeters = 3000.0,
            durationSeconds = 1200,
            platformFeeAmountCents = 1000,
            cashCollectedAmountCents = 0,
            tipAmountCents = 0,
            status = TripStatus.COMPLETED
        )

        tripRepository.insertTrip(trip1)
        tripRepository.insertTrip(trip2)

        // Expense: $8 fuel (800 cents)
        val fuelExpense = Expense(
            id = "exp-1",
            shiftId = shiftId,
            category = ExpenseCategory.FUEL,
            amountCents = 800,
            timestampMillis = 4000000,
            notes = "Gasoline top-up"
        )
        shiftRepository.insertExpense(fuelExpense)

        val summary = getShiftSummaryUseCase(shiftId)

        assertEquals(2, summary.totalTrips)
        assertEquals(3200L, summary.totalGrossEarningsCents) // $32.00
        assertEquals(2500L, summary.digitalBalanceCents)     // $25.00
        assertEquals(700L, summary.cashInHandCents)          // $7.00 ($5 cash + $2 tip)
        assertEquals(800L, summary.totalExpensesCents)       // $8.00 fuel
        assertEquals(2400L, summary.netTakeHomeCents)        // $24.00 net
        assertEquals(8000.0, summary.totalActualDistanceMeters, 0.01) // 8.0 km
        assertEquals(8.0, summary.totalActualDistanceKm, 0.01)
        assertEquals(7500.0, summary.totalQuotedDistanceMeters, 0.01) // 7.5 km
        assertEquals(500.0, summary.totalUncompensatedMeters, 0.01)   // 500m uncompensated
        assertEquals(0.5, summary.totalUncompensatedKm, 0.01)
        assertEquals(3000L, summary.totalDurationSeconds)             // 50 mins

        // Efficiency: $32 / 8.0 km = $4.00/km (400 cents/km)
        assertEquals(400.0, summary.averageEarningsPerKmCents, 0.01)
    }
}
