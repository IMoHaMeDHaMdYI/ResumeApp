package mohamed.mohamedresume.retrofitrxcomponentarch.data.datasource

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import io.reactivex.disposables.CompositeDisposable
import mohamed.mohamedresume.models.Result
import mohamed.mohamedresume.retrofitrxcomponentarch.data.database.GitHubDbUser
import mohamed.mohamedresume.retrofitrxcomponentarch.data.database.UserDao

class UserDataSourceFactory(
        private val userDao: UserDao
        , private val compositeDisposable: CompositeDisposable
) : DataSource.Factory<Long, Result<GitHubDbUser>>() {
    val userDataSourceLiveData = MutableLiveData<UserDataSource>()
    override fun create(): DataSource<Long, Result<GitHubDbUser>> {
        val userDataSource = UserDataSource(userDao, compositeDisposable)
        userDataSourceLiveData.postValue(userDataSource)
        return userDataSource
    }
}