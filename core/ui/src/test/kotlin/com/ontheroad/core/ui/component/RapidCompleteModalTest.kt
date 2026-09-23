package com.ontheroad.core.ui.component

import org.junit.Assert.assertEquals
import org.junit.Test

class RapidCompleteModalTest {

    @Test
    fun `formats and parses currency values using major units at the UI boundary`() {
        assertEquals("80000", centsToInputText(8_000_000L))
        assertEquals(8_000_000L, parseInputAmountToCents("80000"))
        assertEquals(8_000_000L, parseInputAmountToCents("80000.00"))
    }

    @Test
    fun `invalid or non-positive currency input becomes zero`() {
        assertEquals("", centsToInputText(0L))
        assertEquals(0L, parseInputAmountToCents(""))
        assertEquals(0L, parseInputAmountToCents("-10"))
        assertEquals(0L, parseInputAmountToCents("not-a-number"))
    }

    @Test
    fun `formats and parses quoted distance in kilometers`() {
        assertEquals("20", metersToInputText(20_000.0))
        assertEquals("", metersToInputText(null))
        assertEquals("", metersToInputText(0.0))
    }
}
