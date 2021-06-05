package com.xcaret.loyaltyreps.view.fragments.store


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import org.json.JSONObject
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.adapter.XProductAdapter
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentProductsBinding
import com.xcaret.loyaltyreps.model.*
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class ProductsFragment : Fragment() {

    lateinit var binding: FragmentProductsBinding
    lateinit var xUserViewModel: XUserViewModel

    private var mposition: Int = 0
    private var categoryId: Int = 0
    private var categoryName: String = ""
    private var categoryProducts: ArrayList<XProduct> = ArrayList()

    lateinit var catSpinnerAdapter: ArrayAdapter<XPCategory>
    private var xpCategories: ArrayList<XPCategory> = ArrayList()

    private val mClickListenerXProduct: (XProduct) -> Unit = this::onXProductClicked
    private var productAdapter: XProductAdapter? = null

    companion object {
        fun newInstance(
            mposition: Int,
            categoryId: Int,
            categoryName: String,
            categoryProducts: ArrayList<XProduct>) : ProductsFragment {

            val bundle = Bundle()
            bundle.putInt("mposition", mposition)
            bundle.putInt("categoryId", categoryId)
            bundle.putString("categoryName", categoryName)
            bundle.putParcelableArrayList("categoryProducts", categoryProducts)
            val fragment = ProductsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    private fun readBundle(bundle: Bundle?) {
        if (bundle != null) {
            mposition = bundle.getInt("mposition")
            categoryId = bundle.getInt("categoryId")
            categoryName = bundle.getString("categoryName")!!
            categoryProducts = bundle.getParcelableArrayList("categoryProducts")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_products, container, false)
        val mApplication = requireNotNull(this.activity).application
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)
        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        readBundle(arguments)

        loadViews()

        return binding.root
    }

    private fun loadViews(){

        xUserViewModel.currentXUser.observe(this, Observer {
                xuser ->
                xuser?.let {
                    val ptsArticulos = "${NumberFormat.getNumberInstance(Locale.US).format(it.puntosParaArticulos)} pts"
                    binding.articulosPuntos.text = ptsArticulos
                }
        })

        if (categoryId == 0) {
            loadProductCategories()
            populateSpinner()
        } else {
            binding.textView3.visibility = View.GONE
            binding.selectCategory.visibility = View.GONE
        }

        if (categoryProducts.isEmpty()) {
            binding.runoutOfItems.visibility = View.VISIBLE
            //binding.selectCategory.visibility = View.GONE
        } else {
            loadProducts()
            binding.productsRecyclerView.setHasFixedSize(true)
            binding.productsRecyclerView.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        }

    }

    private fun populateSpinner(){
        catSpinnerAdapter = ArrayAdapter(activity!!, R.layout.spinner_item, xpCategories)
        binding.selectCategory.adapter = catSpinnerAdapter
        if (!categoryProducts.isEmpty()) {
            binding.selectCategory.onItemSelectedListener = catpSpinnerListener
        }
    }

    private fun loadProductCategories(){
        xpCategories.clear()
        xpCategories.add(
            XPCategory(0, "Todos", "")
        )
        AndroidNetworking.get(AppPreferences.XCARET_API_URL_ROOT+"Articulo/getCategoriasArticulo")
            .setPriority(Priority.MEDIUM)
            .addHeaders("Authorization", "bearer ${AppPreferences.userToken}")
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    try {
                        for (item in 0 until response.getJSONArray("value").length()){
                            xpCategories.add(
                                XPCategory(
                                    response.getJSONArray("value").getJSONObject(item).getInt("idCategoriaArticulo"),
                                    response.getJSONArray("value").getJSONObject(item).getString("dsDescripcion"),
                                    response.getJSONArray("value").getJSONObject(item).getString("feAlta")
                                )
                            )
                        }
                        NotifyAdapter()
                    }catch (anError: Error){
                        println("omg$anError")
                    }
                }
                override fun onError(error: ANError) {
                    println("eeeeeooo$error")
                }
            })
    }

    private val catpSpinnerListener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            try {
                val cat = parent.selectedItem as XPCategory
                if (cat.idCategoriaArticulo != 0) {
                    for (item in categoryProducts) {
                        item.isVisible = item.idCategoriaArticulo == cat.idCategoriaArticulo
                    }
                    binding.productsRecyclerView.adapter!!.notifyDataSetChanged()
                } else {
                    for (item in categoryProducts) {
                        if (!item.isVisible){
                            item.isVisible = true
                        }
                    }
                    if (binding.productsRecyclerView.adapter != null) {
                        binding.productsRecyclerView.adapter!!.notifyDataSetChanged()
                    }
                }
            }catch (er: Error){
                println("omg$er")
            }

        }
        override fun onNothingSelected(parent: AdapterView<*>) {
        }
    }

    private fun NotifyAdapter() {
        try {
            activity!!.runOnUiThread(Runnable {
                catSpinnerAdapter.notifyDataSetChanged()
            })
        } catch (er: Exception) {
            er.printStackTrace()
        }
    }

    private fun loadProducts(){
        productAdapter = XProductAdapter(mClickListenerXProduct, activity!!, R.layout.cardview_store_item, categoryProducts)
        binding.productsRecyclerView.adapter = productAdapter
    }

    private fun onXProductClicked(xProduct: XProduct){
        val bundle = Bundle()
        bundle.putParcelable("xitem", xProduct)
        view?.findNavController()?.navigate(R.id.to_productDetailsFragment, bundle)
    }

}
