package edu.temple.flossplayer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider

class BookFragment : Fragment() {
    private lateinit var titleTextView: TextView
    private lateinit var authorTextView: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_book, container, false).apply {
            titleTextView = findViewById(R.id.titleTextView)
            authorTextView = findViewById(R.id.authorTextView)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ViewModelProvider(requireActivity())[BookViewModel::class.java]
            .getSelectedBook()?.observe(requireActivity()) {updateBook(it)}
    }

    private fun updateBook(book: Book?) {
        book?.run {
            titleTextView.text = title
            authorTextView.text = author
        }
    }

}