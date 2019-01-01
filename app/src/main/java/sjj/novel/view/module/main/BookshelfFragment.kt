package sjj.novel.view.module.main

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_books.*
import kotlinx.android.synthetic.main.item_book_list.view.*
import org.jetbrains.anko.support.v4.alert
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast
import sjj.novel.BaseFragment
import sjj.novel.DISPOSABLE_ACTIVITY_MAIN_REFRESH
import sjj.novel.R
import sjj.novel.databinding.ItemBookListBinding
import sjj.novel.util.getModel
import sjj.novel.view.adapter.BaseAdapter
import sjj.novel.view.fragment.ChooseBookSourceFragment
import sjj.novel.view.module.details.DetailsActivity
import sjj.novel.view.module.read.ReadActivity

/**
 * Created by SJJ on 2017/10/7.
 */
class BookshelfFragment : BaseFragment() {

    private lateinit var model: BookShelfViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_books, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        model = getModel()
        bookList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        val adapter = Adapter()
        adapter.setHasStableIds(true)
        bookList.adapter = adapter
        model.books.observeOn(AndroidSchedulers.mainThread()).subscribe {
            adapter.data = it
            adapter.notifyDataSetChanged()
        }.destroy()
        bookListRefreshLayout.setOnRefreshListener {
            model.refresh()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnError {
                        toast("$it")
                    }.doOnTerminate {
                        bookListRefreshLayout.isRefreshing = false
                    }.subscribe()
                    .destroy(DISPOSABLE_ACTIVITY_MAIN_REFRESH)
        }

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.fragment_book_shelf, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.search_book_shelf -> {
                findNavController(this).navigate(R.id.searchFragment)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private inner class Adapter : BaseAdapter() {
        var data: List<BookShelfViewModel.BookShelfItemViewModel>? = null

        override fun itemLayoutRes(viewType: Int): Int {
            return R.layout.item_book_list
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val bind = DataBindingUtil.bind<ItemBookListBinding>(holder.itemView)
            val viewModel = data!!.get(position)
            bind!!.model = viewModel
            holder.itemView.setOnClickListener { v ->

                if (viewModel.isThrough && viewModel.index <= ((viewModel.book.lastChapter?.index
                                ?: 0) - 1)) {
                    //有更新点击阅读直接进入下一章
                    model.setReadIndex(viewModel.book.lastChapter!!, viewModel.book).observeOn(AndroidSchedulers.mainThread()).subscribe {
                        startActivity<ReadActivity>(ReadActivity.BOOK_NAME to viewModel.book.name, ReadActivity.BOOK_AUTHOR to viewModel.book.author)
                    }.destroy("read book")

                } else {
                    startActivity<ReadActivity>(ReadActivity.BOOK_NAME to viewModel.book.name, ReadActivity.BOOK_AUTHOR to viewModel.book.author)
                }
            }
            holder.itemView.setOnLongClickListener { _ ->
                alert {
                    title = "确认删除？"
                    message = "确认删除书籍：${viewModel.bookName.get()}？"
                    negativeButton("取消") {}
                    positiveButton("删除") {
                        model.delete(viewModel.book)
                    }
                }.show()
                true
            }
            holder.itemView.intro.setOnClickListener {
                startActivity<DetailsActivity>(DetailsActivity.BOOK_NAME to viewModel.book.name, DetailsActivity.BOOK_AUTHOR to viewModel.book.author)
            }
            holder.itemView.origin.setOnClickListener {
                ChooseBookSourceFragment.newInstance(viewModel.book.name, viewModel.book.author).show(fragmentManager)
            }
        }

        override fun getItemCount(): Int = data?.size ?: 0

        override fun getItemId(position: Int): Long {
            val viewModel = data?.get(position) ?: return 0
            return viewModel.id
        }
    }
}