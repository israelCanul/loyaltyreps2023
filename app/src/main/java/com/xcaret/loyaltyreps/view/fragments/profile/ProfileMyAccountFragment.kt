package com.xcaret.loyaltyreps.view.fragments.profile


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import android.text.Selection
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.*
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.koushikdutta.ion.Ion
import com.obsez.android.lib.filechooser.ChooserDialog
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import com.xcaret.loyaltyreps.MainActivity
import com.xcaret.loyaltyreps.R
import com.xcaret.loyaltyreps.database.XCaretLoyaltyDatabase
import com.xcaret.loyaltyreps.databinding.FragmentProfileMyAccountBinding
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AlbumStorageDirFactory
import com.xcaret.loyaltyreps.util.AppPreferences
import com.xcaret.loyaltyreps.util.XcaretLoyaltyApi
import com.xcaret.loyaltyreps.viewmodel.XUserViewModel
import com.xcaret.loyaltyreps.viewmodel.XUserViewModelFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import java.nio.file.Files.createFile

/**
 * A simple [Fragment] subclass.
 *
 */
class ProfileMyAccountFragment : Fragment() {

    lateinit var binding: FragmentProfileMyAccountBinding
    lateinit var xUserViewModel: XUserViewModel

    /*user photo utils*/
    var REQUEST_SELECT_TAKE_SELECT = 101
    var REQUEST_SELECT_IMAGE_IN_ALBUM = 1
    var TAKE_PHOTO_REQUEST = 2
    var PDF_REQUEST_CODE = 0
    var mCurrentPhotoPath: String = ""
    var fileUri: Uri? = null
    var file: File? = null

    var pic1 = 0
    val currentXUser = XUser()
    var phoneClicked = false

    private var repTarjeta = ""
    private var idRep = 0

    private var termsAndConditionsAcepted = false
    private lateinit var selected: Drawable
    private lateinit var unselected: Drawable

