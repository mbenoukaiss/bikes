package io.github.mbenoukaiss.bikes.models

class Contract(
    val name: String,
    val commercialName: String,
    val countryCode: String,
    val cities: Array<String>?
)