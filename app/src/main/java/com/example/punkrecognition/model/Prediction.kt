package com.example.punkrecognition.model

data class Prediction(
    var name: String? = null,
    var location: List<Double> = emptyList(),
)
