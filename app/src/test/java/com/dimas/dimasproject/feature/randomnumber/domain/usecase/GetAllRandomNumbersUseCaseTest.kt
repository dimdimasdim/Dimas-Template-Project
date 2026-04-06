package com.dimas.dimasproject.feature.randomnumber.domain.usecase

import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber
import com.dimas.dimasproject.feature.randomnumber.domain.repository.RandomNumberRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class GetAllRandomNumbersUseCaseTest {

    private val repository: RandomNumberRepository = mockk()
    private lateinit var useCase: GetAllRandomNumbersUseCase

    @Before
    fun setUp() {
        useCase = GetAllRandomNumbersUseCase(repository)
    }

    @Test
    fun `invoke delegates to repository getAll`() = runTest {
        every { repository.getAll() } returns flowOf(emptyList())

        useCase()

        verify(exactly = 1) { repository.getAll() }
    }

    @Test
    fun `invoke returns the flow emitted by repository`() = runTest {
        val numbers = listOf(
            RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6),
            RandomNumber(number = 5, rangeFrom = 1, rangeTo = 10)
        )
        every { repository.getAll() } returns flowOf(numbers)

        val result = useCase().first()

        assertEquals(numbers, result)
    }

    @Test
    fun `invoke returns empty flow when repository has no data`() = runTest {
        every { repository.getAll() } returns flowOf(emptyList())

        val result = useCase().first()

        assertEquals(emptyList(), result)
    }
}

