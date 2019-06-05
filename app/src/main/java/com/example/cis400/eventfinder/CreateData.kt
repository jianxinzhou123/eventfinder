package com.example.cis400.eventfinder

import java.io.Serializable

data class CreateData(
    val access: String
):Serializable{
    constructor(): this("")
}