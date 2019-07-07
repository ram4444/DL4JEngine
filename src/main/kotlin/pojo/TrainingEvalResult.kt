package main.kotlin.pojo

data class TrainingEvalResult(
        val id: String,
        val model: String,
        val accuracy: Float,
        val precision: Float,
        val f1: Float,
        val recall: Float
)