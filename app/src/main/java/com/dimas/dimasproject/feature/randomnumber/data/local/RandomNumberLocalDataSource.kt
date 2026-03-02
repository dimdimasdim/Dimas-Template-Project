package com.dimas.dimasproject.feature.randomnumber.data.local

import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumberEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RandomNumberLocalDataSource @Inject constructor(
    private val dao: RandomNumberDao
) {
    fun getAll(): Flow<List<RandomNumberEntity>> = dao.getAll()

    suspend fun getLatest(): RandomNumberEntity? = dao.getLatest()

    suspend fun save(entity: RandomNumberEntity) = dao.insert(entity)

    suspend fun clearAll() = dao.deleteAll()
}

