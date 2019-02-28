package mohamed.mohamedresume.retrofitrxcomponentarch.ui.adpater

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_user.view.*
import mohamed.mohamedresume.R
import mohamed.mohamedresume.intro.ui.adapters.BaseAdapter
import mohamed.mohamedresume.retrofitrxcomponentarch.models.GitHubUser
import mohamed.mohamedresume.retrofitrxcomponentarch.models.ISourceAdapter
import mohamed.mohamedresume.utils.GlideApp

class GitHubUserAdapter(
    private val context: Context, private val userList: ArrayList<GitHubUser>,
    private val onAddClicked: (user: GitHubUser) -> Unit = {}
) :
    BaseAdapter<GitHubUserViewHolder, GitHubUser>(context, userList), ISourceAdapter {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GitHubUserViewHolder {
        return when (viewType) {
            HOLDER -> {
                Log.d(this::class.java.simpleName, "holder")
                GitHubUserViewHolder(LayoutInflater.from(context).inflate(R.layout.item_user, parent, false))
            }
            else -> {
                Log.d(this::class.java.simpleName, "Loading holder")
                GitHubUserViewHolder(LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: GitHubUserViewHolder, position: Int) {
        if (userList.size == 0) return
        val user = userList[position]
        holder.bind(user,onAddClicked)
//        holder.apply {
//            tvName?.text = user.name ?: user.login ?: "UnKnown !"
//            GlideApp.with(context)
//                .load(user.avatarUrl)
//                .circleCrop()
//                .into(imgAvatar!!)
//            motionLayout?.transitionToEnd()
//            imgAdd?.apply {
//                if (user.saved) {
//                    visibility = View.INVISIBLE
//                }
//                setOnClickListener {
//                    onAddClicked(user)
//                }
//            }
//        }
    }
}

class GitHubUserViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val imgAvatar = view.imgAvatar ?: null
    val tvName = view.tvName ?: null
    val motionLayout = view.motionLayout ?: null
    val imgAdd = view.imgAdd ?: null
    fun bind(user: GitHubUser, onAddClicked: (user: GitHubUser) -> Unit = {}) {
        tvName?.text = user.name ?: user.login ?: "UnKnown !"
        GlideApp.with(view.context)
            .load(user.avatarUrl)
            .circleCrop()
            .into(imgAvatar!!)
        motionLayout?.transitionToEnd()
        imgAdd?.apply {
            visibility = if (user.saved) {
                View.INVISIBLE
            } else {
                View.VISIBLE
            }
            setOnClickListener {
                onAddClicked(user)
            }
        }
    }
}