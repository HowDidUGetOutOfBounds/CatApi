package com.example.catapi.presentation

import android.animation.Animator
import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.catapi.R
import com.example.catapi.Utills.CONSTANTS.TAG

class RecyclerItemClickListener(private val context: Context, val recyclerView: RecyclerView, val listener: OnItemClickListener) : RecyclerView.OnItemTouchListener {

    var mGestureDetector: GestureDetector
    private val currentAnimator: Animator? = null

    interface OnItemClickListener{
        fun onItemClick(view: View, position: Int)

        fun onLongItemClick(view: View, position: Int)
    }

    init {
        mGestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener(){
            override fun onSingleTapUp(e: MotionEvent?): Boolean {
               return true
            }

            override fun onLongPress(e: MotionEvent?) {
                val child: View? = e?.let { recyclerView.findChildViewUnder(it.x, e.y) }

                if(child != null)
                {
                    listener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(recycler: RecyclerView, e: MotionEvent): Boolean {
        val child: View? = recycler.findChildViewUnder(e.x, e.y)
        val imageView: ImageView = recycler.findViewById(R.id.movie_poster)
        val imageViewExpanded: ImageView  = recycler.findViewById(R.id.movie_poster_expanded)


        if (child != null && mGestureDetector.onTouchEvent(e))
        {
            val position = recycler.getChildAdapterPosition(child)

            imageView.setOnClickListener {
                zoomImageFromThumb(imageView,imageViewExpanded, position)
            }

            listener.onItemClick(child, position)
            return true
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }

    private fun zoomImageFromThumb(imageView: ImageView, imageViewExpanded: ImageView, position: Int) {
        var myAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null

        if(recyclerView.adapter is PaginationAdapter) {
            var myAdapter = recyclerView.adapter as PaginationAdapter
        }
        else
        {
            Log.d(TAG, "zoomImageFromThumb: ADAPTER TYPE NOT SUPPORTED")
            return
        }


        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        Glide.with(context)
            .load(
            (myAdapter as PaginationAdapter).getItem(position).url)
            .apply(RequestOptions.centerCropTransform())
            .into(imageViewExpanded)
    }

}