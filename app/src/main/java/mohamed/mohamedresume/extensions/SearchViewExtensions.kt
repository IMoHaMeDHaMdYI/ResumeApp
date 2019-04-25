package mohamed.mohamedresume.extensions

import androidx.appcompat.widget.SearchView

fun SearchView.onQuerySubmitted(fn: (String) -> Unit) {
    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            query?.let {
                fn(query)
            }
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            return true
        }
    })
}