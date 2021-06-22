package com.xcaret.loyaltyreps.view

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import android.net.Uri
import android.os.Build
import android.view.MenuItem
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.LinearLayout
import androidx.constraintlayout.widget.Constraints
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.*
import com.google.android.exoplayer2.util.Util
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.databinding.ActivityXvideoBinding

class XVideoActivity : AppCompatActivity() {

    lateinit var binding: ActivityXvideoBinding
    var xvideoUrl = ""
    private val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
    private var trackSelector: DefaultTrackSelector? = null
    private var player: SimpleExoPlayer? = null
    private var shouldAutoPlay: Boolean = true
    private lateinit var mediaDataSourceFactory: DataSource.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_xvideo)

        supportActionBar!!.title = resources.getString(R.string.go_back)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.hide()

        xvideoUrl = intent.getStringExtra("xvideo_url")!!
        val video_id = intent.getStringExtra("video_id")!!.toInt()
        if (video_id == 0){
            println("video trainning")
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            binding.xvideoPlayer.visibility = View.GONE
            binding.tutorial.visibility = View.VISIBLE
            binding.tutorial.setVideoURI(Uri.parse(xvideoUrl))
            binding.tutorial.start()
        } else {
            println("video no se ")
            //ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                window.insetsController?.let {
                    it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_BARS_BY_TOUCH
                    window.navigationBarColor = getColor(R.color.colorTransparent)
                    it.hide(WindowInsets.Type.statusBars())
                }
            }else{
                @Suppress("Deprecation")
                window.decorView.systemUiVisibility = (
                        //View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Hide the nav bar and status bar
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        // Keep the app content behind the bars even if user swipes them up
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            }
            binding.tutorial.visibility = View.GONE
            binding.xvideoPlayer.visibility = View.VISIBLE
            mediaDataSourceFactory = DefaultHttpDataSourceFactory(
                Util.getUserAgent(this, "loyaltyreps"),
                null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true
            )

            playVideo(xvideoUrl)
        }
    }

    private fun playVideo(xv_url: String?){
        binding.xvideoPlayer.requestFocus()

        val audioAttributes: AudioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_SPEECH)
            .build()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory(bandwidthMeter)
        trackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        player = ExoPlayerFactory.newSimpleInstance(this@XVideoActivity, trackSelector)

        binding.xvideoPlayer.player = player

        player!!.setAudioAttributes(audioAttributes, true)
        player!!.playWhenReady = shouldAutoPlay
        binding.xvideoPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT


        val mediaSource = ExtractorMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource(Uri.parse(xv_url))
        player!!.prepare(mediaSource)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (player != null) {
                    player!!.playWhenReady = false
                    player!!.release()
                    player = null
                }
                if (binding.tutorial.isPlaying) {
                    binding.tutorial.stopPlayback()
                }
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            player!!.playWhenReady = false
            player!!.release()
            player = null
        }

        if (binding.tutorial.isPlaying) {
            binding.tutorial.stopPlayback()
        }

    }

}
