package com.dimas.dimasproject.feature.randomnumber.presentation

import app.cash.turbine.test
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber
import com.dimas.dimasproject.feature.randomnumber.domain.usecase.FetchRandomNumberUseCase
import com.dimas.dimasproject.feature.randomnumber.domain.usecase.GetAllRandomNumbersUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RandomNumberViewModelTest {

    // Used for most tests — coroutines run eagerly so effects/state are available immediately
    private val testDispatcher = UnconfinedTestDispatcher()

    private val fetchRandomNumber: FetchRandomNumberUseCase = mockk()
    private val getAllRandomNumbers: GetAllRandomNumbersUseCase = mockk()

    private val historyFlow = MutableStateFlow<List<RandomNumber>>(emptyList())

    private lateinit var viewModel: RandomNumberViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getAllRandomNumbers() } returns historyFlow
        viewModel = RandomNumberViewModel(fetchRandomNumber, getAllRandomNumbers)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── Initial State ─────────────────────────────────────────────────────────────

    @Test
    fun `initial state has correct default values`() {
        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertNull(state.latest)
        assertTrue(state.history.isEmpty())
    }

    // ── History observation ───────────────────────────────────────────────────────

    @Test
    fun `when history emits a list, state history and latest are updated`() = runTest(testDispatcher) {
        val numbers = listOf(
            RandomNumber(number = 5, rangeFrom = 1, rangeTo = 10),
            RandomNumber(number = 2, rangeFrom = 1, rangeTo = 6)
        )

        viewModel.uiState.test {
            awaitItem() // initial state

            historyFlow.emit(numbers)

            val updated = awaitItem()
            assertEquals(numbers, updated.history)
            assertEquals(numbers.first(), updated.latest)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when history emits empty list, latest remains null and history is empty`() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // initial — StateFlow deduplicates equal values, no re-emission

            cancelAndIgnoreRemainingEvents()
        }

        assertNull(viewModel.uiState.value.latest)
        assertTrue(viewModel.uiState.value.history.isEmpty())
    }

    @Test
    fun `when history emits multiple times, each emission updates state`() = runTest(testDispatcher) {
        val first = listOf(RandomNumber(number = 1, rangeFrom = 1, rangeTo = 6))
        val second = listOf(
            RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6),
            RandomNumber(number = 1, rangeFrom = 1, rangeTo = 6)
        )

        viewModel.uiState.test {
            awaitItem() // initial

            historyFlow.emit(first)
            val stateAfterFirst = awaitItem()
            assertEquals(first, stateAfterFirst.history)
            assertEquals(first.first(), stateAfterFirst.latest)

            historyFlow.emit(second)
            val stateAfterSecond = awaitItem()
            assertEquals(second, stateAfterSecond.history)
            assertEquals(second.first(), stateAfterSecond.latest)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── FetchRandom — loading state ───────────────────────────────────────────────
    //
    // StateFlow uses DROP_OLDEST for its subscriber slots. When the ViewModel mock
    // returns immediately (no real suspension), both updateState calls (isLoading=true
    // then isLoading=false) happen before the Turbine collector polls — the slot is
    // overwritten and only the final value is delivered. This is correct StateFlow
    // behaviour, not a bug.
    //
    // To observe the intermediate isLoading=true we must introduce a real suspension
    // (delay) so the scheduler can run the collector between the two state changes.
    // That requires StandardTestDispatcher + advanceTimeBy.

    @Test
    fun `FetchRandom intent shows isLoading true during fetch, then false on success`() = runTest {
        // Use StandardTestDispatcher so we can pause execution mid-coroutine
        val stdDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(stdDispatcher)

        val localViewModel = RandomNumberViewModel(fetchRandomNumber, getAllRandomNumbers)
        advanceUntilIdle() // run init / observeHistory subscription

        val randomNumber = RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6)
        coEvery { fetchRandomNumber() } coAnswers {
            delay(500) // real suspension — gives collector time to see isLoading=true
            Result.success(randomNumber)
        }

        localViewModel.uiState.test {
            awaitItem() // initial

            launch { localViewModel.onIntent(RandomNumberUiEvent.FetchRandom) }
            advanceTimeBy(1) // run coroutine up to the delay() suspension point

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            advanceUntilIdle() // complete the delay and the rest of fetchRandom

            val doneState = awaitItem()
            assertFalse(doneState.isLoading)
            assertEquals(randomNumber, doneState.latest)

            cancelAndIgnoreRemainingEvents()
        }

        Dispatchers.setMain(testDispatcher) // restore for subsequent tests
    }

    // ── FetchRandom — final state (success) ──────────────────────────────────────
    //
    // Note: with UnconfinedTestDispatcher + StateFlow DROP_OLDEST, both isLoading=true
    // and isLoading=false updates are merged into a single emission of the final state.
    // These tests assert on that final, conflated state.

    @Test
    fun `FetchRandom intent on success final state has isLoading false and latest set`() =
        runTest(testDispatcher) {
            val randomNumber = RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6)
            coEvery { fetchRandomNumber() } returns Result.success(randomNumber)

            viewModel.uiState.test {
                awaitItem() // initial

                viewModel.onIntent(RandomNumberUiEvent.FetchRandom)

                val finalState = awaitItem() // conflated final state
                assertFalse(finalState.isLoading)
                assertEquals(randomNumber, finalState.latest)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `FetchRandom intent on success sends ShowSnackbar effect with fetched number`() =
        runTest(testDispatcher) {
            val randomNumber = RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6)
            coEvery { fetchRandomNumber() } returns Result.success(randomNumber)

            viewModel.effect.test {
                viewModel.onIntent(RandomNumberUiEvent.FetchRandom)

                val effect = awaitItem()
                assertTrue(effect is RandomNumberUiEffect.ShowSnackbar)
                assertEquals(
                    "Fetched number: 3 \uD83C\uDFB2",
                    (effect as RandomNumberUiEffect.ShowSnackbar).message
                )

                cancelAndIgnoreRemainingEvents()
            }
        }

    // ── FetchRandom — final state (failure) ──────────────────────────────────────

    @Test
    fun `FetchRandom intent on failure final state has isLoading false`() = runTest(testDispatcher) {
        // On failure: isLoading goes true→false. Since the final state equals the
        // initial state (isLoading=false, latest=null), StateFlow deduplicates and
        // emits nothing. Read the value directly — UnconfinedTestDispatcher guarantees
        // the coroutine has fully completed before onIntent() returns.
        coEvery { fetchRandomNumber() } returns Result.failure(RuntimeException("Oops"))

        viewModel.onIntent(RandomNumberUiEvent.FetchRandom)

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.latest)
    }

    @Test
    fun `FetchRandom intent on failure sends ShowSnackbar effect with error message`() =
        runTest(testDispatcher) {
            val error = RuntimeException("Network error")
            coEvery { fetchRandomNumber() } returns Result.failure(error)

            viewModel.effect.test {
                viewModel.onIntent(RandomNumberUiEvent.FetchRandom)

                val effect = awaitItem() as RandomNumberUiEffect.ShowSnackbar
                assertEquals("Network error", effect.message)

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `FetchRandom intent on failure with null message sends ShowSnackbar with default text`() =
        runTest(testDispatcher) {
            coEvery { fetchRandomNumber() } returns Result.failure(RuntimeException())

            viewModel.effect.test {
                viewModel.onIntent(RandomNumberUiEvent.FetchRandom)

                val effect = awaitItem() as RandomNumberUiEffect.ShowSnackbar
                assertEquals("Unknown error", effect.message)

                cancelAndIgnoreRemainingEvents()
            }
        }

    // ── FetchRandom — use case invocation ────────────────────────────────────────

    @Test
    fun `FetchRandom intent calls fetchRandomNumber use case exactly once`() =
        runTest(testDispatcher) {
            coEvery { fetchRandomNumber() } returns Result.success(
                RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6)
            )

            viewModel.onIntent(RandomNumberUiEvent.FetchRandom)

            coVerify(exactly = 1) { fetchRandomNumber() }
        }

    @Test
    fun `FetchRandom intent on success does not change history`() = runTest(testDispatcher) {
        val randomNumber = RandomNumber(number = 3, rangeFrom = 1, rangeTo = 6)
        coEvery { fetchRandomNumber() } returns Result.success(randomNumber)

        val historyBefore = viewModel.uiState.value.history

        viewModel.uiState.test {
            awaitItem() // initial
            viewModel.onIntent(RandomNumberUiEvent.FetchRandom)
            val finalState = awaitItem() // conflated final state
            assertEquals(historyBefore, finalState.history)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
