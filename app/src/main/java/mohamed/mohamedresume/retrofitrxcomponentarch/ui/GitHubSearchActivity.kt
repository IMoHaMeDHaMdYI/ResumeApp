package mohamed.mohamedresume.retrofitrxcomponentarch.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_git_hub_search.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.hardcodeddata.newUserAdded
import mohamed.mohamedresume.models.Result
import mohamed.mohamedresume.retrofitrxcomponentarch.data.database.GitHubDbUser
import mohamed.mohamedresume.retrofitrxcomponentarch.models.DataSource
import mohamed.mohamedresume.retrofitrxcomponentarch.ui.adpater.GitHubDataSourceAdapter
import mohamed.mohamedresume.retrofitrxcomponentarch.ui.adpater.GitHubUserAdapter
import mohamed.mohamedresume.retrofitrxcomponentarch.ui.adpater.UserAdapter
import mohamed.mohamedresume.retrofitrxcomponentarch.viewmodels.GitHubViewModel
import mohamed.mohamedresume.extensions.onQuerySubmitted

class GitHubSearchActivity : AppCompatActivity() {

    private lateinit var viewModel: GitHubViewModel
    private val TAG = this::class.java.simpleName
    private lateinit var userAdapter: UserAdapter
    private lateinit var parentAdapter: GitHubDataSourceAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_git_hub_search)
        viewModel = ViewModelProviders.of(this).get(GitHubViewModel::class.java)
        val githubUserAdapter = GitHubUserAdapter(this, ArrayList()) {
            viewModel.insertUser(it)
        }
        userAdapter = UserAdapter(this)
        parentAdapter = GitHubDataSourceAdapter(
            this, arrayListOf(
                DataSource("Search", githubUserAdapter),
                DataSource("Saved", userAdapter)
            )
        )
        initiateAdapter()
        viewModel.userLiveData.observe(this, Observer {
            it?.let { result ->
                githubUserAdapter.setLoading(false)

                result.data?.let { user ->
                    Log.d(TAG, "$user")
                    githubUserAdapter.replace(user, 0)
                }
                result.error?.let { error ->
                    Log.d(TAG, "$error")
                }
            }

        })

        viewModel.searchUser.observe(this, Observer {
            Log.d(TAG, "in Observe Search")
            if (it == newUserAdded)
                viewModel.invalidatePagedList()
            githubUserAdapter.notifyItemChanged(0)
        })
        searchView.onQuerySubmitted {
            viewModel.getUser(it.toLowerCase())
            githubUserAdapter.clear()
            githubUserAdapter.setLoading(true)
        }
    }


    private fun initiateAdapter() {
        rvGitHubUsers.layoutManager = LinearLayoutManager(this)
        rvGitHubUsers.adapter = parentAdapter
        viewModel.userList.observe(this, Observer<PagedList<Result<GitHubDbUser>>> {
            Log.d(TAG, "List submitted ")
            userAdapter.submitList(it)
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("one", "one")
    }
}