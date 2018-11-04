package com.demo.ike.doordashproject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Retrofit
import java.util.*

private const val DOORDASH_LAT = 37.422740
private const val DOORDASH_LNG = -122.139956

class MainActivityPresenter(private val retrofit: Retrofit) :
    LifecycleHandler<MainActivityViewHolder> {
    private var isViewInitialized = false
    private var viewHolder: MainActivityViewHolder? = null
    private var disposable: Disposable? = null
    private var listItems: List<Restaurant> = Collections.emptyList()

    override fun onViewAttached(view: MainActivityViewHolder) {
        this.viewHolder = view
        isViewInitialized = false
    }

    private fun fetchData() {
        disposable = retrofit.create(RestaurantListService::class.java)
            .getRestaurants(DOORDASH_LAT, DOORDASH_LNG).subscribeOn(
                Schedulers.io()
            ).observeOn(AndroidSchedulers.mainThread()).subscribe({ result ->
                                                                      listItems = result
                                                                      viewHolder?.updateList(
                                                                          listItems = listItems,
                                                                          onPullToRefresh = ::fetchData
                                                                      )
                                                                  }, { error ->
                                                                      viewHolder?.showError(
                                                                          message = error.message,
                                                                          onUserPressedRetry = ::fetchData
                                                                      )
                                                                  })
    }

    override fun onResume() {
        if (!isViewInitialized) {
            isViewInitialized = true
            fetchData()
        }
    }

    override fun onPause() {
        disposable?.let {
            if (!it.isDisposed) {
                it.dispose()
                isViewInitialized = false
            }
        }
    }

    override fun onDestroyView() {
        viewHolder = null
    }

    override fun onDestroy() {
    }
}