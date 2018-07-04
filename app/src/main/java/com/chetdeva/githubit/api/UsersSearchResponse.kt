package com.chetdeva.githubit.api

import com.google.gson.annotations.SerializedName

/**
 * @author chetansachdeva
 */

class UsersSearchResponse {
    @SerializedName("total_count")
    var totalCount: Int? = null
    @SerializedName("incomplete_results")
    var incompleteResults: Boolean? = null
    @SerializedName("items")
    var items: List<Item>? = null
}