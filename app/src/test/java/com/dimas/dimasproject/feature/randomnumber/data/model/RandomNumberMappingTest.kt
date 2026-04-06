package com.dimas.dimasproject.feature.randomnumber.data.model

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RandomNumberMappingTest {

    // ── RandomNumberEntity.toDomain() ────────────────────────────────────────────

    @Test
    fun `RandomNumberEntity toDomain maps all fields correctly`() {
        val entity = RandomNumberEntity(
            id = 1,
            number = 5,
            rangeFrom = 1,
            rangeTo = 10
        )

        val domain = entity.toDomain()

        assertEquals(5, domain.number)
        assertEquals(1, domain.rangeFrom)
        assertEquals(10, domain.rangeTo)
    }

    @Test
    fun `RandomNumberEntity toDomain does not include id in domain model`() {
        val entity = RandomNumberEntity(id = 99, number = 3, rangeFrom = 1, rangeTo = 6)

        val domain = entity.toDomain()

        assertEquals(RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6), domain)
    }

    // ── RandomNumber.toEntity() ──────────────────────────────────────────────────

    @Test
    fun `RandomNumber toEntity maps all fields correctly`() {
        val domain = RandomNumber(number = 4, rangeFrom = 1, rangeTo = 6)

        val entity = domain.toEntity()

        assertEquals(4, entity.number)
        assertEquals(1, entity.rangeFrom)
        assertEquals(6, entity.rangeTo)
    }

    @Test
    fun `RandomNumber toEntity uses default id of 0 for autoGenerate`() {
        val domain = RandomNumber(number = 4, rangeFrom = 1, rangeTo = 6)

        val entity = domain.toEntity()

        assertEquals(0, entity.id)
    }

    @Test
    fun `toDomain and toEntity are inverse operations`() {
        val original = RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6)

        val roundTripped = original.toEntity().toDomain()

        assertEquals(original, roundTripped)
    }

    // ── RandomNumberResponse.toDomain() ─────────────────────────────────────────

    @Test
    fun `RandomNumberResponse toDomain maps all fields correctly`() {
        val response = RandomNumberResponse(
            randomNumber = 3,
            range = RangeResponse(from = 1, to = 6)
        )

        val domain = response.toDomain()

        assertEquals(3, domain.number)
        assertEquals(1, domain.rangeFrom)
        assertEquals(6, domain.rangeTo)
    }

    @Test
    fun `RandomNumberResponse toDomain uses 0 as default when randomNumber is null`() {
        val response = RandomNumberResponse(randomNumber = null, range = RangeResponse(1, 6))

        val domain = response.toDomain()

        assertEquals(0, domain.number)
    }

    @Test
    fun `RandomNumberResponse toDomain uses 0 as default when range is null`() {
        val response = RandomNumberResponse(randomNumber = 5, range = null)

        val domain = response.toDomain()

        assertEquals(0, domain.rangeFrom)
        assertEquals(0, domain.rangeTo)
    }

    @Test
    fun `RandomNumberResponse toDomain uses 0 for null range fields`() {
        val response = RandomNumberResponse(
            randomNumber = 5,
            range = RangeResponse(from = null, to = null)
        )

        val domain = response.toDomain()

        assertEquals(0, domain.rangeFrom)
        assertEquals(0, domain.rangeTo)
    }
}

