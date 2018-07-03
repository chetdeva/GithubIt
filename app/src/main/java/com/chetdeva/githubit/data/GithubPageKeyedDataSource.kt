package com.chetdeva.githubit.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import com.chetdeva.githubit.api.GithubApiService
import com.chetdeva.githubit.api.Item
import java.util.concurrent.Executor

/**
 *
 */
class GithubPageKeyedDataSource(
        private val searchQuery: String,
        private val apiService: GithubApiService,
        private val retryExecutor: Executor
) : PageKeyedDataSource<Int, Item>() {

    var retry: (() -> Any)? = null
    val network = MutableLiveData<NetworkState>()
    val initial = MutableLiveData<NetworkState>()

    override fun loadBefore(params: LoadParams<Int>,
                            callback: LoadCallback<Int, Item>) {
        // ignored, since we only ever append to our initial load
    }

    /**
     * load initial
     */
    override fun loadInitial(params: LoadInitialParams<Int>,
                             callback: LoadInitialCallback<Int, Item>) {

        val currentPage = 1
        val nextPage = currentPage + 1

        makeLoadInitialRequest(params, callback, currentPage, nextPage)
    }

    private fun makeLoadInitialRequest(params: LoadInitialParams<Int>,
                                       callback: LoadInitialCallback<Int, Item>,
                                       currentPage: Int,
                                       nextPage: Int) {

        // triggered by a refresh, we better execute sync
        apiService.searchUsersSync(
                query = searchQuery,
                page = currentPage,
                perPage = params.requestedLoadSize,
                onPrepared = {
                    postInitialState(NetworkState.LOADING)
                },
                onSuccess = { responseBody ->
                    val items = responseBody?.items ?: emptyList()
                    retry = null
                    postInitialState(NetworkState.LOADED)
                    callback.onResult(items, null, nextPage)
                },
                onError = { errorMessage ->
                    retry = { loadInitial(params, callback) }
                    postInitialState(NetworkState.error(errorMessage))
                })
    }

    /**
     * load after
     */
    override fun loadAfter(params: LoadParams<Int>,
                           callback: LoadCallback<Int, Item>) {

        val currentPage = params.key
        val nextPage = currentPage + 1

        makeLoadAfterRequest(params, callback, currentPage, nextPage)
    }

    private fun makeLoadAfterRequest(params: LoadParams<Int>,
                                     callback: LoadCallback<Int, Item>,
                                     currentPage: Int,
                                     nextPage: Int) {

        apiService.searchUsersAsync(
                query = searchQuery,
                page = currentPage,
                perPage = params.requestedLoadSize,
                onPrepared = {
                    postAfterState(NetworkState.LOADING)
                },
                onSuccess = { responseBody ->
                    val items = responseBody?.items ?: emptyList()
                    retry = null
                    callback.onResult(items, nextPage)
                    postAfterState(NetworkState.LOADED)
                },
                onError = { errorMessage ->
                    retry = { loadAfter(params, callback) }
                    postAfterState(NetworkState.error(errorMessage))
                })
    }

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let { retry ->
            retryExecutor.execute { retry() }
        }
    }

    private fun postInitialState(state: NetworkState) {
        network.postValue(state)
        initial.postValue(state)
    }

    private fun postAfterState(state: NetworkState) {
        network.postValue(state)
    }
}