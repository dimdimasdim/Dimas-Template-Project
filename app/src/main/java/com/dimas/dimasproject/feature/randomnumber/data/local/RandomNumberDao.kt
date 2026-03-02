package com.dimas.dimasproject.feature.randomnumber.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RandomNumberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: RandomNumberEntity)

    @Query("SELECT * FROM random_numbers ORDER BY id DESC")
    fun getAll(): Flow<List<RandomNumberEntity>>

    @Query("SELECT * FROM random_numbers ORDER BY id DESC LIMIT 1")
    suspend fun getLatest(): RandomNumberEntity?

    @Query("DELETE FROM random_numbers")
    suspend fun deleteAll()
}

