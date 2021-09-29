package com.example.catapi.presentation

import android.animation.Animator
import android.content.Context
import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*


import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.catapi.R
import com.example.catapi.Utills.CONSTANTS.TAG
import com.example.catapi.retrofit.Cat
import com.example.catapi.retrofit.Movie
import java.lang.ref.WeakReference


class PaginationAdapter(context: Context, listener: OnItemClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var context: Context = context
    var catList: MutableList<Cat>
    val LOADING = 0
    val ITEM = 1
    var isLoadingAdded = false
    private val listener: OnItemClickListener
    var currentAnimator: Animator? = null

    // The system "short" animation time duration, in milliseconds. This
    // duration is ideal for subtle animations or animations that occur
    // very frequently.
    private val shortAnimationDuration = 1000

    init {
        catList = LinkedList()
        this.listener = listener
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
                viewHolder = MovieViewHolder(viewItem, listener)
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

                movieViewHolder.movieImage.setOnClickListener {
                    Log.d(TAG, "click from vh at: $position")
                    zoomImageFromThumb(movieViewHolder, position)
                }

                Glide.with(context).load(movie.url).apply(RequestOptions.centerCropTransform())
                    .into(movieViewHolder.movieImage)
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

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(Cat()) // add just footer, no data required
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        var position = catList!!.size - 1
        val result: Cat = getItem(position)

        catList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun add(cat: Cat) {
        catList.add(cat)
        notifyItemInserted(catList.size - 1)
    }

    fun addAll(movieResults: MutableList<Cat>) {
        for (elem in movieResults) {
            add(elem)
        }
    }

    fun getItem(position: Int): Cat {
        return catList[position]
    }


    private fun zoomImageFromThumb(vh: MovieViewHolder, position: Int) {

        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        Glide.with(context)
            .load(this.getItem(position).url)
            .apply(RequestOptions.centerCropTransform())
            .into(vh.movieImageExpanded)
    }


}

class MovieViewHolder(itemView: View, listener: OnItemClickListener) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var movieTitle: TextView
    var movieImage: ImageView
    var movieImageExpanded: ImageView
    var downloadBtn: Button
    private var listenerRef: WeakReference<OnItemClickListener>

    init {
        movieTitle = itemView.findViewById(R.id.movie_title)
        movieImage = itemView.findViewById(R.id.movie_poster)
        movieImageExpanded = itemView.findViewById(R.id.movie_poster_expanded)
        downloadBtn = itemView.findViewById(R.id.downloadBtn)
        listenerRef = WeakReference(listener)

        downloadBtn.setOnClickListener(this)
        movieImage.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            downloadBtn.id -> {
                Log.d(TAG, "onClick: case download")
            }
            movieImage.id -> {
                Log.d(TAG, "onClick: case image click")
            }
            else -> {
                Log.d(TAG, "onClick: case default")
            }
        }

        listenerRef.get()?.onItemClick(itemView, adapterPosition)
    }
}

class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var progressBar: ProgressBar = itemView.findViewById(R.id.loadmore_progres)
}


