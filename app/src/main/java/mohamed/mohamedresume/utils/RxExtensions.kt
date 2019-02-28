package mohamed.mohamedresume.utils

import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import mohamed.mohamedresume.hardcodeddata.*
import mohamed.mohamedresume.models.Error
import mohamed.mohamedresume.models.Result
import retrofit2.HttpException
import java.net.SocketTimeoutException

fun <T> Single<T>.get(onComplete: (T?) -> Unit = {}, onError: (Error) -> Unit = {}): Disposable {
    return this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            onComplete(it)
        }) { throwable ->
            handleError(throwable, onError)
        }
}


fun <T> Observable<T>.get(
    onNext: (t: T) -> Unit, onComplete: () -> Unit = {}
    , onError: (Error) -> Unit = {}
): Disposable {
    return this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            onNext(it)
        }, { throwable ->
            handleError(throwable, onError)
        }, {
            onComplete()
        })
}

fun <T> Maybe<T>.get(
    onSuccess: (t: T) -> Unit, onComplete: () -> Unit = {}
    , onError: (Error) -> Unit = {}
): Disposable {
    return this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            onSuccess(it)
        }, { throwable ->
            handleError(throwable, onError)
        }, {
            onComplete()
        })
}

fun <T> Flowable<T>.get(
    onSuccess: (t: T) -> Unit, onComplete: () -> Unit = {}
    , onError: (Error) -> Unit = {}
): Disposable {
    return this.subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
            onSuccess(it)
        }, { throwable ->
            handleError(throwable, onError)
        }, {
            onComplete()
        })
}

fun handleError(throwable: Throwable?, onError: (Error) -> Unit) {

    throwable?.let { nonNullThrowable ->
        val type: String = when (throwable) {
            is SocketTimeoutException -> {
                socketTimeoutException
            }
            is HttpException -> {
                val code = (nonNullThrowable as HttpException).code()
                when (code) {
                    404 -> httpNotFountException
                    500 -> httpServerException
                    else -> httpUnknownException
                }
            }
            else -> {
                unKnownException
            }
        }
        onError(Error(type, nonNullThrowable))
    }
    if (throwable == null)
        onError(mohamed.mohamedresume.models.Error(unKnownException, Throwable("")))
}

//
//fun <T> Single<Result<T>>.onlySuccess(): Maybe<T> {
//    return filter { it.isSuccess() }
//        .map { it.data!! }
//}
//
//fun <T> Single<Result<T>>.onlyError(): Maybe<Throwable> {
//    return filter { it.isError() }
//        .map { it.error!! }
//}
//
//fun <T> Single<Result<T>>.onlyHttpException(): Maybe<HttpException> {
//    return filter { it.isError() && it.error is HttpException }
//        .map { it.error as HttpException }
//}
//
//fun <T> Single<Result<T>>.onlyHttpException(code: Int): Maybe<HttpException> {
//    return onlyHttpException()
//        .filter { it.code() == code }
//}
//
//fun <T> Single<Result<T>>.onlyHttpExceptionExcluding(vararg codes: Int): Maybe<HttpException> {
//    return onlyHttpException()
//        .filter { codes.contains(it.code()) }
//}
//
//fun <T> Single<Result<T>>.onlySocketTimeoutE(): Maybe<SocketTimeoutException> {
//    return filter { it.isError() && it.error is SocketTimeoutException }
//        .map { it.error as SocketTimeoutException }
//}

fun <T> Observable<T>.toResult(): Observable<Result<T>> {
    return map { Result.fromData(it) }
        .onErrorResumeNext(
            Function { throwable ->
                var error: Error? = null
                throwable.let { nonNullThrowable ->
                    val type: String = when (throwable) {
                        is SocketTimeoutException -> {
                            socketTimeoutException
                        }
                        is HttpException -> {
                            val code = (nonNullThrowable as HttpException).code()
                            when (code) {
                                404 -> httpNotFountException
                                500 -> httpServerException
                                else -> httpUnknownException
                            }
                        }
                        else -> {
                            unKnownException
                        }
                    }
                    error = Error(type, nonNullThrowable)
                }
                if (error == null)
                    error = Error(unKnownException, throwable)
                Observable.just(Result.fromError(error!!))
            }
        )
}