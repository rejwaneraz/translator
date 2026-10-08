package com.rejwane.reelslocal.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

    @Test
    fun `formatCount keeps small numbers verbatim`() {
        assertEquals("0", formatCount(0))
        assertEquals("999", formatCount(999))
    }

    @Test
    fun `formatCount clamps negatives to zero`() {
        assertEquals("0", formatCount(-5))
    }

    @Test
    fun `formatCount compacts thousands with one decimal`() {
        assertEquals("1.2K", formatCount(1_234))
        assertEquals("12K", formatCount(12_000))
        assertEquals("125K", formatCount(125_000))
    }

    @Test
    fun `formatCount compacts millions`() {
        assertEquals("1.2M", formatCount(1_234_567))
        assertEquals("3M", formatCount(3_000_000))
    }

    @Test
    fun `formatDuration renders zero and sub-minute`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:00", formatDuration(-1))
        assertEquals("0:09", formatDuration(9_000))
    }

    @Test
    fun `formatDuration renders minutes and hours`() {
        assertEquals("1:05", formatDuration(65_000))
        assertEquals("1:02:03", formatDuration(3_723_000))
    }

    @Test
    fun `formatRelativeTime buckets by elapsed time`() {
        val now = 1_000_000_000_000L
        val min = 60_000L
        assertEquals("just now", formatRelativeTime(now, now))
        assertEquals("2m", formatRelativeTime(now - 2 * min, now))
        assertEquals("1h", formatRelativeTime(now - 60 * min, now))
        assertEquals("Yesterday", formatRelativeTime(now - 30 * 60 * min, now))
        assertEquals("3d", formatRelativeTime(now - 3 * 24 * 60 * min, now))
    }

    @Test
    fun `formatRelativeTime never reports future timestamps`() {
        val now = 1_000_000_000_000L
        assertEquals("just now", formatRelativeTime(now + 5_000, now))
    }
}
