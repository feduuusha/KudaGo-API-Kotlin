package ru.itis.kotlin.news_api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.errors.*
import kotlinx.serialization.json.Json
import ru.itis.kotlin.news_api.model.News
import ru.itis.kotlin.news_api.model.NewsResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.stream.Collectors

private const val ZONE_OFFSET_ID = "+3"
private const val API_URL = "https://kudago.com/public-api/v1.4/news/"

private val client: HttpClient = HttpClient()
private val json = Json {
    ignoreUnknownKeys = true
}
private val logger: Logger = LoggerFactory.getLogger("KudaGoNewsApi.kt")

suspend fun getNews(count: Int = 100): List<News> {
    return getNewsByPageAndPageSize(1, count)
}

// метод для получения самых оцененных и удовлетворяющих периоду времени новостей, конкретного списка новостей
fun List<News>.getMostRatedNews(count: Int, period: ClosedRange<LocalDate>): List<News> {
    logger.info("Calling the getMostRatedNews method with count=$count and period=$period")
    return this.asSequence()
        .sortedByDescending { news -> news.rating }
        .filter { news ->
            val newsDate: LocalDate = LocalDateTime.ofEpochSecond(
                news.publicationDate,
                0, ZoneOffset.of(ZONE_OFFSET_ID)
            ).toLocalDate()
            period.start <= newsDate && newsDate <= period.endInclusive
        }
        .take(count)
        .toList()
}

// метод для получения самых оцененных новостей за конкретный период среди всех новостей сайта
suspend fun getMostRatedNewsAmongAllNews(count: Int, period: ClosedRange<LocalDate>): List<News> {
    logger.info("Calling the getMostRatedNewsAmongAllNews method with count=$count and period=$period")
    val answer: ArrayList<News> = arrayListOf()
    var news: List<News>
    var page = 1
    var flag = true
    while (flag) {
        news = getNewsByPageAndPageSize(page++, 1000)
        for (headline: News in news) {
            val newsDate = LocalDateTime.ofEpochSecond(
                headline.publicationDate, 0, ZoneOffset.of(ZONE_OFFSET_ID)
            ).toLocalDate()
            if (period.start <= newsDate && newsDate <= period.endInclusive) {
                answer.add(headline)
            } else if (newsDate < period.start) {
                logger.info("The news has ended in the specified period")
                flag = false
                break
            }
        }
        if (news.size != 1000) {
            logger.info("The last page with the news was received")
            flag = false
        }
    }
    return answer
        .stream()
        .sorted { news1, news2 -> if (news1.rating > news2.rating) -1 else if (news1.rating < news2.rating) 1 else 0 }
        .limit(count.toLong())
        .collect(Collectors.toList())
}

// метод для получения списка новостей используя пагинацию
private suspend fun getNewsByPageAndPageSize(page: Int, pageSize: Int): List<News> {
    logger.info("Calling the getNewsByPageAndPageSize method with page=$page and pageSize=$pageSize")
    val response: HttpResponse = client.get(API_URL) {
        url {
            parameters.append("page", "$page")
            parameters.append("page_size", "$pageSize")
            parameters.append("text_format", "text")
            parameters.append("expand", "place")
            parameters.append("order_by", "-publication_date")
            parameters.append("location", "kzn")
            parameters.append(
                "fields", "id,title,place,description,site_url," +
                        "favorites_count,comments_count,publication_date"
            )

        }
    }
    val newsResponse: NewsResponse = json.decodeFromString(response.body())
    return newsResponse.results
}

// метод для сохранения новостей в .csv файл
fun saveNews(path: String, news: Collection<News>) {
    logger.info("Calling the saveNews method with path=$path")
    if (path.endsWith(".csv")) {
        val file = File(path)
        if (file.createNewFile()) {
            file.writer().use { outputStream ->
                outputStream.write(news.joinToString(separator = "\n") { it.toCSVString() })
            }
        } else {
            throw IOException("The file in the specified path already exists")
        }
    } else {
        throw IOException("The file path must end with .csv")
    }
}

fun closeNewsClient() {
    logger.info("Calling the closeNewsClient method")
    client.close()
}