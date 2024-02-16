package com.example.appvicentemiscocos


data class ApiResponse(val results: List<Result>)

data class Result(val picture: Picture)

data class Picture(val large: String)
