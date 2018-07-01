package com.chetdeva.githubit.data

import com.chetdeva.githubit.api.Item

/**
 * @author chetansachdeva
 */

interface GithubRepository {
    fun searchUsers(searchQuery: String, pageSize: Int): Listing<Item>
}