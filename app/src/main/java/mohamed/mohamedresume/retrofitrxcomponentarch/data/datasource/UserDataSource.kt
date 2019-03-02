package mohamed.mohamedresume.retrofitrxcomponentarch.data.datasource

import androidx.paging.ItemKeyedDataSource
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers
import mohamed.mohamedresume.models.Result
import mohamed.mohamedresume.retrofitrxcomponentarch.data.database.GitHubDbUser
import mohamed.mohamedresume.retrofitrxcomponentarch.data.database.UserDao
import mohamed.mohamedresume.extensions.get

class UserDataSource(
        private val userDao: UserDao
        , private val compositeDisposable: CompositeDisposable
) : ItemKeyedDataSource<Long, Result<GitHubDbUser>>() {

    override fun loadInitial(params: LoadInitialParams<Long>, callback: LoadInitialCallback<Result<GitHubDbUser>>) {
        val disposable = userDao.getUsers(0, params.requestedLoadSize.toLong()).get(callback,
                Action {
                    loadInitial(params, callback)
                })
        compositeDisposable.add(disposable)
    }

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Result<GitHubDbUser>>) {
        if (params.key == -1L) return
        val disposable = userDao.getUsers(params.key, params.requestedLoadSize.toLong()).get(callback,
                Action {
                    loadAfter(params, callback)
                })
        compositeDisposable.add(disposable)
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Result<GitHubDbUser>>) {
    }

    override fun getKey(item: Result<GitHubDbUser>): Long {
        item.data?.let {
            return it.id
        }
        return -1
    }

    fun Flowable<Array<GitHubDbUser>>.get(callback: LoadCallback<Result<GitHubDbUser>>, action: Action): Disposable {
        return this.get({
            it.let { userArray ->
                callback.onResult(Result.fromData(userArray))
                setRetry(null)
            }
        }) {
            //            callback.onResult(arrayListOf(Result.fromError(it)))
            setRetry(action)
        }
    }

    private fun setRetry(action: Action?) {
        if (action == null) {
            this.retryCompletable = null
        } else {
            this.retryCompletable = Completable.fromAction(action)
        }
    }

    private var retryCompletable: Completable? = null

    fun retry() {
        retryCompletable?.let {
            compositeDisposable.add(
                    it
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe()
            )
        }
    }
}