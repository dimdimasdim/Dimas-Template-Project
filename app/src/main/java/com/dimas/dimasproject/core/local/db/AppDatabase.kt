package com.dimas.dimasproject.core.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.dimas.dimasproject.core.local.db.entity.UserEntity
import com.dimas.dimasproject.feature.randomnumber.data.local.RandomNumberDao
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumberEntity

@Database(
    entities = [UserEntity::class, RandomNumberEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun randomNumberDao(): RandomNumberDao
}
