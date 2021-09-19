package com.example.catapi.presentation

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*


import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.catapi.R
import com.example.catapi.retrofit.Cat


class PaginationAdapter(context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var context: Context = context
    var catList: MutableList<Cat>
    val LOADING = 1
    val ITEM = 1
    var isLoadingAdded = false

    init {
        catList = LinkedList()
    }

    @JvmName("setMovieList1")
    fun setMovieList(inputMovieList: MutableList<Cat>) {
        catList = inputMovieList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)

        when (viewType) {
            ITEM -> {
                val viewItem: View = inflater.inflate(
                    R.layout.list_item,
                    parent,
                    false
                )
                viewHolder = MovieViewHolder(viewItem)
            }
            LOADING -> {
                val viewLoading: View = inflater.inflate(
                    R.layout.list_progress,
                    parent,
                    false
                )
                viewHolder = LoadingViewHolder(viewLoading)
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var movie = catList[position]
        when (getItemViewType(position)) {
            ITEM -> {
                var movieViewHolder: MovieViewHolder = holder as MovieViewHolder
                movieViewHolder.movieTitle.text = movie?.id

                Glide.with(context).load(movie.url).apply(RequestOptions.centerCropTransform()).into(movieViewHolder.movieImage);
            }
            LOADING -> {
                var loadingViewHolder: LoadingViewHolder = holder as LoadingViewHolder
                loadingViewHolder.progressBar.visibility = View.VISIBLE
            }
        }
    }


    override fun getItemCount(): Int {
        return if (catList == null) 0 else catList!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == catList!!.size - 1 && isLoadingAdded) LOADING else ITEM
    }

    fun addLoadingFooter(){
        isLoadingAdded = true
        //add(Movie())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        var position = catList!!.size - 1
        val result: Cat = getItem(position)

        catList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun add(cat: Cat)
    {
        catList.add(cat)
        notifyItemInserted(catList.size - 1)
    }

    fun addAll(movieResults: MutableList<Cat>){
        for(elem in movieResults)
        {
            add(elem)
        }
    }

    fun getItem(position: Int):Cat
    {
        return catList[position]
    }
}

class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var movieTitle: TextView = itemView.findViewById(R.id.movie_title)
    var movieImage: ImageView = itemView.findViewById(R.id.movie_poster)

}

class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var progressBar: ProgressBar = itemView.findViewById(R.id.loadmore_progres)
}