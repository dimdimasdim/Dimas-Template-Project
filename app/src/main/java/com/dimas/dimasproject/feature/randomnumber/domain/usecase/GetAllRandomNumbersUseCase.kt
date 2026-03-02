package com.dimas.dimasproject.feature.randomnumber.domain.usecase

import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber
import com.dimas.dimasproject.feature.randomnumber.domain.repository.RandomNumberRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllRandomNumbersUseCase @Inject constructor(
    private val repository: RandomNumberRepository
) {
    operator fun invoke(): Flow<List<RandomNumber>> = repository.getAll()
}

