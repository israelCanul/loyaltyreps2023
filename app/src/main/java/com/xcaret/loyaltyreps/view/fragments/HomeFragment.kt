package com.xcaret.loyaltyreps.view.fragments


import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.crashlytics.android.Crashlytics
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentHomeBinding
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * A simple [Fragment] subclass.
 *
 */
class HomeFragment : Fragment()  {

    lateinit var binding: FragmentHomeBinding
    lateinit var xUserViewModel: XUserViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        val mApplication = requireNotNull(this.activity).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)

        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.userViewModel = xUserViewModel
        binding.lifecycleOwner = this

        loadUserInfo()

        loadStaticViews()
        handleActions()

        println("idRep ${AppPreferences.idRep}")
        println("mytoken ${AppPreferences.userToken}")

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    private fun loadStaticViews(){
        Glide.with(activity!!).load(R.drawable.button_home_goto_pickups).centerInside().into(binding.goToPickups)
        Glide.with(activity!!).load(R.drawable.icon_home_training).into(binding.imgTraining)
        Glide.with(activity!!).load(R.drawable.icon_home_support).into(binding.imgSupport)
        Glide.with(activity!!).load(R.drawable.icon_home_news).into(binding.imgNewsfeed)
        Glide.with(activity!!).load(R.drawable.icon_home_sales).into(binding.imgSails)
    }

    private fun loadUserInfo(){
        xUserViewModel.currentXUser.observe(this, Observer {
                xuser ->
                xuser?.let {

                    //println("current user $it")
                    AppPreferences.xuser_status = it.estatus
                    AppPreferences.quizzesIds = it.quizzes

                    if (!it.cnMainQuiz){
                        binding.goToQuiz.visibility = View.VISIBLE
                        binding.goToQuiz.setOnClickListener { findNavController().navigate(R.id.to_welcomeMainQuizFragment) }

                        binding.textView4.visibility = View.VISIBLE
                        binding.textView4.text = resources.getString(R.string.home_fragment_title)

                        binding.goToProfile.visibility = View.GONE
                        binding.gotoStore.visibility = View.GONE
                    } else {
                        binding.goToQuiz.visibility = View.GONE
                        binding.textView4.text = resources.getString(R.string.home_fragment_title2)
                        binding.goToProfile.setOnClickListener { findNavController().navigate(R.id.actionXComplimentaries) }
                        binding.gotoStore.setOnClickListener { findNavController().navigate(R.id.actionXShop) }
                    }

                    val totalPoints = it.puntosParaArticulos + it.puntosParaBoletos
                    AppPreferences.userTotalPoints = totalPoints

                    println("puntosParaArticulos: ${it.puntosParaArticulos}")
                    println("puntosParaBoletos: ${it.puntosParaBoletos}")
                    println("puntosParaBoletos: ${AppPreferences.userTotalPoints}")

                    val fullName = "${it.nombre} ${it.apellidoPaterno}"
                    val puntos_formated = NumberFormat.getNumberInstance(Locale.US).format(it.puntosParaArticulos)
                    binding.xUserName.text = fullName
                    binding.xUserStatus.text = if (it.cnMainQuiz && it.estatus && it.idEstatusArchivos == 3) "Estatus: Activo" else "Estatus: Inactivo"
                    binding.xUserPoints.text = puntos_formated//(it.puntosParaArticulos).toString()
                    val user_level = "${getXUserLevel(totalPoints.toFloat()).roundToInt()}"
                    binding.xuserLevel.text = user_level

                    val animationLevel = getXUserLevel(totalPoints.toFloat()).roundToInt()
                    //println("topreplevel $animationLevel")
                    Handler().postDelayed({
                        binding.lottieAnimationView2.playAnimation()
                        thread {
                            while (binding.lottieAnimationView2.isAnimating){ // Loop that checks the progress of your animation
                                if (animationLevel != 10){
                                    if (binding.lottieAnimationView2.progress >= ("0.$animationLevel").toFloat()){// If animation reaches 50%
                                        activity!!.runOnUiThread {
                                            binding.lottieAnimationView2.pauseAnimation()// Pause Animation
                                        }
                                    }
                                }
                            }
                        }
                    },500)

                    if (it.cnMainQuiz && it.estatus && it.idEstatusArchivos == 3){
                        binding.goToProfile.visibility = View.VISIBLE
                        binding.gotoStore.visibility = View.VISIBLE
                        binding.textView4.visibility = View.GONE
                    } else {
                        binding.goToProfile.visibility = View.GONE
                        binding.gotoStore.visibility = View.GONE
                    }
                }
        })
    }

    private fun handleActions(){
        binding.goToPickups.setOnClickListener { findNavController().navigate(R.id.to_pickUpsFragment) }
        binding.cardGoToTraining.setOnClickListener {  findNavController().navigate(R.id.to_trainingFragment) }
        binding.cardGoToSupport.setOnClickListener { findNavController().navigate(R.id.to_supportFragment) }
        binding.cardGoToNewsFeed.setOnClickListener { findNavController().navigate(R.id.to_newsFeedFragment) }
        binding.cardGoToSales.setOnClickListener {
            findNavController().navigate(R.id.to_salesFragment)
        }
    }

    private fun getXUserLevel(totalPoints: Float) : Float {

        val maxLevel = AppPreferences.userMaxLevel.toFloat()
        val maxPoints = AppPreferences.userMaxPoints.toFloat()

        val myLevel = if (totalPoints > maxPoints) maxLevel else totalPoints * maxLevel / maxPoints

        return myLevel
    }

}