    var sourceFrom = ""
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile_my_account, container, false)

        val mApplication = requireNotNull(this.activity).application
        //reference to the DAO
        val dataSource = XCaretLoyaltyDatabase.getInstance(mApplication).loyaltyDatabaseDAO
        //create instance
        val viewModelFactory = XUserViewModelFactory(dataSource, mApplication)
        xUserViewModel = ViewModelProviders.of(
            this, viewModelFactory).get(XUserViewModel::class.java)
        binding.lifecycleOwner = this

        loadUserInfo()

        binding.logOut.setOnClickListener {
            val mActivity = activity as MainActivity?
            mActivity!!.xUserLogout(activity!!, "", "¿Seguro que quieres cerrar sesión?")
        }


        handleInformationClicks()
        handlePhotoClicks()


        return binding.root
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == TAKE_PHOTO_REQUEST) {
                when(pic1) {
                    1 -> {
                        currentXUser.xuserPhoto = processCapturedPhoto()
                        xUserViewModel.onXUserUpdatePhoto(currentXUser)
                        binding.userPhoto.setText(R.string.photo_selected)
                        uploadFiles(processCapturedPhoto(), "avatar.jpg")
                    }
                    2 -> {
                        currentXUser.xuserPhotoFront = processCapturedPhoto()
                        xUserViewModel.updateFrontDocument(currentXUser)
                        binding.userFrontIDPhoto.setText(R.string.photo_selected)
                        uploadFiles(processCapturedPhoto(), "front.jpg")
                    }
                    3 -> {
                        currentXUser.xuserPhotoBack = processCapturedPhoto()
                        xUserViewModel.updateBackDocument(currentXUser)
                        binding.userIDBackPhoto.setText(R.string.photo_selected)
                        uploadFiles(processCapturedPhoto(), "back.jpg")
                    }
                }

        } else if (requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM &&
            resultCode == Activity.RESULT_OK) {
            when(pic1) {
                1 -> {
                    currentXUser.xuserPhoto = processSelectedPhoto(data?.data!!)
                    xUserViewModel.onXUserUpdatePhoto(currentXUser)
                    binding.userPhoto.setText(R.string.photo_selected)
                    uploadFiles(processSelectedPhoto(data.data!!), "avatar.jpg")
                }
                2 -> {
                    currentXUser.xuserPhotoFront = processSelectedPhoto(data?.data!!)
                    xUserViewModel.updateFrontDocument(currentXUser)
                    binding.userFrontIDPhoto.setText(R.string.photo_selected)
                    uploadFiles(processSelectedPhoto(data.data!!), "front.jpg")
                }
                3 -> {
                    currentXUser.xuserPhotoBack = processSelectedPhoto(data?.data!!)
                    xUserViewModel.updateBackDocument(currentXUser)
                    binding.userIDBackPhoto.setText(R.string.photo_selected)
                    uploadFiles(processSelectedPhoto(data.data!!), "back.jpg")
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun loadUserInfo(){

        selected = ContextCompat.getDrawable(activity!!, R.drawable.toggle_terms_on)!!
        unselected = ContextCompat.getDrawable(activity!!, R.drawable.toggle_terms_off)!!

        binding.termsAndConditionsNotice.makeLinks(
            Pair("Aviso de privacidad", View.OnClickListener {
                val mainActivity = activity as MainActivity?
                mainActivity!!.openActivityPDFile("Aviso de privacidad", AppPreferences.avisoDePrivacidadUrl)
            }),
            Pair("términos y condiciones", View.OnClickListener {
                val mainActivity = activity as MainActivity?
                mainActivity!!.openActivityPDFile("Aviso de privacidad", AppPreferences.terminosYCondicionesUrl)
            })
        )

        xUserViewModel.currentXUser.observe(this, Observer {
            xuser ->
            xuser?.let {
                binding.currUserStatus.text = if (it.cnMainQuiz && it.idEstatusArchivos == 3) "Validado" else "Pendiente"
                binding.userRCX.setText(it.rcx.toUpperCase())
                binding.userName.setText(it.nombre)
                binding.userLastName.setText(it.apellidoPaterno)
                binding.userLastName2nd.setText(it.apellidoMaterno)
                binding.userEmail.setText(it.correo)
                binding.userPhoneNumber.setText(it.telefono)

                binding.validationStatus.text = it.dsEstatusArchivos

                /*if (it.cnTarjetaActiva) {
                    binding.userPhoto.isEnabled = false
                    binding.userPhoto.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_camera_done, 0)
                }*/
                if (it.idEstatusArchivos == 3) {
                    binding.userPhoto.isEnabled = false
                    binding.userPhoto.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_camera_done, 0)
                    binding.userFrontIDPhoto.isEnabled = false
                    binding.userFrontIDPhoto.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_attach_done, 0)
                    binding.userIDBackPhoto.isEnabled = false
                    binding.userIDBackPhoto.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.icon_attach_done, 0)
                }

                repTarjeta = it.rcx
                idRep = it.idRep
                if (it.xuserPhoto != null) {
                    binding.userPhoto.setText(resources.getText(R.string.profile_change_photo))
                }
                if (it.xuserPhotoFront != null) {
                    binding.userFrontIDPhoto.setText(resources.getText(R.string.profile_change_photo))
                }
                if (it.xuserPhotoBack != null) {
                    binding.userIDBackPhoto.setText(resources.getText(R.string.profile_change_photo))
                }
                if (it.cnAceptaPoliticas){
                    binding.acceptTermsAndConditions.isEnabled = false
                    binding.acceptTermsAndConditions.isChecked = true
                }
            }

        })


        binding.userPhoneNumber.setOnFocusChangeListener { view, b ->
            if (b) {
                phoneClicked = true
            }
        }

        acceptTermsAndConditions()
    }

    private fun handlePhotoClicks(){
        binding.userPhoto.setOnClickListener {
            pic1 = 1
            popupSelector()
        }
        binding.userFrontIDPhoto.setOnClickListener {
            pic1 = 2
            showPictureAndPDFOptionsDialog()
        }
        binding.userIDBackPhoto.setOnClickListener {
            pic1 = 3
            showPictureAndPDFOptionsDialog()
        }

        binding.updateMyInfoButton.setOnClickListener {
            updateUserAccount()
        }

    }

    private fun handleInformationClicks(){
        binding.photoInformation.setOnClickListener {
            informationPopup(R.layout.profile_popup_id_information,
                R.drawable.lore_fotografia)
        }
        binding.photoIDInformation.setOnClickListener {
            informationPopup(R.layout.profile_popup_document_information,
                R.drawable.lore_documento)
        }
    }

    private fun informationPopup(layoutId: Int, imageId: Int){
        val alertBuilder = AlertDialog.Builder(activity)
        val dialogView = activity!!.layoutInflater.inflate(layoutId, null)
        Glide.with(activity!!).load(imageId).into(dialogView.findViewById(R.id.instructions))
        alertBuilder.setView(dialogView)
        val alertDialog = alertBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        dialogView.findViewById<ImageButton>(R.id.closeMe).setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.setCancelable(false)
    }

    private fun popupSelector(){
        val options1 = arrayOf(
            resources.getString(R.string.take_picture_from_camera),
            resources.getString(R.string.take_picture_from_gallery)
        )
        val mbuilder1 = AlertDialog.Builder(activity)
        mbuilder1.setTitle(R.string.add_profrile_picture)
        mbuilder1.setItems(options1) { _, which ->
            if (options1[which] == resources.getString(R.string.take_picture_from_camera)) {
                validatePermissions("from_camera")
            } else if (options1[which] == resources.getString(R.string.take_picture_from_gallery)) {
                validatePermissions("from_gallery")
            }
        }
        mbuilder1.show()
    }

    private fun showPictureAndPDFOptionsDialog(){
        val options = arrayOf (
            resources.getString(R.string.take_picture_from_camera),
            resources.getString(R.string.take_picture_from_gallery),
            resources.getString(R.string.select_pdf_file)
        )
        val mbuilder = AlertDialog.Builder(activity)
        mbuilder.setTitle(R.string.add_upload_file)
        mbuilder.setItems(options) { _, which ->
            if (options[which] == resources.getString(R.string.take_picture_from_camera)) {
                validatePermissions("from_camera")
            } else if (options[which] == resources.getString(R.string.take_picture_from_gallery)) {
                validatePermissions("from_gallery")
            } else if (options[which] == resources.getString(R.string.select_pdf_file)) {
                validatePermissions("from_content")
                PDF_REQUEST_CODE = 222
            }
        }
        mbuilder.show()
    }

    private fun validatePermissions(sourceFrom: String){
        Dexter.withActivity(activity)
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
            .withListener(object: MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        when (sourceFrom) {
                            "from_camera" -> launchCamera()
                            "from_gallery" -> processPictureFromGallery()
                            "from_content" -> showFileChooser()
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    println("ajajajajj")
                }
            })
            .withErrorListener { error -> println("ososososo $error") }
            .check()
    }

    private fun launchCamera(){
        val values = ContentValues(1)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
        val fileUri = activity!!.contentResolver
            .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if(intent.resolveActivity(activity!!.packageManager) != null) {
            mCurrentPhotoPath = fileUri?.toString()!!
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                    or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            startActivityForResult(intent, TAKE_PHOTO_REQUEST)
        }
    }

    private fun processCapturedPhoto() : String {
        val cursor = activity!!.contentResolver.query(
            Uri.parse(mCurrentPhotoPath),
            Array(1) {"_data"},
            null, null, null)
        cursor?.moveToFirst()
        val photoPath = cursor?.getString(0)
        cursor?.close()
        file = File(photoPath!!)
        fileUri = Uri.fromFile(file)

        return photoPath
    }

    private fun processPictureFromGallery(){
        val intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        if (intent.resolveActivity(activity!!.packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
    }

    private fun processSelectedPhoto(photoUri: Uri) : String {

        val filePathColumn = Array(1){"_data"}

        val cursor = activity!!.contentResolver.query(
            photoUri, filePathColumn, null, null, null
        )
        cursor?.moveToFirst()
        val columindex = cursor?.getColumnIndex(filePathColumn[0])
        val mPath = cursor?.getString(columindex!!)
        cursor?.close()

        return mPath!!
    }

    private fun showFileChooser() {
        ChooserDialog(context)
            .withFilterRegex(false, true, ".*\\.pdf$")
            //.withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
            .withChosenListener { path, pathFile ->
                if (PDF_REQUEST_CODE == 222 && pic1 == 2){
                    currentXUser.xuserPhotoFront = path
                    xUserViewModel.updateFrontDocument(currentXUser)
                    binding.userFrontIDPhoto.setText(R.string.photo_selected)
                    uploadFiles(path, "front.pdf")
                } else if (PDF_REQUEST_CODE == 222 && pic1 == 3){
                    currentXUser.xuserPhotoBack = path
                    xUserViewModel.updateBackDocument(currentXUser)
                    binding.userIDBackPhoto.setText(R.string.photo_selected)
                    uploadFiles(path, "back.pdf")
                }
            }
            .withNavigateUpTo { true }
            .withNavigateTo { true }
            .build()
            .show()
    }

    private fun validateUserInfo() : Boolean {
        var valid =  true

        if (binding.userEmail.text.toString().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(
                binding.userEmail.text.toString()).matches()) {
            binding.userEmail.error = resources.getString(R.string.error_invalid_profile_email)
            valid = false
        } else {
            binding.userEmail.error = null
        }

        if (phoneClicked){
            if (binding.userPhoneNumber.text.toString().length < 10 ||
                binding.userPhoneNumber.text.toString().length > 10) {
                binding.userPhoneNumber.error = resources.getString(R.string.error_invalid_profile_phone)
                valid = false
            } else {
                binding.userPhoneNumber.error = null
            }
        }

        return valid
    }

    private fun acceptTermsAndConditions(){
        binding.acceptTermsAndConditions.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked){
                buttonView.setCompoundDrawablesRelativeWithIntrinsicBounds(selected, null, null, null)
                termsAndConditionsAcepted = true
                binding.termsError.visibility = View.GONE
            } else {
                buttonView.setCompoundDrawablesRelativeWithIntrinsicBounds(unselected, null, null, null)
                termsAndConditionsAcepted = false
            }
        }
    }

    fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
        val spannableString = SpannableString(this.text)
        for (link in links) {
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(view: View) {
                    Selection.setSelection((view as TextView).text as Spannable, 0)
                    view.invalidate()
                    link.second.onClick(view)
                }
            }
            val startIndexOfLink = this.text.toString().indexOf(link.first)
            spannableString.setSpan(clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        this.movementMethod = LinkMovementMethod.getInstance() // without LinkMovementMethod, link can not click
        this.setText(spannableString, TextView.BufferType.SPANNABLE)
    }

    private fun updateUserAccount(){
        if (!validateUserInfo()){
            return
        }

        if (!termsAndConditionsAcepted) {
            binding.termsError.visibility = View.VISIBLE
            return
        }
        binding.progressBar.visibility = View.VISIBLE

        val mprofile = activity as MainActivity?
        mprofile!!.updateUserProfile(
            binding.userEmail.text.toString(),
            binding.userPhoneNumber.text.toString()
        )
        mprofile.updateTermsAndConditionsStatus()
        binding.progressBar.visibility = View.GONE
    }

    private fun uploadFiles(filePath: String, fileName: String){
        binding.progressBar.visibility = View.VISIBLE
        val byteArrayOutputStream = ByteArrayOutputStream()
        val bitmap = Ion.with(activity!!).load(filePath).asBitmap().get()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream)
        byteArrayOutputStream.toByteArray()

        val file1 = File(filePath)
        try {
            val fo = FileOutputStream(file1)
            fo.write(byteArrayOutputStream.toByteArray())
            fo.flush()
            fo.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file1)

        val body1 = MultipartBody.Part.createFormData("Files", fileName, requestFile)

        XcaretLoyaltyApi.retrofitService.upload(
            "Bearer ${AppPreferences.userToken}",
            body1,
            AppPreferences.userRCX!!
        ).enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>, t: Throwable) {
                informationPopup(R.layout.profile_popup_upload_file_failed,
                    R.drawable.lore_documento)
                binding.progressBar.visibility = View.GONE
            }
            override fun onResponse(call: Call<String>, response: Response<String>) {
                binding.progressBar.visibility = View.GONE
                if (response.body() == "true") {
                    val mainActivity = activity as MainActivity?
                    mainActivity!!.requestResponse("Archivo actualizado con éxito")
                    //snackBarMessage("Archivo actualizado con éxito")
                } else {
                    informationPopup(R.layout.profile_popup_upload_file_failed,
                        R.drawable.lore_documento)
                    //snackBarMessage("Algo salió mal al subir el archivo. Por favor, inténtalo más tarde.")
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
