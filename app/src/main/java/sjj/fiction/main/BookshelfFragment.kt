package sjj.fiction.main

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_books.*
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity
import sjj.fiction.BaseFragment
import sjj.fiction.R
import sjj.fiction.details.DetailsActivity
import sjj.fiction.model.Book
import sjj.fiction.util.domain
import sjj.fiction.util.getModel

/**
 * Created by SJJ on 2017/10/7.
 */
class BookshelfFragment : BaseFragment() {

    private val model by lazy { getModel<MainViewModel>() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bookList.layoutManager = LinearLayoutManager(context)
        val adapter = Adapter()
        bookList.adapter = adapter
        model.books.observeOn(AndroidSchedulers.mainThread()).subscribe {
            adapter.data = it
            adapter.notifyDataSetChanged()
        }.destroy()
    }

    private inner class Adapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var data: List<Book>? = null
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_book_list, parent, false)) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val book = data!![position]
            holder.itemView.find<TextView>(R.id.bookName).text = book.name
            holder.itemView.find<TextView>(R.id.author).text = book.author
            holder.itemView.find<TextView>(R.id.originWebsite).text = book.url.domain()
            holder.itemView.find<TextView>(R.id.lastChapter).text = book.chapterList.last().chapterName
            holder.itemView.find<SimpleDraweeView>(R.id.bookCover).setImageURI(book.bookCoverImgUrl)
            holder.itemView.setOnClickListener { v ->
                startActivity<DetailsActivity>(DetailsActivity.BOOK_NAME to book.name,DetailsActivity.BOOK_AUTHOR to book.author)
            }
            holder.itemView.setOnLongClickListener {
                alert {
                    title = "确认删除？"
                    message = "确认删除书籍：${book.name}？"
                    negativeButton("取消", {})
                    positiveButton("删除") {
                        model.delete(book)
                    }
                }.show()
                true
            }
        }

        override fun getItemCount(): Int = data?.size ?: 0
    }
}