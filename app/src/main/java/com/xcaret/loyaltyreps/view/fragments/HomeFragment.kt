package com.xcaret.loyaltyreps.view.fragments


import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.bumptech.glide.Glide
import com.xcaret.loyaltyreps.MainActivity
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentHomeBinding
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.EventsTrackerFunctions.trackClickButtonEvent
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import org.json.JSONObject
import java.text.NumberFormat
import java.util.*
import kotlin.concurrent.thread
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

        //loadUserDataFromServer()

        loadStaticViews()
        handleActions()

        println("idRep ${AppPreferences.idRep}")
        println("mytoken ${AppPreferences.userToken}")

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
        loadUserDataFromServer()
    }

    private fun loadStaticViews(){
        Glide.with(activity!!).load(R.drawable.button_home_goto_pickups).centerInside().into(binding.goToPickups)
        Glide.with(activity!!).load(R.drawable.icon_home_training).into(binding.imgTraining)
        Glide.with(activity!!).load(R.drawable.icon_home_support).into(binding.imgSupport)
        Glide.with(activity!!).load(R.drawable.icon_home_news).into(binding.imgNewsfeed)
        Glide.with(activity!!).load(R.drawable.icon_home_sales).into(binding.imgSails)
    }

    private fun loadUserInfo(){
        xUserViewModel.currentXUser.observe(viewLifecycleOwner, Observer {
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

                    val totalPoints = it.puntosPorVentas
                    AppPreferences.userTotalPoints = totalPoints
                    val fullName = "${it.nombre} ${it.apellidoPaterno}"
                    val puntos_formated = NumberFormat.getNumberInstance(Locale.US).format(it.puntosParaArticulos)

                    binding.xUserStatus.text = if (it.estatus) "Estatus: Activo" else "Estatus: Inactivo"


                    binding.xUserPoints.text = puntos_formated//(it.puntosParaArticulos).toString()
                    val user_level = "${getXUserLevel(totalPoints.toInt())}"
                    binding.xuserLevel.text = user_level


                    if(getXUserLevel(totalPoints.toInt()) >= 10){
//                    if(true){
                        binding.xUserName.text = fullName + "\n Top Rep"
                        binding.xUserName.setTextColor(ContextCompat.getColor(context!!,R.color.gold))
                    }else{
                        binding.xUserName.text = fullName
                    }


                    var animationLevel = getXUserLevel(totalPoints.toInt()) * 0.1;
                    //animationLevel = 2 * 0.1
                    //println("topreplevel $totalPoints $animationLevel")

                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.lottieAnimationView2.playAnimation()
                        thread {
                            binding.lottieAnimationView2.setMinAndMaxProgress(0f, 1f)
                            binding.lottieAnimationView2.progress = animationLevel.toFloat()//
                            while (binding.lottieAnimationView2.isAnimating){ // Loop that checks the progress of your animation
                                //if (animationLevel != 10){
                                    if (binding.lottieAnimationView2.progress >= animationLevel.toFloat()){// If animation reaches 50%
                                        activity!!.runOnUiThread {
                                            binding.lottieAnimationView2.pauseAnimation()// Pause Animation
                                        }
                                    }
                                //}
                            }
                        }
                    },1000)

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
        binding.goToPickups.setOnClickListener {
            trackClickButtonEvent("Pickups")
            findNavController().navigate(R.id.to_pickUpsFragment)
        }
        binding.cardGoToTraining.setOnClickListener {
            trackClickButtonEvent("Training")
            findNavController().navigate(R.id.to_trainingFragment)
        }
        binding.cardGoToSupport.setOnClickListener {
            trackClickButtonEvent("Soporte")
            findNavController().navigate(R.id.to_supportFragment)
        }
        binding.cardGoToNewsFeed.setOnClickListener {
            trackClickButtonEvent("Newsfeed")
            findNavController().navigate(R.id.to_newsFeedFragment)
        }
        binding.cardGoToSales.setOnClickListener {
            trackClickButtonEvent("Ventas")
            findNavController().navigate(R.id.to_salesFragment)
        }
    }
    private fun getXUserLevel(totalPoints: Int) : Int {
        //val maxLevel = AppPreferences.userMaxLevel.toFloat()
        //val maxPoints = AppPreferences.userMaxPoints.toFloat()
        //var myLevel = if (totalPoints > maxPoints) maxLevel else totalPoints * maxLevel / maxPoints
        var myLevel:Int = 1;
        when(totalPoints){
            in 0..1999 ->{
                println("topreplevel in 1 $totalPoints")
                myLevel = 1
            }
            in 2000..3999 ->{
                myLevel = 2
            }
            in 4000..5999 ->{
                myLevel = 3
            }
            in 6000..7999 ->{
                myLevel = 4
            }
            in 8000..9999 ->{
                myLevel = 5
            }
            in 10000..11999 ->{
                myLevel = 6
            }
            in 12000..13999 ->{
                myLevel = 7
            }
            in 14000..15999 ->{
                myLevel = 8
            }
            in 16000..17999 ->{
                myLevel = 9
            }
            else -> myLevel = 10
        }

        return myLevel
    }
    fun loadUserDataFromServer() {

        // de aqui es donde se debe obtener los datos para actualizar en el home
        //Israel Canul
        println("prueba activo/baja: ${AppPreferences.XCARET_API_URL_ROOT}rep/getDetalleRepById/${AppPreferences.idRep}")
        val user2update = XUser()
        AndroidNetworking.get("${AppPreferences.XCARET_API_URL_ROOT}rep/getDetalleRepById/${AppPreferences.idRep}")
            .setTag("user_info")
            .addHeaders("Authorization", "Bearer ${AppPreferences.userToken}")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onError(anError: ANError?) {
                    println("prueba activo/baja: " + anError?.errorCode.toString())
                    if (anError!!.errorCode == 401 || anError.errorCode == 400 || anError.errorCode == 0 || anError.errorCode == 404 || anError.errorCode == 204 ) {
                        val mActivity = activity as MainActivity?
                        mActivity!!.xUserLogout(activity!!, "La sesión ha caducado", "Vuelve a iniciar sesión")
                    }
                }
                override fun onResponse(response: JSONObject) {
                    if (response.length() > 0){
                        println(response.getJSONObject("value"))

                        //obtenemos los campos necesarios de la respuesta
                        user2update.puntosPorVentas = response.getJSONObject("value").getInt("puntosPorVentas")
                        user2update.puntosParaArticulos = response.getJSONObject("value").getInt("puntosParaArticulos")
                        user2update.puntosParaBoletos = response.getJSONObject("value").getInt("puntosParaBoletos")
                        user2update.estatus = response.getJSONObject("value").getBoolean("estatus")
                        user2update.idEstatusArchivos = response.getJSONObject("value").getInt("idEstatusArchivos")
                        user2update.cnMainQuiz = response.getJSONObject("value").getBoolean("cnMainQuiz")
                        user2update.idEdoCivil = response.getJSONObject("value").getInt("idEdoCivil")
                        user2update.hijos = response.getJSONObject("value").getInt("hijos")
                        user2update.idEstadoNacimiento = response.getJSONObject("value").getInt("idEstadoNacimiento")
                        user2update.idMunicipioNacimiento = response.getJSONObject("value").getInt("idMunicipioNacimiento")
                        user2update.intereses = response.getJSONObject("value").getString("intereses")
                        //user2update.cnMainQuiz = false
                        //actualizamos el current user
                        xUserViewModel.updatePuntosArticuloRifa(user2update)
                        xUserViewModel.updateStatusUser(user2update)
                        xUserViewModel.onUpdateInterestsTop(user2update)
                        xUserViewModel.onUpdateInterests(user2update.intereses)
                        xUserViewModel.updateUserPointsForSales(user2update)
                        //actualizamos los controles en la vista
                        val totalPoints = user2update.puntosParaArticulos + user2update.puntosParaBoletos
                        AppPreferences.userTotalPoints = totalPoints
                        val puntos_formated = NumberFormat.getNumberInstance(Locale.US).format(user2update.puntosParaArticulos)
                        binding.xUserPoints.text = puntos_formated//(it.puntosParaArticulos).toString()
                        binding.xUserStatus.text = if (user2update.estatus) "Estatus: Activo" else "Estatus: Inactivo"

                    }
                }

            })
    }

}
