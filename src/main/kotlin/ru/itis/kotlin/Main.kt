package ru.itis.kotlin

import io.ktor.utils.io.errors.*
import ru.itis.kotlin.news_api.model.News
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.itis.kotlin.news_api.*
import java.time.LocalDate

private val logger: Logger = LoggerFactory.getLogger("ru/itis/kotlin/Main.kt/kotlin/Main.kt")

suspend fun main() {
    val list: List<News> = getNews(1000)
    val mostRatedList: List<News> = list.getMostRatedNews(50, LocalDate.of(2024, 9, 13)..LocalDate.of(2024, 9, 15))
    val mostRatedListAmongAllNews: List<News> = getMostRatedNewsAmongAllNews(50, LocalDate.of(2022, 1, 1)..LocalDate.of(2024, 9, 15))
    try {
        saveNews("test.csv", mostRatedListAmongAllNews)
        saveNews("test1.csv", mostRatedList)
    } catch (exception: IOException) {
        logger.warn(exception.message)
    }
    closeNewsClient()
}