package com.chetdeva.githubit.data

import android.arch.lifecycle.Transformations.switchMap
import android.arch.paging.LivePagedListBuilder
import android.support.annotation.MainThread
import com.chetdeva.githubit.api.GithubApi
import com.chetdeva.githubit.api.Item
import java.util.concurrent.Executor

/**
 * Repository implementation that returns a [Listing] that loads data directly from network by using
 * the previous / next page keys returned in the query.
 */
class InMemoryByPageKeyRepository(
        private val githubApi: GithubApi,
        private val networkExecutor: Executor
) : GithubRepository {

    @MainThread
    override fun searchUsers(searchQuery: String, pageSize: Int): Listing<Item> {

        val sourceFactory = GithubDataSourceFactory(githubApi, searchQuery, networkExecutor)

        val livePagedList = LivePagedListBuilder(sourceFactory, pageSize)
                .setFetchExecutor(networkExecutor)
                .build()

        return Listing(
                pagedList = livePagedList,
                networkState = switchMap(sourceFactory.source) { it.network },
                retry = { sourceFactory.source.value?.retryAllFailed() },
                refresh = { sourceFactory.source.value?.invalidate() },
                refreshState = switchMap(sourceFactory.source) { it.initial })
    }
}

