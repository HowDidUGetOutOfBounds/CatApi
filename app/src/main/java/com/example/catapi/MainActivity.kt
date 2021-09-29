package com.example.catapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.example.catapi.Utills.CONSTANTS
import com.example.catapi.Utills.CONSTANTS.TAG
import com.example.catapi.presentation.OnItemClickListener

import com.example.catapi.presentation.PaginationAdapter
import com.example.catapi.presentation.PaginationScrollListener
import com.example.catapi.retrofit.ApiInterface
import com.example.catapi.retrofit.Cat
import com.example.catapi.retrofit.ClientApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private var paginationAdapter: PaginationAdapter? = null
    private var catService: ApiInterface? = null
    private var progressBar: ProgressBar? = null
    private var PAGE_START = 1
    private var isLoading = false
    private var isLastPage = false
    private var TOTAL_PAGES = 5
    private var currentPage = PAGE_START


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var recyclerView: RecyclerView = findViewById(R.id.recyclerview)
        progressBar = findViewById(R.id.progressbar)

        catService = ClientApi.getClient()?.create(ApiInterface::class.java)
        var linearLayoutManager: LinearLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        paginationAdapter = PaginationAdapter(this, object : OnItemClickListener {
                override fun onItemClick(view: View, position: Int) {

                    Log.d(TAG, "onItemClick: $position")
                }

                override fun onLongItemClick(view: View, position: Int) {
                    Log.d(TAG, "onLongItemClick: $position")
                }

        })
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = paginationAdapter

        recyclerView.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {

            override fun loadMoreItems() {
                Log.d(CONSTANTS.TAG, "load more")
                isLoading = true
                //currentPage += 1
                loadNextPage()
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }

        })

//        recyclerView
//            .addOnItemTouchListener(
//            RecyclerItemClickListener(applicationContext, recyclerView, object : RecyclerItemClickListener.OnItemClickListener{
//                override fun onItemClick(view: View, position: Int) {
//
//                    Log.d(TAG, "onItemClick: $position")
//                }
//
//                override fun onLongItemClick(view: View, position: Int) {
//                    Log.d(TAG, "onLongItemClick: $position")
//                }
//
//            })
//        )

        loadFirstPage()
    }

    private fun loadNextPage() {

        catService!!.getCats("10").enqueue(object : Callback<MutableList<Cat>> {
            override fun onResponse(
                call: Call<MutableList<Cat>>,
                response: Response<MutableList<Cat>>
            ) {
                        paginationAdapter!!.removeLoadingFooter()
                        isLoading = false
                        val results: MutableList<Cat> = response.body()
                        paginationAdapter!!.addAll(results)
                        if (currentPage != TOTAL_PAGES) paginationAdapter!!.addLoadingFooter()
                        else isLastPage = true
            }

            override fun onFailure(call: Call<MutableList<Cat>>?, t: Throwable) {
                t.printStackTrace()
            }

        })
    }


    private fun loadFirstPage() {
        catService!!.getCats("10").enqueue(object : Callback<MutableList<Cat>> {
            override fun onResponse(
                call: Call<MutableList<Cat>?>?,
                response: Response<MutableList<Cat>>
            ) {
                        val results: MutableList<Cat> = response.body()
                        progressBar!!.visibility = View.GONE
                        paginationAdapter!!.addAll(results)
                        if (currentPage <= TOTAL_PAGES) paginationAdapter!!.addLoadingFooter() else isLastPage =
                            true
            }

            override fun onFailure(call: Call<MutableList<Cat>?>?, t: Throwable?) {

            }
        })
    }

}
