package com.dimas.dimasproject.feature.randomnumber.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dimas.dimasproject.feature.randomnumber.data.model.RandomNumber

@Entity(tableName = "random_numbers")
data class RandomNumberEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "number")
    val number: Int,

    @ColumnInfo(name = "range_from")
    val rangeFrom: Int,

    @ColumnInfo(name = "range_to")
    val rangeTo: Int
)

fun RandomNumberEntity.toDomain(): RandomNumber = RandomNumber(
    number = number,
    rangeFrom = rangeFrom,
    rangeTo = rangeTo
)

fun RandomNumber.toEntity(): RandomNumberEntity = RandomNumberEntity(
    number = number,
    rangeFrom = rangeFrom,
    rangeTo = rangeTo
)

