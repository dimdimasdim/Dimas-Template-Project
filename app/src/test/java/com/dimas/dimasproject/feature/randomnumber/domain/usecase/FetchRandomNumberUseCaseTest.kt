package com.dimas.dimasproject.feature.randomnumber.domain.usecase

import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber
import com.dimas.dimasproject.feature.randomnumber.domain.repository.RandomNumberRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FetchRandomNumberUseCaseTest {

    private val repository: RandomNumberRepository = mockk()
    private lateinit var useCase: FetchRandomNumberUseCase

    @Before
    fun setUp() {
        useCase = FetchRandomNumberUseCase(repository)
    }

    @Test
    fun `invoke returns success result from repository`() = runTest {
        val expected = RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6)
        coEvery { repository.fetchAndSave() } returns Result.success(expected)

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `invoke returns failure result from repository`() = runTest {
        val error = RuntimeException("Network error")
        coEvery { repository.fetchAndSave() } returns Result.failure(error)

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }

    @Test
    fun `invoke delegates to repository fetchAndSave`() = runTest {
        coEvery { repository.fetchAndSave() } returns Result.success(
            RandomNumber(number = 1, rangeFrom = 1, rangeTo = 6)
        )

        useCase()

        coVerify(exactly = 1) { repository.fetchAndSave() }
    }
}

