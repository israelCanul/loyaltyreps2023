package com.xcaret.loyaltyreps.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "xuser_table")
data class XUser (

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0L,

    @ColumnInfo(name = "idRep")
    var idRep: Int = 0,

    @ColumnInfo(name = "nombre")
    var nombre: String = "",

    @ColumnInfo(name = "apellidoPaterno")
    var apellidoPaterno: String = "",

    @ColumnInfo(name = "apellidoMaterno")
    var apellidoMaterno: String = "",

    @ColumnInfo(name = "rcx")
    var rcx: String = "",

    @ColumnInfo(name = "puntosPorVentas")
    var puntosPorVentas: Int = 0,

    @ColumnInfo(name = "puntosParaArticulos")
    var puntosParaArticulos: Int = 0,

    @ColumnInfo(name = "puntosParaBoletos")
    var puntosParaBoletos: Int = 0,

    @ColumnInfo(name = "estatus")
    var estatus: Boolean = false,

    @ColumnInfo(name = "intereses")
    var intereses: String = "",

    @ColumnInfo(name = "idEdoCivil")
    var idEdoCivil: Int = 0,

    @ColumnInfo(name = "estadoCivil")
    var estadoCivil: String = "",

    @ColumnInfo(name = "hijos")
    var hijos: Int = 0,

    @ColumnInfo(name = "correo")
    var correo: String = "",

    @ColumnInfo(name = "telefono")
    var telefono: String = "",

    @ColumnInfo(name = "fechaNacimiento")
    var fechaNacimiento: String = "",

    @ColumnInfo(name = "idAgencia")
    var idAgencia: Int = 0,

    @ColumnInfo(name = "agencia")
    var agencia: String = "",

    @ColumnInfo(name = "isTopRep")
    var isTopRep: Boolean = false,

    @ColumnInfo(name = "cnPerFilCompletado")
    var cnPerFilCompletado: Boolean = false,

    @ColumnInfo(name = "idMunicipioNacimiento")
    var idMunicipioNacimiento: Int = 0,

    @ColumnInfo(name = "municipioNacimiento")
    var municipioNacimiento: String = "",

    @ColumnInfo(name = "cnMainQuiz")
    var cnMainQuiz: Boolean = false,

    @ColumnInfo(name = "idEstadoNacimiento")
    var idEstadoNacimiento: Int = 0,

    @ColumnInfo(name = "estadoNacimiento")
    var estadoNacimiento: String = "",

    @ColumnInfo(name = "tokenFirebase")
    var tokenFirebase: String = "",

    @ColumnInfo(name = "level")
    var level: Float = 0f,

    @ColumnInfo(name = "totalPoint")
    var totalPoints: Float = 0f,

    @ColumnInfo(name = "totalPointsArticulos")
    var totalPointsArticulos: Float = 0f,

    @ColumnInfo(name = "totalPointsBoletos")
    var totalPointsBoletos: Float = 0f,

    @ColumnInfo(name = "xuserPhoto")
    var xuserPhoto: String? = null,

    @ColumnInfo(name = "xuserPhotoFront")
    var xuserPhotoFront: String? = null,

    @ColumnInfo(name = "xuserPhotoBack")
    var xuserPhotoBack: String? = null,

    @ColumnInfo(name = "idEstatusArchivos")
    var idEstatusArchivos: Int = 0,

    @ColumnInfo(name = "dsEstatusArchivos")
    var dsEstatusArchivos: String? = null,

    @ColumnInfo(name = "cnTarjetaActiva")
    var cnTarjetaActiva: Boolean = false,

    @ColumnInfo(name = "quizzes")
    var quizzes: String = "",

    @ColumnInfo(name = "cnAceptaPoliticas")
    var cnAceptaPoliticas: Boolean = false,

    @ColumnInfo(name = "fechaAceptaPoliticas")
    var fechaAceptaPoliticas: String? = null

)