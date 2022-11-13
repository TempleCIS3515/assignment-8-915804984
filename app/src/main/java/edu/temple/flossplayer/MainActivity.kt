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
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    //for search button
    private lateinit var buttonSearch: Button
    lateinit var volleyQueue : RequestQueue

    private val isSingleContainer : Boolean by lazy {
        findViewById<View>(R.id.container2) == null
    }

    private val bookViewModel : BookViewModel by lazy {
        ViewModelProvider(this)[BookViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleIntent(intent)

        bookViewModel.setBookList(BookList())

        buttonSearch = findViewById(R.id.SearchButton)

        //when search button is clicked...
        buttonSearch.setOnClickListener {
            onSearchRequested()
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
            doMySearch("")
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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
        setIntent(intent)
        buttonSearch = findViewById(R.id.SearchButton)
        buttonSearch.setOnClickListener{
            onSearchRequested()
            }
        }

     private fun handleIntent(intent: Intent?) {
        volleyQueue = Volley.newRequestQueue(this)
         //receiving the query...check
        if (Intent.ACTION_SEARCH == intent?.action)
        {
            intent.getStringExtra(SearchManager.QUERY)?.also{ query ->
                doMySearch(query)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container1, BookListFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

     private fun doMySearch(search: String) {
         val url = "https://kamorris.com/lab/flossplayer/search.php?query=${search}"
        volleyQueue.add (
            JsonArrayRequest(
                com.android.volley.Request.Method.GET
                , url
                , null
                , {
                    try {
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
                                ))}
                        bookViewModel.setBookList(bookList)
                        bookViewModel.clearSelectedBook()

                    } catch (e : JSONException) {
                        e.printStackTrace()
                    }
                }
                , {
                    Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show()
                }))}

    override fun onBackPressed() {
        // BackPress clears the selected book
        bookViewModel.clearSelectedBook()
        super.onBackPressed()
    }}