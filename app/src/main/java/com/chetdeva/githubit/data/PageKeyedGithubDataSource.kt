package com.chetdeva.githubit.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PageKeyedDataSource
import com.chetdeva.githubit.api.GithubApi
import com.chetdeva.githubit.api.Item
import com.chetdeva.githubit.api.UsersSearchResponse
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * A data source that uses the before/after keys returned in page requests.
 * <p>
 * See ItemKeyedSubredditDataSource
 */
class PageKeyedGithubDataSource(
        private val githubApi: GithubApi,
        private val searchQuery: String,
        private val retryExecutor: Executor) : PageKeyedDataSource<String, Item>() {

    private val firstPage = 1
    // keep a function reference for the retry event
    private var retry: (() -> Any)? = null

    /**
     * There is no sync on the state because paging will always call loadInitial first then wait
     * for it to return some success value before calling loadAfter.
     */
    val networkState = MutableLiveData<NetworkState>()

    val initialLoad = MutableLiveData<NetworkState>()

    fun retryAllFailed() {
        val prevRetry = retry
        retry = null
        prevRetry?.let {
            retryExecutor.execute {
                it.invoke()
            }
        }
    }

    override fun loadBefore(
            params: LoadParams<String>,
            callback: LoadCallback<String, Item>) {
        // ignored, since we only ever append to our initial load
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Item>) {
        networkState.postValue(NetworkState.LOADING)
        githubApi.searchUsers(query = searchQuery,
                page = params.key,
                itemsPerPage = params.requestedLoadSize).enqueue(
                object : retrofit2.Callback<UsersSearchResponse> {
                    override fun onFailure(call: Call<UsersSearchResponse>, t: Throwable) {
                        retry = {
                            loadAfter(params, callback)
                        }
                        networkState.postValue(NetworkState.error(t.message ?: "unknown err"))
                    }

                    override fun onResponse(
                            call: Call<UsersSearchResponse>,
                            response: Response<UsersSearchResponse>) {
                        if (response.isSuccessful) {
                            val items = response.body()?.items!!
                            retry = null
                            callback.onResult(items, params.key)
                            networkState.postValue(NetworkState.LOADED)
                        } else {
                            retry = {
                                loadAfter(params, callback)
                            }
                            networkState.postValue(
                                    NetworkState.error("error code: ${response.code()}"))
                        }
                    }
                }
        )
    }

    override fun loadInitial(
            params: LoadInitialParams<String>,
            callback: LoadInitialCallback<String, Item>) {

        networkState.postValue(NetworkState.LOADING)
        githubApi.searchUsers(query = searchQuery,
                page = firstPage.toString(),
                itemsPerPage = params.requestedLoadSize).enqueue(
                object : retrofit2.Callback<UsersSearchResponse> {
                    override fun onFailure(call: Call<UsersSearchResponse>, t: Throwable) {
                        retry = {
                            loadInitial(params, callback)
                        }
                        networkState.postValue(NetworkState.error(t.message ?: "unknown err"))
                    }

                    override fun onResponse(
                            call: Call<UsersSearchResponse>,
                            response: Response<UsersSearchResponse>) {
                        if (response.isSuccessful) {
                            val items = response.body()?.items!!
                            retry = null
                            callback.onResult(items, firstPage.toString(), (firstPage + 1).toString())
                            networkState.postValue(NetworkState.LOADED)
                        } else {
                            retry = {
                                loadInitial(params, callback)
                            }
                            networkState.postValue(
                                    NetworkState.error("error code: ${response.code()}"))
                        }
                    }
                }
        )
    }
}