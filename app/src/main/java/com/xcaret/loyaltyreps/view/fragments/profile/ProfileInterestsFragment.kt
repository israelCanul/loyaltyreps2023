package com.xcaret.loyaltyreps.view.fragments.profile

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.google.android.material.snackbar.Snackbar
import com.xcaret.loyaltyreps.MainActivity
import org.json.JSONException
import org.json.JSONObject

import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.XUserHobbyAdapter
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentProfileInterestsBinding
import com.xcaret.loyaltyreps.model.*
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 *
 */
class ProfileInterestsFragment : Fragment() {

    lateinit var binding: FragmentProfileInterestsBinding
    lateinit var xUserViewModel: XUserViewModel

    private var listOfHobbies: ArrayList<XUserHobby> = ArrayList()
    private var hobbiesAdapter: XUserHobbyAdapter? = null

    private lateinit var selected: Drawable
    private lateinit var unselected: Drawable

    private var idRep = 0
    private var idEdoCivil = 0
    private var idEstadoNacimiento = 0
    private var hijos = 0
    private var idMunicipio = 0

    lateinit var estadoCivilListAdapter: ArrayAdapter<XEstadoCivil>
    lateinit var estadosMexicoAdapter: ArrayAdapter<XEstado>
    lateinit var municipioXEstadoAdapter: ArrayAdapter<Municipio>

