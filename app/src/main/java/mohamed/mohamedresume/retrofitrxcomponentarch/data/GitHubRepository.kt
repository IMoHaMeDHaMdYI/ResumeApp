package mohamed.mohamedresume.retrofitrxcomponentarch.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import mohamed.mohamedresume.hardcodeddata.err
import mohamed.mohamedresume.hardcodeddata.newUserAdded
import mohamed.mohamedresume.hardcodeddata.unKnownException
import mohamed.mohamedresume.hardcodeddata.userExists
import mohamed.mohamedresume.models.Error
import mohamed.mohamedresume.models.Result
import mohamed.mohamedresume.retrofitrxcomponentarch.data.database.GitHubDataBase
import mohamed.mohamedresume.retrofitrxcomponentarch.data.database.GitHubDbUser
import mohamed.mohamedresume.retrofitrxcomponentarch.models.GitHubUser
import mohamed.mohamedresume.extensions.get

class GitHubRepository(
        private val githubClient: GitHubClient = GitHubClient.githubClient,
        db: GitHubDataBase) {

    private val userDao = db.userDao()
    private val compositeDisposable = CompositeDisposable()
    val TAG = this::class.java.simpleName

    fun getUser(userName: String): LiveData<Result<GitHubUser>> {
        val userLiveData = MutableLiveData<Result<GitHubUser>>()
        val disposable = getUserFromDb(userName, {
            userLiveData.value = Result.fromData(it)
        }, {
            Log.d(TAG, "started new request")
            val user = githubClient.getUser(userName)
            val disposable = user.get({
                it?.let { nonNullUser ->
                    userLiveData.value = Result.fromData(nonNullUser)
                }
                if (it == null)
                    userLiveData.value = Result.fromError(Error(unKnownException, Throwable("unKnown")))

            }) { error ->
                userLiveData.value = Result.fromError(error)
            }
            compositeDisposable.add(disposable)
        }, {
            userLiveData.value = Result.fromError(it)
        })
        compositeDisposable.add(disposable)
        return userLiveData
    }


    fun getUserFromDb(
            id: Long
            , userExists: (user: GitHubUser) -> Unit
            , userAdded: () -> Unit
            , errorOccurred: (t: Error) -> Unit
    ): Disposable {
        return userDao.getUser(id).get({
            userExists(it.toGitHubUser())
        }, {
            userAdded()
        }, {
            errorOccurred(it)
        })
    }

    fun getUserFromDb(
            log: String
            , userExists: (user: GitHubUser) -> Unit
            , userAdded: () -> Unit
            , errorOccurred: (t: Error) -> Unit
    ): Disposable {
        return userDao.getUser(log).get({
            userExists(it.toGitHubUser())
        }, {
            userAdded()
        }, {
            errorOccurred(it)
        })
    }

    fun insertUser(user: GitHubUser): MutableLiveData<String> {
        Log.d(TAG, "in the insertUser method")
        val stateLiveData = MutableLiveData<String>()
        val disposable = getUserFromDb(user.login ?: "", {
            Log.d(TAG, "User Exists $it")
            stateLiveData.value = userExists
        }, {
            // If the use not exist insert new one
            val disposable = Completable.fromAction {
                userDao.insertUser(GitHubDbUser.fromGitHubUser(user))
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe()
            stateLiveData.value = newUserAdded
            compositeDisposable.add(disposable)

        }, {
            stateLiveData.value = err
            Log.d(TAG, "Failed baby failed $it")
        })
        compositeDisposable.add(disposable)
        return stateLiveData
    }


    fun onClear() {
        compositeDisposable.clear()
    }

}