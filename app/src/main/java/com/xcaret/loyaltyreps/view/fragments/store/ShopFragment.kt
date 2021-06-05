package com.xcaret.loyaltyreps.view.fragments.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject
import com.xcaret.loyaltyreps.MainActivity
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.MViewPagerAdapter
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentShopBinding
import com.xcaret.loyaltyreps.model.XProduct
import com.xcaret.loyaltyreps.model.XStoreCategory
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory

class ShopFragment : Fragment() {

    lateinit var binding: FragmentShopBinding
    lateinit var xUserViewModel: XUserViewModel

    var mChildFragManager: FragmentManager? = null
    private var listOfCategories: ArrayList<XStoreCategory> = ArrayList()

    private var listOfProducts: ArrayList<XProduct> = ArrayList()
    private var listOfCourses: ArrayList<XProduct> = ArrayList()
    private var listOfHotSales: ArrayList<XProduct> = ArrayList()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shop, container, false)
        mChildFragManager = childFragmentManager

        val mApplication = requireNotNull(this.activity).application
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)
        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this


        return binding.root

    }

    private fun loadViews(){
        xUserViewModel.currentXUser.observe(this, Observer {
                xuser ->
                xuser?.let {
                    if (it.cnMainQuiz && it.estatus && it.idEstatusArchivos == 3){
                        loadStoreCategories()
                        loadUserDataFromServer()
                    } else {
                        findNavController().navigate(R.id.moduleNotAvailableFragment)
                    }
                }
        })
        binding.shopViewPager.offscreenPageLimit = 3
    }

    override fun onStart() {
        super.onStart()
        loadViews()
    }

    private fun loadStoreCategories(){
        listOfCategories.clear()
        listOfCourses.clear()
        listOfProducts.clear()
        listOfHotSales.clear()

        AndroidNetworking.get("${AppPreferences.XCARET_API_URL_ROOT}Articulo/${AppPreferences.idRep}")
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    if (response.getJSONObject("value").getInt("error") == 1) {
                        binding.statusCompra.visibility = View.VISIBLE
                        binding.statusCompra.text = response.getJSONObject("value").getString("detalle")
                    }
                    try {
                        if (response.getJSONObject("value").has("articulos") && response.getJSONObject("value").getJSONArray("articulos").length() > 0) {
                            for(item in 0 until response.getJSONObject("value").getJSONArray("articulos").length()){
                                val product_item = response.getJSONObject("value").getJSONArray("articulos").getJSONObject(item)

                                val product = XProduct(
                                    product_item.getInt("id_art"),
                                    AppPreferences.emptyString(product_item.getString("nombre")),
                                    if (product_item.get("idCategoriaArticulo").toString() == "" ||
                                        product_item.get("idCategoriaArticulo").toString() == "null") 0 else product_item.getInt("idCategoriaArticulo"),
                                    AppPreferences.emptyString(product_item.getString("clave")),
                                    product_item.getInt("puntos"),
                                    AppPreferences.emptyString(product_item.getString("descripcion")),
                                    AppPreferences.emptyString(product_item.getString("fechalta")),
                                    AppPreferences.emptyString(product_item.getString("foto")),
                                    AppPreferences.emptyString(product_item.getString("thumb")),
                                    AppPreferences.emptyString(product_item.getString("llave")),
                                    product_item.getInt("stock"),
                                    if (product_item.get("prodmes").toString() == "null") false else product_item.getBoolean("prodmes"),
                                    if (product_item.get("canjeoModo").toString() == "null") 0 else product_item.getInt("canjeoModo"),
                                    product_item.getBoolean("esRifa"),
                                    product_item.getBoolean("activo"),
                                    AppPreferences.emptyString(product_item.getString("feVigencia")),
                                    product_item.getBoolean("cnCurso"),
                                    product_item.getBoolean("cnHotSale"),
                                    true
                                )

                                when {
                                    product_item.getBoolean("cnCurso") -> listOfCourses.add(product)
                                    product_item.getBoolean("cnHotSale") -> listOfHotSales.add(product)
                                    else -> listOfProducts.add(product)
                                }
                                binding.mprogressBar.visibility = View.GONE

                            }

                            listOfCategories.add(XStoreCategory(0, 1, resources.getString(R.string.store_items), listOfProducts))
                            listOfCategories.add(XStoreCategory(1, 2, resources.getString(R.string.store_curses), listOfCourses))
                            listOfCategories.add(XStoreCategory(2, 3, resources.getString(R.string.store_hotsale), listOfHotSales))

                            setupViewPager(binding.shopViewPager)
                            binding.storeTablayout.setupWithViewPager(binding.shopViewPager)
                        } else {
                            //binding.runoutOfItems.visibility = View.VISIBLE
                            listOfCategories.add(XStoreCategory(0, 1, resources.getString(R.string.store_items), listOfProducts))
                            listOfCategories.add(XStoreCategory(1, 2, resources.getString(R.string.store_curses), listOfCourses))
                            listOfCategories.add(XStoreCategory(2, 3, resources.getString(R.string.store_hotsale), listOfHotSales))
                            //AppPreferences.toastMessage(activity!!, resources.getString(R.string.runout_ofproducts))
                            binding.mprogressBar.visibility = View.GONE
                            setupViewPager(binding.shopViewPager)
                            binding.storeTablayout.setupWithViewPager(binding.shopViewPager)
                        }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                    }
                }

                override fun onError(error: ANError) {
                    if (error.errorDetail == "connectionError"){
                        //binding.runoutOfItems.visibility = View.VISIBLE
                        AppPreferences.toastMessage(activity!!, resources.getString(R.string.profile_update_respose_error))
                        binding.mprogressBar.visibility = View.GONE
                    } else if (error.errorCode == 401) {
                        val mActivity = activity as MainActivity?
                        mActivity!!.xUserLogout(activity!!, "La sesión ha caducado", "Vuelve a iniciar sesión")
                    }
                }
            })

    }

    private fun setupViewPager(viewPager: ViewPager?) {
        val shopAdapter = MViewPagerAdapter(mChildFragManager!!)

        for (prodCategory in listOfCategories) {
            val newFragment = ProductsFragment.newInstance(
                prodCategory.categoryPosition,
                prodCategory.id,
                prodCategory.categoryName,
                prodCategory.categoryProducts
            )
            shopAdapter.addFragment(newFragment, prodCategory.categoryName)
        }

        viewPager!!.adapter = shopAdapter
    }

    private fun loadUserDataFromServer() {
        val user2update = XUser()
        AndroidNetworking.get("${AppPreferences.XCARET_API_URL_ROOT}rep/getDetalleRepById/${AppPreferences.idRep}")
            .setTag("user_info")
            .addHeaders("Authorization", "Bearer ${AppPreferences.userToken}")
            .setPriority(Priority.MEDIUM)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onError(anError: ANError?) {
                }

                override fun onResponse(response: JSONObject) {
                    if (response.length() > 0){
                        user2update.puntosParaArticulos = response.getJSONObject("value").getInt("puntosParaArticulos")
                        user2update.puntosParaBoletos = response.getJSONObject("value").getInt("puntosParaBoletos")
                        xUserViewModel.updatePuntosArticuloRifa(user2update)
                    }
                }

            })
    }
}
