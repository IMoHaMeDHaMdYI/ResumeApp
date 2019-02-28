package mohamed.mohamedresume.models

data class Result<T>(val data: T?, val error: Error?) {
    companion object {
        fun <T> fromData(data: T): Result<T> {
            return Result(data, null)
        }

        fun <T> fromData(data: Array<T>): ArrayList<Result<T>> {
            val l = ArrayList<Result<T>>()
            data.forEach { l.add(fromData(it)) }
            return l
        }

        fun <T> fromError(error: Error): Result<T> {
            return Result(null, error)
        }
    }

    fun isError() = error != null
    fun isSuccess() = data != null
}

//fun <T> Single<T>.toResult(): Single<Result<T>> {
//    return map { Result.fromData(it) }
//        .onErrorResumeNext { Single.just(Result.fromError(it)) }
//}

data class Error(val type: String, val throwable: Throwable)