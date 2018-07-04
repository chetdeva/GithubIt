package com.chetdeva.githubit.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations.map
import android.arch.lifecycle.Transformations.switchMap
import android.arch.lifecycle.ViewModel
import com.chetdeva.githubit.data.GithubRepository

class SearchUsersViewModel(
        private val repository: GithubRepository
) : ViewModel() {

    private val searchQuery = MutableLiveData<String>()
    private val itemResult = map(searchQuery) {
        repository.searchUsers(it, PAGE_SIZE)
    }
    val items = switchMap(itemResult) { it.pagedList }!!
    val networkState = switchMap(itemResult) { it.networkState }!!
    val refreshState = switchMap(itemResult) { it.refreshState }!!

    fun refresh() {
        itemResult.value?.refresh?.invoke()
    }

    fun showSearchResults(searchQuery: String): Boolean {
        if (this.searchQuery.value == searchQuery) {
            return false
        }
        this.searchQuery.value = searchQuery
        return true
    }

    fun retry() {
        val listing = itemResult?.value
        listing?.retry?.invoke()
    }

    fun currentSearchQuery(): String? = searchQuery.value

    companion object {
        const val PAGE_SIZE: Int = 5
    }
}