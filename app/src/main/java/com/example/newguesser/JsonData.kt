package com.example.newguesser

data class Question(
    val en: String = "",
    val de: String = ""
)

data class Hint(
    val en: String = "",
    val de: String = ""
)

data class Answer(
    val en: String = "",
    val de: String = ""
)

data class DataItem(
    val question: Question,
    val hint: List<Hint>,
    val answer: Answer
)

data class JsonData(
    val data: List<DataItem>
)

data class CharacterPosition(
    val char: Char,
    val position: Int
)