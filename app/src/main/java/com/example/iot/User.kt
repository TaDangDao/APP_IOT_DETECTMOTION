package com.example.iot

data class User(
    val email: String = "",
    val role: String = "user", // Mặc định là "user"
    val deviceId:String  =""
)
