package mohamed.mohamedresume.retrofitrxcomponentarch.ui.adpater

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import mohamed.mohamedresume.R
import mohamed.mohamedresume.models.Result
import mohamed.mohamedresume.retrofitrxcomponentarch.data.database.GitHubDbUser
import mohamed.mohamedresume.retrofitrxcomponentarch.models.ISourceAdapter


class UserAdapter(private val context: Context) :
    PagedListAdapter<Result<GitHubDbUser>, GitHubUserViewHolder>(UserDiffCallback),
    ISourceAdapter {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GitHubUserViewHolder {
        return GitHubUserViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user, parent, false))
    }

    override fun onBindViewHolder(holder: GitHubUserViewHolder, position: Int) {
        val user = getItem(position)?.data
        user?.let {
            holder.bind(it.toGitHubUser())
        }
    }

    companion object {
        val UserDiffCallback = object : DiffUtil.ItemCallback<Result<GitHubDbUser>>() {
            override fun areItemsTheSame(oldItem: Result<GitHubDbUser>, newItem: Result<GitHubDbUser>): Boolean {
                oldItem.data?.let { old ->
                    newItem.data?.let { new ->
                        return old.login?.toLowerCase() == new.login?.toLowerCase()
                    }
                }
                return false
            }

            override fun areContentsTheSame(oldItem: Result<GitHubDbUser>, newItem: Result<GitHubDbUser>): Boolean {
                return oldItem == newItem
            }
        }
    }
}