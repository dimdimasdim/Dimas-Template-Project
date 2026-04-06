package com.dimas.dimasproject.feature.randomnumber.domain.usecase

import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber
import com.dimas.dimasproject.feature.randomnumber.domain.repository.RandomNumberRepository

class FetchRandomNumberUseCase(
    private val repository: RandomNumberRepository
) {
    suspend operator fun invoke(): Result<RandomNumber> = repository.fetchAndSave()
}

