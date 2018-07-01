package com.chetdeva.githubit.data

import android.arch.lifecycle.Transformations
import android.arch.paging.LivePagedListBuilder
import android.support.annotation.MainThread
import com.chetdeva.githubit.api.GithubApi
import com.chetdeva.githubit.api.Item
import java.util.concurrent.Executor

/**
 * Repository implementation that returns a Listing that loads data directly from network by using
 * the previous / next page keys returned in the query.
 */
class InMemoryByPageKeyRepository(private val githubApi: GithubApi,
                                  private val networkExecutor: Executor) : GithubRepository {
    @MainThread
    override fun searchUsers(searchQuery: String, pageSize: Int): Listing<Item> {
        val sourceFactory = GithubDataSourceFactory(githubApi, searchQuery, networkExecutor)

        val livePagedList = LivePagedListBuilder(sourceFactory, pageSize)
                // provide custom executor for network requests, otherwise it will default to
                // Arch Components' IO pool which is also used for disk access
                .setFetchExecutor(networkExecutor)
                .build()

        val refreshState = Transformations.switchMap(sourceFactory.sourceLiveData) {
            it.initialLoad
        }
        return Listing(
                pagedList = livePagedList,
                networkState = Transformations.switchMap(sourceFactory.sourceLiveData) {
                    it.networkState
                },
                retry = {
                    sourceFactory.sourceLiveData.value?.retryAllFailed()
                },
                refresh = {
                    sourceFactory.sourceLiveData.value?.invalidate()
                },
                refreshState = refreshState
        )
    }
}

