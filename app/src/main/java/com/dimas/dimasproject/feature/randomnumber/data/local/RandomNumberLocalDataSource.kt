package com.dimas.dimasproject.feature.randomnumber.data.local

import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumberEntity
import kotlinx.coroutines.flow.Flow

class RandomNumberLocalDataSource(
    private val dao: RandomNumberDao
) {
    fun getAll(): Flow<List<RandomNumberEntity>> = dao.getAll()

    suspend fun getLatest(): RandomNumberEntity? = dao.getLatest()

    suspend fun save(entity: RandomNumberEntity) = dao.insert(entity)

    suspend fun clearAll() = dao.deleteAll()
}

