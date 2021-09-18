package com.example.catapi.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.catapi.retrofit.Movie
import java.util.*

import android.R
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import org.w3c.dom.Text


class PaginationAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var context: Context? = null
    var movieList: MutableList<Movie>
    val LOADING = 1
    val ITEM = 1
    var isLoadingAdded = false

    init {
        this.context = context
        movieList = LinkedList()
    }

    @JvmName("setMovieList1")
    fun setMovieList(inputMovieList: MutableList<Movie>) {
        movieList = inputMovieList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> {
                val viewItem: View = inflater.inflate(
                    R.layout.item_list,
                    parent,
                    false
                )
                viewHolder = MovieViewHolder(viewItem)
            }
            LOADING -> {
                val viewLoading: View = inflater.inflate(
                    R.layout.item_progress,
                    parent,
                    false
                )
                viewHolder = LoadingViewHolder(viewLoading)
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var movie = movieList?.get(position)
        when (getItemViewType(position)) {
            ITEM -> {
                var movieViewHolder: MovieViewHolder = holder as MovieViewHolder
                movieViewHolder.movieTitle.text = movie?.getTitle()
            }
            LOADING -> {
                var loadingViewHolder: LoadingViewHolder = holder as LoadingViewHolder
                loadingViewHolder.progressBar.visibility = View.VISIBLE
            }
        }
    }


    override fun getItemCount(): Int {
        return if (movieList == null) 0 else movieList!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == movieList!!.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    fun addLoadingFooter(){
        isLoadingAdded = true
        add(Movie())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        var position = movieList!!.size - 1
        val result: Movie = getItem(position)

        if (result != null) {
            movieList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun add(movie: Movie)
    {
        movieList.add(movie)
        notifyItemInserted(movieList.size - 1)
    }

    fun addAll(movieResults: MutableList<Movie>){
        for(elem in movieResults)
        {
            add(elem)
        }
    }

    fun getItem(position: Int):Movie
    {
        return movieList[position]
    }
}

class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var movieTitle: TextView = itemView.findViewById(R.id.movie_title)
    var movieImage: ImageView = itemView.findViewById(R.id.movie_image)

}

class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var progressBar: ProgressBar = itemView.findViewById(R.id.loadmore_progres)
}