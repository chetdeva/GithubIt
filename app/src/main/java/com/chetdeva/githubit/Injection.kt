package com.chetdeva.githubit

import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import com.chetdeva.githubit.api.GithubApi
import com.chetdeva.githubit.api.GithubApiService
import com.chetdeva.githubit.data.GithubRepository
import com.chetdeva.githubit.data.InMemoryByPageKeyRepository
import com.chetdeva.githubit.ui.ViewModelFactory
import java.util.concurrent.Executors

/**
 * Class that handles object creation.
 * Like this, objects can be passed as parameters in the constructors and then replaced for
 * testing, where needed.
 */
object Injection {

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    /**
     * Creates an instance of [GithubRepository] based on the [GithubApiService]
     */
    private fun provideGithubRepository(): GithubRepository {
        return InMemoryByPageKeyRepository(provideGithubApiService(), NETWORK_IO)
    }

    /**
     * Creates an instance of [GithubApiService] based on the [GithubApi]
     */
    private fun provideGithubApiService(): GithubApiService {
        return GithubApiService(GithubApi.create())
    }

    /**
     * Provides the [ViewModelProvider.Factory] that is then used to get a reference to
     * [ViewModel] objects.
     */
    fun provideViewModelFactory(): ViewModelProvider.Factory {
        return ViewModelFactory(provideGithubRepository())
    }
}