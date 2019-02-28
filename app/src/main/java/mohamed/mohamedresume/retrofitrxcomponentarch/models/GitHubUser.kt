package mohamed.mohamedresume.retrofitrxcomponentarch.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class GitHubUser(
    @Expose
    @SerializedName("avatar_url")
    val avatarUrl: String?,
    @Expose
    val bio: String?,
    @Expose
    val login: String?,
    @Expose
    val name: String?,
    @Expose
    val message: String?,
    var saved: Boolean = false
)