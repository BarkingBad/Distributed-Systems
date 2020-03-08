package main

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.html.respondHtml
import io.ktor.request.receiveParameters
import io.ktor.routing.*
import kotlinx.html.*
import java.net.URL

fun Application.main() {

    routing {
        get("/") {

            val categories = (CURRENT_API_CATEGORIES.map { Pair(it, "CURRENT API") } + NYT_CATEGORIES.map { Pair(it, "NYT") })
                    .groupBy({ it.first }, { it.second })
                    .map { "${it.key} [${it.value.joinToString(separator = ", ")}]" }

            call.respondHtml {
                body {
                    form(method = FormMethod.post, encType = FormEncType.multipartFormData) {
                        textInput { name = "query" }
                        submitInput { value = "Wyślij" }
                    }
                    br
                    +"New York Times and Current API compatible queries:"
                    br
                    ul {
                        categories.forEach {
                            li { +it }
                        }
                    }
                }
            }
        }
        post("/") {
            val query = call.receiveParameters()["query"] ?: throw IllegalStateException("Have not received 'query' parameters")

            val results = listOf(
                    fromJson<NewApiResults>(NEW_API_LINK(query).toJson).toGenericResults(),
                    fromJson<GuardianResponse>(GUARDIAN_LINK(query).toJson).toGenericResults(),
                    if (query in NYT_CATEGORIES) fromJson<NytResults>(NYT_LINK(query).toJson).toGenericResults() else GenericResults(emptyList()),
                    if (query in CURRENT_API_CATEGORIES) fromJson<CurrentApiResults>(CURRENT_API_LINK(query).toJson).toGenericResults() else GenericResults(emptyList())
            ).flatMap { it.results }.shuffled()

            call.respondHtml {
                body {
                    results.forEach {
                        h1 {
                            +it.title
                        }
                        h5 {

                            +it.origin
                        }
                        +it.description
                        br
                        a(href = it.url) {
                            +it.url
                        }
                    }
                }
            }
        }
    }
}

inline fun <reified T> fromJson(json: String) = Gson().fromJson<T>(json, T::class.java)

val NEWS_API_KEY = "1165c41e2b284289b962de292c5227f1"
fun NEW_API_LINK(query: String) = "http://newsapi.org/v2/everything?q=$query&apiKey=$NEWS_API_KEY"
data class NewApiResults(val articles: List<NewApiArticle>) { fun toGenericResults() = GenericResults(articles.map { it.toGenericArticle() }) }
data class NewApiArticle(val title: String, val description: String, val url: String) { fun toGenericArticle() = GenericArticle(title, "News Api", description, url) }

val GUARIDAN_KEY = "03e6ee6e-3ebf-4969-a34e-2242c86f15a2"
fun GUARDIAN_LINK(query: String) = "https://content.guardianapis.com/search?q=$query&api-key=$GUARIDAN_KEY"
data class GuardianResponse(val response: GuardianResults) { fun toGenericResults() = GenericResults(response.results.map { it.toGenericArticle() }) }
data class GuardianResults(val results: List<GuardianArticle>)
data class GuardianArticle(val webTitle: String, val webUrl: String) { fun toGenericArticle() = GenericArticle(webTitle, "Guardian", "", webUrl) }

val CURRENT_API_KEY = "aLOixwdON57r1Gor1R7S2b9XpkGtYgAcgA58qnHv_l7pnPJH"
fun CURRENT_API_LINK(category: String) = "https://api.currentsapi.services/v1/search?category=$category&apiKey=$CURRENT_API_KEY"
data class CurrentApiResults(val news: List<CurrentApiArticle>) { fun toGenericResults() = GenericResults(news.map { it.toGenericArticle() }) }
data class CurrentApiArticle(val title: String, val description: String, val url: String) { fun toGenericArticle() = GenericArticle(title, "Current Api", description, url) }

val CURRENT_API_CATEGORIES by lazy {
    URL("https://api.currentsapi.services/v1/available/categories").readText().let {
        fromJson<CurrentApiCategories>(it).categories
    }
}
data class CurrentApiCategories(val categories: List<String>)

val NYT_KEY = "dkXZ76tbFQLYVTlVqKtIiLmgPG6HZp7G"
fun NYT_LINK(category: String) = "https://api.nytimes.com/svc/topstories/v2/$category.json?api-key=$NYT_KEY"
val NYT_CATEGORIES = listOf("arts", "automobiles", "books", "business", "fashion", "food", "health", "home", "insider", "magazine", "movies", "nyregion", "obituaries", "opinion", "politics", "realestate", "science", "sports", "sundayreview", "technology", "theater", "t-magazine", "travel", "upshot", "us", "world")
data class NytResults(val results: List<NytArticle>) { fun toGenericResults() = GenericResults(results.map { it.toGenericArticle() }) }
data class NytArticle(val title: String, val abstract: String, val url: String) { fun toGenericArticle() = GenericArticle(title, "New York Times", abstract, url) }

open class GenericResults(val results: List<GenericArticle>)
open class GenericArticle(val title: String, val origin: String, val description: String, val url: String)

val String.toJson: String
    get() = URL(this).readText()
