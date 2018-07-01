package com.chetdeva.githubit.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.chetdeva.githubit.Injection
import com.chetdeva.githubit.R
import com.chetdeva.githubit.api.Item
import com.chetdeva.githubit.data.NetworkState
import kotlinx.android.synthetic.main.activity_search_repositories.*

class SearchRepositoriesActivity : AppCompatActivity() {

    companion object {
        const val KEY_GITHUB_USER = "github_user"
        const val DEFAULT_USER = "Jake Wharton"
    }

    private lateinit var input: EditText
    private lateinit var list: RecyclerView
    private lateinit var model: SearchUsersViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_repositories)
        input = findViewById(R.id.input)
        list = findViewById(R.id.list)

        model = getViewModel()
        initAdapter()
        initSwipeToRefresh()
        initSearch()
        val subreddit = savedInstanceState?.getString(KEY_GITHUB_USER) ?: DEFAULT_USER
        model.showSearchResults(subreddit)
    }

    private fun getViewModel(): SearchUsersViewModel {
        val viewModelFactory = Injection.provideViewModelFactory(this)
        return ViewModelProviders.of(this, viewModelFactory)[SearchUsersViewModel::class.java]
    }

    private fun initAdapter() {
        val adapter = UsersAdapter()
        list.adapter = adapter
        model.posts.observe(this, Observer<PagedList<Item>> {
            adapter.submitList(it)
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh() {
        model.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            model.refresh()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_GITHUB_USER, model.currentSearchQuery())
    }

    private fun initSearch() {
        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updatedSubredditFromInput()
                true
            } else {
                false
            }
        }
        input.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updatedSubredditFromInput()
                true
            } else {
                false
            }
        }
    }

    private fun updatedSubredditFromInput() {
        input.text.trim().toString().let {
            if (it.isNotEmpty()) {
                if (model.showSearchResults(it)) {
                    list.scrollToPosition(0)
                    (list.adapter as? UsersAdapter)?.submitList(null)
                }
            }
        }
    }
}
