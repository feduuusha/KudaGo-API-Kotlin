package ru.itis.kotlin

import kotlinx.coroutines.runBlocking
import ru.itis.kotlin.news_api.NewsClient

fun main() = runBlocking {
    val client = NewsClient()
    val start = System.currentTimeMillis()
    client.getNewsAsync(13134, "text.txt")
    client.close()
    print(System.currentTimeMillis() - start)
}