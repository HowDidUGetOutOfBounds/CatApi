package com.example.catapi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView

import com.example.catapi.presentation.PaginationAdapter
import com.example.catapi.presentation.PaginationScrollListener
import com.example.catapi.retrofit.ApiInterface
import com.example.catapi.retrofit.ClientApi
import com.example.catapi.retrofit.Movie
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    private var paginationAdapter: PaginationAdapter? = null
    private var movieService: ApiInterface? = null
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

        movieService = ClientApi.getClient()?.create(ApiInterface::class.java)
        var linearLayoutManager: LinearLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        paginationAdapter = PaginationAdapter(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = paginationAdapter

        recyclerView.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager) {

            override fun loadMoreItems() {
                isLoading = true
                currentPage += 1
                loadNextPage()
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading;
            }

        })

        loadFirstPage();
    }

    private fun loadNextPage() {

        movieService!!.getMovies().enqueue(object : Callback<MutableList<Movie>> {
            override fun onResponse(
                call: Call<MutableList<Movie>>,
                response: Response<MutableList<Movie>>
            ) {
                paginationAdapter!!.removeLoadingFooter()
                isLoading = false
                val results: MutableList<Movie> = response.body()
                paginationAdapter!!.addAll(results)
                if (currentPage != TOTAL_PAGES) paginationAdapter!!.addLoadingFooter()
                else isLastPage = true
            }

            override fun onFailure(call: Call<MutableList<Movie>>?, t: Throwable) {
                t.printStackTrace()
            }

        })
    }


    private fun loadFirstPage() {
        movieService!!.getMovies().enqueue(object : Callback<MutableList<Movie>> {
            override fun onResponse(
                call: Call<MutableList<Movie>?>?,
                response: Response<MutableList<Movie>>
            ) {
                val results: MutableList<Movie> = response.body()
                progressBar!!.visibility = View.GONE
                paginationAdapter!!.addAll(results)
                if (currentPage <= TOTAL_PAGES) paginationAdapter!!.addLoadingFooter() else isLastPage =
                    true
            }

            override fun onFailure(call: Call<MutableList<Movie>?>?, t: Throwable?) {

            }
        })
    }

}
