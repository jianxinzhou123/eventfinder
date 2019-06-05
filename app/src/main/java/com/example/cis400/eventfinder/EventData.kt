package com.example.cis400.eventfinder

import java.io.Serializable

data class EventData(
    val date: String,
    val description: String,
    val image: String,
    val interested: Int,
    val location: String,
    val mediaPhoto: String,
    val mediaYoutube: String?,
    val name: String,
    val tableKey: String,
    val time: String
):Serializable{
    constructor(): this("", "", "", -1, "", "", "", "", "", "")
}