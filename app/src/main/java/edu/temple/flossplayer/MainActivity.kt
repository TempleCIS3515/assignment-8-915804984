package edu.temple.flossplayer

import android.app.SearchManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley


class MainActivity : AppCompatActivity() {

    var url = "https://kamorris.com/lab/flossplayer/search.php?query=search_term" //correct URL?

    //private val JARGON: String = ""

    //for search button
    private var buttonSearch: Button = findViewById(R.id.SearchButton)

    private val isSingleContainer : Boolean by lazy{
        findViewById<View>(R.id.container2) == null
    }

    private val bookViewModel : BookViewModel by lazy {
        ViewModelProvider(this)[BookViewModel::class.java]
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bookViewModel.setBookList(getBookList())

        val queue: RequestQueue = Volley.newRequestQueue(applicationContext) //need?

        //when search button is clicked...
        buttonSearch.setOnClickListener {
            fun onClick() {
                onSearchRequested()
            }
        }

        //receiving the query...check
        if (Intent.ACTION_SEARCH == intent.action)
        {
            intent.getStringExtra(SearchManager.QUERY)?.also{ query ->
                doMySearch(query)
            }
        }

        //type-to-search functionality
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL)

        // If we're switching from one container to two containers
        // clear BookPlayerFragment from container1
        if (supportFragmentManager.findFragmentById(R.id.container1) is BookPlayerFragment) {
            supportFragmentManager.popBackStack()
        }

        // If this is the first time the activity is loading, go ahead and add a BookListFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container1, BookListFragment())
                .commit()
        } else
        // If activity loaded previously, there's already a BookListFragment
        // If we have a single container and a selected book, place it on top
            if (isSingleContainer && bookViewModel.getSelectedBook()?.value != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container1, BookPlayerFragment())
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit()
            }

        // If we have two containers but no BookPlayerFragment, add one to container2
        if (!isSingleContainer && supportFragmentManager.findFragmentById(R.id.container2) !is BookPlayerFragment)
            supportFragmentManager.beginTransaction()
                .add(R.id.container2, BookPlayerFragment())
                .commit()


        // Respond to selection in portrait mode using flag stored in ViewModel
        bookViewModel.getSelectedBook()?.observe(this){
            if (!bookViewModel.hasViewedSelectedBook()) {
                if (isSingleContainer) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container1, BookPlayerFragment())
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit()
                }
                bookViewModel.markSelectedBookViewed()
            }
        }
    }

    //check with this
    //val jargon: Boolean = intent.getBundleExtra(SearchManager.APP_DATA)?.getBoolean(JARGON) ?: false
    override fun onSearchRequested(): Boolean {
        val appData = Bundle().apply {
            //putBoolean(JARGON, true)
        }
        startSearch(null, false, appData, false)
        return true
    }

    override fun onBackPressed() {
        // BackPress clears the selected book
        bookViewModel.clearSelectedBook()
        super.onBackPressed()
    }

    private fun getBookList() : BookList {
        val bookList = BookList()
        repeat (10) {
//            bookList.add(Book("Book ${it + 1}", "Author ${10 - it}"))
        }

        return bookList
    }
}