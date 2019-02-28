package mohamed.mohamedresume.retrofitrxcomponentarch.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAllUsers(): LiveData<Array<GitHubDbUser>>

    @Query("SELECT * From user LIMIT :number OFFSET :from ")
    fun getUsers(from: Long, number: Long): Flowable<Array<GitHubDbUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: GitHubDbUser)

    @Query("SELECT * FROM user WHERE id = :id")
    fun getUser(id: Long): Maybe<GitHubDbUser>

    @Query("SELECT * FROM user WHERE login = :log")
    fun getUser(log: String): Maybe<GitHubDbUser>
}