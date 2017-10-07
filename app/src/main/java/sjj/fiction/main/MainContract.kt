package sjj.fiction.main

import android.support.v4.app.FragmentManager
import android.view.View
import sjj.fiction.BasePresenter
import sjj.fiction.BaseView

/**
 * Created by SJJ on 2017/10/7.
 */
interface MainContract {
    interface presenter : BasePresenter {
    }

    interface view : BaseView<presenter> {
    }
}