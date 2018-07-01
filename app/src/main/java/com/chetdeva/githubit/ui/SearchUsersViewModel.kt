package com.chetdeva.githubit.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.Transformations.switchMap
import android.arch.lifecycle.ViewModel
import com.chetdeva.githubit.data.GithubRepository

/**
 * A RecyclerView ViewHolder that displays a single reddit post.
 */
class SearchUsersViewModel(private val repository: GithubRepository) : ViewModel() {

    private val searchQuery = MutableLiveData<String>()
    private val repoResult = map(searchQuery) {
        repository.searchUsers(it, 10)
    }
    val posts = switchMap(repoResult) { it.pagedList }!!
    val networkState = switchMap(repoResult) { it.networkState }!!
    val refreshState = switchMap(repoResult) { it.refreshState }!!

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun showSearchResults(searchQuery: String): Boolean {
        if (this.searchQuery.value == searchQuery) {
            return false
        }
        this.searchQuery.value = searchQuery
        return true
    }

    fun retry() {
        val listing = repoResult?.value
        listing?.retry?.invoke()
    }

    fun currentSearchQuery(): String? = searchQuery.value
}