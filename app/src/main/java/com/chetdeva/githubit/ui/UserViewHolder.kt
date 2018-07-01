package com.chetdeva.githubit.ui

import android.content.Intent
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.chetdeva.githubit.R
import com.chetdeva.githubit.api.Item

/**
 * View Holder for a [Item] RecyclerView list item.
 */
class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val name: TextView = view.findViewById(R.id.name)

    private var repo: Item? = null

    init {
        view.setOnClickListener {
            repo?.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }
        }
    }

    fun bind(repo: Item?) {
        if (repo == null) {
            val resources = itemView.resources
            name.text = resources.getString(R.string.loading)
        } else {
            showItemData(repo)
        }
    }

    private fun showItemData(repo: Item) {
        this.repo = repo
        name.text = repo.login
    }

    companion object {
        fun create(parent: ViewGroup): UserViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.repo_view_item, parent, false)
            return UserViewHolder(view)
        }
    }
}