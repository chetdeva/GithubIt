package com.chetdeva.githubit.data

import android.arch.lifecycle.MutableLiveData
import android.arch.paging.DataSource
import com.chetdeva.githubit.api.GithubApiService
import com.chetdeva.githubit.api.Item
import java.util.concurrent.Executor

/**
 * A simple data source factory which also provides a way to observe the last created data source.
 * This allows us to channel its network request status etc back to the UI. See the Listing creation
 * in the Repository class.
 */
class GithubDataSourceFactory(
        private val searchQuery: String,
        private val githubApi: GithubApiService,
        private val retryExecutor: Executor
) : DataSource.Factory<Int, Item>() {

    val source = MutableLiveData<GithubPageKeyedDataSource>()

    override fun create(): DataSource<Int, Item> {
        val source = GithubPageKeyedDataSource(searchQuery, githubApi, retryExecutor)
        this.source.postValue(source)
        return source
    }
}
