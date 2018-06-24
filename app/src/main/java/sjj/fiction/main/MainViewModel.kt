package sjj.fiction.main

import android.arch.lifecycle.ViewModel
import io.reactivex.BackpressureStrategy
import io.reactivex.Observable
import sjj.fiction.data.repository.fictionDataRepository
import sjj.fiction.model.Book
import sjj.fiction.model.BookSourceRecord
import sjj.fiction.model.Chapter

class MainViewModel : ViewModel() {
    val books = fictionDataRepository.getBooks().flatMap {list->
        Observable.fromIterable(list).flatMap { b ->
            getLatestChapter(b.url).map {
                b.chapterList = listOf(it)
                b
            }
        }.map { list }.toFlowable(BackpressureStrategy.LATEST)
    }

    fun delete(book: Book) {
        fictionDataRepository.deleteBook(book.name, book.author)
                .subscribe()
    }

    fun search(text: String) = fictionDataRepository.search(text)

    fun saveBookSourceRecord(books: List<Pair<BookSourceRecord, List<Book>>>) = fictionDataRepository.saveBookSourceRecord(books)

    private fun getLatestChapter(bookUrl: String): Observable<Chapter> {
        return fictionDataRepository.getLatestChapter(bookUrl)
    }

}