package com.ontheroad.core.domain.usecase

import com.ontheroad.core.domain.repository.LocationRepository
import com.ontheroad.core.model.AddressSuggestion
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeLocationRepository : LocationRepository {
    var currentLocation: AddressSuggestion? = AddressSuggestion(
        title = "Monas",
        fullAddress = "Monas, Jakarta Pusat, DKI Jakarta",
        latitude = -6.175392,
        longitude = 106.827153
    )

    val sampleAddresses = listOf(
        AddressSuggestion(
            title = "Soekarno-Hatta International Airport",
            fullAddress = "Tangerang, Banten",
            latitude = -6.125556,
            longitude = 106.655833
        ),
        AddressSuggestion(
            title = "Grand Indonesia Mall",
            fullAddress = "Jl. M.H. Thamrin, Jakarta Pusat",
            latitude = -6.195000,
            longitude = 106.823056
        )
    )

    override suspend fun getCurrentLocation(): AddressSuggestion? = currentLocation

    override suspend fun searchAddresses(
        query: String,
        biasLatitude: Double?,
        biasLongitude: Double?,
        maxResults: Int
    ): List<AddressSuggestion> {
        return sampleAddresses.filter {
            it.title.contains(query, ignoreCase = true) || it.fullAddress.contains(query, ignoreCase = true)
        }.take(maxResults)
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): String? = "Monas, Jakarta"
}

class LocationUseCasesTest {

    private lateinit var fakeRepo: FakeLocationRepository
    private lateinit var searchAddressUseCase: SearchAddressUseCase
    private lateinit var getCurrentLocationUseCase: GetCurrentLocationUseCase

    @Before
    fun setUp() {
        fakeRepo = FakeLocationRepository()
        searchAddressUseCase = SearchAddressUseCase(fakeRepo)
        getCurrentLocationUseCase = GetCurrentLocationUseCase(fakeRepo)
    }

    @Test
    fun `searchAddressUseCase returns empty list for short queries`() = runTest {
        val resultBlank = searchAddressUseCase("")
        val resultSingleChar = searchAddressUseCase("a")

        assertTrue(resultBlank.isEmpty())
        assertTrue(resultSingleChar.isEmpty())
    }

    @Test
    fun `searchAddressUseCase finds matching address suggestions`() = runTest {
        val results = searchAddressUseCase("Airport")

        assertEquals(1, results.size)
        assertEquals("Soekarno-Hatta International Airport", results.first().title)
        assertEquals(-6.125556, results.first().latitude, 0.0001)
    }

    @Test
    fun `getCurrentLocationUseCase returns current location coordinates and address`() = runTest {
        val location = getCurrentLocationUseCase()

        assertNotNull(location)
        assertEquals("Monas", location?.title)
        assertEquals(-6.175392, location?.latitude ?: 0.0, 0.0001)
    }
}
