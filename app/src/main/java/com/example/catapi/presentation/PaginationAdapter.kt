package com.example.catapi.presentation


import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.catapi.R
import com.example.catapi.Utills.CONSTANTS.TAG
import com.example.catapi.retrofit.Cat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import android.graphics.Bitmap
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception


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

                movieViewHolder.downloadBtn.setOnClickListener {
                    GlobalScope.launch {
                        saveImageToGallery(position)
                    }
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

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        var startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the image
        // view. Also set the image view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        vh.movieImage.getGlobalVisibleRect(startBoundsInt)

        startBoundsInt.set(startBoundsInt.left+10, startBoundsInt.top+10, startBoundsInt.right-120, startBoundsInt.bottom-120)
        vh.container.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).

        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        vh.movieImage.alpha = 0f
        vh.movieImageExpanded.visibility = View.VISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        vh.movieImageExpanded.pivotX = 0f
        vh.movieImageExpanded.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                vh.movieImageExpanded,
                View.X,
                startBounds.left,
                finalBounds.left)
            ).apply {
                with(ObjectAnimator.ofFloat(vh.movieImageExpanded, View.Y, startBounds.top, finalBounds.top))
                with(ObjectAnimator.ofFloat(vh.movieImageExpanded, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(vh.movieImageExpanded, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }

        // Upon clicking the zoomed-in image, it should zoom back down
        // to the original bounds and show the thumbnail instead of
        // the expanded image.
        vh.movieImageExpanded.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning/sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(vh.movieImageExpanded, View.X, startBounds.left)).apply {
                    with(ObjectAnimator.ofFloat(vh.movieImageExpanded, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(vh.movieImageExpanded, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(vh.movieImageExpanded, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        vh.movieImage.alpha = 1f
                        vh.movieImageExpanded.visibility = View.GONE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        vh.movieImage.alpha = 1f
                        vh.movieImageExpanded.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }

    }

    private suspend fun saveImageToGallery(position: Int) {
        val bitmapDrawable : BitmapDrawable = BitmapDrawable(context.resources, getBitmapFromURL(catList[position].url))

        val bitmap : Bitmap = bitmapDrawable.bitmap

        var outputStream : FileOutputStream? = null
        var file : File = Environment.getExternalStorageDirectory()

        val dir: File = File(file.absolutePath + "/MyPics")
        dir.mkdirs()

        val fileName : String = String.format("%d.png", System.currentTimeMillis())

        val outFile = File(dir, fileName)
        try {
            outputStream = FileOutputStream(outFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        try {
            outputStream!!.flush()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            outputStream!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBitmapFromURL(src: String?): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.setDoInput(true)
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            null
        }
    }
}

class MovieViewHolder(itemView: View, listener: OnItemClickListener) :
    RecyclerView.ViewHolder(itemView), View.OnClickListener {
    var movieTitle: TextView
    var movieImage: ImageView
    var movieImageExpanded: ImageView
    var downloadBtn: Button
    var container: ConstraintLayout
    private var listenerRef: WeakReference<OnItemClickListener>

    init {
        movieTitle = itemView.findViewById(R.id.movie_title)
        movieImage = itemView.findViewById(R.id.movie_poster)
        movieImageExpanded = itemView.findViewById(R.id.movie_poster_expanded)
        downloadBtn = itemView.findViewById(R.id.downloadBtn)
        container = itemView.findViewById(R.id.container)
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


