package ru.itis.kotlin.news_dsl

fun main() {
    val text = news {
        +"Невероятное событие произошло сегодня в Москве"
        title { +"Кот перевернулся с боку на бок!!!" }
        description { +"Кот Боря после долго сна, сладко перевернулся на другой бок и уснул опять..." }
        place {
            +"Москва. Патрики"
            title { +"Улица Пушкина дом Колотушкина" }
            description { +"Красивый дом, ничего не скажешь" }
        }
        siteUrl { +"https://itis.ru" }
        favoritesCount { +"100" }
        commentsCount { +"999" }
        rating { +"10/10" }
    }
    print(text)
}