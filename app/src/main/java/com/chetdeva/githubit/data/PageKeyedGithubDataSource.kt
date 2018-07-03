package com.chetdeva.githubit.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import com.chetdeva.githubit.api.GithubApi
import com.chetdeva.githubit.api.Item
import com.chetdeva.githubit.api.UsersSearchResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.Callback
import java.io.IOException
import java.util.concurrent.Executor

/**
 *
 */
class PageKeyedGithubDataSource(
        private val githubApi: GithubApi,
        private val searchQuery: String,
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
        val nextPage = computeNextPage(currentPage)

        val request = githubApi.searchUsers(searchQuery, currentPage, params.requestedLoadSize)

        makeLoadInitialRequest(params, callback, request, nextPage)
    }

    private fun makeLoadInitialRequest(params: LoadInitialParams<Int>,
                                       callback: LoadInitialCallback<Int, Item>,
                                       request: Call<UsersSearchResponse>,
                                       nextPage: Int) {

        postInitialState(NetworkState.LOADING)

        // triggered by a refresh, we better execute sync
        try {
            val response = request.execute()
            val items = response.body()?.items ?: emptyList()
            retry = null
            postInitialState(NetworkState.LOADED)
            callback.onResult(items, null, nextPage)
        } catch (exception: IOException) {
            retry = { loadInitial(params, callback) }
            postInitialState(NetworkState.error(exception.message ?: "unknown error"))
        }
    }

    /**
     * load after
     */
    override fun loadAfter(params: LoadParams<Int>,
                           callback: LoadCallback<Int, Item>) {

        val currentPage = params.key
        val nextPage = computeNextPage(params.key)

        val request = githubApi.searchUsers(searchQuery, currentPage, params.requestedLoadSize)

        makeLoadAfterRequest(params, callback, request, nextPage)
    }

    private fun makeLoadAfterRequest(params: LoadParams<Int>,
                                     callback: LoadCallback<Int, Item>,
                                     request: Call<UsersSearchResponse>,
                                     nextPage: Int) {

        postAfterState(NetworkState.LOADING)

        request.enqueue(object : Callback<UsersSearchResponse> {

            override fun onFailure(call: Call<UsersSearchResponse>, t: Throwable) {
                retry = { loadAfter(params, callback) }
                postAfterState(NetworkState.error(t.message ?: "unknown err"))
            }

            override fun onResponse(
                    call: Call<UsersSearchResponse>,
                    response: Response<UsersSearchResponse>) {
                if (response.isSuccessful) {
                    val items = response.body()?.items!!
                    retry = null
                    callback.onResult(items, nextPage)
                    postAfterState(NetworkState.LOADED)
                } else {
                    retry = { loadAfter(params, callback) }
                    postAfterState(NetworkState.error("error code: ${response.code()}"))
                }
            }
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

    private fun computeNextPage(page: Int): Int {
        return page + 1
    }
}