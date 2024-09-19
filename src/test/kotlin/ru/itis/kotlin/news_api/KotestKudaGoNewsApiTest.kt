package ru.itis.kotlin.news_api

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.longs.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import ru.itis.kotlin.news_api.model.News
import java.io.File
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

private const val ZONE_OFFSET_ID = "+3"

class KotestKudaGoNewsApiTest : StringSpec ( {

    val tempDir = tempdir()

    "getNews should return list of first news" {
        // given
        val size = 100

        // when
        val list = getNews(size)

        // then
        withClue({"list = getNews($size) method was called, but the list.size=${list.size}"}) {
            list shouldHaveSize size
        }
    }

    "getMostRatedNews should return list of news that satisfy time period" {
        // given
        val size = 3
        val startDate = LocalDate.of(2024, 1, 15)
        val endDate = LocalDate.of(2024, 9, 15)
        var list = listOf(
            News(1, "", null, "", "", 4, 0, 1710507332),
            News(2, "", null, "", "", 2, 0, 1600000000),
            News(3, "", null, "", "", 1, 0, 2000000000),
            News(4, "", null, "", "", 3, 0, 1500000000),
            News(5, "", null, "", "", 0, 0, 1700508832),
            News(6, "", null, "", "", 5, 0, 1710507332),
            News(7, "", null, "", "", 7, 0, 1600000000),
            News(8, "", null, "", "", 3, 0, 1710534332),
            News(9, "", null, "", "", 2, 0, 1500000000),
            News(10, "", null, "", "", 1, 0, 1710508822)
        )

        // when
        list = list.getMostRatedNews(size, startDate..endDate)

        // then
        assertSoftly {
            withClue({"list.getMostRatedNews($size,startDate..endDate) method was called, but the list.size=${list.size}"}) {
                list shouldHaveSize size
            }

            list.forEach {news ->
                withClue({"list.getMostRatedNews($size,$startDate..$endDate) method was called, " +
                        "but the news in list have publicationDate=${news.publicationDate}"}) {
                    news.publicationDate shouldBeGreaterThanOrEqual startDate.toEpochSecond(
                        LocalTime.MIN, ZoneOffset.of(
                        ZONE_OFFSET_ID
                    ))
                    news.publicationDate shouldBeLessThanOrEqual endDate.toEpochSecond(
                        LocalTime.MIN, ZoneOffset.of(
                        ZONE_OFFSET_ID
                    ))
                }
            }
        }
    }

    "getMostRatedNews should return list sorted by rating" {
        // given
        val size = 3
        val startDate = LocalDate.of(2024, 1, 15)
        val endDate = LocalDate.of(2024, 9, 15)
        var list = listOf(
            News(1, "", null, "", "", 4, 0, 1710507332),
            News(2, "", null, "", "", 2, 0, 1600000000),
            News(3, "", null, "", "", 1, 0, 2000000000),
            News(4, "", null, "", "", 3, 0, 1500000000),
            News(5, "", null, "", "", 0, 0, 1700508832),
            News(6, "", null, "", "", 5, 0, 1710507332),
            News(7, "", null, "", "", 7, 0, 1600000000),
            News(8, "", null, "", "", 3, 0, 1710534332),
            News(9, "", null, "", "", 2, 0, 1500000000),
            News(10, "", null, "", "", 1, 0, 1710508822)
        )

        // when
        list = list.getMostRatedNews(size, startDate..endDate)

        // then
        var previous = Double.MAX_VALUE
        assertSoftly {
            list.forEach {news ->
                withClue({"The news is not in descending order in the list"}) {
                    news.rating shouldBeLessThanOrEqual previous
                }
                previous = news.rating
            }
        }
    }

    "getMostRatedNewsAmongAllNews should return list of news that satisfy time period" {
        // given
        val size = 50
        val startDate = LocalDate.of(2024, 1, 15)
        val endDate = LocalDate.of(2024, 9, 15)

        // when
        val list = getMostRatedNewsAmongAllNews(size, startDate..endDate)

        // then
        assertSoftly {
            withClue({"list.getMostRatedNewsAmongAllNews($size,startDate..endDate) " +
                    "method was called, but the list.size=${list.size}"}) {
                list shouldHaveSize size
            }

            list.forEach {news ->
                withClue({"list.getMostRatedNewsAmongAllNews($size,$startDate..$endDate) method was called, " +
                        "but the news in list have publicationDate=${news.publicationDate}"}) {
                    news.publicationDate shouldBeGreaterThanOrEqual startDate.toEpochSecond(
                        LocalTime.MIN, ZoneOffset.of(
                            ZONE_OFFSET_ID
                        )
                    )
                    news.publicationDate shouldBeLessThanOrEqual endDate.toEpochSecond(
                        LocalTime.MIN, ZoneOffset.of(
                            ZONE_OFFSET_ID
                        )
                    )
                }
            }
        }
    }

    "getMostRatedNewsAmongAllNews should return list of news sorted by rating" {
        // given
        val size = 50
        val startDate = LocalDate.of(2024, 1, 15)
        val endDate = LocalDate.of(2024, 9, 15)

        // when
        val list = getMostRatedNewsAmongAllNews(size, startDate..endDate)

        // then
        var previous = Double.MAX_VALUE
        assertSoftly {
            list.forEach {news ->
                withClue({"The news is not in descending order in the list"}) {
                    news.rating shouldBeLessThanOrEqual previous
                }
                previous = news.rating
            }
        }
    }

    "saveNews should save collection of news in csv file" {
        // given
        val list = listOf(
            News(1, "123", null, "description", "url", 1, 2, 90000000),
            News(2, "223", null, "description3", "url3", 5, 10, 1000000))
        val path = tempDir.resolve("saveNewsTest.csv").toString()

        // when
        saveNews(path, list)

        // then
        assertSoftly {
            val file = File(path)
            withClue({"A $path file should have been created after call saveNews($path, list)"}) {
                file.exists() shouldBe true
            }

            file.bufferedReader().use {
                list.forEach {news ->
                    withClue({"The entries in the list must correspond one to one entries from the file"}) {
                        it.readLine() shouldBe news.toCSVString()
                    }
                }
            }
        }
    }

    "saveNews should throw IOException if file already exist" {
        // given
        val path = tempDir.resolve("alreadyExist.csv").toString()
        File(path).createNewFile()

        // when
        withClue({"The file already exist, but IOException wasn't thrown"}) {
            shouldThrowWithMessage<IOException>("The file in the specified path already exists") {
                saveNews(path, listOf())
            }
        }
    }

    "saveNews should throw IOException if file dont ends with csv" {
        // given
        val path = tempDir.resolve("file").toString()

        // when
        withClue({"The file dont ends with csv, but IOException wasn't thrown"}) {
            shouldThrowWithMessage<IOException>("The file path must end with .csv") {
                saveNews(path, listOf())
            }
        }
    }

})