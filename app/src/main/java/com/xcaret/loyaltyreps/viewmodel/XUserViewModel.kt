package com.xcaret.loyaltyreps.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import com.xcaret.loyaltyreps.database.XUserDatabaseDAO
import com.xcaret.loyaltyreps.model.XUser
import com.xcaret.loyaltyreps.util.AppPreferences

class XUserViewModel (
    val databaseDao: XUserDatabaseDAO,
    application: Application
) : AndroidViewModel(application) {

    private var viewModelJob = Job()

    private var uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var currentUser = MutableLiveData<XUser?> ()

    var currentXUser = MutableLiveData<XUser> ()

    private val _navigateToCurrentUser = MutableLiveData<XUser> ()

    val navigateToCurrentUser: LiveData<XUser>
        get() = _navigateToCurrentUser

    fun doneNavigating() {
        _navigateToCurrentUser.value = null
    }

    init {
        initializeUser()
    }

    private fun initializeUser(){
        uiScope.launch {
            //create coroutine wthout blocking the current context
            //get value of current user
            currentUser.value = getUserInformationFromDatabase()

            currentXUser.value = getUserInformationFromDatabase()

        }
    }

    private suspend fun getUserInformationFromDatabase(): XUser? {
        return withContext(Dispatchers.IO) {
            val xUser = databaseDao.getCurrentUser()

            xUser
        }
    }

    fun onUserLogin(xUser: XUser){
        uiScope.launch {
            insert(xUser)
            currentUser.value = getUserInformationFromDatabase()
        }
    }

    fun onLoadCurrUserData() {
        uiScope.launch {
            val currUser = currentUser.value ?: return@launch

            _navigateToCurrentUser.value = currUser
        }
    }

    fun onUpdateMyAccount(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch

            oldUser.apellidoMaterno = xUser.apellidoMaterno
            oldUser.correo = xUser.correo
            oldUser.telefono = xUser.telefono

            update(oldUser)
        }
    }

    fun onUpdateInterestsTop(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch

            oldUser.idEstadoNacimiento = xUser.idEstadoNacimiento
            oldUser.idMunicipioNacimiento = xUser.idMunicipioNacimiento
            oldUser.idEdoCivil = xUser.idEdoCivil
            oldUser.hijos = xUser.hijos

            update(oldUser)
        }
    }

    fun onUpdateInterests(xUserInterests: String){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch
            oldUser.intereses = xUserInterests

            update(oldUser)
        }
    }

    fun onUpdateQuizzesIds(quizzesIds: String){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch
            oldUser.quizzes = quizzesIds

            update(oldUser)
        }
    }

    fun onXUserUpdatePhoto(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch
            oldUser.xuserPhoto = xUser.xuserPhoto

            update(oldUser)
        }
    }

    fun updateFrontDocument(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch
            oldUser.xuserPhotoFront = xUser.xuserPhotoFront

            update(oldUser)
        }
    }

    fun updateBackDocument(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch
            oldUser.xuserPhotoBack = xUser.xuserPhotoBack

            update(oldUser)
        }
    }

    fun updateAcceptTerms(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch

            oldUser.cnAceptaPoliticas = xUser.cnAceptaPoliticas

            update(oldUser)
        }
    }

    fun updateMainQuizPuntosArticulos(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch
            oldUser.cnMainQuiz = xUser.cnMainQuiz
            oldUser.puntosParaArticulos += xUser.puntosParaArticulos

            update(oldUser)
        }
    }
    fun updateUserAssignPoints(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch

            oldUser.puntosParaArticulos += xUser.puntosParaArticulos
            oldUser.puntosParaBoletos += xUser.puntosParaBoletos
            oldUser.quizzes = xUser.quizzes

            update(oldUser)

            AppPreferences.quizzesIds = oldUser.quizzes
        }
    }

    fun updateXUserPointFromStore(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch

            oldUser.puntosParaBoletos -= xUser.puntosParaBoletos
            oldUser.puntosParaArticulos -= xUser.puntosParaArticulos

            update(oldUser)
        }
    }

    fun updateStatusArchivos(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch

            oldUser.idEstatusArchivos = xUser.idEstatusArchivos
            oldUser.cnTarjetaActiva = xUser.cnTarjetaActiva
            oldUser.dsEstatusArchivos = xUser.dsEstatusArchivos

            update(oldUser)
        }
    }

    fun updatePuntosArticuloRifa(xUser: XUser){
        uiScope.launch {
            val oldUser = currentUser.value ?: return@launch

            oldUser.puntosParaArticulos = xUser.puntosParaArticulos
            oldUser.puntosParaBoletos = xUser.puntosParaBoletos

            update(oldUser)
        }
    }

    private suspend fun insert(xUser: XUser){
        withContext(Dispatchers.IO) {
            databaseDao.insert(xUser)
            println("xUser inserted $xUser")
        }
    }

    private suspend fun update(xUser: XUser){
        withContext(Dispatchers.IO) {
            databaseDao.update(xUser)

            println("user updated $xUser")
        }
    }

    fun onClear(){
        uiScope.launch {
            clear()
            currentUser.value = null
        }
    }

    suspend fun clear(){
        withContext(Dispatchers.IO) {
            databaseDao.clear()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}