package com.example.catapi.presentation

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerItemClickListener(context: Context, recyclerView: RecyclerView, val listener: OnItemClickListener) : RecyclerView.OnItemTouchListener {

    var mGestureDetector: GestureDetector

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
                    //TODO: add onClick download here
                    listener.onItemClick(child, recyclerView.getChildAdapterPosition(child))
                }
            }
        })
    }

    override fun onInterceptTouchEvent(recycler: RecyclerView, e: MotionEvent): Boolean {
        val child: View? = recycler.findChildViewUnder(e.x, e.y)

        if (child != null && mGestureDetector.onTouchEvent(e))
        {
            listener.onItemClick(child, recycler.getChildAdapterPosition(child))
            return true
        }
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {

    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }

}