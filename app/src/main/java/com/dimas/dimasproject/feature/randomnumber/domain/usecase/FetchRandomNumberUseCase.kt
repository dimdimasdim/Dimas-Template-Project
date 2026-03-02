package com.dimas.dimasproject.feature.randomnumber.domain.usecase

import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber
import com.dimas.dimasproject.feature.randomnumber.domain.repository.RandomNumberRepository
import javax.inject.Inject

class FetchRandomNumberUseCase @Inject constructor(
    private val repository: RandomNumberRepository
) {
    suspend operator fun invoke(): Result<RandomNumber> = repository.fetchAndSave()
}

