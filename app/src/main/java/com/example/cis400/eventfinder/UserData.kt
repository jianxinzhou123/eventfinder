package com.example.cis400.eventfinder

import java.io.Serializable

data class UserData(
    val last: String,
    val first: String
): Serializable {
    constructor(): this("", "")
}