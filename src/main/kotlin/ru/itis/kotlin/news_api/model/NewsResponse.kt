package ru.itis.kotlin.news_api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NewsResponse(
    @SerialName("results")
    val results: List<News>,
)