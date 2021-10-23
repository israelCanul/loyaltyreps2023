package com.xcaret.loyaltyreps.adapter

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.cardview_training_video.view.*
import org.json.JSONObject
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.model.XQuestion
import com.xcaret.loyaltyreps.model.XQuestionChoice
import com.xcaret.loyaltyreps.model.XVideo
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.DownloadImage

class XVideoAdapter(
    private val context: Context,
    private val resource: Int,
    private var xvArrayList: ArrayList<XVideo>,
    private var listOfQuizIds: List<Int>,
    private var activity: Activity
) : RecyclerView.Adapter<XVideoAdapter.ViewHolder>(){

    var END_POINT = "quiz?video_id="
    lateinit var xquizQuestions: ArrayList<XQuestion>
    var mydownloadID : Long = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemContainer = LayoutInflater.from(parent.context)
            .inflate(resource, parent, false) as ViewGroup


        var br = object: BroadcastReceiver(){
            override fun onReceive(context: Context?, intent: Intent?) {
                var id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID,-1)
                if(id==mydownloadID){
                    Toast.makeText(context, "Download Completed", Toast.LENGTH_LONG).show()
                }
            }

        }


        return ViewHolder(itemContainer)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val xvideo = xvArrayList[position]
        holder.video_title.text = xvideo.name
        val textPoints = "${xvideo.points} puntos"
        holder.video_points.text = textPoints
        holder.video_availability.text = AppPreferences.formatDate(xvideo.deadline)
        Glide.with(context).load(xvideo.cover_img).into(holder.vide_cover)

        if (listOfQuizIds.isNotEmpty()) {
            for (item in listOfQuizIds){
                if (item == xvideo.quiz_id) {
                    holder.video_button.visibility = View.GONE
                    holder.video_avalability_con.visibility = View.GONE
                    holder.quiz_completed.visibility = View.VISIBLE
                }
            }
        }

        if (!xvideo.quiz_available) {
            holder.video_button.background = ContextCompat.getDrawable(context, R.drawable.button_disabled)
        } else {
            holder.video_button.setOnClickListener {
                loadVideoQuizData(xvideo.id.toString(), xvideo.name!!, holder.video_button)
            }
        }
        if(xvideo.active!!){
            holder.download_video.setOnClickListener {
                var dm : DownloadImage = DownloadImage()
                mydownloadID = dm.saveVideo(context!!,activity,xvideo.video.toString(),xvideo.name!! + "_quizz")
            }
        }
        holder.vide_cover.setOnClickListener {
            val bundle = Bundle().also {
                it.putString("xvideo_url", xvideo.video)
                it.putString("video_id", "1")
            }
            holder.itemView.findNavController().navigate(R.id.to_XVideoActivity, bundle)
        }
    }

    override fun getItemCount(): Int = xvArrayList.size

    class ViewHolder(itemViewGroup: ViewGroup) : RecyclerView.ViewHolder(itemViewGroup){
        val vide_cover: ImageView = itemViewGroup.videoCover
        val video_title: TextView = itemViewGroup.newsTitle
        val video_points: TextView = itemViewGroup.newsPublishDate
        val video_availability: TextView = itemViewGroup.availabilityDate
        val video_button: Button = itemViewGroup.videoQuiz
        val video_avalability_con: LinearLayout = itemViewGroup.availabilityContainer
        val quiz_completed: ConstraintLayout = itemViewGroup.quiz_completed_container
        val download_video: Button = itemViewGroup.downloadVideoQuiz
    }

    private fun loadVideoQuizData(video_id: String, video_name: String, mView: Button) {
        xquizQuestions = ArrayList()
        AndroidNetworking.get(AppPreferences.PUNK_API_URL+END_POINT+video_id)
            .setPriority(Priority.LOW)
            .addHeaders("Authorization", "token "+ AppPreferences.PUNK_API_TOKEN)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject?) {
                    if (response?.getJSONArray("questions")!!.length() > 0){
                        for (quizquestion in 0 until response.getJSONArray("questions").length()){
                            val questionitem = response.getJSONArray("questions").getJSONObject(quizquestion)

                            val xQChoices: ArrayList<XQuestionChoice> = ArrayList()
                            if (questionitem.getJSONArray("choices").length() > 0){

                                for (choice in 0 until questionitem.getJSONArray("choices").length()){
                                    val moption = questionitem.getJSONArray("choices").getJSONObject(choice)
                                    val quizOption = XQuestionChoice(
                                        moption.getInt("id"),
                                        moption.getString("option"),
                                        moption.getBoolean("is_correct"),
                                        moption.getInt("question")
                                    )
                                    xQChoices.add(quizOption)
                                }
                            }

                            xquizQuestions.add(
                                XQuestion(
                                    questionitem.getInt("id"),
                                    xQChoices,
                                    questionitem.getString("question"),
                                    questionitem.getInt("quiz")
                                )
                            )
                        }
                        val bundle = Bundle().also {
                            it.putInt("idQuiz", response.getInt("id"))
                            it.putInt("wallet", response.getInt("wallet"))
                            it.putInt("points", response.getInt("points"))
                            it.putString("comentario", response.getString("name"))
                            it.putParcelableArrayList("mainquestions", xquizQuestions)
                        }
                        mView.findNavController().navigate(R.id.to_trainingVideoQuizFragment, bundle)
                    }
                }
                override fun onError(anError: ANError?) {
                    println("ahahah $anError")
                }
            })
    }
}