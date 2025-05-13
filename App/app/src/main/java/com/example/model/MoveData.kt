package com.example.model

import kotlinx.serialization.Serializable

@Serializable
data class MoveData(
    val initial: OffsetData,
    val final: OffsetData
)
@Serializable
data class OffsetData(
    val x: Int,
    val y: Int
)
