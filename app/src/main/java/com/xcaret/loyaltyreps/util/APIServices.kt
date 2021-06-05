package com.xcaret.loyaltyreps.util

import com.androidnetworking.interceptors.HttpLoggingInterceptor
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.*

private const val BASE_URL = "https://api.loyaltyreps.com/api/"

private val interceptor  = HttpLoggingInterceptor()
    .setLevel(HttpLoggingInterceptor.Level.BODY)
    .setLevel(HttpLoggingInterceptor.Level.HEADERS)

private val client = OkHttpClient.Builder()
    .addInterceptor(interceptor)
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface APIService {
    @Multipart
    @POST("Files/uploadFilebyForm/")
    fun upload(
        @Header("Authorization") authToken: String,
        @Part filePhoto: MultipartBody.Part,
        @Part("Tarjeta") tarjeta: String
    ): Call<String>
}
//Create the API object using retrofit to implement the ApiService
object XcaretLoyaltyApi {
    val retrofitService : APIService by lazy {
        retrofit.create(APIService::class.java)
    }
}