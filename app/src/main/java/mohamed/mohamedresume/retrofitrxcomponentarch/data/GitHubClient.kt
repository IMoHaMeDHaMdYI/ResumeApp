package mohamed.mohamedresume.retrofitrxcomponentarch.data

import io.reactivex.Single
import mohamed.mohamedresume.hardcodeddata.githubUrl
import mohamed.mohamedresume.hardcodeddata.usersPath
import mohamed.mohamedresume.retrofitrxcomponentarch.models.GitHubUser
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubClient {

    @GET("$usersPath{username}")
    fun getUser(@Path("username") username: String): Single<GitHubUser>

    companion object {
        val githubClient: GitHubClient by lazy {
            Retrofit.Builder().baseUrl(githubUrl)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(OkHttpClient.Builder().retryOnConnectionFailure(true).build())
                    .build()
                    .create(GitHubClient::class.java)
        }
    }

}