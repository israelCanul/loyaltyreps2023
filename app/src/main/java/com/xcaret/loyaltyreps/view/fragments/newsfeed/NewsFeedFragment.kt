package com.xcaret.loyaltyreps.view.fragments.newsfeed

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import com.bumptech.glide.Glide
import org.json.JSONArray

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.XNewsFeedAdapter
import com.xcaret.loyaltyreps.databinding.FragmentNewsFeedBinding
import com.xcaret.loyaltyreps.model.XNews
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import org.apache.commons.lang3.StringUtils

class NewsFeedFragment : Fragment() {

    lateinit var binding: FragmentNewsFeedBinding

    val ENDPOINT_VIDEOS = "newsfeed"
    var newsFeedAdapter: XNewsFeedAdapter? = null
    var allNews: ArrayList<XNews> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_news_feed, container, false)


        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        loadViews()
        loadNews()

        createSearchView()
    }

    private fun loadViews(){
        binding.newsFeedRexyclerView.layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
        binding.newsFeedRexyclerView.setHasFixedSize(true)
    }

    private fun loadNews(){
        allNews.clear()
        AndroidNetworking.get(AppPreferences.PUNK_API_URL+ENDPOINT_VIDEOS)
            .setPriority(Priority.LOW)
            .addHeaders("Authorization", "token "+ AppPreferences.PUNK_API_TOKEN)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onError(anError: ANError?) {
                    Toast.makeText(activity!!, "Algo salió mal, inténtalo nuevamente!", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.GONE
                }
                override fun onResponse(response: JSONArray) {
                    binding.progressBar.visibility = View.GONE
                    try {
                        val latestNew = response.getJSONObject(0)

                        val bundleNews = XNews(
                            latestNew.getInt("id"),
                            latestNew.getString("cover_img"),
                            latestNew.getString("title"),
                            latestNew.getString("description"),
                            latestNew.getString("date"),
                            latestNew.getString("updated_at")
                        )

                        Glide.with(context!!).load(bundleNews.cover_img).into(binding.newsCover)
                        binding.newsTitle.text = bundleNews.title
                        binding.newsPublishDate.text = StringUtils.capitalize(
                            AppPreferences.formatStringToDate(
                                bundleNews.created_at!!
                            )
                        )

                        binding.latestNew.setOnClickListener {
                            EventsTrackerFunctions.trackEvent(EventsTrackerFunctions.newsFeedFeaturedView)
                            val bundle = Bundle().also { it.putParcelable("xnews", bundleNews) }
                            findNavController().navigate(R.id.to_newsDetailsFragment, bundle)
                        }

                        for (item in 1 until response.length()){
                            val xvideo_item = response.getJSONObject(item)
                            allNews.add(
                                XNews(
                                    xvideo_item.getInt("id"),
                                    xvideo_item.getString("cover_img"),
                                    xvideo_item.getString("title"),
                                    xvideo_item.getString("description"),
                                    xvideo_item.getString("date"),
                                    xvideo_item.getString("updated_at")
                                )
                            )
                        }

                        newsFeedAdapter = XNewsFeedAdapter(activity!!, allNews)
                        binding.newsFeedRexyclerView.adapter = newsFeedAdapter
                        binding.newsFeedRexyclerView.adapter!!.notifyDataSetChanged()
                    } catch (exp: Exception) {
                        exp.printStackTrace()
                    }
                }
            })
    }

    private fun createSearchView(){
        binding.searchNews.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                newsFeedAdapter!!.filter.filter(newText)
                return false
            }

        })
    }

}
