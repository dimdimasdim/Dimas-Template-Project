package com.dimas.dimasproject.feature.randomnumber.data.repository

import com.dimas.dimasproject.feature.randomnumber.data.local.RandomNumberLocalDataSource
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumberEntity
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumberResponse
import com.dimas.dimasproject.feature.randomnumber.data.model.RangeResponse
import com.dimas.dimasproject.feature.randomnumber.data.remote.RandomNumberRemoteDataSource
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RandomNumberRepositoryImplTest {

    private val remote: RandomNumberRemoteDataSource = mockk()
    private val local: RandomNumberLocalDataSource = mockk()
    private lateinit var repository: RandomNumberRepositoryImpl

    @Before
    fun setUp() {
        repository = RandomNumberRepositoryImpl(remote, local)
    }

    // ── fetchAndSave ─────────────────────────────────────────────────────────────

    @Test
    fun `fetchAndSave returns success with correct domain model`() = runTest {
        val response = RandomNumberResponse(
            randomNumber = 3,
            range = RangeResponse(from = 1, to = 6)
        )
        coEvery { remote.fetchRandomNumber() } returns response
        coJustRun { local.save(any()) }

        val result = repository.fetchAndSave()

        assertTrue(result.isSuccess)
        assertEquals(RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6), result.getOrNull())
    }

    @Test
    fun `fetchAndSave saves the correct entity to local storage`() = runTest {
        val response = RandomNumberResponse(
            randomNumber = 4,
            range = RangeResponse(from = 1, to = 10)
        )
        coEvery { remote.fetchRandomNumber() } returns response
        coJustRun { local.save(any()) }

        repository.fetchAndSave()

        coVerify(exactly = 1) {
            local.save(
                RandomNumberEntity(id = 0, number = 4, rangeFrom = 1, rangeTo = 10)
            )
        }
    }

    @Test
    fun `fetchAndSave returns failure when remote throws`() = runTest {
        val error = RuntimeException("Connection timeout")
        coEvery { remote.fetchRandomNumber() } throws error

        val result = repository.fetchAndSave()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `fetchAndSave does not save to local when remote throws`() = runTest {
        coEvery { remote.fetchRandomNumber() } throws RuntimeException("Error")

        repository.fetchAndSave()

        coVerify(exactly = 0) { local.save(any()) }
    }

    // ── getAll ────────────────────────────────────────────────────────────────────

    @Test
    fun `getAll maps entity list to domain model list`() = runTest {
        val entities = listOf(
            RandomNumberEntity(id = 1, number = 3, rangeFrom = 1, rangeTo = 6),
            RandomNumberEntity(id = 2, number = 5, rangeFrom = 1, rangeTo = 10)
        )
        every { local.getAll() } returns flowOf(entities)

        val result = repository.getAll().first()

        assertEquals(
            listOf(
                RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6),
                RandomNumber(number = 5, rangeFrom = 1, rangeTo = 10)
            ),
            result
        )
    }

    @Test
    fun `getAll returns empty list when local storage is empty`() = runTest {
        every { local.getAll() } returns flowOf(emptyList())

        val result = repository.getAll().first()

        assertEquals(emptyList(), result)
    }

    // ── getLatest ─────────────────────────────────────────────────────────────────

    @Test
    fun `getLatest returns domain model when entity exists`() = runTest {
        val entity = RandomNumberEntity(id = 1, number = 3, rangeFrom = 1, rangeTo = 6)
        coEvery { local.getLatest() } returns entity

        val result = repository.getLatest()

        assertEquals(RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6), result)
    }

    @Test
    fun `getLatest returns null when no entity in local storage`() = runTest {
        coEvery { local.getLatest() } returns null

        val result = repository.getLatest()

        assertNull(result)
    }
}
