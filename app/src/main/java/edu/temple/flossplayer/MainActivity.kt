package edu.temple.flossplayer

import android.app.SearchManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    //for search button
    private lateinit var buttonSearch: Button

    val volleyQueue : RequestQueue by lazy {
        Volley.newRequestQueue(this)
    }

    private val isSingleContainer : Boolean by lazy {
        findViewById<View>(R.id.container2) == null
    }

    private val bookViewModel : BookViewModel by lazy {
        ViewModelProvider(this)[BookViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bookViewModel.setBookList(BookList())
        buttonSearch = findViewById(R.id.SearchButton)


        //when search button is clicked...
        buttonSearch.setOnClickListener {
            onSearchRequested()
        }

        //type-to-search functionality
//       setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL)

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
                if (isSingleContainer && it != null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container1, BookPlayerFragment())
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit()
                }
                bookViewModel.markSelectedBookViewed()
            }
        }

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        setIntent(intent)
        handleIntent(intent)
        super.onNewIntent(intent)
    }

     fun handleIntent(intent: Intent?) {
        //receiving the query...check
        if (Intent.ACTION_SEARCH == intent?.action)
        {
            intent.getStringExtra(SearchManager.QUERY)?.also{ query ->
                doMySearch(query)
            }
        }
    }

    private fun doMySearch(i: String) {
        val url = "https://kamorris.com/lab/flossplayer/search.php?query=${i}"
        volleyQueue.add (
            JsonArrayRequest(
                com.android.volley.Request.Method.GET
                , url
                , null
                , {
                    try {
                        // TODO: get books and parse them, update viewModel, clearSelectedBook
                        val bookList = BookList()
                        var nextBook: JSONObject
                        for (i in 0 until it.length()) {
                            nextBook = it.getJSONObject(i)
                            bookList.add(
                                Book(
                                    nextBook.getString("book_title"),
                                    nextBook.getString("author_name"),
                                    nextBook.getInt("book_id"),
                                    nextBook.getString("cover_uri")
                                )
                            )
                        }
                        bookViewModel.setBookList(BookList())
                        bookViewModel.clearSelectedBook()

                    } catch (e : JSONException) {
                        e.printStackTrace()
                    }
                }
                , {
                    Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
                })
        )
    }

    //check with this
    //val jargon: Boolean = intent.getBundleExtra(SearchManager.APP_DATA)?.getBoolean(JARGON) ?: false
//    override fun onSearchRequested(): Boolean {
////        val appData = Bundle().apply {
////            //putBoolean(JARGON, true)
////        }
//        startSearch(null, false, appData, false)
//        return true
//    }

    override fun onBackPressed() {
        // BackPress clears the selected book
        bookViewModel.clearSelectedBook()
        super.onBackPressed()
    }

}