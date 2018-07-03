package com.chetdeva.githubit.api

/**
 * @author chetansachdeva
 */

class GithubApiService(
        private val githubApi: GithubApi
) {

    fun searchUsersSync(query: String, page: Int, perPage: Int,
                        onPrepared: () -> Unit,
                        onSuccess: (UsersSearchResponse?) -> Unit,
                        onError: (String) -> Unit) {

        val request = githubApi.searchUsers(query, page, perPage)
        onPrepared()
        ApiRequestHelper.syncRequest(request, onSuccess, onError)
    }

    fun searchUsersAsync(query: String, page: Int, perPage: Int,
                         onPrepared: () -> Unit,
                         onSuccess: (UsersSearchResponse?) -> Unit,
                         onError: (String) -> Unit) {

        val request = githubApi.searchUsers(query, page, perPage)
        onPrepared()
        ApiRequestHelper.asyncRequest(request, onSuccess, onError)
    }
}
