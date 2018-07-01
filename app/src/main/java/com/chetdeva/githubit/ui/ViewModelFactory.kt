package com.chetdeva.githubit.ui

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import com.chetdeva.githubit.data.GithubRepository

/**
 * Factory for ViewModels
 */
class ViewModelFactory(private val repository: GithubRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchUsersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SearchUsersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}