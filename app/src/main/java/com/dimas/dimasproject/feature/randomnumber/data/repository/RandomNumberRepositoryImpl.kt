package com.dimas.dimasproject.feature.randomnumber.data.repository

import com.dimas.dimasproject.feature.randomnumber.data.local.RandomNumberLocalDataSource
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber
import com.dimas.dimasproject.feature.randomnumber.data.model.toDomain
import com.dimas.dimasproject.feature.randomnumber.data.model.toEntity
import com.dimas.dimasproject.feature.randomnumber.data.remote.RandomNumberRemoteDataSource
import com.dimas.dimasproject.feature.randomnumber.domain.repository.RandomNumberRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RandomNumberRepositoryImpl @Inject constructor(
    private val remote: RandomNumberRemoteDataSource,
    private val local: RandomNumberLocalDataSource
) : RandomNumberRepository {

    override suspend fun fetchAndSave(): Result<RandomNumber> = runCatching {
        val response = remote.fetchRandomNumber()
        val domain = response.toDomain()
        local.save(domain.toEntity())
        domain
    }

    override fun getAll(): Flow<List<RandomNumber>> =
        local.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getLatest(): RandomNumber? =
        local.getLatest()?.toDomain()
}

