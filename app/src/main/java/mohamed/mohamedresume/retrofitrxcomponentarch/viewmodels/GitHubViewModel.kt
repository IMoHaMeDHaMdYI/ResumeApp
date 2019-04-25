package mohamed.mohamedresume.retrofitrxcomponentarch.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.paging.ItemKeyedDataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import io.reactivex.disposables.CompositeDisposable
import mohamed.mohamedresume.models.Result
import mohamed.mohamedresume.retrofitrxcomponentarch.data.GitHubClient
import mohamed.mohamedresume.retrofitrxcomponentarch.data.GitHubRepository
import mohamed.mohamedresume.retrofitrxcomponentarch.data.database.GitHubDataBase
import mohamed.mohamedresume.retrofitrxcomponentarch.data.database.GitHubDbUser
import mohamed.mohamedresume.retrofitrxcomponentarch.data.datasource.UserDataSourceFactory
import mohamed.mohamedresume.retrofitrxcomponentarch.models.GitHubUser

class GitHubViewModel(application: Application) : AndroidViewModel(application) {
    private val db = GitHubDataBase.getInstance(application)
    private val repository = GitHubRepository(GitHubClient.githubClient, db)
    private val compositeDisposable = CompositeDisposable()
    private val _user = MutableLiveData<String>()
    private val sourceFactory: UserDataSourceFactory

    var userList: LiveData<PagedList<Result<GitHubDbUser>>>

    init {
        sourceFactory = UserDataSourceFactory(db.userDao(), compositeDisposable)
        val config = PagedList.Config.Builder()
            .setPageSize(5)
            .setInitialLoadSizeHint(5)
            .setEnablePlaceholders(false)
            .build()
        userList = LivePagedListBuilder<Long, Result<GitHubDbUser>>(sourceFactory, config)
            .build()
    }

    fun invalidatePagedList() = sourceFactory.userDataSourceLiveData.value?.invalidate()

    val userLiveData = Transformations.switchMap(_user) {
        repository.getUser(it)
    }

    fun getUser(userName: String) {
        _user.value = userName
    }

    private val _searchUser = MutableLiveData<GitHubUser>()
    val searchUser = Transformations.switchMap(_searchUser) {
        repository.insertUser(it)
    }

    fun insertUser(user: GitHubUser) {
        _searchUser.value = user
    }

    fun refresh(key: Long): LiveData<Result<GitHubDbUser>> {
        val liveData = MutableLiveData<Result<GitHubDbUser>>()
        sourceFactory.userDataSourceLiveData.value?.loadAfter(ItemKeyedDataSource.LoadParams(key, 1), object :
            ItemKeyedDataSource.LoadCallback<Result<GitHubDbUser>>() {
            override fun onResult(data: MutableList<Result<GitHubDbUser>>) {
                liveData.postValue(data[0])
            }
        })
        return liveData
    }

    override fun onCleared() {
        super.onCleared()
        repository.onClear()
        compositeDisposable.clear()
    }


}