package mohamed.mohamedresume.retrofitrxcomponentarch.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import mohamed.mohamedresume.retrofitrxcomponentarch.models.GitHubUser

@Entity(tableName = "user")
data class GitHubDbUser(
        @ColumnInfo(name = "avatar_url")
        val avatarUrl: String?,

        val login: String?,

        val name: String?,

        var saved: Boolean = false,

        @PrimaryKey(autoGenerate = true)
        val id: Long = 0) {

    fun toGitHubUser(): GitHubUser {
        return GitHubUser(
                avatarUrl = avatarUrl,
                bio = null,
                login = login,
                name = name,
                message = null,
                saved = true)
    }

    companion object {
        fun fromGitHubUser(user: GitHubUser): GitHubDbUser {
            return GitHubDbUser(user.avatarUrl, user.login, user.name, true)
        }

        fun isSameUser(u1: GitHubDbUser, u2: GitHubUser): Boolean {
            return u1.login == u2.login
        }

        fun isSameUser(u1: GitHubDbUser, u2: GitHubDbUser): Boolean {
            return u1.login == u2.login
        }
    }

}