    lateinit var hijosSpinnerAdapter: ArrayAdapter<Hijo>
    private var imcurrentUser = XUser()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_interests, container, false)
        val mApplication = requireNotNull(this.activity).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)

        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        loadUserCurrInfo()
        loadViews()

        loadEstadoCivilList()
        populateEstadoCivilSpinner()

        loadEstados()
        populateEstadosMexicoSpinner()

        populateMunicipioXEstados()

        return binding.root
    }

    private fun loadUserCurrInfo(){
        xUserViewModel.currentXUser.observe(this, Observer {
                xuser ->
                xuser?.let {
                    imcurrentUser = it
                    binding.userBirthDAte.setText(xuser.fechaNacimiento)

                    idRep = it.idRep
                    idEdoCivil = it.idEdoCivil
                    idEstadoNacimiento = it.idEstadoNacimiento
                    idMunicipio = it.idMunicipioNacimiento


                    try {
                        val result = it.intereses.split(",").map(String::toInt)
                        loadHobbies(result)
                    } catch (error: Exception){
                        error.printStackTrace()
                    }

                    loadMunicipiosByEstado(idEstadoNacimiento.toString())
                    populateMunicipioXEstados()

                    loadHijos()


                    binding.saveInteresesButton.setOnClickListener {
                        updateHobbies()
                    }
                }
        })
    }

    private fun loadEstadoCivilList(){
        AppPreferences.stadoCivilList.clear()
        AndroidNetworking.get("${AppPreferences.XCARET_API_URL_ROOT}General/getEstadoCivil")
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
            .setTag("get_estado_civil")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    AppPreferences.stadoCivilList.add(
                        XEstadoCivil(0, "Estado civil")
                    )
                    val listOfEC = response.getJSONArray("value")
                    for (item in 0 until listOfEC.length()){
                        val estadoC = listOfEC.getJSONObject(item)
                        AppPreferences.stadoCivilList.add(
                            XEstadoCivil(
                                estadoC.getInt("idEdoCivil"),
                                estadoC.getString("nombre")
                            )
                        )
                    }
                    notifyAdapter()
                }

                override fun onError(error: ANError) {
                    println("eeeeeooo${error.errorDetail}")
                }
            })
    }

    private fun notifyAdapter() {
        try {
            activity!!.runOnUiThread(Runnable {
                estadoCivilListAdapter.notifyDataSetChanged()
            })
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun populateEstadoCivilSpinner(){
        estadoCivilListAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, AppPreferences.stadoCivilList)
        binding.spinnerEstadoCivil.adapter = estadoCivilListAdapter

        binding.spinnerEstadoCivil.onItemSelectedListener = estadoCivilSpinnerListener
    }

    private val estadoCivilSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            try {
                if (position == 0) {
                    var currIdEdoC = 0
                    for (item in 0 until AppPreferences.stadoCivilList.size) {
                        if (AppPreferences.stadoCivilList[item].idEdoCivil == idEdoCivil) {
                            currIdEdoC = item
                        }
                    }
                    parent.setSelection(currIdEdoC)
                } else {
                    val estCivil = parent.selectedItem as XEstadoCivil
                    idEdoCivil = estCivil.idEdoCivil
                }
            } catch (err:Exception){
                err.printStackTrace()
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private fun loadEstados(){
        AppPreferences.estadosList.clear()
        AndroidNetworking.get("${AppPreferences.XCARET_API_URL_ROOT}Estados/getAllEstados/484")
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
            .setTag("get_estados_de_mexico")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        AppPreferences.estadosList.add(
                            XEstado(0, "Estado", 484, 1)
                        )
                        val listOfEC = response.getJSONArray("value")
                        for (item in 0 until listOfEC.length()){
                            val estadoC = listOfEC.getJSONObject(item)
                            AppPreferences.estadosList.add(
                                XEstado(
                                    estadoC.getInt("idEstado"),
                                    estadoC.getString("dsEstado"),
                                    estadoC.getInt("idPais"),
                                    estadoC.getInt("idIdioma")
                                )
                            )
                        }
                        notifyAdapterEstados()
                    } catch (except: Exception){
                        except.printStackTrace()
                    }
                }

                override fun onError(error: ANError) {
                    println("eeeeeooo${error.errorDetail}")
                }
            })
    }

    private fun notifyAdapterEstados() {
        try {
            activity!!.runOnUiThread(Runnable {
                estadosMexicoAdapter.notifyDataSetChanged()
            })
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun populateEstadosMexicoSpinner(){
        estadosMexicoAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, AppPreferences.estadosList)
        binding.spinnerEstado.adapter = estadosMexicoAdapter

        binding.spinnerEstado.onItemSelectedListener = estadosMexicoSpinnerListener
    }

    private val estadosMexicoSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            try {
                if (position == 0){
                    var currPosition = 0
                    for (item in 0 until AppPreferences.estadosList.size){
                        val itemm = AppPreferences.estadosList[item]
                        if (itemm.idEstado == idEstadoNacimiento) {
                            currPosition = item
                        }
                    }
                    parent.setSelection(currPosition)
                } else {
                    val estado = parent.selectedItem as XEstado
                    idEstadoNacimiento = estado.idEstado
                    loadMunicipiosByEstado(idEstadoNacimiento.toString())
                    populateMunicipioXEstados()
                }
            } catch (err: Exception) {
                err.printStackTrace()
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private fun loadMunicipiosByEstado(estadoId: String){
        AppPreferences.municipiosList.clear()
        AndroidNetworking.get("${AppPreferences.XCARET_API_URL_ROOT}Municipios/getMunicipiosByEstado/$estadoId")
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
            .setTag("get_municipios_porestado_de_mexico")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        AppPreferences.municipiosList.add(
                            Municipio(0, "Municipio", "SELECT", true)
                        )
                        val listOfEC = response.getJSONArray("value")
                        for (item in 0 until listOfEC.length()){
                            val estadoC = listOfEC.getJSONObject(item)

                            AppPreferences.municipiosList.add(
                                Municipio(
                                    estadoC.getInt("idMunicipio"),
                                    estadoC.getString("dsMunicipio"),
                                    estadoC.getString("dsClave"),
                                    estadoC.getBoolean("cnActivo")
                                )
                            )
                        }
                        notifyAdapterMunicipio()
                    } catch (except: Exception){
                        except.printStackTrace()
                    }
                }

                override fun onError(error: ANError) {
                    println("eeeeeooo${error.errorDetail}")
                }
            })
    }

    private fun notifyAdapterMunicipio(){
        try {
            activity!!.runOnUiThread(Runnable {
                municipioXEstadoAdapter.notifyDataSetChanged()
            })
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun populateMunicipioXEstados(){
        municipioXEstadoAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, AppPreferences.municipiosList)
        binding.spinnerMunicipio.adapter = municipioXEstadoAdapter

        binding.spinnerMunicipio.onItemSelectedListener = municipioXEstadoSpinnerListener
    }

    private val municipioXEstadoSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            try {
                if (position == 0) {
                    var currMunicipio = 0
                    for (item in 0 until AppPreferences.municipiosList.size) {
                        if (AppPreferences.municipiosList[item].idMunicipio == idMunicipio) {
                            currMunicipio = item
                        }
                    }
                    parent.setSelection(currMunicipio)
                } else {
                    val municipio = parent.selectedItem as Municipio
                    idMunicipio = municipio.idMunicipio
                }
            } catch (err: Exception) {
                err.printStackTrace()
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
            idMunicipio = -1
        }
    }

    private fun loadHijos(){
        AppPreferences.hijosList.clear()
        hijos = imcurrentUser.hijos
        AppPreferences.hijosList.add(Hijo(100,"Hijos"))
        AppPreferences.hijosList.add(Hijo(0,"0"))
        for (item in 1 until 11){
            AppPreferences.hijosList.add(Hijo(item,item.toString()))
        }
        notifyAdapterHijo()

        hijosSpinnerAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, AppPreferences.hijosList)
        binding.spinnerHijos.adapter = hijosSpinnerAdapter

        binding.spinnerHijos.onItemSelectedListener = hijosSpinnerListener
    }

    private val hijosSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            try{
                if (position == 0){
                    var currHijos = 0
                    for (item in 0 until AppPreferences.hijosList.size){
                        if (AppPreferences.hijosList[item].desc == hijos.toString()) {
                            currHijos = item
                        }
                    }
                    parent.setSelection(currHijos)
                } else {
                    val hij = parent.selectedItem as Hijo
                    hijos = hij.desc.toInt()
                }
            } catch (error: Exception) {
                error.printStackTrace()
            }
        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private fun notifyAdapterHijo(){
        try {
            activity!!.runOnUiThread(Runnable {
                hijosSpinnerAdapter.notifyDataSetChanged()
            })
        } catch (error: Exception) {
            error.printStackTrace()
        }
    }

    private fun loadHobbies(listOfIds: List<Int>){
        listOfHobbies.clear()
        AppPreferences.selectedInterestsIds.clear()

        binding.progressBar.visibility = View.VISIBLE
        binding.hobbiesRecyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.hobbiesRecyclerView.setHasFixedSize(true)
        hobbiesAdapter = XUserHobbyAdapter(activity!!, listOfHobbies, listOfIds)
        binding.hobbiesRecyclerView.adapter = hobbiesAdapter

        AndroidNetworking.get(AppPreferences.XCARET_API_URL_ROOT+"hobby/getall")
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        for (item in 0 until response.getJSONArray("value").length()) {
                            val mhobby = response.getJSONArray("value").getJSONObject(item)
                            listOfHobbies.add(
                                XUserHobby(
                                    mhobby.getInt("id"),
                                    mhobby.getString("dsDescripcion"),
                                    mhobby.getBoolean("activo"),
                                    mhobby.getBoolean("visible"),
                                    mhobby.getString("fechaAlta")
                                )
                            )
                        }
                        binding.hobbiesRecyclerView.adapter!!.notifyDataSetChanged()
                        binding.progressBar.visibility = View.GONE
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                    }
                }
                override fun onError(anError: ANError?) {
                }
            })

    }

    private fun loadViews(){
        selected = ContextCompat.getDrawable(activity!!, R.drawable.toggle_on)!!
        unselected = ContextCompat.getDrawable(activity!!, R.drawable.toggle_off)!!
    }

    private fun updateHobbies(){
        val mprofile = activity as MainActivity?
        if (AppPreferences.selectedInterestsIds.isNotEmpty()) {
            binding.progressBar.visibility = View.VISIBLE
            val jsonObject = JSONObject("""{"idrep": "$idRep", "idHobbies": ${AppPreferences.selectedInterestsIds}}""")

            val intIds = "${AppPreferences.selectedInterestsIds}".replace("[", "")
            val intIds2 = intIds.replace("]", "")
            val finalids = intIds2.replace(" ", "")

            AndroidNetworking.post("${AppPreferences.XCARET_API_URL_ROOT}hobby/update/")
                .addJSONObjectBody(jsonObject) // posting json
                .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
                .setTag("update_user_interests")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(object : JSONObjectRequestListener {
                    override fun onError(anError: ANError?) {
                        //mprofile!!.requestResponse(resources.getString(R.string.profile_update_respose_error))
                    }
                    override fun onResponse(response: JSONObject) {
                        if (response.getJSONObject("value").getInt("error") == 0){
                            xUserViewModel.onUpdateInterests(finalids)
                            //mprofile!!.requestResponse(resources.getString(R.string.profile_update_respose_success))
                            val result = finalids.split(",").map(String::toInt)
                            loadHobbies(result)
                        }
                    }
                })

            uploadUserInfo()

        } else {
            uploadUserInfo()
        }
    }

    private fun uploadUserInfo(){
        val mprofile = activity as MainActivity?
        val jsonObject = JSONObject()
        try {
            jsonObject.put("idRep", idRep)
            jsonObject.put("idEstadoNacimiento", idEstadoNacimiento)
            jsonObject.put("idMunicipioNacimiento", "$idMunicipio")
            jsonObject.put("idEdoCivil", idEdoCivil)
            jsonObject.put("hijos", hijos)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        val updateUser = XUser()
        updateUser.idEstadoNacimiento = idEstadoNacimiento
        if (idMunicipio == -1){
            updateUser.idMunicipioNacimiento = 0
        } else {
            updateUser.idMunicipioNacimiento = idMunicipio
        }
        updateUser.idEdoCivil = idEdoCivil
        updateUser.hijos = hijos
        AndroidNetworking.post("${AppPreferences.XCARET_API_URL_ROOT}rep/update/")
            .addJSONObjectBody(jsonObject) // posting json
            .addHeaders("Authorization", "bearer "+AppPreferences.userToken)
            .setTag("update_user_account")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onError(anError: ANError?) {
                    mprofile!!.requestResponse(resources.getString(R.string.profile_update_respose_error))
                }

                override fun onResponse(response: JSONObject) {
                    if (response.getJSONObject("value").getInt("error") == 0) {
                        xUserViewModel.onUpdateInterestsTop(updateUser)
                        //snackBarMessage("¡Felididades: Tus datos se actualizaron con éxito!")
                        mprofile!!.requestResponse(resources.getString(R.string.profile_update_respose_success))
                    }
                }
            })
    }

    private fun snackBarMessage(apiResponse: String){
        Snackbar.make(binding.mainContainer,
            apiResponse,
            Snackbar.LENGTH_LONG)
            .show()
        binding.progressBar.visibility = View.GONE
    }

}
