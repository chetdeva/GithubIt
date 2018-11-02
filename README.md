# GithubIt

A simple app that uses `PageKeyedDataSource` from Android Paging Library. It uses the [Github API](https://developer.github.com/v3/search/#search-users) for searching users.

<img src="./README_images/paging_with_network_screenshot.gif" width="300" height="534"/>

### PageKeyedDataSource

Use `GithubPageKeyedDataSource` to `loadInitial` and `loadAfter`.

```kotlin
/**
 * A data source that uses the before/after keys returned in page requests.
 */
class GithubPageKeyedDataSource : PageKeyedDataSource<Int, Item>() {

    override fun loadInitial(params: LoadInitialParams<Int>,
                             callback: LoadInitialCallback<Int, Item>) {

        val currentPage = 1
        val nextPage = currentPage + 1

        val request = githubApi.searchUsers(
                query = searchQuery,
                page = currentPage,
                perPage = params.requestedLoadSize)

        // Retrofit Call onResponse omitted
        
        callback.onResult(items, null, nextPage)
    }

    override fun loadAfter(params: LoadParams<Int>,
                           callback: LoadCallback<Int, Item>) {

        val currentPage = params.key
        val nextPage = currentPage + 1

        val request = githubApi.searchUsersAsync(
                query = searchQuery,
                page = currentPage,
                perPage = params.requestedLoadSize)

        // Retrofit Call onResponse omitted
        
        callback.onResult(items, nextPage)
    }
}
```

### DataSource.Factory

Create a `GithubDataSourceFactory`.

```kotlin
class GithubDataSourceFactory(
        private val searchQuery: String,
        private val githubApi: GithubApiService,
        private val retryExecutor: Executor
) : DataSource.Factory<Int, Item>() {

    val source = MutableLiveData<GithubPageKeyedDataSource>()

    override fun create(): DataSource<Int, Item> {
        val source = GithubPageKeyedDataSource(searchQuery, githubApi, retryExecutor)
        this.source.postValue(source)
        return source
    }
}
```

### Repository

Hook it up with your `InMemoryByPageKeyRepository`.

```kotlin
class InMemoryByPageKeyRepository(
        private val githubApi: GithubApiService,
        private val networkExecutor: Executor
) : GithubRepository {

    @MainThread
    override fun searchUsers(searchQuery: String, pageSize: Int): Listing<Item> {

        val factory = githubDataSourceFactory(searchQuery)

        val config = pagedListConfig(pageSize)

        val livePagedList = LivePagedListBuilder(factory, config)
                .setFetchExecutor(networkExecutor)
                .build()

        return Listing(
                pagedList = livePagedList,
                networkState = switchMap(factory.source) { it.network },
                retry = { factory.source.value?.retryAllFailed() },
                refresh = { factory.source.value?.invalidate() },
                refreshState = switchMap(factory.source) { it.initial })
    }

    private fun githubDataSourceFactory(searchQuery: String): GithubDataSourceFactory {
        return GithubDataSourceFactory(searchQuery, githubApi, networkExecutor)
    }

    private fun pagedListConfig(pageSize: Int): PagedList.Config {
        return PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(pageSize * 2)
                .setPageSize(pageSize)
                .build()
    }
}
```

### ViewModel

Call `searchUsers` from the `SearchUsersViewModel`:

```kotlin
class SearchUsersViewModel(
        private val repository: GithubRepository
) : ViewModel() {

    private val searchQuery = MutableLiveData<String>()
    private val itemResult = map(searchQuery) {
        repository.searchUsers(it, PAGE_SIZE)
    }
    val items = switchMap(itemResult) { it.pagedList }!!
    val networkState = switchMap(itemResult) { it.networkState }!!
    val refreshState = switchMap(itemResult) { it.refreshState }!!

    ...
}
```

Thanks for stopping by! :)

### References:

- [PagingWithNetworkSample](https://github.com/googlesamples/android-architecture-components/tree/master/PagingWithNetworkSample)
