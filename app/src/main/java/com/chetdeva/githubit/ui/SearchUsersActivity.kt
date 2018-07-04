package com.chetdeva.githubit.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.arch.paging.PagedList
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import com.chetdeva.githubit.Injection
import com.chetdeva.githubit.R
import com.chetdeva.githubit.api.Item
import com.chetdeva.githubit.data.NetworkState
import com.chetdeva.githubit.util.GlideApp
import kotlinx.android.synthetic.main.activity_search_repositories.*


class SearchUsersActivity : AppCompatActivity() {

    companion object {
        const val KEY_GITHUB_USER = "github_user"
        const val DEFAULT_USER = "google"
    }

    private lateinit var list: RecyclerView
    private lateinit var model: SearchUsersViewModel
    private val glideRequests by lazy { GlideApp.with(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_repositories)
        list = findViewById(R.id.list)

        model = viewModel()
        initAdapter()
        initSwipeToRefresh()
        val searchQuery = savedInstanceState?.getString(KEY_GITHUB_USER) ?: DEFAULT_USER
        model.showSearchResults(searchQuery)
    }

    private fun viewModel(): SearchUsersViewModel {
        val viewModelFactory = Injection.provideViewModelFactory()
        return ViewModelProviders.of(this, viewModelFactory)[SearchUsersViewModel::class.java]
    }

    private fun initAdapter() {
        val adapter = UsersAdapter(glideRequests) {
            model.retry()
        }
        list.adapter = adapter
        model.items.observe(this, Observer<PagedList<Item>> {
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

    /**
     * Search configuration
     */
    private var searchView: SearchView? = null

    private var onQueryTextListener: SearchView.OnQueryTextListener? = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            searchGithub(query)
            return true
        }

        override fun onQueryTextChange(newText: String): Boolean {
            // do nothing
            return true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        searchView = searchView(menu)
        searchView?.queryHint = getString(R.string.search)
        searchView?.setOnQueryTextListener(onQueryTextListener)
        return true
    }

    private fun searchView(menu: Menu?): SearchView? {
        val searchItem = menu?.findItem(R.id.action_search)
        return searchItem?.actionView as? SearchView
    }
    private fun hideKeyboard() {
        if (searchView?.hasFocus() == true) searchView?.clearFocus()
    }

    private fun searchGithub(searchQuery: String) {
        searchQuery.trim().let {
            if (it.isNotEmpty()) {
                if (model.showSearchResults(it)) {
                    list.scrollToPosition(0)
                    (list.adapter as? UsersAdapter)?.submitList(null)
                    hideKeyboard()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.action_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        onQueryTextListener = null
        super.onDestroy()
    }
}
