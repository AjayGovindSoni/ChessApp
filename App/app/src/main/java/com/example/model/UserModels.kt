package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class User(val id: String, val email: String)
