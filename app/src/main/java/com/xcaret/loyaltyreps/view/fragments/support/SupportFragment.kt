package com.xcaret.loyaltyreps.view.fragments.support


import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONArrayRequestListener
import org.json.JSONArray
import com.xcaret.loyaltyreps.MainActivity

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.XSupportAdapter
import com.xcaret.loyaltyreps.databinding.FragmentSupportBinding
import com.xcaret.loyaltyreps.model.XSQuestion
import com.xcaret.loyaltyreps.model.XSupportSubject
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions
import com.xcaret.loyaltyreps.view.XVideoActivity
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 *
 */
class SupportFragment : Fragment() {

    lateinit var binding: FragmentSupportBinding

    val ENDPOINT_SUPPORT = "faqs/"
    var mAdapter: XSupportAdapter? = null
    var mSubjects: ArrayList<XSupportSubject> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_support, container, false)

        setHasOptionsMenu(true)

        loadViews()
        populateSubjects()

        val activityMain = activity as MainActivity?

        binding.chatWithExperts.setOnClickListener {
            activityMain!!.loadChat()
            EventsTrackerFunctions.trackEvent(EventsTrackerFunctions.chatOpen)
        }

        return binding.root
    }

    private fun loadViews(){
        val linearLayoutManager = LinearLayoutManager(activity)
        binding.subjectsRecyclerView.setHasFixedSize(true)
        binding.subjectsRecyclerView.layoutManager = linearLayoutManager

        mAdapter = XSupportAdapter(activity!!, mSubjects)
        binding.subjectsRecyclerView.adapter = mAdapter
    }

    private fun populateSubjects(){
        mSubjects.clear()
        AndroidNetworking.get(AppPreferences.PUNK_API_URL+ENDPOINT_SUPPORT)
            .setPriority(Priority.LOW)
            .addHeaders("Authorization", "token "+AppPreferences.PUNK_API_TOKEN)
            .build()
            .getAsJSONArray(object : JSONArrayRequestListener {
                override fun onError(anError: ANError?) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(activity, "Algo salió mal, inténtalo nuevamente!", Toast.LENGTH_SHORT).show()
                }
                override fun onResponse(response: JSONArray) {
                    binding.progressBar.visibility = View.GONE
                    try {
                        for (item in 0 until response.length()){
                            val support_item = response.getJSONObject(item)

                            val questions: ArrayList<XSQuestion> = ArrayList()
                            if (support_item.getJSONArray("faqs").length() > 0){
                                for (q_item in 0 until support_item.getJSONArray("faqs").length()){
                                    val question_item = support_item.getJSONArray("faqs").getJSONObject(q_item)
                                    questions.add(
                                        XSQuestion(
                                            question_item.getInt("id"),
                                            question_item.getString("question"),
                                            question_item.getString("answer"),
                                            support_item.getInt("id")
                                        )
                                    )
                                }
                            }
                            mSubjects.add(
                                XSupportSubject(
                                    support_item.getInt("id"),
                                    support_item.getString("title"),
                                    questions,
                                    false
                                )
                            )
                        }
                        binding.subjectsRecyclerView.adapter!!.notifyDataSetChanged()
                    } catch (exep: Exception) {
                        exep.printStackTrace()
                    }
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.support, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.watchTutorial -> {
                val tutIntent = Intent(activity!!, XVideoActivity::class.java)
                tutIntent.putExtra("video_id", "0")
                tutIntent.putExtra("xvideo_url", "android.resource://com.xcaret.loyaltyreps/${R.raw.tutorial_lore}")
                startActivity(tutIntent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

}
