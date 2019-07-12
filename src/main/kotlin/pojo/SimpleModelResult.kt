package main.kotlin.pojo

data class SimpleModelResult(
        val model: String,
        val label: String?,
        val outputIndex: Int,
        val confidence: Float?
)