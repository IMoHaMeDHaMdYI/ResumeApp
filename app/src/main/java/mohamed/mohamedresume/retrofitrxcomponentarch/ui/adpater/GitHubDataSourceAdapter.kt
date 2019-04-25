package mohamed.mohamedresume.retrofitrxcomponentarch.ui.adpater

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_user_source.view.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.intro.ui.adapters.BaseAdapter
import mohamed.mohamedresume.models.Displayable
import mohamed.mohamedresume.retrofitrxcomponentarch.models.DataSource

class GitHubDataSourceAdapter(
    private val context: Context,
    private val displayableList: ArrayList<DataSource>
) : BaseAdapter<GitHubDataSourceViewHolder, DataSource>(context, displayableList) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GitHubDataSourceViewHolder {
        val inflater = LayoutInflater.from(context)
        return GitHubDataSourceViewHolder(inflater.inflate(R.layout.item_user_source, parent, false))
    }

    override fun onBindViewHolder(holder: GitHubDataSourceViewHolder, position: Int) {
        if (displayableList.size == 0) return
        val data = displayableList[position]
        data.let {
            holder.apply {
                if (it.githubUserAdapter is GitHubUserAdapter) {
                    rvGitHubUser?.adapter = it.githubUserAdapter
                } else if (it.githubUserAdapter is UserAdapter) {
                    rvGitHubUser?.adapter = it.githubUserAdapter
                }
                rvGitHubUser?.layoutManager = LinearLayoutManager(context)
                tvSourceName?.text = it.source
            }
        }
    }
}

class GitHubDataSourceViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val rvGitHubUser = view.rvGitHubUsers ?: null
    val tvSourceName = view.tvSourceName ?: null
}