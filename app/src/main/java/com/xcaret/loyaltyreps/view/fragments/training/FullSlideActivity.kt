package com.xcaret.loyaltyreps.view.fragments.training

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.XTrainingSlideAdapter
import com.xcaret.loyaltyreps.databinding.ActivityFullSlideBinding
import com.xcaret.loyaltyreps.model.XImageSlide
import com.xcaret.loyaltyreps.util.DownloadImage
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions

class FullSlideActivity : AppCompatActivity() {

    lateinit var images: ArrayList<XImageSlide>
    lateinit var binding: ActivityFullSlideBinding
    lateinit var parkName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_full_slide)

        images = intent.extras?.getParcelableArrayList("images")!!
        parkName = intent.extras?.getString("parkName", "")!!

        binding.xParkSlideViewPager.adapter = XTrainingSlideAdapter(this,
            images, R.layout.full_slide_item, 0)

        val slideSize = binding.xParkSlideViewPager.adapter!!.count -1

        binding.xParkSlideViewPager.currentItem = intent.extras?.getInt("position")!!
        binding.slideIndicator.setupWithViewPager(binding.xParkSlideViewPager)

        binding.xParkSlideViewPager.addOnPageChangeListener(object  : ViewPager.OnPageChangeListener {
            var thresholdOffset: Float = 0.5f
            var thresholdOffsetPixels: Int = 1
            var scrollStarted: Boolean = false
            var checkDirection: Boolean = false

            override fun onPageScrollStateChanged(state: Int) {
                if (!scrollStarted && state == ViewPager.SCROLL_STATE_DRAGGING) {
                    scrollStarted = true
                    checkDirection = true
                } else {
                    scrollStarted = false
                }
            }
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if(parkName !=""){
                    EventsTrackerFunctions.trackTrainingParkSectionEvent(parkName,"galeria")
                }
                if (checkDirection) {
                    if (thresholdOffset > positionOffset && positionOffsetPixels > thresholdOffsetPixels) {

                    } else{
                        if (position == slideSize && scrollStarted) {
                            setItem0()
                        }
                    }
                }
                checkDirection = false
            }

            override fun onPageSelected(position: Int) {
            }

        })


        binding.closeMe.setOnClickListener {
            finish()
        }
        binding.downLoadImageSelected.setOnClickListener{
            println("Si imprime" + images[binding.xParkSlideViewPager.currentItem].image)
            var dm : DownloadImage = DownloadImage()
            dm.save(this, this, images[binding.xParkSlideViewPager.currentItem].image, intent.extras?.getString("parkName", "")!! + binding.xParkSlideViewPager.currentItem)
        }

    }

    private fun setItem0() {
        binding.xParkSlideViewPager.setCurrentItem(0, false)
    }
}
