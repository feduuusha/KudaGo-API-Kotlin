package ru.itis.kotlin.news_api

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIOException
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import ru.itis.kotlin.news_api.model.News
import java.io.File
import java.nio.file.Path
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import kotlin.io.use

private const val ZONE_OFFSET_ID = "+3"

class KudaGoNewsApiTest {

    @TempDir
    private lateinit var tempDir:Path
    @Test
    fun `getNews should return list of first news`() {
        return runTest {
            // given
            val size = 100

            // when
            val list = getNews(size)

            // then
            assertThat(list.size)
                .`as`("list = getNews($size) method was called, but the list.size=${list.size}")
                .isEqualTo(size)
        }
    }

    @Test
    fun `getMostRatedNews should return list of news that satisfy time period`() {
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
        val softAssertions = SoftAssertions()

        softAssertions.assertThat(list.size)
            .`as`("list.getMostRatedNews($size,startDate..endDate) method was called, but the list.size=${list.size}")
            .isEqualTo(size)

        for (news in list) {
            softAssertions
                .assertThat(news.publicationDate)
                .`as` {
                    "list.getMostRatedNews($size,$startDate..$endDate) method was called, " +
                            "but the news in list have publicationDate=${news.publicationDate}"
                }
                .isBetween(
                    startDate.toEpochSecond(
                        LocalTime.MIN, ZoneOffset.of(
                            ZONE_OFFSET_ID
                        )
                    ), endDate.toEpochSecond(LocalTime.MAX, ZoneOffset.of(ZONE_OFFSET_ID))
                )

        }

        softAssertions.assertAll()
    }

    @Test
    fun `getMostRatedNews should return list sorted by rating`() {
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
        val softAssertions = SoftAssertions()

        softAssertions.assertThat(list.size)
            .`as`("list.getMostRatedNews($size,startDate..endDate) method was called, but the list.size=${list.size}")
            .isEqualTo(size)
        var previousRating = Double.MAX_VALUE
        for (news in list) {
            softAssertions
                .assertThat(news.rating)
                .`as`("The news is not in descending order in the list")
                .isLessThanOrEqualTo(previousRating)

            previousRating = news.rating
        }

        softAssertions.assertAll()
    }

    @Test
    fun `getMostRatedNewsAmongAllNews should return list of news that satisfy time period`() {
        return runTest {
            // given
            val size = 50
            val startDate = LocalDate.of(2024, 1, 15)
            val endDate = LocalDate.of(2024, 9, 15)

            // when
            val list = getMostRatedNewsAmongAllNews(size, startDate..endDate)

            // then
            val softAssertions = SoftAssertions()

            softAssertions
                .assertThat(list.size)
                .`as` {
                    "list.getMostRatedNewsAmongAllNews($size,startDate..endDate) " +
                            "method was called, but the list.size=${list.size}"
                }
                .isEqualTo(size)
            for (news in list) {
                softAssertions
                    .assertThat(news.publicationDate)
                    .`as` {
                        "list.getMostRatedNewsAmongAllNews($size,$startDate..$endDate) method was called, " +
                                "but the news in list have publicationDate=${news.publicationDate}"
                    }
                    .isBetween(
                        startDate.toEpochSecond(
                            LocalTime.MIN, ZoneOffset.of(
                                ZONE_OFFSET_ID
                            )
                        ), endDate.toEpochSecond(LocalTime.MAX, ZoneOffset.of(ZONE_OFFSET_ID))
                    )
            }

            softAssertions.assertAll()
        }
    }

    @Test
    fun `getMostRatedNewsAmongAllNews should return list of news sorted by rating`() {
        return runTest {
            // given
            val size = 50
            val startDate = LocalDate.of(2024, 1, 15)
            val endDate = LocalDate.of(2024, 9, 15)

            // when
            val list = getMostRatedNewsAmongAllNews(size, startDate..endDate)

            // then
            val softAssertions = SoftAssertions()

            softAssertions
                .assertThat(list.size)
                .`as` {
                    "list.getMostRatedNewsAmongAllNews($size,startDate..endDate) " +
                            "method was called, but the list.size=${list.size}"
                }
                .isEqualTo(size)
            var previousRating = Double.MAX_VALUE
            for (news in list) {
                softAssertions
                    .assertThat(news.rating)
                    .`as`("The news is not in descending order in the list")
                    .isLessThanOrEqualTo(previousRating)
                previousRating = news.rating
            }

            softAssertions.assertAll()
        }
    }

    @Test
    fun `saveNews should save collection of news in csv file`() {
        // given
        val list = listOf(
            News(1, "123", null, "description", "url", 1, 2, 90000000),
            News(2, "223", null, "description3", "url3", 5, 10, 1000000))
        val path = tempDir.resolve("saveNewsTest.csv").toString()

        // when
        saveNews(path, list)

        // then
        val softAssertions = SoftAssertions()
        val file = File(path)
        softAssertions
            .assertThat(file.exists())
            .`as`("A $path file should have been created after call saveNews($path, list)")
            .isTrue
        file.bufferedReader().use {
            for (news in list) {
                val line = it.readLine()
                softAssertions
                    .assertThat(line)
                    .`as`("The entries in the list must correspond one to one entries from the file")
                    .isEqualTo(news.toCSVString() + "\n")
            }
        }

    }

    @Test
    fun `saveNews should throw IOException if file already exist`() {
        // given
        val path = tempDir.resolve("alreadyExist.csv").toString()
        File(path).createNewFile()

        // when
        assertThatIOException()
            .`as`("The file already exist, but IOException wasn't thrown")
            .isThrownBy { saveNews(path, listOf()) }
    }

    @Test
    fun `saveNews should throw IOException if file dont ends with csv`() {
        // given
        val path = tempDir.resolve("file").toString()

        // when
        assertThatIOException()
            .`as`("The file dont ends with csv, but IOException wasn't thrown")
            .isThrownBy { saveNews(path, listOf()) }
    }

}
