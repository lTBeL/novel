package sjj.novel.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore

/**
 * Created by SJJ on 2017/10/14.
 */
@Entity(primaryKeys = ["bookName", "author"])
class BookSourceRecord {
    var bookName = ""
    var author = ""
    var bookUrl = ""
    /**
     * 当前阅读的当前索引
     */
    var readIndex: Int = 0
    /**
     * 当前阅读的章节名
     */
    var chapterName: String = ""
    /**
     * 是否已经读完
     */
    var isThrough: Boolean = false

    @Ignore
    var books:List<Book>?=null

    @Ignore
    var currentBook:Book?=null

    override fun toString(): String {
        return "BookSourceRecord(bookName='$bookName', author='$author', bookUrl='$bookUrl', readIndex=$readIndex)"
    }
}

