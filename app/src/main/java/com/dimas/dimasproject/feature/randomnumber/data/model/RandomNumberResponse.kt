package com.dimas.dimasproject.feature.randomnumber.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RandomNumberResponse(
    @SerialName("random_number") val randomNumber: Int?,
    @SerialName("range") val range: RangeResponse?
)

@Serializable
data class RangeResponse(
    @SerialName("from") val from: Int?,
    @SerialName("to") val to: Int?
)

fun RandomNumberResponse.toDomain(): RandomNumber = RandomNumber(
    number = randomNumber ?: 0,
    rangeFrom = range?.from ?: 0,
    rangeTo = range?.to ?: 0
)

