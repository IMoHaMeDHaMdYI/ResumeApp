package mohamed.mohamedresume.retrofitrxcomponentarch.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import mohamed.mohamedresume.extensions.SingletonHolder

@Database(entities = [GitHubDbUser::class], version = 1)
abstract class GitHubDataBase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object : SingletonHolder<GitHubDataBase, Context>({
        Room.databaseBuilder(
            it.applicationContext,
            GitHubDataBase::class.java, "github-user.db"
        ).build()
    })

}