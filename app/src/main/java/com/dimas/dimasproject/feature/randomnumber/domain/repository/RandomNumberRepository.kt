package com.dimas.dimasproject.feature.randomnumber.domain.repository

import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber
import kotlinx.coroutines.flow.Flow

interface RandomNumberRepository {
    suspend fun fetchAndSave(): Result<RandomNumber>
    fun getAll(): Flow<List<RandomNumber>>
    suspend fun getLatest(): RandomNumber?
}

