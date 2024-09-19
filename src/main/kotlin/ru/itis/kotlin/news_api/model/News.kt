package ru.itis.kotlin.news_api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.exp

private const val ZONE_OFFSET_ID = "+3"

@Serializable
data class News(
    val id: Long,
    val title: String,
    val place: Place?,
    val description: String,
    @SerialName("site_url")
    val siteUrl: String,
    @SerialName("favorites_count")
    val favoritesCount: Long,
    @SerialName("comments_count")
    val commentsCount: Long,
    @SerialName("publication_date")
    val publicationDate: Long
) {
    val rating: Double by lazy { (1 / (1 + exp((-(favoritesCount / (commentsCount + 1).toDouble()))))) }
    fun toCSVString(): String {
        return "$id,\"${title.replace("\"", "\"\"")}\",\"${place?.id}\",\"${description.replace("\"", "\"\"")}\"," +
                "\"$siteUrl\",$favoritesCount,$commentsCount,${
                    LocalDateTime.ofEpochSecond(
                        publicationDate,
                        0,
                        ZoneOffset.of(ZONE_OFFSET_ID)
                    )
                },$rating"
    }
}
