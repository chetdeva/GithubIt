package com.chetdeva.githubit.ui

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.chetdeva.githubit.R
import com.chetdeva.githubit.api.Item

/**
 * A RecyclerView ViewHolder that displays a reddit post.
 */
class RedditPostViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val title: TextView = view.findViewById(R.id.title)
    private val subtitle: TextView = view.findViewById(R.id.subtitle)
    private val score: TextView = view.findViewById(R.id.score)
    private val thumbnail: ImageView = view.findViewById(R.id.thumbnail)
    private var post: Item? = null

    init {
        view.setOnClickListener {
            post?.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }
        }
    }

    fun bind(item: Item?) {
        this.post = item
        title.text = item?.login ?: "loading"
//        subtitle.text = itemView.context.resources.getString(R.string.post_subtitle,
//                item?.author ?: "unknown")
//        score.text = "${item?.score ?: 0}"
//        if (item?.avatarUrl?.startsWith("http") == true) {
//            thumbnail.visibility = View.VISIBLE
//            glide.load(item.avatarUrl)
//                    .centerCrop()
//                    .placeholder(R.drawable.ic_insert_photo_black_48dp)
//                    .into(thumbnail)
//        } else {
//            thumbnail.visibility = View.GONE
//            glide.clear(thumbnail)
//        }
    }

    companion object {
        fun create(parent: ViewGroup): RedditPostViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.reddit_post_item, parent, false)
            return RedditPostViewHolder(view)
        }
    }
}