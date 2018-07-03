package com.chetdeva.githubit.api

import com.google.gson.annotations.SerializedName

class Item {
    var login: String? = null
    var id: Int? = null
    @SerializedName("node_id")
    var nodeId: String? = null
    @SerializedName("avatar_url")
    var avatarUrl: String? = null
    @SerializedName("gravatar_id")
    var gravatarId: String? = null
    var url: String? = null
    @SerializedName("html_url")
    var htmlUrl: String? = null
    @SerializedName("followers_url")
    var followersUrl: String? = null
    @SerializedName("subscriptions_url")
    var subscriptionsUrl: String? = null
    @SerializedName("organizations_url")
    var organizationsUrl: String? = null
    @SerializedName("repos_url")
    var reposUrl: String? = null
    @SerializedName("received_events_url")
    var receivedEventsUrl: String? = null
    var type: String? = null
    var score: Float? = null
}