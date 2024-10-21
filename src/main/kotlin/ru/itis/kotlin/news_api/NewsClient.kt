package ru.itis.kotlin.news_api

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.errors.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.sync.Semaphore
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.itis.kotlin.news_api.model.News
import ru.itis.kotlin.news_api.model.NewsResponse
import java.io.File
import java.lang.Exception
import java.lang.IllegalStateException


@OptIn(DelicateCoroutinesApi::class)
class NewsClient(
    private val apiUrl: String = "https://kudago.com/public-api/v1.4/news/",
    private val countOfWorkers: Int = 16,
    countOfThreads: Int = 8,
    numberConnectionAtSameTime: Int = 5
) {
    init {
        require(countOfThreads > 0) {"CountOfThreads should be positive"}
        require(countOfWorkers > 0) {"CountOfWorkers should be positive"}
        require(numberConnectionAtSameTime > 0) {"NumberConnectionAtSameTime should be positive"}
    }

    private val logger: Logger = LoggerFactory.getLogger("NewsClient.kt")
    private val threadPoolContext = newFixedThreadPoolContext(countOfThreads, "GettingNewsThreadPool")
    private val client = HttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val semaphore = Semaphore(numberConnectionAtSameTime)

    private inner class Worker(
        private val scope: CoroutineScope,
        private val channel: SendChannel<List<News>>,
        private val countOfNews: Int,
        private val startPage: Int,
        private val endPage: Int
    ) {
        fun start(): Job = scope.launch {
            logger.info("Start Worker coroutine with countOfNews=${countOfNews}, startPage=${startPage}, endPage=${endPage}")
            for (i in startPage..endPage step countOfWorkers) {
                var news = getNewsByPageAndPageSize(i, 100)
                news = news.take(countOfNews)
                channel.send(news)
            }
        }
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    fun CoroutineScope.newsActor(filename: String) = actor<List<News>> {
        logger.info("Calling CoroutineScope.newsActor method with filename=${filename}")
        val file = File(filename)
        try {
            file.createNewFile()
            file.writer().use { writer ->
                for (news in channel) {
                    writer.write(news.joinToString("\n", postfix = "\n"))
                }
            }
        } catch (e : IOException) {
            logger.error(e.message)
            throw IllegalStateException(e)
        }
    }

    suspend fun getNewsAsync(count: Int = 100, filename: String) {
        require(count >= 0) {"newsCount should be non negative"}
        logger.info("Calling getNewsAsync method with count=${count}, filename=${filename}")
        val scope = CoroutineScope(threadPoolContext)
        val sendChannel = scope.newsActor(filename)
        val jobs: MutableList<Job> = mutableListOf()
        for (i in 1..(if (countOfWorkers > count / 100) count / 100 else countOfWorkers)) {
            val worker = Worker(scope, sendChannel, 100, i, count / 100)
            jobs.add(worker.start())
        }
        if (count % 100 != 0) {
            val worker = Worker(scope, sendChannel, count % 100, (count / 100) + 1, (count / 100) + 1)
            jobs.add(worker.start())
        }
        jobs.joinAll()
        sendChannel.close()
    }


    private suspend fun getNewsByPageAndPageSize(page: Int, pageSize: Int): List<News> {
        logger.info("Calling getNewsByPageAndPageSize method with page=${page}, pageSize=${pageSize}")
        semaphore.acquire()
        try {
            val response: HttpResponse = client.get(apiUrl) {
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
        } catch (e : Exception) {
            logger.error(e.message)
            throw IllegalStateException(e)
        } finally {
            semaphore.release()
        }
    }

    fun close() {
        logger.info("Calling close method")
        client.close()
        threadPoolContext.close()
    }
}