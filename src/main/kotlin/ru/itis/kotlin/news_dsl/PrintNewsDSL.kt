package ru.itis.kotlin.news_dsl

@DslMarker
annotation class NewsTagMarker

@NewsTagMarker
abstract class Element(private val elementName: String) {
    private val children = arrayListOf<Element>()
    private var text = ""
    private fun render(builder: StringBuilder, indent: String) {
        builder.append("${indent}${elementName}: ${text}\n")
        for (c in children) {
            c.render(builder, indent + "\t")
        }
    }

    protected fun <T : Element> initElement(tag: T, init: T.() -> Unit): T {
        tag.init()
        children.add(tag)
        return tag
    }

    override fun toString(): String {
        val builder = StringBuilder()
        render(builder, "")
        return builder.toString()
    }

    operator fun String.unaryPlus() {
        text = this
    }
}

fun news(init: NewsPrinter.() -> Unit): NewsPrinter {
    val news = NewsPrinter()
    news.init()
    return news
}

class NewsPrinter : Element("Новость") {

    fun title(init: Title.() -> Unit): Title = initElement(Title(), init)

    fun place(init: PlacePrinter.() -> Unit): PlacePrinter = initElement(PlacePrinter(), init)

    fun description(init: Description.() -> Unit): Description = initElement(Description(), init)

    fun siteUrl(init: SiteUrl.() -> Unit): SiteUrl = initElement(SiteUrl(), init)

    fun favoritesCount(init: FavoritesCount.() -> Unit): FavoritesCount = initElement(FavoritesCount(), init)

    fun commentsCount(init: CommentsCount.() -> Unit): CommentsCount = initElement(CommentsCount(), init)

    fun rating(init: Rating.() -> Unit): Rating = initElement(Rating(), init)

}

class PlacePrinter : Element("Место") {

    fun title(init: Title.() -> Unit): Title = initElement(Title(), init)

    fun description(init: Description.() -> Unit): Description = initElement(Description(), init)

}


class Title : Element("Заголовок")

class Description : Element("Описание")

class SiteUrl : Element("Ссылка на новость на сайте")

class FavoritesCount : Element("Количество лайков")

class CommentsCount : Element("Количество комментариев")

class Rating : Element("Рейтинг новости")
