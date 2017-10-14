package sjj.fiction.data.source.remote.dhzw

import io.reactivex.Observable
import org.jsoup.Jsoup
import sjj.fiction.data.Repository.FictionDataRepository
import sjj.fiction.data.source.remote.HttpDataSource
import sjj.fiction.data.source.remote.HttpInterface
import sjj.fiction.model.*
import java.net.URLEncoder

/**
 * Created by SJJ on 2017/9/3.
 */
class DhzwDataSource : HttpDataSource(), FictionDataRepository.RemoteSource {
    override fun baseUrl(): String = "http://www.dhzw.org"

    override fun domain(): Url = Url(baseUrl())

    private val service = create<HttpInterface>()

    override fun search(search: String): Observable<List<Book>> {
        return service.searchForGBK("modules/article/search.php", mapOf(Pair("searchkey", URLEncoder.encode(search, "gbk"))))
                .map {
                    val elementsByClass = Jsoup.parse(it).body().getElementById("newscontent").getElementsByTag("ul")[0].getElementsByTag("li")
                    val results = List(elementsByClass.size) {
                        val ahref = elementsByClass[it].child(1).select("a[href]")[0]
                        Book(Url(ahref.attr("href")), ahref.text(), elementsByClass[it].child(3).child(0).text())
                    }
                    results
                }
    }

    override fun loadBookDetailsAndChapter(book: Book): Observable<Book> {
        return service.loadHtmlForGBK(book.url.url).map {
            val parse = Jsoup.parse(it, book.url.url).body()
            book.bookCoverImg = Url(parse.getElementById("fmimg").select("a[href]")[0].attr("src"))
            book.intro = parse.getElementById("info").child(1).text()
            book.chapterList = parse.getElementById("list").select("a[href]").map { Chapter(it.text(), Url(it.attr("abs:href"))) }
            book.chapterListUrl = book.url
            book
        }
    }

    override fun loadBookChapter(chapter: Chapter): Observable<Chapter> {
        return service.loadHtmlForGBK(chapter.url.url).map {
            val parse = Jsoup.parse(it).getElementById("BookText")
            chapter.content = parse.html()
            chapter
        }
    }
}
