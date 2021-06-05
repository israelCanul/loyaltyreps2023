package com.xcaret.loyaltyreps.view.fragments.newsfeed

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import com.airbnb.lottie.model.DocumentData
import com.bumptech.glide.Glide
import org.apache.commons.lang3.StringUtils

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.FragmentNewsDetailsBinding
import com.xcaret.loyaltyreps.model.XNews
import com.xcaret.loyaltyreps.util.AppPreferences
import kotlinx.android.synthetic.main.fragment_item_list_dialog_item.view.*
import org.w3c.dom.Text

class NewsDetailsFragment : Fragment() {

    lateinit var binding: FragmentNewsDetailsBinding
    var xNewsItem: XNews? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_news_details, container, false)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        xNewsItem = arguments!!.getParcelable("xnews") as XNews


        loadNewsDetails()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadNewsDetails(){
        binding.newsTitle.text = xNewsItem!!.title
        binding.newsDatePublished.text = StringUtils.capitalize(AppPreferences.formatStringToDate(xNewsItem!!.created_at!!))
        Glide.with(activity!!).load(xNewsItem!!.cover_img).into(binding.newsCoverImage)

        /*var content = "<html><body style=\"background-color:transparent;text-align:justify;\"><div align=\"justify\">"
        content+= "${xNewsItem!!.description}"
        content+= "</div></body></html>"*/

        val spannedText: Spanned = HtmlCompat.fromHtml(xNewsItem!!.description!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.newsContentDescription.text = spannedText
        //binding.newsContentDescription.text = HtmlCompat.fromHtml(descrhtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.newsContentDescription.movementMethod = LinkMovementMethod.getInstance()

    }

}
